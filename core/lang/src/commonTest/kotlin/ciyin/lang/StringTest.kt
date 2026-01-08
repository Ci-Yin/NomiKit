package ciyin.lang

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * String 扩展函数测试类
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/8/24
 */
class StringTest {

    // ========== format 函数测试 ==========

    @Test
    fun testFormat_StringExtension() {
        // 测试字符串扩展函数 format
        assertEquals("Hello World", "Hello %s".format("World"))
        assertEquals(
            "Hello World, you have 5 items",
            "Hello %s, you have %d items".format("World", 5)
        )
        assertEquals("Price: 19.99, Quantity: 3", "Price: %s, Quantity: %d".format("19.99", 3))
    }

    @Test
    fun testFormat_StringCompanion() {
        // 测试 String.Companion.format
        assertEquals("Hello World", String.format("Hello %s", "World"))
        assertEquals("Number: 42", String.format("Number: %d", 42))
        assertEquals("Value: 3.14", String.format("Value: %f", 3.14))
        assertEquals(
            "Price: 19.99, Quantity: 3",
            String.format("Price: %.2f, Quantity: %d", 19.99, 3)
        )
    }

    @Test
    fun testFormat_MultipleArgs() {
        // 测试多个参数
        assertEquals("A B C", "%s %s %s".format("A", "B", "C"))
        assertEquals("1 2 3", "%d %d %d".format(1, 2, 3))
        assertEquals("1.1 2.2 3.3", "%.1f %.1f %.1f".format(1.1, 2.2, 3.3))
    }

    @Test
    fun testFormat_PercentSign() {
        // 测试百分号转义
        assertEquals("50%", "50%%".format())
        assertEquals("Discount: 20%", "Discount: %d%%".format(20))
        assertEquals("100% complete", "%d%% complete".format(100))
    }

    @Test
    fun testFormat_Precision() {
        // 测试浮点数精度
        assertEquals("Price: 19.99", String.format("Price: %.2f", 19.99))
        assertEquals("Value: 3.142", String.format("Value: %.3f", 3.14159))
        assertEquals("Amount: 100.0", String.format("Amount: %.1f", 100.0))
    }

    @Test
    fun testFormat_NullValues() {
        // 测试 null 值处理
        assertEquals("null", "%s".format(null))
        assertEquals("0", "%d".format(null))
        assertEquals("0.0", "%f".format(null))
        assertEquals("Hello null", "Hello %s".format(null))
    }

    @Test
    fun testFormat_InvalidArgs() {
        // 测试无效参数
        assertEquals("0", "%d".format("not a number"))
        assertEquals("0.0", "%f".format("not a number"))
        assertEquals("test", "%s".format("test"))
    }

    @Test
    fun testFormat_EmptyString() {
        // 测试空字符串
        assertEquals("", "".format())
        assertEquals("Hello", "Hello".format())
    }

    // ========== match 函数测试 ==========

    @Test
    fun testMatch_Basic() {
        // 测试基本匹配
        assertEquals("123", "Hello123World".match("\\d+"))
        assertEquals("", "Hello".match("\\d+"))
        assertEquals("abc", "test abc def".match("\\w+"))
    }

    @Test
    fun testMatch_WithGroups() {
        // 测试带捕获组的匹配
        assertEquals("Hello", "Hello World".match("(\\w+) (\\w+)"))
        assertEquals("2024", "Date: 2024-01-01".match("(\\d{4})-(\\d{2})-(\\d{2})"))
    }

    @Test
    fun testMatch_NoMatch() {
        // 测试无匹配情况
        assertEquals("", "test".match("xyz"))
        assertEquals("", "".match("\\d+"))
    }

    @Test
    fun testMatch_Email() {
        // 测试邮箱匹配
        assertEquals("test@example.com", "Contact: test@example.com".match("([\\w.]+@[\\w.]+)"))
    }

    // ========== matchIn 函数测试 ==========

