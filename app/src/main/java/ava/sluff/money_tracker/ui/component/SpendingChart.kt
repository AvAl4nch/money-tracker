package ava.sluff.money_tracker.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ava.sluff.money_tracker.data.local.dao.CategorySpending

val ChartPalette = listOf(
    Color(0xFFE91E63), Color(0xFF8BC34A), Color(0xFFFF9800), Color(0xFF9C27B0),
    Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFF4CAF50), Color(0xFFFF5722),
    Color(0xFF673AB7), Color(0xFF795548), Color(0xFFFF6F00), Color(0xFF455A64)
)

@Composable
fun SpendingChart(
    spending: List<CategorySpending>,
    total: Double,
    modifier: Modifier = Modifier
) {
    val strokeWidth = with(LocalDensity.current) { 40.dp.toPx() }
    Canvas(modifier = modifier.aspectRatio(1f)) {
        if (total <= 0.0) return@Canvas
        val inset = strokeWidth / 2
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        var startAngle = -90f
        spending.forEachIndexed { index, item ->
            val sweep = ((item.total / total) * 360.0).toFloat()
            drawArc(
                color = ChartPalette[index % ChartPalette.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweep
        }
    }
}
