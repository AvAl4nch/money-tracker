package ava.sluff.money_tracker.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CategoryChip(
    name: String,
    color: Long,
    onClick: (() -> Unit)? = null
) {
    val chipColor = Color(color and 0xFFFFFFFFL)
    Surface(
        shape = MaterialTheme.shapes.small,
        color = chipColor.copy(alpha = 0.15f),
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = chipColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
