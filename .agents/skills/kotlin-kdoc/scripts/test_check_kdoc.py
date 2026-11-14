#!/usr/bin/env python3
"""直接执行或 unittest discover 均可；磁盘 fixtures 仅使用自动清理的系统 temp。

python -B -X utf8 .agents/skills/kotlin-kdoc/scripts/test_check_kdoc.py
python -B -X utf8 -m unittest discover -s .agents/skills/kotlin-kdoc/scripts -p test_check_kdoc.py -v
"""
import contextlib
import io
import json
from pathlib import Path
import sys
import tempfile
import textwrap
import unittest

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parent))
import check_kdoc as checker


def fixture(text):
    return textwrap.dedent(text).strip() + "\n"


def doc(summary="提供中文说明。", tags=""):
    return "/**\n * " + summary + "\n" + "".join(" * " + line + "\n" for line in tags.splitlines()) + " */\n"


def findings(source, code=None):
    found = checker.check_source(source)["findings"]
    return [item for item in found if code is None or item["code"] == code]


class LexerTests(unittest.TestCase):
    def assertEquivalent(self, a, b):
        self.assertEqual(checker.code_tokens(a), checker.code_tokens(b))
        self.assertEqual(checker.compare_sources(a, b)["status"], "equal")

    def assertDifferent(self, a, b):
        self.assertNotEqual(checker.code_tokens(a), checker.code_tokens(b))
        self.assertEqual(checker.compare_sources(a, b)["status"], "different")

    def test_cr_line_comment_does_not_hide_code_changes(self):
        before = "val before=1 // comment\rval after=2\r"
        after = "val before=1 // comment\rval after=3\r"
        self.assertEqual(checker.compare_sources(before, after)["status"], "different")
        self.assertEqual(checker.compare_sources(before, before.replace("\r", "\r\n"))["status"], "equal")
        self.assertEqual(checker.compare_sources(before, before.replace("\r", "\n"))["status"], "equal")
        token = next(t for t in checker.lex(before) if t.text == "after")
        self.assertEqual((token.line, token.column), (2, 5))

    def test_cr_shebang_does_not_hide_first_statement(self):
        before = "#!/usr/bin/env kotlin\rval x=1\r"
        self.assertEqual(checker.compare_sources(before, before.replace("x=1", "x=2"))["status"], "different")

    def test_nested_comments_and_line_comments(self):
        self.assertEquivalent("val /* 外层 /* 内层 */ 结束 */ x = 1 // 尾部\n", "val x=1")
        self.assertEquivalent("a/** 文档 */+/**/b", "a + b")

    def test_comment_removal_does_not_join_identifiers_or_operators(self):
        self.assertDifferent("a/*说明*/b", "ab")
        self.assertDifferent("x + /*说明*/ +y", "x ++ y")
        self.assertDifferent("a ? /*说明*/ .b", "a?.b")

    def test_comments_inside_regular_string_are_payload(self):
        original = 'val url = "https://example.test/*path*/"'
        self.assertEquivalent(original + " // 注释", original)
        self.assertDifferent(original, original.replace("/*path*/", ""))
        self.assertEqual([t for t in checker.lex(original) if t.kind in checker.COMMENT_KINDS], [])

    def test_escaped_string_and_characters(self):
        source = r'''val s = "escaped \" // /* \\ \n \$name"; val a = '\''; val b = '\\'; val c = '/' '''
        self.assertEquivalent(source + "// tail", source)
        self.assertDifferent(source, source.replace("\\n", "\\t"))
        self.assertDifferent("val x = '/'", "val x = '*' ")

    def test_backtick_identifiers_keep_comment_markers(self):
        q = chr(96)
        source = "val " + q + "literal/*not-comment*/" + q + " = 1"
        self.assertEqual(len([t for t in checker.lex(source) if t.kind in checker.COMMENT_KINDS]), 0)
        self.assertDifferent(source, source.replace("/*not-comment*/", ""))

    def test_raw_strings_preserve_whitespace_comments_and_backslashes(self):
        source = 'val x = """line1\n  // text /* not comment */ \\t\nline3"""'
        self.assertEquivalent(source, source + " /* outside */")
        self.assertDifferent(source, source.replace("  //", " //"))
        self.assertDifferent(source, source.replace("/* not comment */", ""))
        self.assertDifferent(source, source.replace("\n", "\r\n"))

    def test_raw_literal_trailing_quote_run(self):
        source = 'val quote = """ends in a quote""""'
        tokens = checker.lex(source)
        self.assertIn(("string_text", 'ends in a quote"'), [(t.kind, t.text) for t in tokens])
        self.assertDifferent(source, source[:-1])

    def test_simple_template_names_are_not_plain_payload(self):
        source = 'val s = "$name $other"'
        self.assertEqual([t.text for t in checker.lex(source) if t.kind == "template_name"], ["$name", "$other"])
        self.assertDifferent(source, source.replace("$name", "$names"))

    def test_nested_interpolation_strings_braces_and_comments(self):
        source = 'val s = "outer @{ run { val x = "inner @{ "/*payload*/" }"; /* before } /* nested */ */ x } } tail"'.replace("@{", "$" + "{")
        edited = source.replace("/* before } /* nested */ */", "/* changed */")
        self.assertEquivalent(source, edited)
        self.assertDifferent(source, source.replace("/*payload*/", "different"))
        self.assertDifferent(source, source.replace("; /* before", "; x; /* before"))
        report = checker.check_source(source)
        self.assertEqual([d["name"] for d in report["declarations"]], ["s"])

    def test_raw_template_can_contain_nested_raw_literal_and_line_comment(self):
        source = 'val s = """raw @{ run { // } ignored\n """/* literal */""" } }"""'.replace("@{", "$" + "{")
        self.assertEquivalent(source, source.replace("// } ignored", "// another comment"))
        self.assertDifferent(source, source.replace("/* literal */", "changed"))

    def test_numeric_tokens_and_longest_operators(self):
        text = "0xFFuL 0b101 1_000L 1.2e-3F .5 1..2 3..<5 a !in b c !is D !== === -> ::"
        self.assertEqual([t.text for t in checker.lex(text) if t.kind == "number"], ["0xFFuL", "0b101", "1_000L", "1.2e-3F", ".5", "1", "2", "3", "5"])
        self.assertDifferent("1.2e-3", "1.2e-4")
        self.assertDifferent("0xFF", "0xFE")

    def test_unterminated_literals_fail_closed(self):
        for text in ['"unterminated', "/* /* */", "'x", '"""raw', '"@{ run { 1 }"'.replace("@{", "$" + "{")]:
            with self.subTest(text=text):
                with self.assertRaises(checker.LexError):
                    checker.code_tokens(text)
                self.assertEqual(checker.compare_sources(text, text)["status"], "lex-error")
                self.assertEqual(findings(text)[0]["code"], "lex-error")

    def test_experimental_multi_dollar_interpolation_fails_closed(self):
        with self.assertRaises(checker.LexError):
            checker.lex('val x = $$"""$literal"""')

    def test_compound_keywords_preserve_token_boundaries(self):
        self.assertDifferent("x as? String", "x as /* gap */ ? String")
        self.assertDifferent("return@label x", "return /* gap */ @label x")
        self.assertEquivalent("return@label /* comment */ x", "return@label x")

    def test_line_numbers_and_bom(self):
        tokens = checker.lex("\ufeff// 中文\r\nval value = 1")
        self.assertEqual((tokens[1].text, tokens[1].line, tokens[1].column), ("val", 2, 1))


