package com.stand.hypnosis

import android.annotation.SuppressLint
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.SvgDecoder
import coil.request.ImageRequest

@Composable
fun BeatingHeart(
    beating: Boolean,
    outerFillColor: Color,
    outerStrokeColor: Color,
    innerStrokeColor: Color,
    customImagePath: String? = null,
    scale: Float = 1.0f, // 新增缩放比例参数
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "infinite")

    val beatingScale by if (beating) {
        infinite.animateFloat(
            initialValue = 1f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    0.8f at 500
                    1f at 1000
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "scale"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    val context = LocalContext.current

    // 使用 remember 缓存 ImageLoader
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
                add(GifDecoder.Factory())
            }
            .build()
    }

    // 将颜色参数的变化同步到 SVG
    val svgData = remember(outerFillColor, outerStrokeColor, innerStrokeColor) {
        """
        <svg width="500" height="500" viewBox="0 0 500 500" xmlns="http://www.w3.org/2000/svg">
            <!-- 外层心形 -->
            <path d="M250.23,148.82c-2.85,-4.23 -35.06,-50.39 -84.46,-48.78c-46.47,1.51 -78.73,44.33 -87.63,73.69C58.28,239.24 125.75,333.15 251.29,400c123.34,-66.36 189.73,-159.66 169.98,-225.23c-8.61,-28.59 -39.46,-71.35 -85.52,-73.69C286,98.55 252.92,144.93 250.23,148.82z"
                  fill="${outerFillColor.toHex()}"
                  stroke="${outerStrokeColor.toHex()}"
                  stroke-width="80"
                  stroke-linecap="round"
                  stroke-linejoin="round"/>
            
            <!-- 内层心形 -->
            <path d="M250.52,148.82c-2.85,-4.23 -35.06,-50.39 -84.46,-48.78c-46.47,1.51 -78.73,44.33 -87.63,73.69C58.57,239.24 126.04,333.15 251.58,400c123.34,-66.36 189.73,-159.66 169.98,-225.23c-8.61,-28.59 -39.46,-71.35 -85.52,-73.69C286.29,98.55 253.21,144.93 250.52,148.82z"
                  fill="${outerFillColor.toHex()}"
                  stroke="${innerStrokeColor.toHex()}"
                  stroke-width="40"
                  stroke-linecap="round"
                  stroke-linejoin="round"/>
        </svg>
        """.trimIndent().toByteArray(Charsets.UTF_8)
    }

    Box(
        modifier = modifier
            .fillMaxWidth(0.6f)
            .aspectRatio(1f)
            .scale(scale * beatingScale),
        contentAlignment = Alignment.Center
    ) {
        if (customImagePath != null && customImagePath.isNotEmpty()) {
            // 使用 Coil 加载自定义图片
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(customImagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Custom heart image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                imageLoader = imageLoader
            )
        } else {
            // 使用 SVG 矢量图
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(svgData)
                    .build(),
                contentDescription = "Heart icon",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                imageLoader = imageLoader
            )
        }
    }
}

private fun Color.toHex(): String {
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return String.format("#%02X%02X%02X", red, green, blue)
}