    @Test
    fun testMatchIn_Basic() {
        // 测试基本匹配检查
        assertTrue("Hello123".matchIn("\\d+"))
        assertFalse("Hello".matchIn("\\d+"))
        assertTrue("test@example.com".matchIn("@"))
        assertFalse("test".matchIn("@"))
    }

    @Test
    fun testMatchIn_Email() {
        // 测试邮箱匹配
        assertTrue("user@example.com".matchIn("[\\w.]+@[\\w.]+"))
        assertFalse("not an email".matchIn("[\\w.]+@[\\w.]+"))
    }

    @Test
    fun testMatchIn_EmptyString() {
        // 测试空字符串
        assertFalse("".matchIn("\\d+"))
        assertTrue("".matchIn(""))
    }

    // ========== matchGroup 函数测试 ==========

    @Test
    fun testMatchGroup_Basic() {
        // 测试基本匹配组
        val groups = "123".matchGroup("\\d+")
        assertEquals(1, groups.size)
        assertEquals("123", groups[0])
    }

    @Test
    fun testMatchGroup_WithCaptureGroups() {
        // 测试带捕获组的匹配
        val groups = "Hello World".matchGroup("(\\w+) (\\w+)")
        assertEquals(3, groups.size)
        assertEquals("Hello World", groups[0])
        assertEquals("Hello", groups[1])
        assertEquals("World", groups[2])
    }

    @Test
    fun testMatchGroup_DatePattern() {
        // 测试日期模式
        val groups = "2024-01-01".matchGroup("(\\d{4})-(\\d{2})-(\\d{2})")
        assertEquals(4, groups.size)
        assertEquals("2024-01-01", groups[0])
        assertEquals("2024", groups[1])
        assertEquals("01", groups[2])
        assertEquals("01", groups[3])
    }

    @Test
    fun testMatchGroup_NoMatch() {
        // 测试无匹配情况
        val groups = "test".matchGroup("xyz")
        assertTrue(groups.isEmpty())
    }

    // ========== isChinese 函数测试 ==========

    @Test
    fun testIsChinese_Basic() {
        // 测试基本中文字符
        assertTrue("你好".isChinese())
        assertTrue("世界".isChinese())
        assertFalse("Hello".isChinese())
        assertFalse("123".isChinese())
    }

    @Test
    fun testIsChinese_Mixed() {
        // 测试混合字符串
        assertTrue("Hello你好".isChinese())
        assertTrue("你好World".isChinese())
        assertTrue("123你好".isChinese())
        assertFalse("Hello World".isChinese())
    }

    @Test
    fun testIsChinese_Empty() {
        // 测试空字符串
        assertFalse("".isChinese())
    }

    @Test
    fun testIsChinese_CommonCharacters() {
        // 测试常用中文字符
        assertTrue("中文".isChinese())
        assertTrue("测试".isChinese())
        assertTrue("编程".isChinese())
        assertTrue("开发".isChinese())
    }

    // ========== containsOrDefault 函数测试 ==========

    @Test
    fun testContainsOrDefault_Regex_Match() {
        // 测试匹配情况（Regex 版本）
        val result = "hello123".containsOrDefault(Regex("\\d+")) { "no match" }
        assertEquals("hello123", result)
    }

    @Test
    fun testContainsOrDefault_Regex_NoMatch() {
        // 测试无匹配情况（Regex 版本）
        val result = "hello".containsOrDefault(Regex("\\d+")) { "no match" }
        assertEquals("no match", result)
    }

    @Test
    fun testContainsOrDefault_String_Match() {
        // 测试匹配情况（String 版本）
        val result = "hello123".containsOrDefault("\\d+") { "no match" }
        assertEquals("hello123", result)
    }

    @Test
    fun testContainsOrDefault_String_NoMatch() {
        // 测试无匹配情况（String 版本）
        val result = "hello".containsOrDefault("\\d+") { "no match" }
        assertEquals("no match", result)
    }

    @Test
    fun testContainsOrDefault_EmptyString() {
        // 测试空字符串
        val result1 = "".containsOrDefault(Regex("\\d+")) { "default" }
        assertEquals("default", result1)

        val result2 = "".containsOrDefault("\\d+") { "default" }
        assertEquals("default", result2)
    }

