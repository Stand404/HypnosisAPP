package com.stand.hypnosis

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.View.LAYER_TYPE_HARDWARE
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val scope = MainScope()
    private val activeAnimators = mutableListOf<ValueAnimator>()
    private lateinit var rootLayout: FrameLayout
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var maxRadius: Float = 0f
    private var baseStroke: Float = 50f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Scaffold(modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.heart),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .aspectRatio(1f)
                    )
                }
            }
        }

        rootLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        findViewById<ViewGroup>(android.R.id.content).addView(rootLayout, 0)

        // 3. 获取屏幕尺寸并计算动画参数
        val displayMetrics = resources.displayMetrics
        centerX = displayMetrics.widthPixels / 2f
        centerY = displayMetrics.heightPixels / 2f
        maxRadius = (displayMetrics.heightPixels + 200f) / 2f

        // 开始无限循环创建圆形动画
        startInfiniteCircleAnimation()
    }

    private fun startInfiniteCircleAnimation() {
        scope.launch {
            val totalCircles = 10
            repeat(totalCircles) { i ->
                launch {
                    createPreCreatedCircle(i, totalCircles)
                }
            }

            while (true) {
                launch {
                    createCircleAnimation()
                }
                delay(200)
            }
        }
    }

    private fun createPreCreatedCircle(index: Int, totalCircles: Int) {
        val progress = index.toFloat() / totalCircles
        val initialRadius = maxRadius * progress
        val remainingDuration = (2000 * (1 - progress)).toLong()

        createCircleAnimation(
            initialRadius = initialRadius,
            maxRadius = maxRadius,
            duration = remainingDuration
        )
    }

    private fun createCircleAnimation() {
        createCircleAnimation(
            initialRadius = 0f,
            maxRadius = maxRadius,
            duration = 2000L
        )
    }

    private fun createCircleAnimation(initialRadius: Float, maxRadius: Float, duration: Long) {
        // 从对象池获取CircleView
        val circleView = CircleView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setLayerType(LAYER_TYPE_HARDWARE, null) // 启用硬件加速
        }

        circleView.setCenter(centerX, centerY)
        circleView.setRadius(initialRadius)
        circleView.visibility = View.VISIBLE
        rootLayout.addView(circleView)

        ValueAnimator.ofFloat(initialRadius, maxRadius).apply {
            this.duration = duration
            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                val radius = animation.animatedValue as Float
                circleView.setRadius(radius + radius / maxRadius * 50f)
                circleView.setStroke(baseStroke * (1 + radius / maxRadius))
                circleView.invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rootLayout.removeView(circleView)
                    activeAnimators.remove(animation as ValueAnimator)
                }
            })

            start()
            activeAnimators.add(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // 取消协程
        activeAnimators.forEach { it.cancel() } // 取消所有动画
        rootLayout.removeAllViews() // 清理所有View
    }
}

// 自定义圆形View
class CircleView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = "#EF36CD".toColorInt() // 紫色描边
        strokeWidth = 50f
        strokeCap = Paint.Cap.ROUND
    }

    private var radius: Float = 0f
    private var centerX: Float = 0f
    private var centerY: Float = 0f

    fun setRadius(radius: Float) {
        this.radius = radius
    }

    fun setCenter(x: Float, y: Float) {
        this.centerX = x
        this.centerY = y
    }

    fun setStroke(stroke: Float) {
        paint.strokeWidth = stroke
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(centerX, centerY, radius, paint)
    }
}