package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ConsumerEntity
import com.example.data.model.BillingStatus
import com.example.data.model.ConsumerCategory
import com.example.data.model.DashboardSummary
import com.example.data.model.EmployeeProfile
import com.example.ui.components.ConsumerAvatar
import com.example.ui.components.EmployeeIdCard
import com.example.ui.components.MonthlyConsumptionTrendCard
import com.example.ui.components.RadialGaugeCard
import com.example.ui.i18n.BijliLabels
import com.example.ui.theme.LocalBijliColors
import com.example.ui.theme.MonoFontFamily
import com.example.util.Formatters

@Composable
fun OverviewScreen(
    labels: BijliLabels,
    employeeProfile: EmployeeProfile,
    summary: DashboardSummary,
    priorityConsumers: List<ConsumerEntity>,
    onEditEmployeeClick: () -> Unit,
    onQuickFilterClick: (ConsumerCategory?, BillingStatus?) -> Unit,
    onConsumerClick: (Int) -> Unit,
    onTogglePaid: (ConsumerEntity) -> Unit,
    onAddConsumerClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onViewAllConsumersClick: () -> Unit,
    onShareAppClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sem = LocalBijliColors.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                EmployeeIdCard(
                    profile = employeeProfile,
                    onEditClick = onEditEmployeeClick
                )
            }

            item {
                Text(
                    text = labels.dashboardTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Monthly Consumption Trend Chart Card
            item {
                MonthlyConsumptionTrendCard(
                    title = labels.monthlyConsumptionTrend,
                    totalUnitsKwh = summary.totalUnitsConsumed
                )
            }

            // 3 Radial Gauge Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RadialGaugeCard(
                        valueText = Formatters.formatNumber(summary.totalConsumers),
                        label = labels.totalConsumers,
                        progress = (summary.activeCount.toFloat() / summary.totalConsumers.coerceAtLeast(1)),
                        accentColor = sem.cyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                    RadialGaugeCard(
                        valueText = Formatters.formatRupeesCompact(summary.totalPendingDues),
                        label = labels.pendingDues,
                        progress = if (summary.totalBilledAmount > 0.0) {
                            (summary.totalPendingDues / summary.totalBilledAmount).toFloat()
                        } else 0f,
                        accentColor = sem.partial,
                        modifier = Modifier.weight(1f)
                    )
                    RadialGaugeCard(
                        valueText = "${summary.collectionEfficiencyPercent}%",
                        label = labels.collectionRate,
                        progress = summary.collectionEfficiencyPercent / 100f,
                        accentColor = sem.paid,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Total Units & Total Billed Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, sem.cardBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = "Total Units",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = labels.totalUnitsLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${Formatters.formatNumber(summary.totalUnitsConsumed)} kWh",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ReceiptLong,
                                    contentDescription = "Total Billed",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = labels.totalBilledLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Formatters.formatRupeesCompact(summary.totalBilledAmount),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Quick Filter Chips (Category + Paid/Unpaid)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = labels.quickFilters,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickFilterChip(
                            label = "Paid (${summary.paidCount})",
                            accent = sem.paid,
                            icon = Icons.Filled.CheckCircle,
                            onClick = { onQuickFilterClick(null, BillingStatus.PAID) }
                        )
                        QuickFilterChip(
                            label = "Unpaid (${summary.unpaidCount})",
                            accent = sem.unpaid,
                            icon = Icons.Filled.ErrorOutline,
                            onClick = { onQuickFilterClick(null, BillingStatus.UNPAID) }
                        )
                        QuickFilterChip(
                            label = "Domestic (${summary.domesticCount})",
                            accent = sem.domestic,
                            icon = null,
                            onClick = { onQuickFilterClick(ConsumerCategory.DOMESTIC, null) }
                        )
                        QuickFilterChip(
                            label = "Commercial (${summary.commercialCount})",
                            accent = sem.commercial,
                            icon = null,
                            onClick = { onQuickFilterClick(ConsumerCategory.COMMERCIAL, null) }
                        )
                        QuickFilterChip(
                            label = "Agri (${summary.agriculturalCount})",
                            accent = sem.agricultural,
                            icon = null,
                            onClick = { onQuickFilterClick(ConsumerCategory.AGRICULTURAL, null) }
                        )
                        QuickFilterChip(
                            label = "Industrial (${summary.industrialCount})",
                            accent = sem.industrial,
                            icon = null,
                            onClick = { onQuickFilterClick(ConsumerCategory.INDUSTRIAL, null) }
                        )
                    }
                }
            }

            // Priority / Recent Accounts Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = labels.priorityDues,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onViewAllConsumersClick,
                        modifier = Modifier.testTag("overview_view_all_button")
                    ) {
                        Text(text = "See all (${summary.totalConsumers})")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "See all consumers",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Compact Consumer Rows inspired by the attached screenshot
            items(priorityConsumers, key = { it.id }) { consumer ->
                OverviewConsumerRow(
                    consumer = consumer,
                    onClick = { onConsumerClick(consumer.id) },
                    onTogglePaid = { onTogglePaid(consumer) }
                )
            }

            // Quick Actions Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = labels.quickActions,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionTile(
                            title = "Add Consumer",
                            subtitle = "नया खाता",
                            icon = Icons.Filled.PersonAdd,
                            accent = MaterialTheme.colorScheme.primary,
                            onClick = onAddConsumerClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("overview_add_consumer")
                        )
                        QuickActionTile(
                            title = "Export CSV",
                            subtitle = "डेटा डाउनलोड",
                            icon = Icons.Filled.Download,
                            accent = sem.paid,
                            onClick = onExportCsvClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("overview_export_csv")
                        )
                        QuickActionTile(
                            title = "Share App",
                            subtitle = "WhatsApp शेयर",
                            icon = Icons.Filled.Share,
                            accent = sem.commercial,
                            onClick = onShareAppClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("overview_share_app")
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    QuickActionTile(
                        title = "All Consumers Directory (${summary.totalConsumers})",
                        subtitle = "सभी ${summary.totalConsumers} उपभोक्ता खाते देखें",
                        icon = Icons.Filled.Groups,
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = onViewAllConsumersClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("overview_directory")
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun QuickFilterChip(
    label: String,
    accent: Color,
    icon: ImageVector?,
    onClick: () -> Unit
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        leadingIcon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = accent.copy(alpha = 0.45f)
        )
    )
}

@Composable
private fun OverviewConsumerRow(
    consumer: ConsumerEntity,
    onClick: () -> Unit,
    onTogglePaid: () -> Unit
) {
    val sem = LocalBijliColors.current
    val isPaid = consumer.billingStatus == BillingStatus.PAID

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, sem.cardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConsumerAvatar(
                initials = consumer.initials,
                category = consumer.category,
                size = 40.dp
            )
            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${consumer.name} (${consumer.category.code})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${consumer.kNumber} • ${consumer.unitsConsumed} kWh",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isPaid) "Paid" else "Due: ${Formatters.formatRupees(consumer.dueAmount)}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isPaid) sem.paid else sem.partial
                )
                Spacer(modifier = Modifier.height(4.dp))
                FilledTonalButton(
                    onClick = if (isPaid) onClick else onTogglePaid,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isPaid) MaterialTheme.colorScheme.surfaceContainerHigh else sem.paidContainer,
                        contentColor = if (isPaid) MaterialTheme.colorScheme.primary else sem.paid
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = if (isPaid) "Details" else "Pay Now",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sem = LocalBijliColors.current
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, sem.cardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
