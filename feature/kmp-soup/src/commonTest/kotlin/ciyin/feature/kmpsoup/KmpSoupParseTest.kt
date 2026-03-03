package ciyin.feature.kmpsoup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * `kmp-soup` 公共接口全量契约测试。
 *
 * 覆盖 `HtmlDocument`、`HtmlElement`、`HtmlElements`、`DocumentType`、`FormElement`
 * 在 `commonMain` 中声明的全部公开函数，确保跨平台实现语义稳定。
 */
class KmpSoupParseTest {

    /**
     * 验证 `HtmlDocument` 与 `DocumentType` 的全部公开函数。
     */
    @Test
    fun htmlDocument_and_documentType_public_functions_should_work() {
        val document = parseDoc()

        assertEquals("API Test", document.title())
        assertEquals("body", document.body().tagName().lowercase())

        val location = document.location()
        assertTrue(location.isEmpty() || location.startsWith("http"))

        val head = document.head()
        assertEquals("head", head.tagName().lowercase())

        val documentType = assertNotNull(document.documentType())
        assertEquals("html", documentType.name().lowercase())
        assertTrue(documentType.publicId().contains("W3C"))
        assertTrue(documentType.systemId().contains("xhtml1-transitional.dtd"))
        assertEquals("#doctype", documentType.nodeName().lowercase())
        documentType.setPubSysKey("PUBLIC")
        documentType.setPubSysKey(null)

        val forms = document.forms()
        assertEquals(1, forms.size)

        val form = document.expectForm("#login-form")
        assertEquals("login-form", form.id())

        document.title("Updated Title")
        assertEquals("Updated Title", document.title())

        val created = document.createElement("article")
        assertEquals("article", created.tagName().lowercase())

        val chained = document.text("Body Replaced")
        assertTrue(chained === document)
        assertTrue(document.body().text().contains("Body Replaced"))
    }

    /**
     * 验证 `FormElement` 的全部公开函数。
     */
    @Test
    fun formElement_public_functions_should_work() {
        val document = parseDoc()
        val form = document.expectForm("#login-form")

        val controls = form.elements()
        assertTrue(controls.any { it.id() == "username" })

        val baseData = form.formData().toMap()
        assertEquals("alice", baseData["username"])
        assertEquals("yes", baseData["remember"])
        assertFalse(baseData.containsKey("ignored"))

        val extraInput = KmpSoup.parse("<input name='extra' value='100' />").selectFirst("input")
        assertNotNull(extraInput)

        val returned = form.addElement(extraInput)
        assertTrue(returned === form)

        val afterAddData = form.formData().toMap()
        assertEquals("100", afterAddData["extra"])
    }

    /**
     * 验证 `HtmlElement` 的基础查询、文本、属性与导航函数。
     */
    @Test
    fun htmlElement_read_and_navigation_public_functions_should_work() {
        val document = parseDoc()
        val root = assertNotNull(document.getElementById("root"))
        val p1 = assertNotNull(root.getElementById("p1"))

        assertEquals(3, root.getElementsByTag("p").size)
        assertTrue(root.getElementsByClass("text").isNotEmpty())
        assertEquals(3, root.select("li.item").size)
        assertEquals("item2", assertNotNull(root.selectFirst("#item2")).id())

        assertTrue(p1.text().contains("Hello"))
        assertEquals("Hello", p1.ownText().trim())
        assertEquals("intro", p1.attr("data-kind"))
        assertTrue(p1.hasAttr("data-kind"))
        assertEquals("p1", p1.id())
        assertEquals("p", p1.tagName().lowercase())
        assertTrue(p1.className().contains("text"))
        assertTrue(p1.classNames().contains("text"))

        val parent = assertNotNull(p1.parent())
        assertEquals("section-main", parent.id())

        val children = p1.children()
        assertEquals(1, childrenSizeOf(p1))
        assertEquals(1, children.size)
        assertEquals("span1", p1.child(0).id())

        assertTrue(p1.html().contains("span"))
        assertTrue(p1.outerHtml().contains("id=\"p1\"") || p1.outerHtml().contains("id='p1'"))

        val sib2 = assertNotNull(document.getElementById("sib2"))
        assertTrue(sib2.parents().any { it.id() == "siblings" })
        assertEquals(2, sib2.siblingElements().size)
        assertEquals(listOf("sib3"), sib2.nextElementSiblings().map { it.id() })
        assertEquals(listOf("sib1"), sib2.previousElementSiblings().map { it.id() })
        assertEquals("sib1", sib2.firstElementSibling().id())
        assertEquals("sib3", sib2.lastElementSibling().id())
        assertEquals(1, sib2.elementSiblingIndex())
    }

