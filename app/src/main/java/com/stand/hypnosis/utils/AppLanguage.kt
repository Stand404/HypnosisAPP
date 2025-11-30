package com.stand.hypnosis.utils

import android.app.Activity
import android.content.Context
import java.util.Locale
import androidx.core.content.edit

/**
 * 含应用语言设置的持久化和配置应用工具
 *
 * 提供应用语言的读取、保存、切换和配置功能，支持多语言切换和界面实时更新。
 *
 * @author misaka10032w
 * @since 2025年11月30日
 */
object AppLanguageUtils {
    const val PREFS_NAME = "app_settings"
    const val KEY_LANG_TAG = "language_tag"
    const val DEFAULT_LANG = "zh"

    /**
     * 从 SharedPreferences 中读取用户保存的语言标签。
     *
     * @param context 当前应用上下文，不能为null
     * @return 返回保存的语言标签（如 "zh", "en"），如果未找到则返回 [DEFAULT_LANG]
     * @see saveLanguageTag
     */
    fun readLanguageTag(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG_TAG, DEFAULT_LANG) ?: DEFAULT_LANG
    }

    /**
     * 将新的语言标签保存到 SharedPreferences 中。
     *
     * @param context 当前应用上下文，不能为null
     * @param tag 要保存的语言标签（如 "zh", "ja"），不能为null或空字符串
     * @throws IllegalArgumentException 当tag为null或空时抛出
     * @see readLanguageTag
     */
    fun saveLanguageTag(context: Context, tag: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_LANG_TAG, tag) }
    }

    /**
     * 应用新的语言配置到当前 Context 的资源，并强制 Activity 重建以刷新 UI。
     *
     * @param context 当前应用上下文，不能为null
     * @param localeTag 要切换到的语言标签（如 "zh", "zh-rTW"），不能为null
     * @see applyLanguageConfiguration
     */
    fun changeAppLanguage(context: Context, localeTag: String) {
        val locale = parseLocale(localeTag)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)

        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)

        (context as? Activity)?.recreate()
    }

    /**
     * 根据语言标签创建一个应用了新 Locale 配置的 Context。
     * 主要用于 Activity 的 attachBaseContext 方法，以确保在 Activity 创建之前加载正确的资源。
     *
     * @param context 传入的基础 Context，不能为null
     * @param localeTag 要应用的语言标签，不能为null
     * @return 返回一个新的、应用了指定语言配置的 Context
     * @see changeAppLanguage
     */
    fun applyLanguageConfiguration(context: Context, localeTag: String): Context {
        val locale = parseLocale(localeTag)
        val config = context.resources.configuration
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    /**
     * 将语言标签解析为 Locale 对象。
     *
     * @param localeTag 语言标签
     * @return 对应的 Locale 对象
     */
    private fun parseLocale(localeTag: String): Locale {
        return if (localeTag == "zh-rTW") {
            Locale.Builder().setLanguage("zh").setRegion("TW").build()
        } else {
            Locale.forLanguageTag(localeTag)
        }
    }
}
