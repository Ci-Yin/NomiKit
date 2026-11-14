#!/usr/bin/env python3
"""只读 Kotlin KDoc 候选检查器（仅 Python 标准库）。

用法：python -B check_kdoc.py [文件/目录 ...] --manifest paths.json --json
      python -B check_kdoc.py --root 项目根 --manifest paths.json --compare-root 镜像根

manifest 必须为 JSON 字符串数组；相对路径相对 --root（默认当前目录），不是
manifest 所在目录。镜像使用相同的项目相对路径；不接受 root 外的文件。
不写源码、报告、缓存或快照。退出码：0 无硬错误（仍可能有人工候选），
1 格式/词法/比对硬错误，2 输入或读取失败。--fail-on-candidates 可使候选退出 1。

限制：这是状态机 lexer + 保守结构扫描，不是完整 Kotlin parser/编译器。
词法比较保留字符串正文、转义和字符字面量，递归比较模板表达式中的非注释
词法单元；不把正则删注释后的文本当代码。忽略代码空白/换行，因此 token
相同不保证换行敏感语义相同，也不验证非法 Kotlin 的全部语法。实验性多美元
插值明确拒绝而非声称等价。声明扫描对表达式体返回类型、泛型歧义、复杂接收者、
上下文参数及匿名对象语义仅提供候选；不会解析符号/继承文档或判断契约正确性。
"""
from __future__ import annotations

import argparse
from bisect import bisect_right
from collections import Counter
from dataclasses import asdict, dataclass, field
import json
from pathlib import Path
import re
import sys

LIMITATIONS = (
    "仅词法与保守结构扫描，不是完整 Kotlin parser/编译器；候选必须人工复核。",
    "token 比较忽略代码空白和换行，不证明换行敏感语义；保留字面量正文与转义。",
    "不解析符号、继承、真实返回类型或契约质量；复杂语法可能漏报/误报。",
    "实验性多美元插值不支持，遇到时硬错误；不会给出 token 相同结论。",
)
NEWLINE = re.compile(r"\r\n|\r|\n")
COMMENT_KINDS = {"line_comment", "block_comment", "kdoc"}
DECLARATIONS = {"class", "interface", "object", "fun", "val", "var", "typealias", "constructor"}
MODIFIERS = set("public private protected internal expect actual final open abstract sealed const external override lateinit tailrec vararg suspend inner enum annotation companion inline noinline crossinline reified infix operator data in out value fun".split())
NUMBER = re.compile(r"(?:0[xX][0-9a-fA-F_]+(?:[uU][lL]?|[lL])?|0[bB][01_]+(?:[uU][lL]?|[lL])?|(?:[0-9][0-9_]*(?:\.(?!\.)[0-9][0-9_]*)?|\.[0-9][0-9_]*)(?:[eE][+-]?[0-9][0-9_]*)?(?:[fF]|[uU][lL]?|[lL])?)")
OPERATORS = tuple(sorted({"===", "!==", "...", "..<", "==", "!=", "<=", ">=", "&&", "||", "++", "--", "+=", "-=", "*=", "/=", "%=", "->", "..", "::", "!!", "?.", "?:", "!in", "!is"}, key=len, reverse=True))


@dataclass(frozen=True)
class Token:
    kind: str
    text: str
    start: int
    end: int
    line: int
    column: int


class LexError(ValueError):
    def __init__(self, message: str, offset: int):
        super().__init__(message)
        self.offset = offset


