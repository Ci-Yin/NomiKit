package ciyin.ui.foundation.systemuicontroller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/1/8 20:09
 */


/**
 * 系统UI控制器效果组合函数
 *
 * 这是一个用于控制系统UI（如状态栏、导航栏）的副作用组合函数。
 * 当key1发生变化时，会重新执行block中的系统UI配置。
 *
 * @param key1 用于触发重组的键值。当这个值改变时，LaunchedEffect会取消之前的协程并启动新的协程
 * @param block 在SystemUiController上下文中执行的挂起函数块，用于配置系统UI的样式和行为
 */
@Composable
fun SystemUiControllerEffect(key1: Any?, block: SystemUiController.() -> Unit) {
    val controller = rememberSystemUiController()
    LaunchedEffect(key1) { controller.block() }
}

/**
 * 系统UI控制器效果组合函数
 *
 * 这是一个用于控制系统UI（如状态栏、导航栏）的副作用组合函数。
 * 当key1或key2任意一个发生变化时，会重新执行block中的系统UI配置。
 *
 * @param key1 用于触发重组的第一个键值
 * @param key2 用于触发重组的第二个键值
 * @param block 在SystemUiController上下文中执行的挂起函数块，用于配置系统UI的样式和行为
 */
@Composable
fun SystemUiControllerEffect(key1: Any?, key2: Any?, block: SystemUiController.() -> Unit) {
    val controller = rememberSystemUiController()
    LaunchedEffect(key1, key2) { controller.block() }
}

/**
 * 系统UI控制器效果组合函数
 *
 * 这是一个用于控制系统UI（如状态栏、导航栏）的副作用组合函数。
 * 当key1、key2或key3任意一个发生变化时，会重新执行block中的系统UI配置。
 *
 * @param key1 用于触发重组的第一个键值
 * @param key2 用于触发重组的第二个键值
 * @param key3 用于触发重组的第三个键值
 * @param block 在SystemUiController上下文中执行的挂起函数块，用于配置系统UI的样式和行为
 */
@Composable
fun SystemUiControllerEffect(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    block: SystemUiController.() -> Unit
) {
    val controller = rememberSystemUiController()
    LaunchedEffect(key1, key2, key3) { controller.block() }
}