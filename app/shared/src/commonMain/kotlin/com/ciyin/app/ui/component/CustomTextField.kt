package com.ciyin.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.AppPreview

@AppPreview
@Composable
fun CustomTextFieldPreview() {
    //SettingsScreen(SettingsViewModel())
    Column {

        CustomTextField(
            modifier = Modifier.widthIn(min = 120.dp),
            value = "com.ciyin.rpa.ui.settings.SettingsScreenKt",
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = { Icon(Icons.Default.Search, null) },
            onValueChange = { },

            )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    hintStyle: TextStyle = TextStyle(
        color = colors.unfocusedIndicatorColor,
        fontSize = 12.sp
    ),
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.Companion.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Companion.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Companion.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentPadding: PaddingValues = PaddingValues(4.dp),
    shape: Shape = OutlinedTextFieldDefaults.shape
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        decorationBox = @Composable { innerTextField ->
            val isFocused = interactionSource.collectIsFocusedAsState().value
            DecorationBox(
                hint = hint,
                hintStyle = hintStyle,
                innerTextField = innerTextField,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                contentPadding = contentPadding,
                shape = shape,
                colors = colors,
                isShowHint = value.isEmpty(),
                isFocused = isFocused
            )
        }
    )
}

@Composable
private fun DecorationBox(
    hint: String,
    hintStyle: TextStyle,
    isShowHint: Boolean,
    innerTextField: @Composable () -> Unit,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    contentPadding: PaddingValues,
    shape: Shape,
    colors: TextFieldColors,
    isFocused: Boolean
) {
    Row(
        modifier = Modifier.Companion
            .background(
                if (isFocused) colors.focusedContainerColor else colors.unfocusedContainerColor,
                shape
            )
            .border(
                if (isFocused) 2.dp else 0.5.dp,
                if (isFocused) colors.focusedIndicatorColor else colors.unfocusedIndicatorColor,
                shape
            )
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        leadingIcon?.invoke()
        Spacer(Modifier.width(4.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (isShowHint && hint.isNotEmpty()) {
                Text(
                    text = hint,
                    style = hintStyle,
                )
            }
            innerTextField()
        }
        trailingIcon?.invoke()
    }
}