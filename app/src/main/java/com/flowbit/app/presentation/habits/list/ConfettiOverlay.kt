package com.flowbit.app.presentation.habits.list

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.isActive
import kotlin.math.sin

private val confettiColors = listOf(
    Color(0xFFE74C3C), Color(0xFF3498DB), Color(0xFF2ECC71),
    Color(0xFFF39C12), Color(0xFF9B59B6), Color(0xFF1ABC9C),
    Color(0xFFE91E63), Color(0xFF00BCD4), Color(0xFFFF5722),
)

private data class ConfettiParticle(
    val xFrac: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotSpeed: Float,
    val wobbleAmp: Float,
    val wobbleFreq: Float,
    val wobblePhase: Float,
    val isRect: Boolean,
)

private fun makeParticles() = (0 until 70).map { i ->
    ConfettiParticle(
        xFrac = i / 70f,
        speed = 0.18f + (i % 7) * 0.045f,
        size = 7f + (i % 5) * 5f,
        color = confettiColors[i % confettiColors.size],
        rotSpeed = if (i % 2 == 0) 180f + i * 3f else -(140f + i * 2f),
        wobbleAmp = 10f + (i % 4) * 8f,
        wobbleFreq = 2f + (i % 3),
        wobblePhase = i * 0.4f,
        isRect = i % 3 != 0,
    )
}

@Composable
fun ConfettiOverlay(onDone: () -> Unit) {
    val startMs = remember { System.currentTimeMillis() }
    val durationMs = 3000L
    var frameNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { ns -> frameNanos = ns }
            if (System.currentTimeMillis() - startMs >= durationMs) {
                onDone()
                break
            }
        }
    }

    val particles = remember { makeParticles() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        @Suppress("UNUSED_EXPRESSION")
        frameNanos
        val elapsed = ((System.currentTimeMillis() - startMs).coerceIn(0L, durationMs)
            .toFloat() / durationMs)
        val alpha = if (elapsed > 0.72f) (1f - (elapsed - 0.72f) / 0.28f).coerceIn(0f, 1f) else 1f

        particles.forEach { p ->
            val t = (p.speed * elapsed * 1.6f) % 1f
            val y = t * (size.height + p.size * 4) - p.size * 2
            val wobble = sin((elapsed * p.wobbleFreq + p.wobblePhase).toDouble()).toFloat() * p.wobbleAmp
            val cx = p.xFrac * size.width + wobble
            val rot = elapsed * p.rotSpeed

            rotate(rot, Offset(cx, y)) {
                if (p.isRect) {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(cx - p.size / 2, y - p.size / 4),
                        size = Size(p.size, p.size * 0.45f),
                    )
                } else {
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size * 0.4f,
                        center = Offset(cx, y),
                    )
                }
            }
        }
    }
}
