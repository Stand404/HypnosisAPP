package com.stand.hypnosis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay

@Composable
fun RingAnimation(
    modifier: Modifier = Modifier,
    color: Color,
    isActive: Boolean = true
) {
    val density = LocalDensity.current

    // 动画参数
    var centerX by remember { mutableFloatStateOf(0f) }
    var centerY by remember { mutableFloatStateOf(0f) }
    var maxRadius by remember { mutableFloatStateOf(0f) }
    val baseStroke = 50f

    // 使用 mutableStateOf 来触发重组
    var rings by remember { mutableStateOf<List<RingData>>(emptyList()) }
    var animationFrame by remember { mutableLongStateOf(0L) }

    BoxWithConstraints(modifier = modifier) {
        LaunchedEffect(maxWidth, maxHeight) {
            with(density) {
                centerX = maxWidth.toPx() / 2f
                centerY = maxHeight.toPx() / 2f
                maxRadius = maxOf(maxHeight.toPx(), maxWidth.toPx())  / 2f + 200f
            }
        }

        // 主动画循环
        LaunchedEffect(isActive, maxRadius, color) {
            if (!isActive || maxRadius <= 0f) return@LaunchedEffect

            // 预创建10个有进度的圆环
            val initialRings = mutableListOf<RingData>()
            val totalCircles = 10

            repeat(totalCircles) { i ->
                val progress = i.toFloat() / totalCircles
                val initialRadius = maxRadius * progress
                val remainingDuration = (2000 * (1 - progress)).toLong()

                initialRings.add(
                    RingData(
                        id = i.toLong(),
                        radius = initialRadius,
                        strokeWidth = baseStroke * (1 + initialRadius / maxRadius),
                        startTime = System.currentTimeMillis() - (2000 - remainingDuration)
                    )
                )
            }

            rings = initialRings
            var nextId = totalCircles.toLong()
            var lastSpawnTime = System.currentTimeMillis()

            while (isActive) {
                val currentTime = System.currentTimeMillis()

                // 每200ms生成新圆环
                if (currentTime - lastSpawnTime >= 200) {
                    val newRing = RingData(
                        id = nextId++,
                        radius = 0f,
                        strokeWidth = baseStroke,
                        startTime = currentTime
                    )
                    rings = rings + newRing // 创建新列表触发重组
                    lastSpawnTime = currentTime
                }

                // 更新所有圆环状态
                val updatedRings = rings.mapNotNull { ring ->
                    val elapsed = currentTime - ring.startTime
                    val progress = (elapsed / 2000f).coerceIn(0f, 1f)

                    if (progress >= 1f) {
                        null // 移除完成的圆环
                    } else {
                        ring.copy(
                            radius = maxRadius * progress,
                            strokeWidth = baseStroke * (1 + (maxRadius * progress) / maxRadius)
                        )
                    }
                }

                rings = updatedRings
                animationFrame++ // 触发重组

                delay(16) // 约60fps
            }

            // 停止时清空
            if (!isActive) {
                rings = emptyList()
            }
        }

        // 绘制圆环
        Canvas(modifier = Modifier.fillMaxSize()) {
            rings.forEach { ring ->
                drawCircle(
                    color = color,
                    radius = ring.radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = ring.strokeWidth)
                )
            }
        }
    }
}

// 数据类需要是不可变的，使用 copy 来更新
private data class RingData(
    val id: Long,
    val radius: Float,
    val strokeWidth: Float,
    val startTime: Long
)
