package com.conundrum.thomas.v2.ui.thinking

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** The F concept: a vertical, luminous three-ribbon thought loom rather than a progress spinner. */
enum class ThomasThoughtState { WAKING, THINKING }

@Composable
fun ThomasThoughtLoom(
    state: ThomasThoughtState,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "thomas-thought-loom")
    val breath = transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == ThomasThoughtState.WAKING) 2_700 else 3_900,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "thought-loom-breath",
    ).value
    val weave = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == ThomasThoughtState.WAKING) 2_100 else 5_200,
                easing = LinearEasing,
            ),
        ),
        label = "thought-loom-weave",
    ).value
    val assemble = if (state == ThomasThoughtState.WAKING) {
        transition.animateFloat(
            initialValue = 0.54f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3_400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "thought-loom-assemble",
        ).value
    } else {
        1f
    }
    val description = if (state == ThomasThoughtState.WAKING) {
        "Thomas is waking up"
    } else {
        "Thomas is forming a response"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.Start,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            shape = MaterialTheme.shapes.large,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Canvas(
                    modifier = Modifier
                        .size(if (state == ThomasThoughtState.WAKING) 92.dp else 78.dp)
                        .graphicsLayer { scaleX = breath; scaleY = breath },
                ) {
                    drawThoughtLoom(weave = weave, assemble = assemble)
                }
                Column(Modifier.padding(start = 10.dp)) {
                    Text("Thomas", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (state == ThomasThoughtState.WAKING) "Waking up…" else "Working…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawThoughtLoom(weave: Float, assemble: Float) {
    val w = size.width
    val h = size.height
    val center = Offset(w / 2f, h * 0.54f)
    val top = Offset(w / 2f, h * (0.11f + (1f - assemble) * 0.12f))
    val bottom = Offset(w / 2f, h * (0.9f - (1f - assemble) * 0.12f))
    val ivory = Color(0xFFF6E7C9)
    val blue = Color(0xFF8DC7FF)
    val paleBlue = Color(0xFFD8EDFF)
    val phase = (weave - 0.5f) * w * 0.08f

    // The central pair cross and separate like the vertical core of concept F.
    drawRibbon(
        Path().apply {
            moveTo(bottom.x, bottom.y)
            cubicTo(w * 0.34f, h * 0.69f, w * 0.72f + phase, h * 0.36f, top.x, top.y)
        },
        Brush.linearGradient(listOf(blue, paleBlue, ivory), bottom, top),
        alpha = 0.90f,
    )
    drawRibbon(
        Path().apply {
            moveTo(bottom.x, bottom.y)
            cubicTo(w * 0.68f, h * 0.68f, w * 0.31f - phase, h * 0.33f, top.x, top.y)
        },
        Brush.linearGradient(listOf(ivory, paleBlue, blue), bottom, top),
        alpha = 0.82f,
    )

    // Two outward ribbons make the calm, branching upper structure of F without rotation.
    drawRibbon(
        Path().apply {
            moveTo(center.x, center.y)
            cubicTo(w * 0.37f, h * 0.44f, w * 0.13f, h * 0.56f + phase, w * 0.16f, h * 0.26f)
        },
        Brush.linearGradient(listOf(ivory, blue), center, Offset(w * 0.16f, h * 0.26f)),
        alpha = 0.70f,
    )
    drawRibbon(
        Path().apply {
            moveTo(center.x, center.y)
            cubicTo(w * 0.63f, h * 0.44f, w * 0.87f, h * 0.56f - phase, w * 0.84f, h * 0.26f)
        },
        Brush.linearGradient(listOf(ivory, blue), center, Offset(w * 0.84f, h * 0.26f)),
        alpha = 0.70f,
    )
    drawRibbon(
        Path().apply {
            moveTo(center.x, center.y)
            cubicTo(w * 0.34f, h * 0.56f, w * 0.17f, h * 0.67f, w * 0.09f, h * 0.53f)
        },
        Brush.linearGradient(listOf(paleBlue, blue), center, Offset(w * 0.09f, h * 0.53f)),
        alpha = 0.45f + assemble * 0.25f,
    )
    drawRibbon(
        Path().apply {
            moveTo(center.x, center.y)
            cubicTo(w * 0.66f, h * 0.56f, w * 0.83f, h * 0.67f, w * 0.91f, h * 0.53f)
        },
        Brush.linearGradient(listOf(paleBlue, blue), center, Offset(w * 0.91f, h * 0.53f)),
        alpha = 0.45f + assemble * 0.25f,
    )

    drawCircle(ivory, radius = w * 0.035f, center = top, alpha = 0.95f)
    drawCircle(ivory, radius = w * 0.027f, center = bottom, alpha = 0.72f)
    drawCircle(ivory, radius = w * 0.018f, center = Offset(top.x, top.y - h * 0.08f), alpha = 0.45f + weave * 0.3f)
    drawCircle(ivory, radius = w * 0.018f, center = Offset(w * 0.16f, h * 0.2f), alpha = 0.35f + (1f - weave) * 0.25f)
    drawCircle(ivory, radius = w * 0.018f, center = Offset(w * 0.84f, h * 0.2f), alpha = 0.35f + weave * 0.25f)
}

private fun DrawScope.drawRibbon(path: Path, brush: Brush, alpha: Float) {
    drawPath(path, brush = brush, alpha = alpha, style = Stroke(width = size.minDimension * 0.055f))
    drawPath(path, color = Color.White, alpha = alpha * 0.30f, style = Stroke(width = size.minDimension * 0.012f))
}
