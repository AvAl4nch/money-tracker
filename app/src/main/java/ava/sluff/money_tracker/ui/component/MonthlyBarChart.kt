package ava.sluff.money_tracker.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ava.sluff.money_tracker.ui.screen.summary.MonthBar

@Composable
fun MonthlyBarChart(
    bars: List<MonthBar>,
    selectedYearMonth: Pair<Int, Int>,
    onBarTap: (MonthBar) -> Unit,
    modifier: Modifier = Modifier
) {
    if (bars.isEmpty()) return
    val maxTotal = bars.maxOf { it.total }.takeIf { it > 0 } ?: 1.0
    val barColor = MaterialTheme.colorScheme.primary
    val selectedColor = MaterialTheme.colorScheme.tertiary

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .pointerInput(bars) {
                    detectTapGestures { offset ->
                        val slot = size.width / bars.size
                        val index = (offset.x / slot).toInt().coerceIn(0, bars.size - 1)
                        onBarTap(bars[index])
                    }
                }
        ) {
            val slot = size.width / bars.size
            val barWidth = slot * 0.55f
            bars.forEachIndexed { i, bar ->
                val h = ((bar.total / maxTotal) * size.height * 0.95).toFloat()
                val selected = (bar.year to bar.month) == selectedYearMonth
                drawRect(
                    color = if (selected) selectedColor else barColor,
                    topLeft = Offset(i * slot + (slot - barWidth) / 2, size.height - h),
                    size = Size(barWidth, h)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            bars.forEach { bar ->
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
