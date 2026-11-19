package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ConsumerEntity
import com.example.data.model.CategoryBreakdown
import com.example.data.model.ConsumerCategory
import com.example.data.model.TariffSettings
import com.example.ui.components.BillingStatusBadge
import com.example.ui.components.ConsumerAvatar
import com.example.ui.components.categoryColor
import com.example.ui.components.categoryIcon
import com.example.ui.theme.LocalBijliColors
import com.example.ui.theme.MonoFontFamily
import com.example.util.Formatters
import kotlin.math.roundToInt

@Composable
fun BillingScreen(
    breakdowns: List<CategoryBreakdown>,
    dueQueue: List<ConsumerEntity>,
    tariffSettings: TariffSettings,
    onCategoryTap: (ConsumerCategory) -> Unit,
    onConsumerClick: (Int) -> Unit,
    onTogglePaid: (ConsumerEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val sem = LocalBijliColors.current
    var calcCategory by remember { mutableStateOf(ConsumerCategory.DOMESTIC) }
    var calcUnitsText by remember { mutableStateOf("180") }
    var calcRateText by remember(tariffSettings) {
        mutableStateOf(tariffSettings.rateFor(ConsumerCategory.DOMESTIC).toString())
    }

    val unitsVal = calcUnitsText.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val rateVal = calcRateText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val estimatedBill = (unitsVal * rateVal).roundToInt()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Billing & Meter Desk • बिलिंग केंद्र",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Quick Bill Estimator Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, sem.cardBorder, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Calculate,
                                contentDescription = "Bill Calculator",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Quick Tariff Estimator • त्वरित बिल गणना",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ConsumerCategory.entries.forEach { cat ->
                                FilterChip(
                                    selected = calcCategory == cat,
                                    onClick = {
                                        calcCategory = cat
                                        calcRateText = tariffSettings.rateFor(cat).toString()
                                    },
                                    label = { Text(cat.code) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = calcUnitsText,
                                onValueChange = { if (it.all { ch -> ch.isDigit() }) calcUnitsText = it },
                                label = { Text("Units (kWh)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = calcRateText,
                                onValueChange = { calcRateText = it },
                                label = { Text("Rate (₹/kWh)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estimated Energy Charge:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Formatters.formatRupees(estimatedBill.toDouble()),
                                style = MaterialTheme.typography.titleLarge.copy(fontFamily = MonoFontFamily),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Category Breakdown Section
            item {
                Text(
                    text = "Category Breakdown • श्रेणीवार विवरण",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(breakdowns, key = { it.category.name }) { item ->
                val accent = categoryColor(item.category)
                val paidFraction = if (item.totalBilled > 0.0) {
                    ((item.totalBilled - item.totalDue) / item.totalBilled).toFloat().coerceIn(0f, 1f)
                } else 1f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, sem.cardBorder, RoundedCornerShape(16.dp))
                        .clickable { onCategoryTap(item.category) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = categoryIcon(item.category),
                                    contentDescription = item.category.englishLabel,
                                    tint = accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = item.category.englishLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.category.hindiLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${item.count} consumers",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                                color = accent
                            )
                        }

                        LinearProgressIndicator(
                            progress = { paidFraction },
                            color = accent,
                            trackColor = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Units: ${Formatters.formatUnitsCompact(item.totalUnits)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Billed: ${Formatters.formatRupeesCompact(item.totalBilled)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Due: ${Formatters.formatRupeesCompact(item.totalDue)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = sem.unpaid
                            )
                        }
                    }
                }
            }

            // Highest Pending Dues Queue
            item {
                Text(
                    text = "Top Pending Dues Queue • बकाया वसूली सूची",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(dueQueue.take(20), key = { it.id }) { consumer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, sem.cardBorder, RoundedCornerShape(14.dp))
                        .clickable { onConsumerClick(consumer.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ConsumerAvatar(
                            initials = consumer.initials,
                            category = consumer.category,
                            size = 38.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = consumer.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = consumer.kNumber,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                BillingStatusBadge(status = consumer.billingStatus, compact = true)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Formatters.formatRupees(consumer.dueAmount),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = MonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = sem.unpaid
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FilledTonalButton(
                                onClick = { onTogglePaid(consumer) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = sem.paidContainer,
                                    contentColor = sem.paid
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Payments,
                                    contentDescription = "Collect",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Collect", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