class CheckerTests(unittest.TestCase):
    def test_delegate_property_name_and_bodyless_classes(self):
        source = doc() + "val delegated by lazy { 1 }\n" + doc() + "class A\n" + doc() + "private class B\n" + doc() + "private val computed get() = 1"
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["delegated", "A", "B", "computed"])
        self.assertEqual(result["findings"], [])

    def test_anonymous_function_not_reported_as_named_api(self):
        source = doc() + "val callback = fun(value: Int) { val local = value }"
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["callback"])
        self.assertEqual(result["findings"], [])

    def test_bodyless_interface_functions_do_not_borrow_next_signature(self):
        source = doc() + "interface Api {\n" + doc() + "fun write()\n" + doc(tags="@return 当前计数。") + "fun read(): Int\n}"
        result = checker.check_source(source)
        self.assertEqual(result["findings"], [])
        self.assertEqual([d["return_check"] for d in result["declarations"]], ["none", "none", "explicit"])

    def test_function_type_return_still_requires_return_tag(self):
        source = doc() + "fun factory(): (Int) -> Unit = {}"
        self.assertTrue(findings(source, "missing-return"))

    def test_standard_function_is_clean(self):
        source = doc(tags="@param count 读取的条目数量。\n@return 读取后的数量。") + "fun read(count: Int): Int = count"
        self.assertEqual(findings(source), [])

    def test_single_line_inline_and_chinese_summary(self):
        codes = {f["code"] for f in findings("/** English */ val x = 1")}
        self.assertTrue({"kdoc-multiline", "kdoc-inline", "kdoc-chinese-summary"}.issubset(codes))
        self.assertIn("kdoc-chinese-summary", {f["code"] for f in findings(doc("English", "@param x 中文参数。") + "fun f(x: Int) {}")})

    def test_multiline_body_stars_and_closing_line(self):
        self.assertTrue(findings("/**\n中文摘要。\n */\nval x = 1", "kdoc-multiline"))
        self.assertTrue(findings("/**\n * 中文摘要。 */\nval x = 1", "kdoc-multiline"))
        self.assertFalse(findings(doc() + "val x = 1", "kdoc-multiline"))

    def test_annotation_association_includes_nested_arguments_and_use_sites(self):
        source = doc() + fixture('''
            @get:Anno(value = call("/* fake */", nested(1)))
            @Other
            private val value: Int = 1
        ''')
        self.assertEqual(findings(source), [])
        source = doc() + '@[First Second(value = 2)]\nprivate val x = 1'
        self.assertEqual(findings(source), [])

    def test_cr_and_mixed_newline_kdoc_location(self):
        source = (doc() + "val value = 1\n").replace("\n", "\r")
        self.assertEqual(findings(source), [])
        inline = source.replace("*/\rval", "*/ val")
        self.assertTrue(findings(inline, "kdoc-inline"))
        failure = checker.check_source("// first\r\n// second\rval text = \"bad")
        self.assertEqual(failure["findings"][0]["line"], 3)

    def test_file_annotations_do_not_belong_to_first_declaration(self):
        for middle in ["", "package sample\n", "package sample\nimport other.Value\n"]:
            for annotation in ['@file:Suppress("unused")', '@file:[Suppress("unused") OptIn(Marker::class)]']:
                source = annotation + "\n" + middle + doc() + "val value = 1"
                self.assertEqual(findings(source), [])

    def test_file_annotation_then_property_annotations_keep_kdoc_order(self):
        source = '@file:Suppress("unused")\n' + doc() + '@get:Marker\n@field:Marker\nval value = 1'
        self.assertEqual(findings(source), [])
        misplaced = '@file:Suppress("unused")\n@get:Marker\n' + doc() + 'val value = 1'
        self.assertTrue(findings(misplaced, "kdoc-after-annotation"))

    def test_doc_between_annotation_and_declaration_is_wrong_order(self):
        source = "@Composable\n" + doc() + "private fun View() {}"
        self.assertTrue(findings(source, "kdoc-after-annotation"))
        self.assertFalse(findings(source, "missing-kdoc"))

    def test_constructor_properties_are_covered_by_class_tags(self):
        source = doc(tags="@property id 稳定标识。\n@property callback 修改时调用。\n@param seed 初始值。") + fixture('''
            @Immutable
            data class Model(
                @get:Anno val id: String,
                val callback: (Map<String, List<Int>>, Int) -> Unit,
                seed: Long = 0,
            )
        ''')
        result = checker.check_source(source)
        self.assertEqual(result["findings"], [])
        self.assertEqual(len(result["declarations"]), 1)
        self.assertEqual(result["declarations"][0]["properties"], ["id", "callback"])
        self.assertEqual(result["declarations"][0]["params"], ["seed"])

    def test_class_property_does_not_cover_unrelated_member(self):
        source = doc(tags="@property id 稳定标识。") + "class Model(val id: Int) {\n val other: Int = id\n}"
        missing = findings(source, "missing-kdoc")
        self.assertEqual([f["declaration"] for f in missing], ["other"])

    def test_embedded_constructor_doc_is_hard_error(self):
        source = doc() + "class Model(\n" + doc() + "val id: Int\n)"
        self.assertTrue(findings(source, "kdoc-embedded"))
        self.assertTrue(findings(source, "missing-property"))
        self.assertFalse(findings(source, "missing-kdoc"))

    def test_nested_generics_function_types_defaults_and_extension(self):
        source = doc(tags="@param values 输入映射。\n@param transform 转换回调。\n@param fallback 无数据时回调。\n@return 转换结果。") + fixture('''
            inline fun <reified T : Any, R> List<T>.convert(
                values: Map<String, List<Pair<T, R>>>,
                transform: suspend (T, Map<String, R>) -> List<R>,
                fallback: @Composable Scope.() -> Unit = { use("a,b"); nested(1, 2) },
            ): List<R> where R : Any = emptyList()
        ''')
        result = checker.check_source(source)
        self.assertEqual(result["findings"], [])
        self.assertEqual(result["declarations"][0]["name"], "convert")
        self.assertEqual(result["declarations"][0]["params"], ["values", "transform", "fallback"])

    def test_function_type_extension_receiver(self):
        source = doc(tags="@param count 调用次数。\n@return 包装函数。") + "fun ((Int) -> String).repeat(count: Int): (Int) -> String = this"
        result = checker.check_source(source)
        self.assertEqual(result["findings"], [])
        self.assertEqual(result["declarations"][0]["params"], ["count"])

    def test_return_candidates_and_unit_exemption(self):
        for declaration in ["fun f(): Unit = Unit", "fun f() {}", "fun f(): kotlin.Unit {}"]:
            self.assertEqual(findings(doc() + declaration), [])
        self.assertTrue(findings(doc() + "fun f(): Int = 1", "missing-return"))
        self.assertTrue(findings(doc() + "fun f() = call()", "inferred-return"))
        self.assertTrue(findings(doc() + "fun f(): Unit? = null", "missing-return"))

    def test_local_declarations_lambdas_and_accessors_are_exempt(self):
        source = doc() + fixture('''
            fun outer() {
                val local = 1
                fun helper() {}
                class Local(val id: Int)
                listOf(1).map { val inside = it; inside }
            }
        ''') + doc() + fixture('''
            val cached: Int
                get() { val local = 1; return local }
        ''')
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["outer", "cached"])
        self.assertEqual(result["findings"], [])

    def test_anonymous_object_members_keep_their_documentation(self):
        source = doc() + "val renderer = object : Base {\n" + doc() + "override fun Render() { val local = 1 }\n}"
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["renderer", "Render"])
        self.assertEqual(result["findings"], [])

    def test_missing_anonymous_member_is_reported_not_local_variables(self):
        source = doc() + "fun outer() { val ignored = 1; val x = object : Base { override val id = 1; override fun f() { val local = 2 } } }"
        missing = findings(source, "missing-kdoc")
        self.assertEqual({f["declaration"] for f in missing}, {"id", "f"})

    def test_nested_anonymous_objects_in_lambda_and_call_arguments(self):
        source = doc() + "fun outer() { remember { build(object : Base {\n" + doc() + "override val child = object : Child {\n" + doc() + "override fun render() {}\n}\n}) } }"
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["outer", "child", "render"])
        self.assertEqual(result["findings"], [])

    def test_anonymous_object_in_default_parameter(self):
        source = doc(tags="@param x 接口实现。") + "fun outer(x: Base = object : Base {\n" + doc() + "override val id = 1\n}) {}"
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["outer", "id"])
        self.assertEqual(result["findings"], [])

    def test_anonymous_delegation_and_constructor_arguments(self):
        source = doc() + "val renderer = object : Base(build(1)) {\n" + doc() + "override val value = 1\n}\n" + doc() + "val proxy = object : Contract by renderer {\n" + doc() + "override val extra = 2\n}"
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["renderer", "value", "proxy", "extra"])
        self.assertEqual(result["findings"], [])

    def test_class_body_does_not_hide_anonymous_constructor_default(self):
        source = doc(tags="@property x 接口实现。") + "class Outer(val x: Base = object : Base {\n" + doc() + "override val id = 1\n}) {\n" + doc() + "val other = 2\n}"
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["Outer", "id", "other"])
        self.assertEqual(result["findings"], [])
        undocumented = doc(tags="@property x 接口实现。") + "class Outer(val x: Base = object : Base { override val id = 1 }) {}"
        self.assertEqual({f["declaration"] for f in findings(undocumented, "missing-kdoc")}, {"id"})

    def test_generic_property_and_alias_allow_matching_type_parameter_tags(self):
        for declaration in ["val <T> List<T>.firstValue: T get() = first()", "typealias Items<T> = List<T>"]:
            self.assertEqual(findings(doc(tags="@param T 元素类型。") + declaration), [])
            self.assertTrue(findings(doc(tags="@param U 不存在的类型。") + declaration, "unknown-param"))

    def test_type_parameter_bounds_do_not_invent_parameter_names(self):
        declaration = "fun <T : Map<String, Int>, R> combine() {}"
        self.assertEqual(findings(doc(tags="@param T 输入映射类型。\n@param R 输出类型。") + declaration), [])
        wrong = findings(doc(tags="@param Int 不是类型参数。") + declaration, "unknown-param")
        self.assertEqual(len(wrong), 1)

    def test_annotated_type_parameter_names_exclude_annotations(self):
        declaration = "fun <@Anno T, @Limit(1, 2) out R> combine() {}"
        self.assertEqual(findings(doc(tags="@param T 输入类型。\n@param R 输出类型。") + declaration), [])
        self.assertTrue(findings(doc(tags="@param Anno 不是类型参数。") + declaration, "unknown-param"))

    def test_anonymous_members_in_supertype_constructor_arguments(self):
        for header in ["val instance = object : Base(", "class Child : Base("]:
            source = doc() + header + "object : Dep {\n" + doc() + "override val id = 1\n}) {\n" + doc() + "override fun render() { val local = 1 }\n}"
            result = checker.check_source(source)
            self.assertEqual([d["name"] for d in result["declarations"]][1:], ["id", "render"])
            self.assertEqual(result["findings"], [])
            missing = doc() + header + "object : Dep { override val id = 1 }) {}"
            self.assertEqual({f["declaration"] for f in findings(missing, "missing-kdoc")}, {"id"})

    def test_class_primary_and_supertype_defaults_are_not_double_scanned(self):
        source = doc(tags="@property dependency 实例依赖。") + "class Child(val dependency: Dep = object : Dep {\n" + doc() + "override val id = 1\n}) : Base(object : Dep {\n" + doc() + "override val id = 2\n}) {}"
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["Child", "id", "id"])
        self.assertEqual(result["findings"], [])

    def test_bodyless_data_class_before_generic_composable_function(self):
        source = doc(tags="@property id 标识。") + "@Immutable\nprivate data class Entry(val id: Int)\n" + doc(tags="@return 入口列表。") + "@Composable\nprivate fun entries(): List<Entry> { val first = 1; val second = 2; return emptyList() }"
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["Entry", "entries"])
        self.assertEqual(result["findings"], [])

    def test_bodyless_delegated_class_before_function_type_parameter(self):
        source = doc(tags="@param delegate 被委托实例。") + "class Restricted(delegate: Base) : Base by delegate\n" + doc(tags="@param content 待验证内容。") + "@OptIn(Api::class)\nprivate fun rejected(content: @Composable () -> Unit) { val local = 1; content() }"
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["Restricted", "rejected"])
        self.assertEqual(result["declarations"][1]["params"], ["content"])
        self.assertEqual(result["findings"], [])

    def test_multiple_bodyless_annotated_data_classes_before_class_body(self):
        source = "".join(doc(tags="@property id 标识。") + "@Immutable\ninternal data class " + name + "(val id: Int)\n" for name in ["First", "Second", "Third"])
        source += doc() + "class Actual {\n" + doc() + "val value = 1\n}"
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["First", "Second", "Third", "Actual", "value"])
        self.assertEqual(result["findings"], [])

    def test_all_visibility_and_override_are_candidates(self):
        source = "class C { private val x = 1; protected fun f() {}; internal class D; override val y = 2 }"
        self.assertEqual({f["declaration"] for f in findings(source, "missing-kdoc")}, {"C", "x", "f", "D", "y"})

    def test_bodyless_class_does_not_consume_following_function_body(self):
        source = doc() + "class Empty\n" + doc() + "fun next() { val local = 1 }"
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["Empty", "next"])
        self.assertEqual(result["findings"], [])

    def test_enum_entries_nested_class_companion_and_alias(self):
        source = doc() + "enum class Mode {\n" + doc() + "Normal,\n" + doc() + "Custom;\n" + doc() + "companion object {\n" + doc() + "val fallback = Normal\n}\n}\n" + doc() + "typealias Alias = Mode"
        result = checker.check_source(source)
        self.assertEqual([d["name"] for d in result["declarations"]], ["Mode", "Normal", "Custom", "Companion", "fallback", "Alias"])
        self.assertEqual(result["findings"], [])

    def test_empty_stale_duplicate_and_multiline_tag_descriptions(self):
        source = doc(tags="@param x\n@param x x\n@param absent 过时入参。") + "fun f(x: Int) {}"
        codes = {f["code"] for f in findings(source)}
        self.assertTrue({"empty-tag-description", "duplicate-tag", "unknown-param"}.issubset(codes))
        self.assertEqual(findings(doc(tags="@param [x]\n  用于计算的值。") + "fun f(x: Int) {}"), [])

    def test_string_fake_docs_do_not_create_findings(self):
        source = doc() + 'val x = """/** English */ fun fake() = 1"""'
        self.assertEqual(findings(source), [])

    def test_kdoc_inside_regular_comment_is_not_separate_doc(self):
        source = "/* outer /** fake */ end */\n" + doc() + "val x = 1"
        self.assertEqual(findings(source), [])

    def test_missing_function_param_is_candidate_not_error(self):
        result = findings(doc() + "fun f(x: Int, y: String) {}", "missing-param")
        self.assertEqual(len(result), 2)
        self.assertEqual({f["severity"] for f in result}, {"candidate"})


class CliTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="nomikit-kdoc-tests-")
        self.addCleanup(self.temp.cleanup)
        self.base = Path(self.temp.name)
        self.root = self.base / "project"
        self.mirror = self.base / "mirror"
        self.root.mkdir()
        self.mirror.mkdir()
        self.manifest = self.base / "manifest.json"

    def put(self, path, text):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(text.encode("utf-8"))

    def run_cli(self, *args):
        output = io.StringIO()
        with contextlib.redirect_stdout(output):
            code = checker.main(["--root", str(self.root), "--json", *args])
        return code, json.loads(output.getvalue())

    def test_manifest_mirror_and_read_only_scan(self):
        source = doc() + 'val x = "https://example/*literal*/"'
        self.put(self.root / "src/One.kt", source)
        self.put(self.mirror / "src/One.kt", "// old\n" + source)
        self.manifest.write_text(json.dumps(["src/One.kt"]), encoding="utf-8")
        before = {p: (p.read_bytes(), p.stat().st_mtime_ns) for p in self.base.rglob("*") if p.is_file()}
        code, report = self.run_cli("--manifest", str(self.manifest), "--compare-root", str(self.mirror))
        self.assertEqual(code, 0)
        self.assertEqual(report["summary"]["comparison_equal"], 1)
        self.assertEqual(before, {p: (p.read_bytes(), p.stat().st_mtime_ns) for p in self.base.rglob("*") if p.is_file()})

    def test_literal_mutation_missing_snapshot_and_exit_codes(self):
        source = doc() + 'val x = "/*literal*/"'
        self.put(self.root / "One.kt", source)
        self.put(self.mirror / "One.kt", source.replace("/*literal*/", "changed"))
        code, report = self.run_cli("One.kt", "--compare-root", str(self.mirror))
        self.assertEqual(code, 1)
        self.assertEqual(report["files"][0]["comparison"]["status"], "different")
        self.put(self.root / "Missing.kt", source)
        code, report = self.run_cli("Missing.kt", "--compare-root", str(self.mirror))
        self.assertEqual(code, 1)
        self.assertEqual(report["files"][0]["comparison"]["status"], "missing-baseline")

    def test_crlf_in_raw_literal_is_not_normalized(self):
        source = doc() + 'val x = """a\nb"""'
        self.put(self.root / "One.kt", source)
        self.put(self.mirror / "One.kt", source.replace("\n", "\r\n"))
        code, report = self.run_cli("One.kt", "--compare-root", str(self.mirror))
        self.assertEqual(code, 1)
        self.assertEqual(report["files"][0]["comparison"]["status"], "different")

    def test_manifest_rejects_objects_non_strings_and_empty_input(self):
        for payload in [{"files": ["One.kt"]}, [42], [""], []]:
            with self.subTest(payload=payload):
                self.manifest.write_text(json.dumps(payload), encoding="utf-8")
                code, report = self.run_cli("--manifest", str(self.manifest))
                self.assertEqual(code, 2)
                self.assertIn("input_error", report)

    def test_paths_cannot_escape_root(self):
        self.put(self.base / "Outside.kt", "val x=1")
        code, report = self.run_cli("../Outside.kt")
        self.assertEqual(code, 2)
        self.assertIn("--root", report["input_error"])

    def test_candidates_are_visible_but_optional_failure(self):
        self.put(self.root / "One.kt", "val missing = 1")
        code, report = self.run_cli("One.kt")
        self.assertEqual(code, 0)
        self.assertEqual(report["summary"]["candidates"], 1)
        code, _ = self.run_cli("One.kt", "--fail-on-candidates")
        self.assertEqual(code, 1)

    def test_directory_scan_deduplicates_and_checks_kts(self):
        self.put(self.root / "src/One.kt", doc() + "val x=1")
        self.put(self.root / "src/Two.kts", doc() + "val x=1")
        self.put(self.root / "src/ignore.txt", "not Kotlin")
        code, report = self.run_cli("src", "src/One.kt")
        self.assertEqual(code, 0)
        self.assertEqual(report["summary"]["files"], 2)


if __name__ == "__main__":
    unittest.main(verbosity=2)
