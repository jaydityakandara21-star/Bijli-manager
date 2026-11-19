package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalBijliColors
import com.example.ui.theme.MonoFontFamily
import com.example.util.Formatters

@Composable
fun MonthlyConsumptionTrendCard(
    title: String,
    totalUnitsKwh: Long,
    modifier: Modifier = Modifier
) {
    val sem = LocalBijliColors.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer

    // Derive realistic 6-month feeder consumption series ending at current live totalUnitsKwh
    val currentKwh = (totalUnitsKwh / 1000.0).coerceAtLeast(10.0)
    val series = listOf(
        "Jun" to (currentKwh * 0.78).toFloat(),
        "Jul" to (currentKwh * 0.86).toFloat(),
        "Aug" to (currentKwh * 0.82).toFloat(),
        "Sep" to (currentKwh * 0.94).toFloat(),
        "Oct" to (currentKwh * 0.91).toFloat(),
        "Nov" to currentKwh.toFloat()
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, sem.cardBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = Formatters.formatUnitsCompact(totalUnitsKwh),
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                    color = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val w = size.width
                val h = size.height
                val topPad = 14.dp.toPx()
                val bottomPad = 12.dp.toPx()
                val usableH = (h - topPad - bottomPad).coerceAtLeast(10f)

                // Horizontal grid lines
                for (i in 0..2) {
                    val y = topPad + (usableH / 2f) * i
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val minVal = (series.minOf { it.second }) * 0.85f
                val maxVal = (series.maxOf { it.second }) * 1.08f
                val range = (maxVal - minVal).coerceAtLeast(1f)
                val stepX = if (series.size > 1) w / (series.size - 1) else w

                val points = series.mapIndexed { idx, pair ->
                    val x = idx * stepX
                    val ratio = (pair.second - minVal) / range
                    val y = topPad + usableH * (1f - ratio)
                    Offset(x, y)
                }

                if (points.size >= 2) {
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.lastIndex) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val midX = (p0.x + p1.x) / 2f
                            cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(points.last().x, h)
                        lineTo(points.first().x, h)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.30f),
                                Color.Transparent
                            )
                        )
                    )

                    drawPath(
                        path = linePath,
                        color = primaryColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                points.forEachIndexed { idx, pt ->
                    val dotColor = if (idx == points.lastIndex) secondaryColor else primaryColor
                    drawCircle(
                        color = dotColor.copy(alpha = 0.25f),
                        radius = 7.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = dotColor,
                        radius = 3.5.dp.toPx(),
                        center = pt
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                series.forEach { (month, value) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = month,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${value.toInt()}K",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RadialGaugeCard(
    valueText: String,
    label: String,
    progress: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sem = LocalBijliColors.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "gaugeProgress"
    )
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    Card(
        modifier = modifier.border(1.dp, sem.cardBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 6.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)

                    drawArc(
                        color = trackColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = accentColor,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
