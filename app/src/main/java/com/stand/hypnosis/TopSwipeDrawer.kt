package com.stand.hypnosis

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * 下滑呼出 / 上滑收起 抽屉
 */
@Composable
fun TopSwipeDrawerLayer(
    modifier: Modifier = Modifier,
    configManager: ConfigManager,
    onConfigUpdate: (() -> Unit)? = null,  // 添加这个参数
) {
    val context = LocalContext.current

    // 使用 mutableStateOf 来管理配置状态
    var isBeating by remember { mutableStateOf(configManager.isBeating) }
    var isFullScreen by remember { mutableStateOf(configManager.isFullScreen) }
    var ringColor by remember { mutableIntStateOf(configManager.ringColor) }
    var bgColor by remember { mutableIntStateOf(configManager.bgColor) }
    // 新增Heart颜色配置
    var heartOuterFillColor by remember { mutableIntStateOf(configManager.heartOuterFillColor) }
    var heartOuterStrokeColor by remember { mutableIntStateOf(configManager.heartOuterStrokeColor) }
    var heartInnerStrokeColor by remember { mutableIntStateOf(configManager.heartInnerStrokeColor) }

    // 新增语言选择
    var language by remember { mutableIntStateOf(configManager.language) }

    // 新增自定义图案路径
    var customPatternPath by remember { mutableStateOf(configManager.patternFilePath) }
    // 新增中心图案缩放比例
    var patternScale by remember { mutableFloatStateOf(configManager.patternScale) }
    // 文件选择启动器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                // 通过 context.contentResolver 访问
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.let { stream ->
                    val outputFile = File(context.filesDir, "custom_pattern.jpg")

                    // 使用 use 自动关闭流
                    stream.use { input ->
                        FileOutputStream(outputFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    // 更新配置
                    configManager.patternFilePath = outputFile.absolutePath
                    customPatternPath = outputFile.absolutePath
                    onConfigUpdate?.invoke()
                }
            }
        }
    )

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

    fun updateLanguage(value: Int) {
        language = value
        configManager.language = value
        onConfigUpdate?.invoke()  // 通知外部配置已更新
    }

    fun updatePatternPath(value: String?) {
        customPatternPath = value
        configManager.patternFilePath = value
        onConfigUpdate?.invoke()  // 通知外部配置已更新
    }

    // 新增中心图案缩放比例更新方法
    fun updatePatternScale(value: Float) {
        patternScale = value
        configManager.patternScale = value
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

    // 新增Heart颜色更新方法
    fun updateHeartOuterFillColor(value: Int) {
        heartOuterFillColor = value
        configManager.heartOuterFillColor = value
        onConfigUpdate?.invoke()
    }

    fun updateHeartOuterStrokeColor(value: Int) {
        heartOuterStrokeColor = value
        configManager.heartOuterStrokeColor = value
        onConfigUpdate?.invoke()
    }

    fun updateHeartInnerStrokeColor(value: Int) {
        heartInnerStrokeColor = value
        configManager.heartInnerStrokeColor = value
        onConfigUpdate?.invoke()
    }

    // -------------------- 内部状态 --------------------

    var isVisible by remember { mutableStateOf(false) }
    val start = 200
    // 新增分页状态
    var currentPage by remember { mutableIntStateOf(0) }
    val totalPages = 3

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
    // 新增Heart颜色选择器状态
    var showHeartOuterFillColorPicker by remember { mutableStateOf(false) }
    var showHeartOuterStrokeColorPicker by remember { mutableStateOf(false) }
    var showHeartInnerStrokeColorPicker by remember { mutableStateOf(false) }
    // 语言选择对话框状态
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
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
                Column(Modifier.animateContentSize()){
                    Text(stringResource(R.string.settings_title),
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(top = 35.dp, start = 16.dp))
                    Text(stringResource(R.string.swipe_hint),
                        color = Color.White.copy(0.7f),
                        modifier = Modifier.padding(top = 10.dp, start = 16.dp, end = 16.dp, bottom = 20.dp))
                    // 新增分页标题和切换按钮
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 左箭头按钮
                        IconButton(
                            onClick = {
                                currentPage = if (currentPage > 0) currentPage - 1 else totalPages - 1
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(painter = painterResource(R.drawable.outline_chevron_left_24),
                                contentDescription = stringResource(R.string.left),
                                tint = MaterialTheme.colorScheme.primary)
                        }

                        // 页标题
                        Text(
                            text = when (currentPage) {
                                0 -> stringResource(id = R.string.global_settings)
                                1 -> stringResource(id = R.string.animation_settings)
                                else -> stringResource(id = R.string.center_settings)
                            },
                            style = TextStyle(fontSize = 20.sp),
                            color = Color.White
                        )

                        // 右箭头按钮
                        IconButton(
                            onClick = {
                                currentPage = if (currentPage < totalPages - 1) currentPage + 1 else 0
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(painter = painterResource(R.drawable.outline_chevron_right_24),
                                contentDescription = stringResource(R.string.right),
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // 为每个配置项添加依次进入动画
                    // 将设置项按页分组
                    val page0Items = listOf<@Composable () -> Unit>(
                        // 全局设置：全屏，语言
                        {
                            SwitchSettingItem(
                                title = stringResource(id = R.string.full_screen),
                                checked = isFullScreen,
                                onCheckedChange = { newValue -> updateIsFullScreen(newValue) }
                            )
                        },
                        {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clickable { showLanguageDialog = true }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(text = stringResource(id = R.string.language),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = when (language) {
                                        0 -> stringResource(R.string.chinese)
                                        1 -> stringResource(R.string.english)
                                        else -> stringResource(R.string.japanese)
                                    },
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    )

                    val page1Items = listOf<@Composable () -> Unit>(
                        // 动画设置：心跳动画，动画颜色，背景颜色
                        {
                            SwitchSettingItem(
                                title = stringResource(id = R.string.heart_animation),
                                checked = isBeating,
                                onCheckedChange = { newValue ->
                                    updateIsBeating(newValue)
                                }
                            )
                        },
                        {
                            ColorSettingItem(
                                title = stringResource(R.string.animation_color),
                                color = ringColor,
                                onColorClick = { showRingColorPicker = true  }
                            )
                        },
                        {
                            ColorSettingItem(
                                title = stringResource(R.string.background_color),
                                color = bgColor,
                                onColorClick = { showBgColorPicker = true })
                        }
                    )

                    val page2Items = listOf<@Composable () -> Unit>(
                        // 中心设置：选择图案，中心色，外描边，内描边
                        {
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(horizontal = 16.dp)
                                .clickable{ if(customPatternPath==null) filePickerLauncher.launch("image/*") },
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = stringResource(R.string.select_pattern),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                if (customPatternPath != null) {
                                    Button(
                                        onClick = { updatePatternPath(null) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.background,
                                            contentColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text(stringResource(R.string.clear))
                                    }
                                }else{
                                    Text(
                                        text = stringResource(R.string.not_selected),
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        },
                        // 中心图案缩放设置
                        {
                            SliderSettingItem(
                                title = stringResource(R.string.pattern_scale),
                                value = patternScale,
                                valueRange = 0f..5.0f,
                                onValueChange = { newValue ->
                                    updatePatternScale(newValue)
                                },
                                formatValue = { String.format("%.1f", it) }
                            )
                        },
                        // 条件显示Heart颜色配置项（仅当没有自定义图案时）
                        {
                            if (customPatternPath == null) {
                                ColorSettingItem(
                                    title = stringResource(R.string.heart_outer_fill_color),
                                    color = heartOuterFillColor,
                                    onColorClick = { showHeartOuterFillColorPicker = true }
                                )
                            }
                        },
                        {
                            if (customPatternPath == null) {
                                ColorSettingItem(
                                    title = stringResource(R.string.heart_outer_stroke_color),
                                    color = heartOuterStrokeColor,
                                    onColorClick = { showHeartOuterStrokeColorPicker = true })
                            }
                        },
                        {
                            if (customPatternPath == null) {
                                ColorSettingItem(
                                    title = stringResource(R.string.heart_inner_stroke_color),
                                    color = heartInnerStrokeColor,
                                    onColorClick = { showHeartInnerStrokeColorPicker = true })
                            }
                        }
                    )

                    if (showResetDialog) {
                        AlertDialog(
                            onDismissRequest = { showResetDialog = false },
                            title = { Text(stringResource(id = R.string.reset_settings)) },
                            text = { Text(stringResource(id = R.string.reset_confirm)) },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        // 重置所有配置
                                        configManager.resetToDefault()

                                        // 更新UI状态
                                        updateIsBeating(configManager.isBeating)
                                        updateIsFullScreen(configManager.isFullScreen)
                                        updateLanguage(configManager.language)
                                        updatePatternPath(configManager.patternFilePath)
                                        updatePatternScale(configManager.patternScale)
                                        updateRingColor(configManager.ringColor)
                                        updateBgColor(configManager.bgColor)
                                        updateHeartOuterFillColor(configManager.heartOuterFillColor)
                                        updateHeartOuterStrokeColor(configManager.heartOuterStrokeColor)
                                        updateHeartInnerStrokeColor(configManager.heartInnerStrokeColor)

                                        showResetDialog = false
                                    }
                                ) {
                                    Text(stringResource(id = R.string.confirm))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showResetDialog = false }) {
                                    Text(stringResource(id = R.string.cancel))
                                }
                            }
                        )
                    }

                    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
                    // 根据当前页面显示对应的设置项
                    val currentItems = when (currentPage) {
                        0 -> page0Items
                        1 -> page1Items
                        else -> page2Items
                    }

                    currentItems.forEachIndexed { index, itemContent ->
                        AnimatedSettingItem(
                            index = index,
                            isVisible = isVisible,
                            screenWidthPx = screenWidthPx,
                            start = start
                        ) {
                            itemContent()
                        }
                    }

                    // 显示重置按钮
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { showResetDialog = true },
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            key(language) {
                                Text(
                                    text = stringResource(R.string.reset_settings),
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                    About()
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

            // 新增Heart颜色选择器对话框
            if (showHeartOuterFillColorPicker) {
                ColorPickerDialog(
                    currentColor = heartOuterFillColor,
                    onColorSelected = { newColor:Int ->
                        updateHeartOuterFillColor(newColor)
                        showHeartOuterFillColorPicker = false
                    },
                    onDismiss = { showHeartOuterFillColorPicker = false }
                )
            }

            if (showHeartOuterStrokeColorPicker) {
                ColorPickerDialog(
                    currentColor = heartOuterStrokeColor,
                    onColorSelected = { newColor:Int ->
                        updateHeartOuterStrokeColor(newColor)
                        showHeartOuterStrokeColorPicker = false
                    },
                    onDismiss = { showHeartOuterStrokeColorPicker = false }
                )
            }

            if (showHeartInnerStrokeColorPicker) {
                ColorPickerDialog(
                    currentColor = heartInnerStrokeColor,
                    onColorSelected = { newColor:Int ->
                        updateHeartInnerStrokeColor(newColor)
                        showHeartInnerStrokeColorPicker = false
                    },
                    onDismiss = { showHeartInnerStrokeColorPicker = false }
                )
            }

            // 语言选择对话框
            if (showLanguageDialog) {
                LanguageSelectDialog(
                    currentLanguage = language,
                    onLanguageSelected = { selected ->
                        updateLanguage(selected)
                    },
                    onDismiss = { showLanguageDialog = false })
            }
        }
    }
}
