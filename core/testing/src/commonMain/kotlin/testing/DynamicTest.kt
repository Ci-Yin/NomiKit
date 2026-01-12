@file:JvmName("DynamicTestKt_common")

package ciyin.testing

import kotlin.jvm.JvmName


/**
 * @see dynamicTest
 */
expect open class DynamicTest // = junit.DynamicTest

/**
 * @see runDynamicTests
 */
expect class DynamicTestsResult

/**
 * Creates a dynamic test
 */
expect fun dynamicTest(displayName: String, action: () -> Unit): DynamicTest

/**
 * The returned value must be returned from the function annotated with [TestFactory], otherwise the tests won't be executed on some platforms.
 */
expect fun runDynamicTests(dynamicTests: List<DynamicTest>): DynamicTestsResult

/**
 * The returned value must be returned from the function annotated with [TestFactory], otherwise the tests won't be executed on some platforms.
 */
fun runDynamicTests(vararg dynamicTests: Iterable<DynamicTest>): DynamicTestsResult =
    runDynamicTests(dynamicTests = dynamicTests.flatMap { it })

/**
 * The returned value must be returned from the function annotated with [TestFactory], otherwise the tests won't be executed on some platforms.
 */
fun runDynamicTests(vararg dynamicTests: Sequence<DynamicTest>): DynamicTestsResult =
    runDynamicTests(dynamicTests = dynamicTests.flatMap { it })

/**
 * The returned value must be returned from the function annotated with [TestFactory], otherwise the tests won't be executed on some platforms.
 */
fun runDynamicTests(vararg dynamicTests: DynamicTest): DynamicTestsResult =
    runDynamicTests(dynamicTests.toList())

/**
 * The returned value must be returned from the function annotated with [TestFactory], otherwise the tests won't be executed on some platforms.
 */
fun runDynamicTests(dynamicTests: Sequence<DynamicTest>): DynamicTestsResult =
    runDynamicTests(dynamicTests.toList())

class DynamicTestsBuilder {
    private val list = mutableListOf<DynamicTest>()
    fun add(displayName: String, action: () -> Unit) {
        list.add(dynamicTest(displayName, action))
    }

    internal fun build() = list
}

/**
 * The returned value must be returned from the function annotated with [TestFactory], otherwise the tests won't be executed on some platforms.
 */
fun runDynamicTests(action: DynamicTestsBuilder.() -> Unit): DynamicTestsResult =
    runDynamicTests(dynamicTests = DynamicTestsBuilder().apply(action).build())