class Lexer:
    """按代码/注释/字面量/模板表达式状态扫描，不在字面量内寻找注释。"""

    def __init__(self, source: str):
        self.source = source
        self.size = len(source)
        self.lines = [0] + [m.end() for m in NEWLINE.finditer(source)]
        self.tokens: list[Token] = []

    def emit(self, kind: str, start: int, end: int) -> None:
        row = bisect_right(self.lines, start)
        self.tokens.append(Token(kind, self.source[start:end], start, end, row, start - self.lines[row - 1] + 1))

    @staticmethod
    def identifier_start(c: str) -> bool:
        return c == "_" or c.isalpha()

    @staticmethod
    def identifier_part(c: str) -> bool:
        return c == "_" or c.isalnum() or ("a" + c).isidentifier()

    def block_comment(self, start: int) -> int:
        pos, depth = start + 2, 1
        while pos < self.size:
            if self.source.startswith("/*", pos):
                depth += 1
                pos += 2
            elif self.source.startswith("*/", pos):
                depth -= 1
                pos += 2
                if not depth:
                    self.emit("kdoc" if self.source.startswith("/**", start) else "block_comment", start, pos)
                    return pos
            else:
                pos += 1
        raise LexError("未闭合块注释", start)

    def quoted(self, start: int, quote: str, kind: str) -> int:
        pos = start + 1
        while pos < self.size:
            c = self.source[pos]
            if c == quote:
                self.emit(kind, start, pos + 1)
                return pos + 1
            if c in "\r\n":
                raise LexError("普通字面量/反引号标识符中出现未转义换行", start)
            if c == "\\" and kind == "char":
                pos += 2
            else:
                pos += 1
        raise LexError("未闭合字符或反引号标识符", start)

    def string(self, start: int) -> int:
        raw = self.source.startswith('"""', start)
        delimiter = '"""' if raw else '"'
        self.emit("raw_start" if raw else "string_start", start, start + len(delimiter))
        pos = chunk = start + len(delimiter)
        while pos < self.size:
            if self.source.startswith(delimiter, pos):
                if raw:
                    run_end = pos + 3
                    while run_end < self.size and self.source[run_end] == '"':
                        run_end += 1
                    pos = run_end - 3
                if chunk < pos:
                    self.emit("string_text", chunk, pos)
                self.emit("raw_end" if raw else "string_end", pos, pos + len(delimiter))
                return pos + len(delimiter)
            c = self.source[pos]
            if not raw and c in "\r\n":
                raise LexError("普通字符串中出现未转义换行", start)
            if not raw and c == "\\":
                pos += 2
                continue
            if self.source.startswith("$" + "{", pos):
                if chunk < pos:
                    self.emit("string_text", chunk, pos)
                self.emit("template_start", pos, pos + 2)
                pos = self.code(pos + 2, template=True)
                chunk = pos
                continue
            if c == "$" and pos + 1 < self.size and self.identifier_start(self.source[pos + 1]):
                if chunk < pos:
                    self.emit("string_text", chunk, pos)
                end = pos + 2
                while end < self.size and self.identifier_part(self.source[end]):
                    end += 1
                self.emit("template_name", pos, end)
                pos = chunk = end
                continue
            pos += 1
        raise LexError("未闭合字符串", start)

    def code(self, pos: int, template: bool = False) -> int:
        braces = 0
        while pos < self.size:
            c = self.source[pos]
            if c.isspace() or (pos == 0 and c == "\ufeff"):
                pos += 1
            elif pos == 0 and self.source.startswith("#!", pos):
                newline = NEWLINE.search(self.source, pos)
                end = newline.start() if newline else self.size
                self.emit("shebang", pos, end)
                pos = end
            elif self.source.startswith("//", pos):
                newline = NEWLINE.search(self.source, pos)
                end = newline.start() if newline else self.size
                self.emit("line_comment", pos, end)
                pos = end
            elif self.source.startswith("/*", pos):
                pos = self.block_comment(pos)
            elif c == '"':
                pos = self.string(pos)
            elif c == "'":
                pos = self.quoted(pos, "'", "char")
            elif c == chr(96):
                pos = self.quoted(pos, chr(96), "identifier")
            elif c == "$":
                raise LexError("代码区出现美元符；不支持实验性多美元插值", pos)
            elif self.identifier_start(c):
                end = pos + 1
                while end < self.size and self.identifier_part(self.source[end]):
                    end += 1
                word = self.source[pos:end]
                if word == "as" and end < self.size and self.source[end] == "?":
                    end += 1
                elif word in {"return", "this", "super", "break", "continue"} and end < self.size and self.source[end] == "@":
                    end += 1
                self.emit("identifier", pos, end)
                pos = end
            elif c.isdigit() or (c == "." and pos + 1 < self.size and self.source[pos + 1].isdigit()):
                match = NUMBER.match(self.source, pos)
                if not match:
                    raise LexError("无法识别数字字面量", pos)
                self.emit("number", pos, match.end())
                pos = match.end()
            elif c == "}" and template and not braces:
                self.emit("template_end", pos, pos + 1)
                return pos + 1
            else:
                op = next((op for op in OPERATORS if self.source.startswith(op, pos) and not (op in {"!in", "!is"} and pos + len(op) < self.size and self.identifier_part(self.source[pos + len(op)]))), c)
                self.emit("symbol", pos, pos + len(op))
                if c == "{":
                    braces += 1
                elif c == "}":
                    braces -= 1
                pos += len(op)
        if template:
            raise LexError("未闭合字符串模板表达式", pos)
        return pos

    def scan(self) -> list[Token]:
        self.code(0)
        return self.tokens


