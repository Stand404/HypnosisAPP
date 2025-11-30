package com.stand.hypnosis

import android.content.Context
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stand.hypnosis.model.Language
import com.stand.hypnosis.utils.AppLanguageUtils.changeAppLanguage
import com.stand.hypnosis.utils.AppLanguageUtils.readLanguageTag
import com.stand.hypnosis.utils.AppLanguageUtils.saveLanguageTag
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


/**
 * 下滑呼出 / 上滑收起 抽屉
 */
@Composable
fun TopSwipeDrawerLayer(
    modifier: Modifier = Modifier,
    configManager: ConfigManager,
    onConfigUpdate: (() -> Unit)? = null  // 添加这个参数
) {
    // 使用 mutableStateOf 来管理配置状态
    var isBeating by remember { mutableStateOf(configManager.isBeating) }
    var isFullScreen by remember { mutableStateOf(configManager.isFullScreen) }
    var ringColor by remember { mutableIntStateOf(configManager.ringColor) }
    var bgColor by remember { mutableIntStateOf(configManager.bgColor) }
    val context = LocalContext.current
    // 读取pref中存储的语言
    val (currentLanguageTag, setCurrentLanguageTag) = remember(context) {
        mutableStateOf(readLanguageTag(context))
    }
    // 当配置改变时，直接更新 ConfigManager
    fun updateIsBeating(value: Boolean) {
        isBeating = value
        configManager.isBeating = value
        onConfigUpdate?.invoke()  // 通知外部配置已更新
    }

    fun updateIsFullScreen(value: Boolean) {
        isFullScreen = value
        configManager.isFullScreen = value
        onConfigUpdate?.invoke()  // 通知外部配置已更新
    }

    fun updateRingColor(value: Int) {
        ringColor = value
        configManager.ringColor = value
        onConfigUpdate?.invoke()
    }

    fun updateBgColor(value: Int) {
        bgColor = value
        configManager.bgColor = value
        onConfigUpdate?.invoke()
    }

    // -------------------- 内部状态 --------------------

    var isVisible by remember { mutableStateOf(false) }
    val start = 200

    // 使用像素偏移而不是百分比，更精确控制
    var drawerHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    // 添加滑动相关状态
    //var isDragging by remember { mutableStateOf(false) }

    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else -drawerHeight.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )

    // 使用两个动画分别控制拉伸和恢复
    var stretchTarget by remember { mutableFloatStateOf(0f) }
    val stretchFactor by animateFloatAsState(
        targetValue = stretchTarget,
        animationSpec = tween(
            durationMillis = 300,
            easing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 1.5f)
        )
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            // 先快速拉伸
            stretchTarget = 0.3f
            delay(200) // 保持拉伸状态一段时间
            // 然后弹性恢复
            stretchTarget = 0f
        } else {
            stretchTarget = 0f
        }
    }

    // 颜色选择器状态
    var showRingColorPicker by remember { mutableStateOf(false) }
    var showBgColorPicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        //isDragging = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        // 如果下滑超过阈值，打开抽屉
                        if (dragAmount.y > 20f) {
                            isVisible = true
                        }
                        // 如果上滑超过阈值，关闭抽屉
                        else if (dragAmount.y < -20f && isVisible) {
                            isVisible = false
                        }
                    },
                    onDragEnd = {
                        //isDragging = false
                    }
                )
            }
    ) {
        // 抽屉 - 关键修改：先测量高度，再应用偏移
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .onSizeChanged { size ->
                    drawerHeight = size.height
                }
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .background(Color.Transparent) // 容器透明
        ) {
            // 内容区域使用半透明黑色，底部添加圆弧和拉伸效果
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        // 只在Y轴底部拉伸
                        scaleY = 1f + stretchFactor
                        transformOrigin = TransformOrigin(0.5f, 0f) // 从底部中心拉伸
                    }
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0x99000000), Color(0xBB000000)),
                            startY = 0f,
                            endY = 100f
                        ),
                        shape = object : Shape {
                            override fun createOutline(
                                size: Size,
                                layoutDirection: LayoutDirection,
                                density: Density
                            ): Outline {
                                val path = Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(size.width, 0f)
                                    lineTo(size.width, size.height - 30f)
                                    // 使用二次贝塞尔曲线创建圆弧，考虑拉伸因子
                                    quadraticTo(
                                        size.width * 0.5f, size.height + 30f * (1f + stretchFactor),
                                        0f, size.height - 30f
                                    )
                                    close()
                                }
                                return Outline.Generic(path)
                            }
                        }
                    )
            ){
                Column{
                    Text(
                        stringResource(R.string.settings_panel), style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(top = 35.dp, start = 16.dp))
                    Text(stringResource(R.string.panel_swipe_hint),
                        color = Color.White,
                        modifier = Modifier.padding(top = 10.dp, start = 16.dp, bottom = 20.dp))

                    // 为每个配置项添加依次进入动画
                    val settingItems = listOf<@Composable () -> Unit>(
                        // 心跳动画开关
                        {
                            SwitchSettingItem(
                                title = stringResource(R.string.heartbeat_animation),
                                checked = isBeating,
                                onCheckedChange = { newValue ->
                                    updateIsBeating(newValue)
                                }
                            )
                        },
                        {
                            SwitchSettingItem(
                                title = stringResource(R.string.fullscreen),
                                checked = isFullScreen,
                                onCheckedChange = { newValue ->
                                    updateIsFullScreen(newValue)
                                }
                            )
                        },
                        // 圆环颜色选择
                        {
                            ColorSettingItem(
                                title = stringResource(R.string.animation_color),
                                color = ringColor,
                                onColorClick = { showRingColorPicker = true  }
                            )
                        },
                        // 背景颜色选择
                        {
                            ColorSettingItem(
                                title = stringResource(R.string.background_color),
                                color = bgColor,
                                onColorClick = { showBgColorPicker = true }
                            )
                        },
                        // 语言选择下拉
                        {
                            LanguageSettingItem(
                                currentLocaleTag = currentLanguageTag,
                                onLanguageSelected = {context, newLocaleTag ->
                                    saveLanguageTag(context, newLocaleTag)  // 持久化语言
                                    setCurrentLanguageTag(newLocaleTag)     // 更新 Compose 状态
                                    changeAppLanguage(
                                        context,
                                        newLocaleTag
                                    )  // 重建 Activity 加载资源
                                }
                            )
                        }
                        // 添加更多配置项...
                    )
                    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
                    settingItems.forEachIndexed { index, itemContent ->
                        AnimatedSettingItem(
                            index = index,
                            isVisible = isVisible,
                            screenWidthPx = screenWidthPx,
                            start = start
                        ) {
                            itemContent()  // 渲染具体的配置组件
                        }
                    }

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
                                    text = AppInfo.COPYRIGHT,
                                    color = Color.White.copy(0.7f),
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = AppInfo.Author.NAME,
                                    color = Color(0xFF29C6FF),
                                    fontSize = 12.sp,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable {
                                        uriHandler.openUri(AppInfo.Author.BILI_URL)
                                    }
                                )
                            }

                            // 第二行：GitHub 链接
                            Text(
                                text = stringResource(R.string.github_repo),
                                color = Color(0xFF6CC644), // GitHub 绿色
                                fontSize = 12.sp,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .clickable {
                                        uriHandler.openUri(AppInfo.Author.GITHUB_URL)
                                    }
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // 颜色选择器对话框
            if (showRingColorPicker) {
                ColorPickerDialog(
                    currentColor = ringColor,
                    onColorSelected = { newColor:Int ->
                        updateRingColor(newColor)
                        showRingColorPicker = false
                    },
                    onDismiss = { showRingColorPicker = false }
                )
            }

            if (showBgColorPicker) {
                ColorPickerDialog(
                    currentColor = bgColor,
                    onColorSelected = { newColor:Int ->
                        updateBgColor(newColor)
                        showBgColorPicker = false
                    },
                    onDismiss = { showBgColorPicker = false }
                )
            }
        }
    }
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
        title = { Text(stringResource(R.string.choose_color))},
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
                Text(
                    stringResource(R.string.color_red),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Slider(
                        value = red,
                        onValueChange = { red = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${(red * 255).toInt()}",
                        modifier = Modifier.width(30.dp)
                    )
                }

                // 绿色通道
                Text(
                    stringResource(R.string.color_green),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Slider(
                        value = green,
                        onValueChange = { green = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${(green * 255).toInt()}",
                        modifier = Modifier.width(30.dp)
                    )
                }

                // 蓝色通道
                Text(
                    stringResource(R.string.color_blue),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Slider(
                        value = blue,
                        onValueChange = { blue = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${(blue * 255).toInt()}",
                        modifier = Modifier.width(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 快速预设颜色
                val presetColors = listOf(
                    0xFFFF0000 to stringResource(R.string.color_red), 0xFF00FF00 to stringResource(R.string.color_green), 0xFF0000FF to stringResource(R.string.color_blue),
                    0xFFFFFF00 to stringResource(R.string.color_yellow), 0xFFFF00FF to stringResource(R.string.color_purple), 0xFF00FFFF to stringResource(R.string.color_cyan),
                    0xFFFFFFFF to stringResource(R.string.color_white), 0xFF000000 to stringResource(R.string.color_black), 0xFFFFA500 to stringResource(R.string.color_orange),
                    0xFF808080 to stringResource(R.string.color_gray), 0xFFA52A2A to stringResource(R.string.color_brown), 0xFFFFC0CB to stringResource(R.string.color_pink)
                )

                Text(stringResource(R.string.quick_select), modifier = Modifier.padding(vertical = 8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetColors) { (color, name) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
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

// 语言选择下拉项
@Composable
fun LanguageSettingItem(
    currentLocaleTag: String,
    onLanguageSelected: (Context, String) -> Unit
) {
    val context = LocalContext.current
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val supportedLanguages = listOf(
        Language(name = "English", localeTag = "en"),
        Language(name = "简体中文", localeTag = "zh"),
        Language(name = "繁體中文", localeTag = "zh-rTW"),
        Language(name = "日本語", localeTag = "ja")
    )

    val selectedLanguage = supportedLanguages.find { it.localeTag == currentLocaleTag }
    val selectedLanguageName = selectedLanguage?.name ?: "System Default"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { setExpanded(true) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_lang_24),
            contentDescription = null,
            tint = Color.White
        )

        Spacer(modifier = Modifier.width(20.dp))

        Text(text = selectedLanguageName,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = Color.White
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) }
        ) {
            supportedLanguages.forEach { language ->
                DropdownMenuItem(
                    onClick = {
                        setExpanded(false)
                        onLanguageSelected(context, language.localeTag)
                    },
                    text = { Text(text = language.name) },
                )
            }
        }
    }
}