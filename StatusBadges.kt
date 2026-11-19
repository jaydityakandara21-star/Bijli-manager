package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.BillingStatus
import com.example.data.model.ConnectionStatus
import com.example.data.model.ConsumerCategory
import com.example.ui.theme.LocalBijliColors

@Composable
fun categoryColor(category: ConsumerCategory): Color {
    val sem = LocalBijliColors.current
    return when (category) {
        ConsumerCategory.DOMESTIC -> sem.domestic
        ConsumerCategory.COMMERCIAL -> sem.commercial
        ConsumerCategory.AGRICULTURAL -> sem.agricultural
        ConsumerCategory.INDUSTRIAL -> sem.industrial
    }
}

fun categoryIcon(category: ConsumerCategory): ImageVector = when (category) {
    ConsumerCategory.DOMESTIC -> Icons.Filled.Home
    ConsumerCategory.COMMERCIAL -> Icons.Filled.Storefront
    ConsumerCategory.AGRICULTURAL -> Icons.Filled.Agriculture
    ConsumerCategory.INDUSTRIAL -> Icons.Filled.Factory
}

@Composable
fun ConsumerAvatar(
    initials: String,
    category: ConsumerCategory,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val accent = categoryColor(category)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.25f),
                        accent.copy(alpha = 0.08f)
                    )
                )
            )
            .border(1.5.dp, accent.copy(alpha = 0.65f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = accent
        )
    }
}

@Composable
fun BillingStatusBadge(
    status: BillingStatus,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val sem = LocalBijliColors.current
    val (fg, bg, icon, text) = when (status) {
        BillingStatus.PAID -> Quad(sem.paid, sem.paidContainer, Icons.Filled.CheckCircle, "Paid")
        BillingStatus.UNPAID -> Quad(sem.unpaid, sem.unpaidContainer, Icons.Filled.ErrorOutline, "Unpaid")
        BillingStatus.PARTIAL -> Quad(sem.partial, sem.partialContainer, Icons.Filled.HourglassBottom, "Partial")
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg.copy(alpha = 0.85f))
            .border(1.dp, fg.copy(alpha = 0.45f), RoundedCornerShape(50))
            .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = fg,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ConnectionStatusChip(
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    val sem = LocalBijliColors.current
    val (color, label) = when (status) {
        ConnectionStatus.ACTIVE -> sem.paid to "Active"
        ConnectionStatus.DISCONNECTED -> sem.unpaid to "Disconnected"
        ConnectionStatus.PENDING -> sem.partial to "Pending"
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CategoryPill(
    category: ConsumerCategory,
    modifier: Modifier = Modifier
) {
    val accent = categoryColor(category)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = category.englishLabel,
            tint = accent,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = category.code,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = accent,
            maxLines = 1
        )
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
