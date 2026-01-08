/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ciyin.ui.foundation.systemuicontroller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance

/**
 * 获取系统 UI 控制器的 Composable 函数。
 *
 * 这是一个 Kotlin Multiplatform 的 expect 函数，需要在各个平台（Android、iOS、Desktop 等）提供对应的 actual 实现。
 * 在 Android 平台上，此函数会返回一个能够控制状态栏和导航栏的实际实现。
 * 在其他平台上，可能会返回一个简化实现或空操作实现。
 *
 * @return 平台特定的 [SystemUiController] 实例
 */
@Composable
expect fun rememberSystemUiController(): SystemUiController

/**
 * 一个在 Jetpack Compose 多平台中提供易于使用的实用程序来更新系统 UI 栏颜色的接口。
 *
 * **重要说明：**
 * - 这是一个 **Kotlin Multiplatform** 接口，位于 `commonMain` 源代码集中。
 * - 虽然接口设计主要参考了 Android 的系统 UI 概念（状态栏、导航栏），但通过多平台实现可以在不同平台上使用。
 * - 在 Android 平台上，此接口提供完整的功能实现，包括状态栏和导航栏的颜色、可见性控制。
 * - 在其他平台（如 iOS、Desktop）上，某些功能可能不可用或表现为空操作，具体取决于平台的 actual 实现。
 * - 接口中的 API 级别限制（如 API 29+）主要针对 Android 平台，其他平台的行为可能不同。
 *
 * @see rememberSystemUiController 获取平台特定的实现实例
 */
@Stable
interface SystemUiController {

    /**
     * 保持状态栏可见性的属性。如果设置为 true，则显示状态栏，否则隐藏状态栏。
     */
    var isStatusBarVisible: Boolean

    /**
     * 保持导航栏可见性的属性。如果设置为 true，则显示导航栏，否则隐藏导航栏。
     */
    var isNavigationBarVisible: Boolean

    /**
     * 保持状态栏和导航栏可见性的属性。如果设置为 true，则显示两个栏，否则隐藏两个栏。
     */
    var isSystemBarsVisible: Boolean
        get() = isNavigationBarVisible && isStatusBarVisible
        set(value) {
            isStatusBarVisible = value
            isNavigationBarVisible = value
        }

    /**
     * 设置状态栏颜色。
     *
     * **平台说明：**
     * - 在 Android 平台上，此方法会实际设置状态栏颜色。如果运行在仅支持白色状态栏图标的 API 级别上，颜色可能需要修改。
     * - 在其他平台上，此方法的行为取决于平台的 actual 实现，可能表现为空操作或简化实现。
     *
     * @param color 要设置的**期望** [Color]。在 Android 上，如果运行在仅支持白色状态栏图标的 API 级别上，这可能需要修改。
     * @param darkIcons 是否首选深色状态栏图标。在 Android 上，这会影响状态栏图标的颜色。
     * @param transformColorForLightContent 一个 lambda，如果请求了深色图标但不可用，将调用该 lambda 来转换 [color]。默认为应用黑色遮罩。
     *
     * @see statusBarDarkContentEnabled
     */
    fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    )

    /**
     * 设置导航栏颜色。
     *
     * **平台说明：**
     * - 在 Android 平台上，此方法会实际设置导航栏颜色。如果运行在仅支持白色导航栏图标的 API 级别上，颜色可能需要修改。
     *   此外，在首选使用手势导航的 API 29+ 上，或者系统 UI 自动在其他导航模式下应用背景保护时，此设置将被忽略并将使用 [Color.Transparent]。
     * - 在其他平台上，导航栏的概念可能不存在或表现不同，具体取决于平台的 actual 实现。
     *
     * @param color 要设置的**期望** [Color]。在 Android 上，如果运行在仅支持白色导航栏图标的 API 级别上，这可能需要修改。
     *   此外，在首选使用手势导航的 API 29+ 上，或者系统 UI 自动在其他导航模式下应用背景保护时，此设置将被忽略并将使用 [Color.Transparent]。
     * @param darkIcons 是否首选深色导航栏图标。在 Android 上，这会影响导航栏图标的颜色。
     * @param navigationBarContrastEnforced 当请求完全透明背景时，系统是否应确保导航栏具有足够的对比度。仅在 Android API 29+ 上支持，其他平台可能忽略此参数。
     * @param transformColorForLightContent 一个 lambda，如果请求了深色图标但不可用，将调用该 lambda 来转换 [color]。默认为应用黑色遮罩。
     *
     * @see navigationBarDarkContentEnabled
     * @see navigationBarContrastEnforced
     */
    fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        navigationBarContrastEnforced: Boolean = true,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    )

    /**
     * 将状态栏和导航栏设置为 [color]。
     *
     * @see setStatusBarColor
     * @see setNavigationBarColor
     */
    fun setSystemBarsColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        isNavigationBarContrastEnforced: Boolean = true,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    ) {
        setStatusBarColor(color, darkIcons, transformColorForLightContent)
        setNavigationBarColor(
            color,
            darkIcons,
            isNavigationBarContrastEnforced,
            transformColorForLightContent
        )
    }

    /**
     * 保持状态栏图标 + 内容是否为“深色”的属性。
     */
    var statusBarDarkContentEnabled: Boolean

    /**
     * 保持导航栏图标 + 内容是否为“深色”的属性。
     */
    var navigationBarDarkContentEnabled: Boolean

    /**
     * 保持状态栏和导航栏图标 + 内容是否为“深色”的属性。
     */
    var systemBarsDarkContentEnabled: Boolean
        get() = statusBarDarkContentEnabled && navigationBarDarkContentEnabled
        set(value) {
            statusBarDarkContentEnabled = value
            navigationBarDarkContentEnabled = value
        }

    /**
     * 保持系统是否确保导航栏在请求完全透明背景时具有足够对比度的属性。
     *
     * **平台说明：**
     * - 在 Android API 29+ 设备上，此属性会实际生效。
     * - 在其他平台或较低版本的 Android 上，此属性可能被忽略或表现为空操作。
     */
    var isNavigationBarContrastEnforced: Boolean
}

/**
 * [SystemUiController] 的通用实现类。
 *
 * 这是位于 `commonMain` 中的基础实现，主要用于非 Android 平台实现的类。
 *
 * **注意：** 此实现类主要用于多平台兼容性，某些功能可能表现为空操作或简化实现。
 */
internal class CommonSystemUiController : SystemUiController {

    override var isStatusBarVisible: Boolean by mutableStateOf(true)

    override var isNavigationBarVisible: Boolean by mutableStateOf(true)


    override var statusBarDarkContentEnabled: Boolean by mutableStateOf(false)

    override var navigationBarDarkContentEnabled: Boolean by mutableStateOf(false)

    override var isNavigationBarContrastEnforced: Boolean by mutableStateOf(false)

    override fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean,
        transformColorForLightContent: (Color) -> Color
    ) {
        statusBarDarkContentEnabled = darkIcons
    }

    override fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean,
        navigationBarContrastEnforced: Boolean,
        transformColorForLightContent: (Color) -> Color
    ) {
        navigationBarDarkContentEnabled = darkIcons
        isNavigationBarContrastEnforced = navigationBarContrastEnforced
    }

}

private val BlackScrim = Color(0f, 0f, 0f, 0.3f) // 30% opaque black
private val BlackScrimmed: (Color) -> Color = { original ->
    BlackScrim.compositeOver(original)
}
