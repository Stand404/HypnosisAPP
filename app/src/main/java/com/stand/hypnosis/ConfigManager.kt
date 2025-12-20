package com.stand.hypnosis

import android.content.Context
import android.content.SharedPreferences
import androidx.core.graphics.toColorInt
import androidx.core.content.edit

class ConfigManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: ConfigManager? = null

        fun getInstance(context: Context): ConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConfigManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        private const val PREFS_NAME = "app_config"
        private const val KEY_IS_BEATING = "is_beating"
        private const val KEY_RING_COLOR = "ring_color"
        private const val KEY_BG_COLOR = "bg_color"
        private const val KEY_IS_FULL_SCREEN = "is_full_screen"
        // 新增配置项 - Heart颜色配置
        private const val KEY_HEART_OUTER_FILL_COLOR = "heart_outer_fill_color"
        private const val KEY_HEART_OUTER_STROKE_COLOR = "heart_outer_stroke_color"
        private const val KEY_HEART_INNER_STROKE_COLOR = "heart_inner_stroke_color"
        private const val KEY_PATTERN_FILE_PATH = "pattern_file_path"
        private const val KEY_LANGUAGE = "language" // 0: 中文, 1: English
        private const val KEY_PATTERN_SCALE = "pattern_scale" // 中心图案缩放比例
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isBeating: Boolean
        get() = prefs.getBoolean(KEY_IS_BEATING, true)
        set(value) = prefs.edit { putBoolean(KEY_IS_BEATING, value) }

    var isFullScreen: Boolean
        get() = prefs.getBoolean(KEY_IS_FULL_SCREEN, true)
        set(value) = prefs.edit { putBoolean(KEY_IS_FULL_SCREEN, value) }

    var ringColor: Int
        get() = prefs.getInt(KEY_RING_COLOR, "#EF36CD".toColorInt())
        set(value) = prefs.edit { putInt(KEY_RING_COLOR, value) }

    var bgColor: Int
        get() = prefs.getInt(KEY_BG_COLOR, "#FFFFFF".toColorInt())
        set(value) = prefs.edit { putInt(KEY_BG_COLOR, value) }

    // 新增：Heart颜色配置 - 支持三个颜色
    // 分别对应heart.xml中的三个颜色：外部填充色、外部描边色、内部描边色
    var heartOuterFillColor: Int
        get() = prefs.getInt(KEY_HEART_OUTER_FILL_COLOR, "#FFFFFF".toColorInt()) // 默认白色
        set(value) = prefs.edit { putInt(KEY_HEART_OUTER_FILL_COLOR, value) }

    var heartOuterStrokeColor: Int
        get() = prefs.getInt(KEY_HEART_OUTER_STROKE_COLOR, "#A500A1".toColorInt()) // 默认#A500A1
        set(value) = prefs.edit { putInt(KEY_HEART_OUTER_STROKE_COLOR, value) }

    var heartInnerStrokeColor: Int
        get() = prefs.getInt(KEY_HEART_INNER_STROKE_COLOR, "#EF36CD".toColorInt()) // 默认#EF36CD
        set(value) = prefs.edit { putInt(KEY_HEART_INNER_STROKE_COLOR, value) }

    // 新增：自定义图案文件路径
    var patternFilePath: String?
        get() = prefs.getString(KEY_PATTERN_FILE_PATH, null)
        set(value) = prefs.edit { putString(KEY_PATTERN_FILE_PATH, value) }

    // 新增：语言设置 (0:中文, 1:English, 2:Japanese)
    var language: Int
        get() = prefs.getInt(KEY_LANGUAGE, 0) // 默认中文
        set(value) = prefs.edit { putInt(KEY_LANGUAGE, value) }

    // 新增：中心图案缩放比例
    var patternScale: Float
        get() = prefs.getFloat(KEY_PATTERN_SCALE, 1.0f) // 默认1.0
        set(value) = prefs.edit { putFloat(KEY_PATTERN_SCALE, value) }

    // 重置为默认值
    fun resetToDefault() {
        prefs.edit { clear() }
    }
}
