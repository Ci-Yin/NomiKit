package ciyin.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.AppPreview


/**
 *
 * kotlin类作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/8/6 上午1:41
 */

@AppPreview
@Composable
private fun ScrollChoosePreview() {
    MaterialTheme {
        Box {
            var selection by remember { mutableIntStateOf(2) }
            ScrollChoose(
                labels = arrayOf("100", "200", "3000000", "400"),
                selection = selection,
                onSelectionChange = {
                    selection = it
                }
            )
            Button(onClick = { selection = 1 }, Modifier.padding(top = 50.dp)) {

            }
            Button(onClick = { selection = 2 }, Modifier.padding(top = 100.dp)) {

            }
        }
    }

}

@Composable
fun ScrollChoose(
    labels: Array<String>,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selection: Int = 0,
    shape: Shape = CircleShape,
    color: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
) {

    var lastSelection = rememberSaveable { -1 }
    var targetX by remember { mutableFloatStateOf(0.0f) }
    val offsetX by animateFloatAsState(targetX, label = "")

    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
    ) {
        Row {
            for ((index, text) in labels.withIndex()) {
                var position: Offset
                ChooseButton(
                    text = text,
                    modifier = Modifier.onGloballyPositioned {
                        position = it.positionInParent()
                        if (selection != lastSelection && index == selection) {
                            targetX = position.x
                            onSelectionChange(index)
                            lastSelection = selection
                        }
                    },
                    onClick = {
                        onSelectionChange(index)
                    }
                )
            }
        }

        ChooseButton(
            modifier = Modifier.offset {
                IntOffset(offsetX.toInt(), 0)
            },
            text = labels[selection],
            isMain = true
        )

    }

}

@Composable
private fun ChooseButton(
    text: String,
    modifier: Modifier = Modifier,
    isMain: Boolean = false,
    onClick: () -> Unit = {}
) {
    Button(
        modifier = modifier
            //.size(50.dp, 30.dp)
            .height(30.dp)
            .sizeIn(
                minWidth = 50.dp,
                minHeight = 30.dp
            )
            .clip(RoundedCornerShape(15.0.dp)),
        contentPadding = PaddingValues(horizontal = 15.dp),
        colors = if (isMain) ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = onClick
    ) {
        Text(text)
    }
}

