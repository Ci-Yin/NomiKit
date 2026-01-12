package ciyin.testing

actual open class DynamicTest(
    val displayName: String,
    val action: () -> Unit,
)

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual typealias DynamicTestsResult = Unit

actual fun dynamicTest(displayName: String, action: () -> Unit): DynamicTest =
    DynamicTest(displayName, action)

actual fun runDynamicTests(dynamicTests: List<DynamicTest>) {
    for (dynamicTest in dynamicTests) {
        println("=".repeat(32) + " ${dynamicTest.displayName} " + "=".repeat(32))
        dynamicTest.action.invoke()
        println("=".repeat(32) + "=".repeat(dynamicTest.displayName.length + 2) + "=".repeat(32))
    }
}