def lex(source: str) -> list[Token]:
    return Lexer(source).scan()


def code_tokens(source: str) -> list[tuple[str, str]]:
    """返回保留字面量的非注释词法序列；词法失败会抛错而非比较部分结果。"""
    return [(t.kind, t.text) for t in lex(source) if t.kind not in COMMENT_KINDS]


def structural_tokens(tokens: list[Token]) -> list[Token]:
    """结构扫描将完整字符串（含模板代码）折叠，避免模板局部声明被当 API。"""
    result = []
    pos = 0
    while pos < len(tokens):
        token = tokens[pos]
        if token.kind in COMMENT_KINDS:
            pos += 1
            continue
        if token.kind in {"string_start", "raw_start"}:
            depth, end = 1, pos + 1
            while end < len(tokens) and depth:
                if tokens[end].kind in {"string_start", "raw_start"}:
                    depth += 1
                elif tokens[end].kind in {"string_end", "raw_end"}:
                    depth -= 1
                end += 1
            result.append(Token("literal", "<string>", token.start, tokens[end - 1].end, token.line, token.column))
            pos = end
        else:
            result.append(token)
            pos += 1
    return result


@dataclass
class Finding:
    code: str
    severity: str
    line: int
    column: int
    message: str
    declaration: str | None = None


@dataclass
class Declaration:
    kind: str
    name: str
    index: int
    start: int
    params: list[str] = field(default_factory=list)
    properties: list[str] = field(default_factory=list)
    type_params: list[str] = field(default_factory=list)
    returns: str = "none"
    parameter_range: tuple[int, int] | None = None
    body: int | None = None
    enum: bool = False
    doc: Token | None = None


