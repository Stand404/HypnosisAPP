package com.stand.hypnosis

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 语言选择对话框
@Composable
fun LanguageSelectDialog(
    currentLanguage: Int, // 0: 中文, 1: English, 2: Japanese
    onLanguageSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        "中文简体" to 0,
        "English" to 1,
        "日本語" to 2
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language_select)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                languages.forEach { (name, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageSelected(value)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 选中状态显示
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(2.dp, Color.Black, CircleShape)
                                .padding(2.dp)
                        ) {
                            if (value == currentLanguage) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black, CircleShape)
                                )
                            }
                        }

                        Text(
                            text = name,
                            modifier = Modifier.padding(start = 12.dp),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {}, // 自定义列表点击，不需要默认按钮
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun AnimatedSettingItem(
    index: Int,
    isVisible: Boolean,
    screenWidthPx: Float,
    start: Int,
    content: @Composable () -> Unit  // 改为接收Composable内容
) {
    val targetOffsetX by animateFloatAsState(
        targetValue = if (isVisible) 0f else screenWidthPx,
        animationSpec = if (isVisible) {
            tween(
                durationMillis = 200,
                delayMillis = start + index * 150,
                easing = FastOutSlowInEasing
            )
        } else {
            tween(durationMillis = 150, easing = FastOutSlowInEasing)
        },
        label = "slide_in_animation_$index"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = targetOffsetX
            }
    ) {
        content()  // 在这里渲染具体的配置组件

    }
}

// 开关配置项
@Composable
fun SwitchSettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 18.sp, modifier = Modifier.weight(1f) )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// 颜色预览配置项
@Composable
fun ColorSettingItem(
    title: String,
    color: Int,
    onColorClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(onClick = onColorClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 18.sp, modifier = Modifier.weight(1f) )
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(Color(color), CircleShape)
        )
    }
}

// 滑块配置项
@Composable
fun SliderSettingItem(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    formatValue: (Float) -> String = { it.toString() }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 18.sp, modifier = Modifier.weight(1f) )
        Row(modifier = Modifier.weight(2f),verticalAlignment = Alignment.CenterVertically){
            Text(
                text = formatValue(value),
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.width(30.dp)
            )
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ColorPickerDialog(
    currentColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var red by remember {
        mutableFloatStateOf((currentColor shr 16 and 0xff) / 255f)
    }
    var green by remember {
        mutableFloatStateOf((currentColor shr 8 and 0xff) / 255f)
    }
    var blue by remember {
        mutableFloatStateOf((currentColor and 0xff) / 255f)
    }

    val selectedColor = remember(red, green, blue) {
        Color(red, green, blue)
    }

    val hexValue = remember(red, green, blue) {
        val r = (red * 255).toInt()
        val g = (green * 255).toInt()
        val b = (blue * 255).toInt()
        String.format("#%02X%02X%02X", r, g, b)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.color_picker))},
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // 当前颜色预览
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(selectedColor)
                ) {
                    Text(
                        text = hexValue,
                        modifier = Modifier.align(Alignment.Center),
                        color = if (selectedColor.luminance() > 0.5f) Color.Black else Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 红色通道
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.red),
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(4.dp))
                    Slider(
                        value = red,
                        onValueChange = { red = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        "${(red * 255).toInt()}",
                        modifier = Modifier.width(30.dp),
                        textAlign = TextAlign.End
                    )
                }

                // 绿色通道
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.green),
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(4.dp))
                    Slider(
                        value = green,
                        onValueChange = { green = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${(green * 255).toInt()}",
                        modifier = Modifier.width(30.dp),
                        textAlign = TextAlign.End
                    )
                }

                // 蓝色通道
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.blue),
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(4.dp))
                    Slider(
                        value = blue,
                        onValueChange = { blue = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${(blue * 255).toInt()}",
                        modifier = Modifier.width(30.dp),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 快速预设颜色
                val presetColors = listOf(
                    0xFFFF0000 to "红", 0xFF00FF00 to "绿", 0xFF0000FF to "蓝",
                    0xFFFFFF00 to "黄", 0xFFFF00FF to "紫", 0xFF00FFFF to "青",
                    0xFFFFFFFF to "白", 0xFF000000 to "黑", 0xFFFFA500 to "橙",
                    0xFF808080 to "灰", 0xFFA52A2A to "棕", 0xFFFFC0CB to "粉"
                )

                Text(stringResource(R.string.quick_select), modifier = Modifier.padding(vertical = 8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetColors) { (color, _) ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(color))
                                .border(
                                    2.dp,
                                    if (color == selectedColor.toArgb()
                                            .toLong()
                                    ) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable {
                                    red = ((color shr 16) and 0xff) / 255f
                                    green = ((color shr 8) and 0xff) / 255f
                                    blue = (color and 0xff) / 255f
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss
                onColorSelected(selectedColor.toArgb())}) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        titleContentColor = MaterialTheme.colorScheme.secondary,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun About() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        val uriHandler = LocalUriHandler.current

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 第一行：版权信息
            Row {
                Text(
                    text = "Copyright © 2025 ",
                    color = Color.White.copy(0.7f),
                    fontSize = 12.sp)
                Text(
                    text = "Stand404",
                    color = Color(0xFF29C6FF),
                    fontSize = 12.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        uriHandler.openUri("https://space.bilibili.com/382365750")
                    })
            }

            // 第二行：GitHub 链接
            Text(
                text = "GitHub",
                color = Color(0xFF6CC644), // GitHub 绿色
                fontSize = 12.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable {
                        uriHandler.openUri("https://github.com/Stand404/HypnosisAPP")
                    }
            )
        }
    }
}