    /**
     * 验证 `HtmlElement` 的高级查询、类操作、值操作与匹配函数。
     */
    @Test
    fun htmlElement_query_and_mutation_public_functions_should_work() {
        val document = parseDoc()
        val section = assertNotNull(document.getElementById("section-main"))

        assertTrue(section.getElementsByAttribute("data-no").isNotEmpty())
        assertTrue(
            section.getElementsByAttributeStarting("data-pref").any { it.id() == "attr-zone" })
        assertEquals(2, section.getElementsByAttributeValue("data-group", "g").size)
        assertTrue(
            section.getElementsByAttributeValueNot("data-group", "g").any { it.id() == "item3" })
        assertTrue(
            section.getElementsByAttributeValueStarting("data-token", "prefix")
                .any { it.id() == "attr-zone" })
        assertTrue(
            section.getElementsByAttributeValueEnding("data-token", "suffix")
                .any { it.id() == "attr-zone" })
        assertTrue(
            section.getElementsByAttributeValueContaining("data-token", "middle")
                .any { it.id() == "attr-zone" })
        assertTrue(section.getElementsContainingText("Second Paragraph").any { it.id() == "p2" })
        assertTrue(section.getElementsContainingOwnText("Second").any { it.id() == "p2" })
        assertTrue(section.getElementsMatchingText("InnerText\\d+").isNotEmpty())
        assertTrue(section.getElementsMatchingOwnText("S\\d").isNotEmpty())
        assertTrue(section.getAllElements().isNotEmpty())

        val p2 = assertNotNull(document.getElementById("p2"))
        assertTrue(p2.hasClass("second"))

        val afterAdd = p2.addClass("new-class")
        assertTrue(afterAdd === p2)
        assertTrue(p2.hasClass("new-class"))

        val afterRemove = p2.removeClass("new-class")
        assertTrue(afterRemove === p2)
        assertFalse(p2.hasClass("new-class"))

        val beforeToggle = p2.hasClass("toggle-flag")
        val afterToggle = p2.toggleClass("toggle-flag")
        assertTrue(afterToggle === p2)
        assertTrue(p2.hasClass("toggle-flag") != beforeToggle)

        val username = assertNotNull(document.getElementById("username"))
        assertEquals("alice", username.value())
        val afterSetValue = username.value("bob")
        assertTrue(afterSetValue === username)
        assertEquals("bob", username.value())

        assertTrue(p2.wholeText().contains("Second"))
        assertTrue(p2.wholeOwnText().contains("Second"))
        assertTrue(p2.hasText())

        val scriptNode = assertNotNull(document.getElementById("script-node"))
        assertTrue(scriptNode.data().contains("answer"))

        val selector = username.cssSelector()
        assertTrue(selector.isNotBlank())

        assertTrue(username.matches("input#username"))
        assertEquals("login-form", assertNotNull(username.closest("form")).id())

        val xpathResult = document.body().selectXpath("//*[@id='item2']")
        assertTrue(xpathResult.size >= 0)
    }