class Checker:
    def __init__(self, source: str):
        self.source = source
        self.all_tokens = lex(source)
        self.tokens = structural_tokens(self.all_tokens)
        self.docs = [t for t in self.all_tokens if t.kind == "kdoc"]
        self.findings: list[Finding] = []
        self.declarations: list[Declaration] = []
        self.pairs: dict[int, int] = {}
        stack = []
        for i, token in enumerate(self.tokens):
            if token.text in {"(", "[", "{"}:
                stack.append(i)
            elif token.text in {")", "]", "}"}:
                if stack and {"(": ")", "[": "]", "{": "}"}[self.tokens[stack[-1]].text] == token.text:
                    first = stack.pop()
                    self.pairs[first] = i
                    self.pairs[i] = first
                else:
                    self.add("structure-unbalanced", "candidate", token, "括号结构无法配对，需人工核对扫描范围。")
        for i in stack:
            self.add("structure-unbalanced", "candidate", self.tokens[i], "括号结构未闭合，需人工核对扫描范围。")
        self.annotation_ends: dict[int, int] = {}
        for i, token in enumerate(self.tokens):
            if token.text == "@":
                end = self.annotation_end(i)
                if end > i:
                    self.annotation_ends[end] = i

    def text(self, i: int) -> str:
        return self.tokens[i].text if 0 <= i < len(self.tokens) else ""

    def add(self, code: str, severity: str, token: Token, message: str, declaration: str | None = None) -> None:
        self.findings.append(Finding(code, severity, token.line, token.column, message, declaration))

    def annotation_end(self, i: int) -> int:
        j = i + 1
        if self.text(j + 1) == ":":
            j += 2
        if self.text(j) == "[":
            return self.pairs.get(j, j) + 1
        if j >= len(self.tokens) or self.tokens[j].kind != "identifier":
            return i
        j += 1
        while self.text(j) == "." and j + 1 < len(self.tokens):
            j += 2
        if self.text(j) == "(":
            j = self.pairs.get(j, j) + 1
        return j

    def prefix_start(self, index: int) -> int:
        j = index
        while j:
            if j in self.annotation_ends:
                annotation = self.annotation_ends[j]
                if self.text(annotation + 1) == "file" and self.text(annotation + 2) == ":":
                    break
                j = annotation
            elif self.text(j - 1) in MODIFIERS:
                j -= 1
            elif self.text(j - 1) == ")" and j - 1 in self.pairs and self.text(self.pairs[j - 1] - 1) == "context":
                j = self.pairs[j - 1] - 1
            else:
                break
        return j

    def angle_end(self, i: int, limit: int) -> int | None:
        depth = 0
        for j in range(i, limit):
            t = self.text(j)
            if t == "<":
                depth += 1
            elif t == ">":
                depth -= 1
                if not depth:
                    return j
            elif t in {";", "=", "{"}:
                return None
        return None

    def top_level(self, start: int, end: int):
        """跳过配对的类型参数、函数类型、调用参数和默认 lambda。"""
        i = start
        while i < end:
            yield i
            if self.text(i) in {"(", "[", "{"} and i in self.pairs:
                i = self.pairs[i] + 1
            elif self.text(i) == "<" and (close := self.angle_end(i, end)) is not None:
                i = close + 1
            else:
                i += 1

    def parameters(self, first: int, last: int) -> tuple[list[str], list[str]]:
        separators = [i for i in self.top_level(first + 1, last) if self.text(i) == ","]
        params, props = [], []
        start = first + 1
        for end in separators + [last]:
            indices = list(self.top_level(start, end))
            colon = next((i for i in indices if self.text(i) == ":" and i > start and self.text(i - 2) != "@"), None)
            if colon is not None and self.tokens[colon - 1].kind == "identifier":
                name = self.text(colon - 1).strip(chr(96))
                (props if any(self.text(i) in {"val", "var"} for i in indices if i < colon) else params).append(name)
            start = end + 1
        return params, props

    def type_parameters(self, index: int, end: int) -> list[str]:
        if self.text(index) != "<":
            return []
        close = self.angle_end(index, end)
        if close is None:
            return []
        names = []
        start = index + 1
        separators = [i for i in self.top_level(start, close) if self.text(i) == ","]
        for stop in separators + [close]:
            i = start
            while i < stop:
                if self.text(i) == "@":
                    next_i = self.annotation_end(i)
                    if next_i <= i:
                        break
                    i = next_i
                elif self.text(i) in MODIFIERS:
                    i += 1
                elif self.tokens[i].kind == "identifier":
                    names.append(self.text(i).strip(chr(96)))
                    break
                else:
                    break
            start = stop + 1
        return names

    def boundary(self, start: int, end: int) -> int:
        """签名尾：新声明/分号/闭花括号/体；不进入调用、函数类型或注解实参。"""
        for i in self.top_level(start, end):
            word = self.text(i)
            if word in {"=", "{", ";", "}"}:
                return i
            if i >= start and i > 0 and self.tokens[i].line > self.tokens[i - 1].line and (word in DECLARATIONS or word in MODIFIERS or word == "@"):
                return i
        return end

    def declaration(self, index: int, end: int) -> Declaration | None:
        kind = self.text(index)
        start = self.prefix_start(index)
        decl = Declaration(kind, "", index, start)
        if kind in {"val", "var"}:
            decl.type_params = self.type_parameters(index + 1, end)
            if self.text(index + 1) == "(":
                self.add("destructuring-review", "candidate", self.tokens[index], "非局部解构声明需人工核对文档。")
                return None
            stop = self.boundary(index + 1, end)
            before_type = next((i for i in self.top_level(index + 1, stop) if self.text(i) in {":", "by", "get", "set"}), stop)
            names = [i for i in self.top_level(index + 1, before_type) if self.tokens[i].kind == "identifier"]
            if not names:
                return None
            decl.name = self.text(names[-1]).strip(chr(96))
        elif kind == "typealias":
            decl.name = self.text(index + 1).strip(chr(96))
            decl.type_params = self.type_parameters(index + 2, end)
        elif kind == "fun" or kind == "constructor":
            if kind == "fun" and self.text(index + 1) == "interface":
                return None
            paren = None
            for i in self.top_level(index + 1, end):
                if self.text(i) in {"=", "{", ";", "}"}:
                    break
                if self.text(i) == "(" and i in self.pairs:
                    close = self.pairs[i]
                    # Parenthesized/function-type extension receiver is not the value parameter list.
                    if self.text(close + 1) in {".", "?.", "->"}:
                        continue
                    if kind == "constructor" or (i > index + 1 and self.tokens[i - 1].kind == "identifier"):
                        paren = i
                        break
            if paren is None:
                return None
            decl.name = "constructor" if kind == "constructor" else self.text(paren - 1).strip(chr(96))
            close = self.pairs[paren]
            decl.parameter_range = (paren, close)
            decl.params, decl.properties = self.parameters(paren, close)
            decl.type_params = self.type_parameters(index + 1, paren)
            stop = self.boundary(close + 1, end)
            if self.text(stop) == "{":
                decl.body = stop
            if kind == "fun":
                tail = [self.text(i) for i in self.top_level(close + 1, stop)]
                if ":" in tail:
                    result_type = tail[tail.index(":") + 1:]
                    if "where" in result_type:
                        result_type = result_type[:result_type.index("where")]
                    decl.returns = "none" if result_type in [["Unit"], ["kotlin", ".", "Unit"]] else "explicit"
                elif self.text(stop) == "=":
                    decl.returns = "inferred"
        else:
            next_word = self.text(index + 1)
            if kind == "object" and (next_word in {":", "{"}):
                if self.text(index - 1) != "companion":
                    return None
                decl.name = "Companion"
            else:
                decl.name = next_word.strip(chr(96))
            decl.enum = any(self.text(i) == "enum" for i in range(start, index))
            decl.type_params = self.type_parameters(index + 2, end)
            colon_seen = False
            for i in self.top_level(index + 2 if decl.name != "Companion" else index + 1, end):
                word = self.text(i)
                if word == "{":
                    decl.body = i
                    break
                if word in {";", "}", "="}:
                    break
                if i >= index + 2 and self.tokens[i].line > self.tokens[index].line and word in DECLARATIONS - {"constructor"}:
                    break
                if word == ":":
                    colon_seen = True
                if word == "(" and i in self.pairs and not colon_seen and decl.parameter_range is None:
                    # Annotation argument lists belong to annotations, not constructors.
                    if any(a <= i < b for b, a in self.annotation_ends.items()):
                        continue
                    close = self.pairs[i]
                    decl.parameter_range = (i, close)
                    decl.params, decl.properties = self.parameters(i, close)
        return decl if decl.name else None

    def anonymous_body(self, index: int, end: int) -> int | None:
        """识别对象表达式的类体，不把 object 本身当成必须命名的声明。"""
        if self.text(index) != "object" or self.text(index + 1) not in {":", "{"}:
            return None
        if self.text(index - 1) == "companion":
            return None
        for i in self.top_level(index + 1, end):
            if self.text(i) == "{":
                return i if i in self.pairs else None
            if self.text(i) in {";", "}", "="}:
                return None
        return None

    def collect_anonymous_members(self, start: int, end: int) -> None:
        """局部表达式本身不检查；其中匿名对象的成员仍有独立 API 契约。"""
        i = start
        while i < end:
            body = self.anonymous_body(i, end)
            if body is not None:
                close = self.pairs[body]
                self.collect_anonymous_members(i + 1, body)
                self.collect(body + 1, close)
                i = close + 1
            else:
                i += 1

    def collect(self, start: int, end: int, enum: bool = False) -> None:
        i = start
        enum_entries = enum
        while i < end:
            token = self.tokens[i]
            word = token.text
            if word == "@":
                next_i = self.annotation_end(i)
                if next_i > i:
                    i = next_i
                    continue
            if word == ";":
                enum_entries = False
            if enum_entries and token.kind == "identifier" and word not in MODIFIERS and word not in DECLARATIONS:
                decl = Declaration("enum-entry", word.strip(chr(96)), i, self.prefix_start(i))
                self.declarations.append(decl)
                i += 1
                if self.text(i) == "(" and i in self.pairs:
                    i = self.pairs[i] + 1
                if self.text(i) == "{" and i in self.pairs:
                    self.collect(i + 1, self.pairs[i])
                    i = self.pairs[i] + 1
                if self.text(i) == ",":
                    i += 1
                elif self.text(i) == ";":
                    enum_entries = False
                    i += 1
                continue
            anonymous = self.anonymous_body(i, end)
            if anonymous is not None:
                close = self.pairs[anonymous]
                self.collect_anonymous_members(i + 1, anonymous)
                self.collect(anonymous + 1, close)
                i = close + 1
                continue
            if word in DECLARATIONS and self.text(i - 1) not in {".", "::"}:
                decl = self.declaration(i, end)
                if decl is not None:
                    self.declarations.append(decl)
                    if decl.body is not None and decl.kind in {"class", "interface", "object"}:
                        close = self.pairs.get(decl.body)
                        if close is not None:
                            self.collect_anonymous_members(i + 1, decl.body)
                            self.collect(decl.body + 1, close, decl.enum)
                            i = close + 1
                            continue
                    if decl.parameter_range:
                        self.collect_anonymous_members(decl.parameter_range[0] + 1, decl.parameter_range[1])
                        i = decl.parameter_range[1] + 1
                        continue
            if word in {"(", "[", "{"} and i in self.pairs:
                # 局部声明不检查，但匿名对象的 override/成员不属于局部变量。
                self.collect_anonymous_members(i + 1, self.pairs[i])
                i = self.pairs[i] + 1
            else:
                i += 1

    def doc_lines(self, doc: Token) -> list[str]:
        body = doc.text[3:-2]
        return [re.sub(r"^\s*\* ?", "", line).strip() for line in body.splitlines()]

    def check_format(self, doc: Token) -> None:
        lines = doc.text.splitlines()
        valid = (len(lines) >= 3 and lines[0].strip() == "/**" and lines[-1].strip() == "*/" and all(re.match(r"^\s*\*(?:\s|$)", line) for line in lines[1:-1]))
        if not valid:
            self.add("kdoc-multiline", "error", doc, "KDoc 须使用独立 /**、逐行 * 和独立 */ 的多行格式。")
        previous_lines = list(NEWLINE.finditer(self.source, 0, doc.start))
        line_start = previous_lines[-1].end() if previous_lines else 0
        next_line = NEWLINE.search(self.source, doc.end)
        line_end = next_line.start() if next_line else len(self.source)
        if self.source[line_start:doc.start].strip() or self.source[doc.end:line_end].strip():
            self.add("kdoc-inline", "error", doc, "KDoc 不得与代码/声明或其他注释同行。")
        summary = []
        for line in self.doc_lines(doc):
            if line.startswith("@"):
                break
            if line:
                summary.append(line)
        if not any("\u3400" <= c <= "\u9fff" or "\U00020000" <= c <= "\U000323af" for c in " ".join(summary)):
            self.add("kdoc-chinese-summary", "error", doc, "KDoc 摘要须包含中文；标签中的中文不能替代摘要。")

    def associate(self, decl: Declaration) -> None:
        prefix = self.tokens[decl.start]
        previous = self.tokens[decl.start - 1].end if decl.start else 0
        preceding = [d for d in self.docs if previous <= d.start and d.end <= prefix.start]
        if preceding:
            decl.doc = preceding[-1]
        else:
            # Still associate misplaced docs to avoid redundant missing-tag noise.
            inside = [d for d in self.docs if prefix.start < d.start < self.tokens[decl.index].start]
            if inside:
                decl.doc = inside[-1]
                self.add("kdoc-after-annotation", "error", decl.doc, "声明 KDoc 应放在所有声明注解/修饰符之前。", decl.name)

    def tags(self, doc: Token) -> list[tuple[str, str, str]]:
        tags = []
        for line in self.doc_lines(doc):
            match = re.match(r"@(\w+)\b\s*(.*)", line)
            if match:
                kind, rest = match.groups()
                if kind in {"param", "property"}:
                    name_match = re.match(r"(?:\[([^]]+)\]|([^\s]+))\s*(.*)", rest)
                    if name_match:
                        bracket, name, description = name_match.groups()
                        tags.append((kind, (bracket or name).strip(chr(96)), description))
                    else:
                        tags.append((kind, "", ""))
                else:
                    tags.append((kind, "", rest))
            elif tags and line:
                kind, name, text = tags[-1]
                tags[-1] = (kind, name, text + " " + line)
        return tags

    def check_contract(self, decl: Declaration) -> None:
        token = self.tokens[decl.index]
        if decl.doc is None:
            self.add("missing-kdoc", "candidate", token, "声明缺少关联 KDoc，请人工核对是否需要补充。", decl.name)
            return
        tags = self.tags(decl.doc)
        for kind, expected in (("param", decl.params), ("property", decl.properties)):
            actual = [name for tag, name, _ in tags if tag == kind]
            for name in expected:
                if name not in actual:
                    self.add("missing-" + kind, "candidate", token, "缺少 @" + kind + " " + name + "；请按真实契约人工补充。", decl.name)
            allowed = set(expected + (decl.type_params if kind == "param" else []))
            for name in actual:
                if name not in allowed:
                    self.add("unknown-" + kind, "candidate", token, "@" + kind + " " + name + " 未与已识别声明匹配，需人工复核。", decl.name)
        for kind, name, description in tags:
            if kind in {"param", "property", "return"} and (not description.strip() or description.strip().strip("。.") == name):
                self.add("empty-tag-description", "candidate", decl.doc, "@" + kind + " " + name + " 缺少有意义的说明。", decl.name)
        duplicates = Counter((kind, name) for kind, name, _ in tags if kind in {"param", "property", "return"})
        for (kind, name), count in duplicates.items():
            if count > 1:
                self.add("duplicate-tag", "candidate", decl.doc, "重复 @" + kind + " " + name + "，需人工合并。", decl.name)
        if decl.returns != "none" and not any(kind == "return" for kind, _, _ in tags):
            message = "显式非 Unit 返回值缺少 @return。" if decl.returns == "explicit" else "表达式体返回类型需人工确认；若非 Unit，应补 @return。"
            self.add("missing-return" if decl.returns == "explicit" else "inferred-return", "candidate", token, message, decl.name)

    def run(self) -> dict:
        self.collect(0, len(self.tokens))
        for decl in self.declarations:
            self.associate(decl)
        attached = {d.doc.start for d in self.declarations if d.doc}
        for doc in self.docs:
            self.check_format(doc)
            embedded = False
            for decl in self.declarations:
                if decl.parameter_range:
                    first, last = decl.parameter_range
                    if doc.start not in attached and self.tokens[first].start < doc.start < self.tokens[last].end:
                        embedded = True
                        self.add("kdoc-embedded", "error", doc, "KDoc 嵌入参数列表；构造属性应移至类型 @property，普通参数使用 @param。", decl.name)
                        break
            if not embedded and doc.start not in attached:
                self.add("unattached-kdoc", "candidate", doc, "未关联到非局部声明：可能是局部文档、文件说明或扫描器未识别的语法。")
        for decl in self.declarations:
            self.check_contract(decl)
        self.findings.sort(key=lambda f: (f.line, f.column, f.code))
        return {
            "declarations": [{"kind": d.kind, "name": d.name, "line": self.tokens[d.index].line, "documented": d.doc is not None, "params": d.params, "properties": d.properties, "return_check": d.returns} for d in self.declarations],
            "findings": [asdict(f) for f in self.findings],
        }