    // ========== findOrDefault 函数测试 ==========

    @Test
    fun testFindOrDefault_Regex_Match() {
        // 测试匹配情况（Regex 版本）
        val result = "price: 19.99".findOrDefault(Regex("\\d+\\.\\d+")) { "0.0" }
        assertEquals("19.99", result)
    }

    @Test
    fun testFindOrDefault_Regex_NoMatch() {
        // 测试无匹配情况（Regex 版本）
        val result = "price: unknown".findOrDefault(Regex("\\d+\\.\\d+")) { "0.0" }
        assertEquals("0.0", result)
    }

    @Test
    fun testFindOrDefault_String_Match() {
        // 测试匹配情况（String 版本）
        val result = "price: 19.99".findOrDefault("\\d+\\.\\d+") { "0.0" }
        assertEquals("19.99", result)
    }

    @Test
    fun testFindOrDefault_String_NoMatch() {
        // 测试无匹配情况（String 版本）
        val result = "price: unknown".findOrDefault("\\d+\\.\\d+") { "0.0" }
        assertEquals("0.0", result)
    }

    @Test
    fun testFindOrDefault_FirstMatch() {
        // 测试只返回第一个匹配
        val result = "123 and 456".findOrDefault(Regex("\\d+")) { "0" }
        assertEquals("123", result)
    }

    @Test
    fun testFindOrDefault_EmptyString() {
        // 测试空字符串
        val result1 = "".findOrDefault(Regex("\\d+")) { "default" }
        assertEquals("default", result1)

        val result2 = "".findOrDefault("\\d+") { "default" }
        assertEquals("default", result2)
    }

    // ========== isHttp 函数测试 ==========

    @Test
    fun testIsHttp_HttpUrl() {
        // 测试 HTTP URL
        assertTrue("http://example.com".isHttp())
        assertTrue("http://www.example.com".isHttp())
        assertTrue("http://example.com/path".isHttp())
    }

    @Test
    fun testIsHttp_HttpsUrl() {
        // 测试 HTTPS URL
        assertTrue("https://example.com".isHttp())
        assertTrue("https://www.example.com".isHttp())
        assertTrue("https://example.com/path?query=1".isHttp())
    }

    @Test
    fun testIsHttp_NotHttp() {
        // 测试非 HTTP URL
        assertFalse("ftp://example.com".isHttp())
        assertFalse("file:///path/to/file".isHttp())
        assertFalse("example.com".isHttp())
        assertFalse("www.example.com".isHttp())
        assertFalse("Hello World".isHttp())
    }

    @Test
    fun testIsHttp_EmptyString() {
        // 测试空字符串
        assertFalse("".isHttp())
    }

    @Test
    fun testIsHttp_WithPort() {
        // 测试带端口的 URL
        assertTrue("http://example.com:8080".isHttp())
        assertTrue("https://example.com:443".isHttp())
    }

    // ========== 边界情况测试 ==========

    @Test
    fun testFormat_MoreArgsThanPlaceholders() {
        // 测试参数多于占位符
        assertEquals("Hello World", "Hello %s".format("World", "Extra"))
    }

    @Test
    fun testFormat_LessArgsThanPlaceholders() {
        // 测试参数少于占位符
        assertEquals("Hello null, you have 0 items", "Hello %s, you have %d items".format())
    }

    @Test
    fun testMatch_SpecialCharacters() {
        // 测试特殊字符匹配
        assertEquals("test@example.com", "Email: test@example.com".match("([\\w.]+@[\\w.]+)"))
    }

    @Test
    fun testMatchGroup_MultipleMatches() {
        // 测试多个匹配（只返回第一个）
        val groups = "123 456 789".matchGroup("\\d+")
        assertEquals(1, groups.size)
        assertEquals("123", groups[0])
    }

    @Test
    fun testIsChinese_SpecialCases() {
        // 测试特殊情况
        assertTrue("，。！？".isChinese()) // 中文标点
        assertFalse(",.!?".isChinese()) // 英文标点
    }
}

