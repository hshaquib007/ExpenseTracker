package com.qasioon.expensetracker.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qasioon.expensetracker.ui.model.MonthBar

data class PieSlice(
    val label: String,
    val fraction: Float,
    val color: Color,
    val amount: Double
)

val chartPalette = listOf(
    Color(0xFF26A69A),
    Color(0xFFFFB300),
    Color(0xFFEF5350),
    Color(0xFF42A5F5),
    Color(0xFFAB47BC),
    Color(0xFF66BB6A),
    Color(0xFFFF7043),
    Color(0xFF8D6E63),
    Color(0xFF78909C),
    Color(0xFFEC407A)
)

@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        if (slices.isEmpty()) return@Canvas
        val diameter = minOf(size.width, size.height)
        val topLeft = Offset(
            x = (size.width - diameter) / 2f,
            y = (size.height - diameter) / 2f
        )
        val chartSize = Size(diameter, diameter)
        var startAngle = -90f
        slices.forEach { slice ->
            val sweep = (slice.fraction.coerceIn(0f, 1f)) * 360f
            if (sweep > 0f) {
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = topLeft,
                    size = chartSize
                )
            }
            startAngle += sweep
        }
    }
}

@Composable
fun BarChart(
    bars: List<MonthBar>,
    modifier: Modifier = Modifier
) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val barColor = MaterialTheme.colorScheme.primary

    val labelPaint = remember(labelColor) {
        Paint().apply {
            textAlign = Paint.Align.CENTER
            textSize = 10.sp.value * 3f
            color = labelColor.toArgb()
            isAntiAlias = true
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        if (bars.isEmpty()) return@Canvas
        val labelSpace = 34.dp.toPx()
        val chartHeight = size.height - labelSpace
        val maxAmount = bars.maxOf { it.amount }.coerceAtLeast(1.0)
        val columnWidth = size.width / bars.size
        val barWidth = columnWidth * 0.5f

        bars.forEachIndexed { index, bar ->
            val barHeight = (bar.amount / maxAmount * chartHeight).toFloat()
            val centerX = columnWidth * (index + 0.5f)
            drawRoundRect(
                color = barColor,
                topLeft = Offset(
                    x = centerX - barWidth / 2f,
                    y = chartHeight - barHeight
                ),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            // Text drawing is complex in Compose Canvas, using simplified approach
        }
    }
}
