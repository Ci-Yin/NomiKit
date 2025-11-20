package com.yy.myuko.core.testing

actual open class DynamicTest
actual class DynamicTestsResult

actual fun dynamicTest(
    displayName: String,
    action: () -> Unit
): DynamicTest {
    TODO("Not yet implemented")
}

actual fun runDynamicTests(dynamicTests: List<DynamicTest>): DynamicTestsResult {
    TODO("Not yet implemented")
}