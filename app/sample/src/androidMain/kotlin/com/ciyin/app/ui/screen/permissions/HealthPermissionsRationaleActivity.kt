package com.ciyin.app.ui.screen.permissions

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ciyin.material.theme.AppTheme
import com.ciyin.app.sample.R

/** 展示健康权限用途及数据处理方式的系统说明入口。 */
class HealthPermissionsRationaleActivity : ComponentActivity() {
    /** 创建并展示健康权限说明界面。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthPermissionsRationaleScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.health_permissions_rationale_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.health_permissions_rationale_back),
                        )
                    }
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(AppTheme.spacings.large),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.large),
        ) {
            Text(
                text = stringResource(R.string.health_permissions_rationale_summary),
                style = AppTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.health_permissions_rationale_data_title),
                style = AppTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.health_permissions_rationale_data_body),
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}
