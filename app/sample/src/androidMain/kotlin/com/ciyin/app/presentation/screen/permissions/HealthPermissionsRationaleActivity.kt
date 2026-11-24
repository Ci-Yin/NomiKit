package com.ciyin.app.presentation.screen.permissions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import com.ciyin.app.presentation.component.SamplePageScaffold
import com.ciyin.app.sample.R

/** 展示健康权限用途及数据处理方式的系统说明入口。 */
class HealthPermissionsRationaleActivity : ComponentActivity() {
    /** 创建并展示健康权限说明界面。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                HealthPermissionsRationaleScreen(onBack = ::finish)
            }
        }
    }
}

/**
 * 展示权限管理示例的健康权限说明。
 *
 * @param onBack 返回调用方的回调。
 */
@Composable
private fun HealthPermissionsRationaleScreen(onBack: () -> Unit) {
    SamplePageScaffold(
        title = stringResource(R.string.health_permissions_rationale_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.health_permissions_rationale_back),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(NomiTheme.spacings.large),
            verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.large),
        ) {
            NomiText(
                text = stringResource(R.string.health_permissions_rationale_summary),
                style = NomiTheme.typography.bodyLarge,
            )
            NomiText(
                text = stringResource(R.string.health_permissions_rationale_data_title),
                style = NomiTheme.typography.titleMedium,
            )
            NomiText(
                text = stringResource(R.string.health_permissions_rationale_data_body),
                style = NomiTheme.typography.bodyMedium,
            )
        }
    }
}