def check_source(source: str) -> dict:
    try:
        return Checker(source).run()
    except LexError as error:
        newlines = list(NEWLINE.finditer(source, 0, error.offset))
        line = len(newlines) + 1
        column = error.offset - (newlines[-1].end() if newlines else 0) + 1
        return {"declarations": [], "findings": [asdict(Finding("lex-error", "error", line, column, str(error)))]}


def compare_sources(current: str, baseline: str) -> dict:
    try:
        left = [t for t in lex(current) if t.kind not in COMMENT_KINDS]
        right = [t for t in lex(baseline) if t.kind not in COMMENT_KINDS]
    except LexError as error:
        return {"status": "lex-error", "message": str(error), "offset": error.offset}
    for index in range(max(len(left), len(right))):
        a = left[index] if index < len(left) else None
        b = right[index] if index < len(right) else None
        a_key = (a.kind, a.text) if a else None
        b_key = (b.kind, b.text) if b else None
        if a_key != b_key:
            return {"status": "different", "token_index": index, "current": asdict(a) if a else None, "baseline": asdict(b) if b else None, "current_count": len(left), "baseline_count": len(right)}
    return {"status": "equal", "token_count": len(left)}


def resolve_files(paths: list[str], manifest: str | None, root: Path) -> list[Path]:
    requested = list(paths)
    if manifest:
        content = json.loads(Path(manifest).read_text(encoding="utf-8-sig"))
        if not isinstance(content, list) or not all(isinstance(value, str) and value.strip() for value in content):
            raise ValueError("--manifest 必须为非空路径字符串组成的 JSON 数组（可为空数组）")
        requested.extend(content)
    if not requested:
        raise ValueError("请指定 Kotlin 文件/目录或非空 --manifest；不会默认扫描全仓")
    result = set()
    for value in requested:
        path = Path(value)
        path = (root / path).resolve() if not path.is_absolute() else path.resolve()
        if not path.is_relative_to(root):
            raise ValueError("扫描路径必须位于 --root 内：" + str(path))
        if not path.exists():
            raise ValueError("扫描路径不存在：" + str(path))
        files = sorted(path.rglob("*")) if path.is_dir() else [path]
        for entry in files:
            if entry.is_file() and entry.suffix in {".kt", ".kts"}:
                resolved = entry.resolve()
                if not resolved.is_relative_to(root):
                    raise ValueError("符号链接指向 --root 外：" + str(entry))
                result.add(resolved)
        if path.is_file() and path.suffix not in {".kt", ".kts"}:
            raise ValueError("仅接受 .kt/.kts 文件：" + str(path))
    if not result:
        raise ValueError("输入未匹配任何 Kotlin 文件")
    return sorted(result)