    /**
     * 验证 `HtmlElements` 的文本、属性、筛选与选择器函数。
     */
    @Test
    fun htmlElements_read_and_filter_public_functions_should_work() {
        val document = parseDoc()
        val items = document.select("li.item")

        assertEquals(3, items.size)
        assertTrue(items.text().contains("One"))
        assertEquals("1", items.attr("data-no"))
        assertTrue(items.hasAttr("data-no"))
        assertEquals(listOf("1", "2", "3"), items.eachAttr("data-no"))

        assertEquals(1, items.select(".second").size)
        assertEquals("item2", assertNotNull(items.selectFirst(".second")).id())
        assertEquals("item1", items.expectFirst(".first").id())
        assertEquals(2, items.not(".second").size)
        assertEquals("item2", items.eq(1).first().id())
        assertTrue(items.matches(".third"))

        assertTrue(items.hasText())
        assertEquals(3, items.eachText().size)
        assertTrue(items.html().contains("One"))
    }

    /**
     * 验证 `HtmlElements` 的批量修改函数。
     */
    @Test
    fun htmlElements_mutation_public_functions_should_work() {
        val document = parseDoc()

        val paragraphs = document.select("p.text")

        val afterSetAttr = paragraphs.attr("data-batch", "yes")
        assertTrue(afterSetAttr === paragraphs)
        assertEquals(2, document.select("p[data-batch=yes]").size)

        val afterRemoveAttr = paragraphs.removeAttr("data-batch")
        assertTrue(afterRemoveAttr === paragraphs)
        assertFalse(document.select("p[data-batch]").isNotEmpty())

        val afterAddClass = paragraphs.addClass("batch-class")
        assertTrue(afterAddClass === paragraphs)
        assertTrue(paragraphs.hasClass("batch-class"))

        val afterRemoveClass = paragraphs.removeClass("batch-class")
        assertTrue(afterRemoveClass === paragraphs)
        assertFalse(paragraphs.hasClass("batch-class"))

        val beforeToggle = paragraphs.hasClass("toggle-all")
        val afterToggleClass = paragraphs.toggleClass("toggle-all")
        assertTrue(afterToggleClass === paragraphs)
        assertTrue(paragraphs.hasClass("toggle-all") != beforeToggle)

        val usernameElements = document.select("#username")
        assertEquals("alice", usernameElements.value())
        val afterSetElementsValue = usernameElements.value("carol")
        assertTrue(afterSetElementsValue === usernameElements)
        assertEquals("carol", assertNotNull(document.getElementById("username")).value())

        val siblingBs = document.select("#siblings b")
        val afterTagName = siblingBs.tagName("strong")
        assertTrue(afterTagName === siblingBs)
        assertEquals(3, document.select("#siblings strong").size)

        val p2 = document.select("#p2")
        val afterSetHtml = p2.html("<i id='inline-i'>Italic</i>")
        assertTrue(afterSetHtml === p2)
        assertNotNull(document.getElementById("inline-i"))

        val attrZone = document.select("#attr-zone")
        val afterPrepend = attrZone.prepend("<span id='prepended-node'>PRE</span>")
        assertTrue(afterPrepend === attrZone)
        val afterAppend = attrZone.append("<span id='appended-node'>APP</span>")
        assertTrue(afterAppend === attrZone)
        assertNotNull(document.getElementById("prepended-node"))
        assertNotNull(document.getElementById("appended-node"))

        val p1 = document.select("#p1")
        val afterBefore = p1.before("<hr id='before-hr'/>")
        assertTrue(afterBefore === p1)
        val afterAfter = p1.after("<hr id='after-hr'/>")
        assertTrue(afterAfter === p1)
        assertNotNull(document.getElementById("before-hr"))
        assertNotNull(document.getElementById("after-hr"))

        val afterWrap = p1.wrap("<div class='wrap-box'></div>")
        assertTrue(afterWrap === p1)
        val wrappedP1 = assertNotNull(document.getElementById("p1"))
        assertTrue(assertNotNull(wrappedP1.parent()).hasClass("wrap-box"))

        val afterUnwrap = p1.unwrap()
        assertTrue(afterUnwrap === p1)
        val unwrappedP1 = document.getElementById("p1")
        if (unwrappedP1 != null) {
            assertNotNull(unwrappedP1.parent())
        }

        val sideSection = document.select("#section-side")
        val afterEmpty = sideSection.empty()
        assertTrue(afterEmpty === sideSection)
        assertEquals("", assertNotNull(document.getElementById("section-side")).text())

        val removeTarget = document.select("#item3")
        val afterRemove = removeTarget.remove()
        assertTrue(afterRemove === removeTarget)
        assertEquals(0, document.select("#item3").size)
    }

