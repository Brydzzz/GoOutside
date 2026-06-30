package com.example.gooutside.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gooutside.R
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun Modifier.scallopBorder(
    color: Color = Color.Black,
    bumpCount: Int = 4,
    borderThickness: Dp = 6.dp,
): Modifier = this.drawWithContent {
    val longestSide = maxOf(size.width, size.height)
    val bumpRadius = longestSide / (bumpCount * 2f)
    val thicknessPx = borderThickness.toPx()
    val paddingPx = bumpRadius + thicknessPx

    clipRect(
        left = paddingPx,
        top = paddingPx,
        right = size.width - paddingPx,
        bottom = size.height - paddingPx
    ) {
        this@drawWithContent.drawContent()
    }

    val path = buildScallopPath(size.width, size.height, bumpRadius, thicknessPx)
    drawPath(path, color = color)
}

private fun buildScallopPath(
    width: Float,
    height: Float,
    bumpRadius: Float,
    borderThickness: Float,
): Path {
    val path = Path().apply {
        fillType = PathFillType.EvenOdd
    }

    val bumpDiameter = bumpRadius * 2f
    val availableWidth = width - (bumpDiameter * 2)
    val availableHeight = height - (bumpDiameter * 2)
    val countH = floor(availableWidth / bumpDiameter).roundToInt().coerceAtLeast(1)
    val countV = floor(availableHeight / bumpDiameter).roundToInt().coerceAtLeast(1)
    val segW = availableWidth / countH
    val segH = availableHeight / countV


    fun drawBump(baseStartX: Float, baseStartY: Float, baseEndX: Float, baseEndY: Float) {
        val dx = baseEndX - baseStartX
        val dy = baseEndY - baseStartY
        val distance = sqrt(dx * dx + dy * dy)
        val radius = distance / 2f

        val midX = (baseStartX + baseEndX) / 2f
        val midY = (baseStartY + baseEndY) / 2f

        val rect = Rect(
            left = midX - radius, top = midY - radius, right = midX + radius, bottom = midY + radius
        )

        val angleRad = atan2(dy, dx)
        val angleDeg = (angleRad * 180f / PI.toFloat())


        if (path.isEmpty) {
            path.moveTo(baseStartX, baseStartY)
        }

        path.arcTo(
            rect = rect,
            startAngleDegrees = angleDeg + 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
    }

    fun drawCornerArc(cx: Float, cy: Float, startAngle: Float, isFirst: Boolean = false) {
        val rect = Rect(
            left = cx - bumpRadius,
            top = cy - bumpRadius,
            right = cx + bumpRadius,
            bottom = cy + bumpRadius
        )
        path.arcTo(
            rect = rect,
            startAngleDegrees = startAngle,
            sweepAngleDegrees = 270f,
            forceMoveTo = isFirst
        )
    }


    // 1. Top-Left Corner
    drawCornerArc(
        cx = bumpRadius, cy = bumpRadius, startAngle = 90f, isFirst = true
    )

    // 2. Top Bumps (Left to Right)
    for (i in 0 until countH) {
        val startX = bumpRadius * 2 + (i * segW)
        drawBump(
            baseStartX = startX,
            baseStartY = bumpRadius,
            baseEndX = startX + segW,
            baseEndY = bumpRadius,
        )
    }

    // 3. Top-Right Corner
    drawCornerArc(
        cx = width - bumpRadius, cy = bumpRadius, startAngle = 180f
    )

    // 4. Right Bumps (Top to Bottom)
    for (i in 0 until countV) {
        val startY = bumpRadius * 2 + (i * segH)
        drawBump(
            baseStartX = width - bumpRadius,
            baseStartY = startY,
            baseEndX = width - bumpRadius,
            baseEndY = startY + segH,
        )
    }

    // 5. Bottom-Right Corner
    drawCornerArc(
        cx = width - bumpRadius, cy = height - bumpRadius, startAngle = 270f
    )

    // 6. Bottom Bumps (Right to Left)
    for (i in 0 until countH) {
        val startX = width - bumpRadius * 2 - (i * segW)
        drawBump(
            baseStartX = startX,
            baseStartY = height - bumpRadius,
            baseEndX = startX - segW,
            baseEndY = height - bumpRadius,
        )
    }

    // 7. Bottom-Left Corner
    drawCornerArc(
        cx = bumpRadius, cy = height - bumpRadius, startAngle = 0f
    )

    // 8. Left Bumps (Bottom to Top)
    for (i in 0 until countV) {
        val startY = height - bumpRadius * 2 - (i * segH)
        drawBump(
            baseStartX = bumpRadius,
            baseStartY = startY,
            baseEndX = bumpRadius,
            baseEndY = startY - segH,
        )
    }

    val innerLeft = bumpRadius + borderThickness
    val innerTop = bumpRadius + borderThickness
    val innerRight = width - bumpRadius - borderThickness
    val innerBottom = height - bumpRadius - borderThickness

    path.addRect(
        Rect(
            left = innerLeft, top = innerTop, right = innerRight, bottom = innerBottom
        )
    )

    path.close()
    return path
}

@Preview
@Composable
fun ScallopBorderPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .padding(10.dp)
            .size(500.dp, 300.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.diary_test_image),
            contentDescription = null,
            modifier = Modifier.scallopBorder(
                    color = Color.White, bumpCount = 15, borderThickness = 2.dp
                ),
            contentScale = ContentScale.Crop
        )
        Text(
            "content", modifier = Modifier
                .scallopBorder(
                    color = Color.White, bumpCount = 15, borderThickness = 2.dp

                )
                .padding(6.dp)
        )
    }
}
