package com.ciyin.app.ui.app.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.ciyin.app.ui.theme.iconpack.Home
import com.ciyin.app.ui.theme.iconpack.IconPack
import com.ciyin.app.ui.theme.iconpack.LightMode
import com.ciyin.app.ui.theme.iconpack.Null
import com.ciyin.app.ui.theme.iconpack.Settings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 导航项 UI 数据类
 *
 * @property id 导航项 ID
 * @property icon 导航项图标
 * @property title 导航项标题
 * @property nav 是否可导航
 */
@Immutable
@Serializable
data class NavUiItem(
    val id: NavId,
    @Serializable(ImageVectorAsNameSerializer::class)
    val icon: ImageVector,
    val title: String,
    val nav: Boolean = false,
)

enum class NavId {
    Main,
    Null,
    Theme,
    Settings,
}

enum class NavigationSuiteType {
    NavigationBar,
    NavigationRail,
    NavigationDrawer
}

private object ImageVectorAsNameSerializer : KSerializer<ImageVector> {
    override val descriptor = PrimitiveSerialDescriptor("ImageVector", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ImageVector) {
        // 这里只序列化成字符串标识，可按需自定义
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): ImageVector {
        return when (decoder.decodeString()) {
            "Home" -> IconPack.Home
            "LightMode" -> IconPack.LightMode
            "Settings" -> IconPack.Settings
            else -> IconPack.Null
        }
    }
}