def audit(files: list[Path], root: Path, compare_root: Path | None = None) -> dict:
    report = {"root": str(root), "compare_root": str(compare_root) if compare_root else None, "limitations": list(LIMITATIONS), "files": [], "summary": {}}
    for path in files:
        relative = path.relative_to(root).as_posix()
        # newline='' preserves raw-string CRLF payload instead of normalizing it away.
        with path.open(encoding="utf-8-sig", newline="") as stream:
            source = stream.read()
        entry = {"path": relative, **check_source(source)}
        if compare_root:
            mirror = compare_root / relative
            if not mirror.is_file():
                entry["comparison"] = {"status": "missing-baseline", "path": str(mirror)}
            elif not mirror.resolve().is_relative_to(compare_root):
                entry["comparison"] = {"status": "invalid-baseline", "message": "镜像符号链接越界"}
            else:
                with mirror.open(encoding="utf-8-sig", newline="") as stream:
                    entry["comparison"] = compare_sources(source, stream.read())
        report["files"].append(entry)
    all_findings = [f for entry in report["files"] for f in entry["findings"]]
    report["summary"] = {
        "files": len(files),
        "declarations": sum(len(entry["declarations"]) for entry in report["files"]),
        "errors": sum(f["severity"] == "error" for f in all_findings),
        "candidates": sum(f["severity"] == "candidate" for f in all_findings),
        "comparison_equal": sum(entry.get("comparison", {}).get("status") == "equal" for entry in report["files"]),
        "comparison_errors": sum(entry.get("comparison", {}).get("status", "equal") != "equal" for entry in report["files"]),
        "codes": dict(sorted(Counter(f["code"] for f in all_findings).items())),
    }
    return report


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("paths", nargs="*", help="显式 Kotlin 文件或目录")
    parser.add_argument("--manifest", help="JSON 路径字符串数组；路径相对 --root")
    parser.add_argument("--root", type=Path, default=Path.cwd(), help="项目根，默认当前工作目录")
    parser.add_argument("--compare-root", type=Path, help="与项目相同相对路径结构的只读快照根")
    parser.add_argument("--json", action="store_true", help="向标准输出输出 JSON，不写报告文件")
    parser.add_argument("--fail-on-candidates", action="store_true", help="存在人工复核候选也退出 1")
    args = parser.parse_args(argv)
    try:
        root = args.root.resolve(strict=True)
        mirror = args.compare_root.resolve(strict=True) if args.compare_root else None
        if not root.is_dir() or (mirror and not mirror.is_dir()):
            raise ValueError("--root/--compare-root 必须是目录")
        files = resolve_files(args.paths, args.manifest, root)
        report = audit(files, root, mirror)
    except (OSError, ValueError, UnicodeError) as error:
        if args.json:
            print(json.dumps({"input_error": str(error), "limitations": list(LIMITATIONS)}, ensure_ascii=False))
        else:
            print("输入/读取失败：" + str(error), file=sys.stderr)
        return 2
    if args.json:
        print(json.dumps(report, ensure_ascii=False, indent=2))
    else:
        for entry in report["files"]:
            for finding in entry["findings"]:
                print(f"{entry['path']}:{finding['line']}:{finding['column']}: {finding['severity']} {finding['code']}: {finding['message']}")
            if "comparison" in entry:
                print(entry["path"] + ": tokens " + json.dumps(entry["comparison"], ensure_ascii=False))
        print(json.dumps(report["summary"], ensure_ascii=False))
        for limitation in LIMITATIONS:
            print("限制：" + limitation)
    summary = report["summary"]
    return int(bool(summary["errors"] or summary["comparison_errors"] or (args.fail_on_candidates and summary["candidates"])))


if __name__ == "__main__":
    sys.exit(main())