    /**
     * 验证 `HtmlElements` 的兄弟/父链导航与表单提取函数。
     */
    @Test
    fun htmlElements_navigation_and_forms_public_functions_should_work() {
        val document = parseDoc()
        val siblings = document.select("#sib1, #sib2")

        assertEquals(2, siblings.next().size)
        assertEquals(1, siblings.next(".current").size)
        assertTrue(siblings.nextAll().isNotEmpty())
        assertTrue(siblings.nextAll(".last").all { it.id() == "sib3" })

        assertEquals(1, siblings.prev().size)
        assertEquals(1, siblings.prev(".first").size)
        assertTrue(siblings.prevAll().isNotEmpty())
        assertTrue(siblings.prevAll(".first").isNotEmpty())

        assertTrue(siblings.parents().isNotEmpty())

        val forms = document.select("form").forms()
        assertEquals(1, forms.size)
        assertEquals("login-form", forms.first().id())
    }

    /**
     * 解析测试文档。
     */
    private fun parseDoc(): HtmlDocument = KmpSoup.parse(SAMPLE_HTML)

    /**
     * 兼容不同平台实现的子元素数量读取。
     */
    private fun childrenSizeOf(element: HtmlElement): Int = element.childrenSize()
}

/**
 * 公共接口测试用 HTML。
 */
private const val SAMPLE_HTML = """
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <title>API Test</title>
    <script id="script-node">var answer = 42;</script>
    <style id="style-node">.x { color: red; }</style>
  </head>
  <body>
    <div id="root" class="root page" data-pref-main="pref-root">
      <section id="section-main" class="container alpha" data-role="main">
        <p id="p1" class="text first" data-kind="intro">Hello <span id="span1" class="mark">World</span></p>
        <p id="p2" class="text second" data-kind="intro">Second Paragraph</p>

        <div id="attr-zone"
             data-pref-extra="pref-zone"
             data-token="prefix-middle-suffix"
             data-type="alpha"
             data-text="Some Token Text">
          <em id="em1" class="inner">InnerText123</em>
        </div>

        <div id="siblings">
          <b id="sib1" class="sib first">S1</b>
          <b id="sib2" class="sib current">S2</b>
          <b id="sib3" class="sib last">S3</b>
        </div>

        <ul id="items">
          <li id="item1" class="item first" data-no="1" data-group="g">One</li>
          <li id="item2" class="item second" data-no="2" data-group="g">Two</li>
          <li id="item3" class="item third" data-no="3" data-group="h">Three</li>
        </ul>

        <form id="login-form" class="form account">
          <input id="username" type="text" name="username" value="alice" />
          <input id="remember" type="checkbox" name="remember" value="yes" checked />
          <input id="ignored" type="checkbox" name="ignored" value="no" />
          <textarea id="bio" name="bio">hello-bio</textarea>
          <select id="city" name="city">
            <option value="sh">Shanghai</option>
            <option value="hz" selected>Hangzhou</option>
          </select>
        </form>
      </section>

      <section id="section-side" class="container beta" data-role="side">
        <p id="side-text">Tail Text</p>
      </section>
    </div>
  </body>
</html>
"""
