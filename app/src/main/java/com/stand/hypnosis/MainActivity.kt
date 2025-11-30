package com.stand.hypnosis

import android.content.Context
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
import com.stand.hypnosis.utils.AppLanguageUtils.applyLanguageConfiguration
import com.stand.hypnosis.utils.AppLanguageUtils.readLanguageTag
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var isBeating by mutableStateOf(true)
    private var isFullScreen by mutableStateOf(true)
    private var ringColor by mutableIntStateOf("#EF36CD".toColorInt())
    private var bgColor by mutableIntStateOf("#FFFFFF".toColorInt())
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
                        BeatingHeart(isBeating)
                    }
                    TopSwipeDrawerLayer(
                        configManager = configManager,
                        onConfigUpdate = {
                            isBeating = configManager.isBeating
                            bgColor = configManager.bgColor
                            ringColor = configManager.ringColor
                            if (isFullScreen != configManager.isFullScreen) {
                                isFullScreen = configManager.isFullScreen
                                toggleFullScreen(isFullScreen)
                            }
                        })
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val localeTag = readLanguageTag(newBase)
        val newContext = applyLanguageConfiguration(newBase, localeTag)
        super.attachBaseContext(newContext)
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
}