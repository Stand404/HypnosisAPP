package com.stand.hypnosis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import android.content.res.Configuration
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var isFullScreen by mutableStateOf(true)
    private var ringColor by mutableIntStateOf("#EF36CD".toColorInt())
    private var bgColor by mutableIntStateOf("#FFFFFF".toColorInt())
    private var language by mutableIntStateOf(0) // 新增语言状态

    // 中心图案设置
    private var isBeating by mutableStateOf(true)
    private var patternFilePath by mutableStateOf<String?>(null)
    private var patternScale by mutableFloatStateOf(1.0f) // 新增中心图案缩放比例
    private var heartOuterFillColor by mutableIntStateOf("#FFFFFF".toColorInt())
    private var heartOuterStrokeColor by mutableIntStateOf("#FFFFFF".toColorInt())
    private var heartInnerStrokeColor by mutableIntStateOf("#FFFFFF".toColorInt())

    private lateinit var configManager: ConfigManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initConfig() // 初始化配置
        /* ---------- Compose UI ---------- */
        toggleFullScreen(isFullScreen)
        setContent {
            MyAppTheme{
                Scaffold(
                    modifier = Modifier.fillMaxSize().background(Color(bgColor)),
                    containerColor = Color.Transparent,
                ) { inner ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(inner),
                        contentAlignment = Alignment.Center
                    ) {
                        // 修改点1：将圆环动画改为Compose实现，并放在heart下面
                        RingAnimation(color=Color(ringColor))
                        if(patternScale > 0){
                            BeatingHeart(
                                beating = isBeating,
                                outerFillColor = Color(heartOuterFillColor),
                                outerStrokeColor = Color(heartOuterStrokeColor),
                                innerStrokeColor = Color(heartInnerStrokeColor),
                                customImagePath = patternFilePath,
                                scale = patternScale // 设置中心图案缩放比例
                            )
                        }
                    }

                    TopSwipeDrawerLayer(
                        configManager = configManager,
                        onConfigUpdate = {
                            // 先保存旧的语言设置
                            val oldLanguage = language

                            // 更新所有状态
                            isBeating = configManager.isBeating
                            bgColor = configManager.bgColor
                            ringColor = configManager.ringColor
                            language = configManager.language
                            heartOuterFillColor = configManager.heartOuterFillColor
                            heartOuterStrokeColor = configManager.heartOuterStrokeColor
                            heartInnerStrokeColor = configManager.heartInnerStrokeColor
                            patternFilePath = configManager.patternFilePath
                            patternScale = configManager.patternScale // 更新中心图案缩放比例

                            // 全屏设置变化时更新
                            if (isFullScreen != configManager.isFullScreen) {
                                isFullScreen = configManager.isFullScreen
                                toggleFullScreen(isFullScreen)
                            }

                            // 语言变化时重新启动Activity以应用新语言
                            if (oldLanguage != configManager.language) {
                                updateLocale(this)
                            }
                        })
                }
            }
        }
    }

    fun initConfig() {
        // 初始化配置管理器
        configManager = ConfigManager.getInstance(this)

        // 从持久化配置加载初始值
        lifecycleScope.launch {
            isBeating = configManager.isBeating
            ringColor = configManager.ringColor
            bgColor = configManager.bgColor
            isFullScreen = configManager.isFullScreen
            language = configManager.language // 加载语言设置
            patternFilePath = configManager.patternFilePath
            heartOuterFillColor = configManager.heartOuterFillColor
            heartOuterStrokeColor = configManager.heartOuterStrokeColor
            heartInnerStrokeColor = configManager.heartInnerStrokeColor
            patternScale = configManager.patternScale // 加载中心图案缩放比例
        }
    }

    // 添加全屏切换方法
    private fun toggleFullScreen(isFullScreen: Boolean) {
        // 始终让内容延伸到系统栏区域
        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            if (isFullScreen) {
                // 全屏模式：隐藏系统栏
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                // 非全屏模式：显示系统栏
                show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // 设置应用Locale
    private fun setLocale(languageCode: String, windowContext:android.content.Context) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        val dm = resources.displayMetrics

        config.setLocale(locale)
        resources.updateConfiguration(config, dm)

        // 也需要更新窗口上下文
        val windowConfig = Configuration(config)
        windowContext.resources.updateConfiguration(windowConfig, dm)
    }

    // 根据语言设置更新Locale
    private fun updateLocale(context: android.content.Context) {
        val languageCode = when (configManager.language) {
            0 -> "zh" // 中文
            1 -> "en" // 英文
            2 -> "ja" // 日文
            else -> "zh" // 默认中文
        }
        setLocale(languageCode, context)
    }

    // 在onCreate之前更新Locale
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(newBase)

        // 在Context创建时应用语言设置
        val prefs = newBase.getSharedPreferences("app_config", MODE_PRIVATE)
        val language = prefs.getInt("language", 0)

        val locale = when (language) {
            0 -> Locale.CHINESE
            1 -> Locale.ENGLISH
            2 -> Locale.JAPANESE
            else -> Locale.CHINESE
        }

        val config = Configuration(newBase.resources.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        newBase.resources.updateConfiguration(config, newBase.resources.displayMetrics)
    }
}
