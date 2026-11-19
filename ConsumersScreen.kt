package com.example.ui.screens

import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ConsumerEntity
import com.example.data.model.BillingStatus
import com.example.data.model.ConnectionStatus
import com.example.data.model.ConsumerCategory
import com.example.data.model.ConsumerFilterState
import com.example.data.model.SortOption
import com.example.ui.components.ConsumerCard
import com.example.ui.i18n.BijliLabels
import com.example.ui.theme.LocalBijliColors
import com.example.ui.theme.MonoFontFamily
import com.example.ui.viewmodel.PagedConsumersUiState
import com.example.util.Formatters

@Composable
fun ConsumersScreen(
    labels: BijliLabels,
    filterState: ConsumerFilterState,
    pagedState: PagedConsumersUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterChange: (ConsumerCategory?) -> Unit,
    onConnectionFilterChange: (ConnectionStatus?) -> Unit,
    onBillingFilterChange: (BillingStatus?) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onPageSizeChange: (Int) -> Unit,
    onPageChange: (Int) -> Unit,
    onClearFilters: () -> Unit,
    onAddConsumerClick: () -> Unit,
    onViewConsumerClick: (Int) -> Unit,
    onEditConsumerClick: (Int) -> Unit,
    onTogglePaidClick: (ConsumerEntity) -> Unit,
    onCallClick: (ConsumerEntity) -> Unit,
    onCopyKNumberClick: (ConsumerEntity) -> Unit,
    onShareConsumerClick: (ConsumerEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sem = LocalBijliColors.current
    val listState = rememberLazyListState()

    LaunchedEffect(pagedState.pageIndex) {
        listState.scrollToItem(0)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            // Sticky Search + Filters Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = filterState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = labels.searchPlaceholder,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (filterState.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = sem.cardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("consumer_search_input")
                )

                // Dropdown Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterDropdownChip(
                        label = filterState.category?.code ?: "Category",
                        selected = filterState.category != null,
                        options = listOf<ConsumerCategory?>(null) + ConsumerCategory.entries,
                        optionLabel = { it?.englishLabel ?: "All Categories" },
                        onSelect = onCategoryFilterChange
                    )

                    FilterDropdownChip(
                        label = filterState.billingStatus?.englishLabel ?: "Billing",
                        selected = filterState.billingStatus != null,
                        options = listOf<BillingStatus?>(null) + BillingStatus.entries,
                        optionLabel = { it?.englishLabel ?: "All Billing Status" },
                        onSelect = onBillingFilterChange
                    )

                    FilterDropdownChip(
                        label = filterState.connectionStatus?.englishLabel ?: "Connection",
                        selected = filterState.connectionStatus != null,
                        options = listOf<ConnectionStatus?>(null) + ConnectionStatus.entries,
                        optionLabel = { it?.englishLabel ?: "All Connections" },
                        onSelect = onConnectionFilterChange
                    )

                    FilterDropdownChip(
                        label = filterState.sortOption.englishLabel,
                        selected = filterState.sortOption != SortOption.ID_ASC,
                        options = SortOption.entries,
                        optionLabel = { it.englishLabel },
                        onSelect = onSortChange,
                        leadingIcon = Icons.AutoMirrored.Filled.Sort
                    )

                    if (filterState.activeFilterCount > 0 || filterState.searchQuery.isNotBlank()) {
                        FilterChip(
                            selected = true,
                            onClick = onClearFilters,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.FilterAltOff,
                                    contentDescription = "Clear Filters",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Reset") }
                        )
                    }
                }

                // Result Count + Page Size Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val startNum = if (pagedState.totalFilteredCount == 0) 0 else pagedState.pageIndex * pagedState.pageSize + 1
                    val endNum = ((pagedState.pageIndex + 1) * pagedState.pageSize).coerceAtMost(pagedState.totalFilteredCount)
                    Text(
                        text = "Showing $startNum–$endNum of ${Formatters.formatNumber(pagedState.totalFilteredCount)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(25, 50).forEach { sizeOption ->
                            val isSelected = pagedState.pageSize == sizeOption
                            Surface(
                                onClick = { onPageSizeChange(sizeOption) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else sem.cardBorder,
                                    RoundedCornerShape(8.dp)
                                )
                            ) {
                                Text(
                                    text = "$sizeOption/pg",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (pagedState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SearchOff,
                            contentDescription = "No matching consumers",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "No Consumers Found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Try searching another Account No, K. No, Name, or clear your filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilledTonalButton(onClick = onClearFilters) {
                            Text("Clear Search & Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(pagedState.items, key = { it.id }) { consumer ->
                        ConsumerCard(
                            consumer = consumer,
                            onViewClick = { onViewConsumerClick(consumer.id) },
                            onEditClick = { onEditConsumerClick(consumer.id) },
                            onTogglePaidClick = { onTogglePaidClick(consumer) },
                            onCallClick = { onCallClick(consumer) },
                            onCopyKNumberClick = { onCopyKNumberClick(consumer) },
                            onShareClick = { onShareConsumerClick(consumer) }
                        )
                    }

                    // Pagination Controls Footer
                    item {
                        PaginationControlsBar(
                            pageIndex = pagedState.pageIndex,
                            totalPages = pagedState.totalPages,
                            onPrev = { onPageChange(pagedState.pageIndex - 1) },
                            onNext = { onPageChange(pagedState.pageIndex + 1) }
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddConsumerClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Consumer"
                )
            },
            text = {
                Text(
                    text = "Add Consumer",
                    fontWeight = FontWeight.SemiBold
                )
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .testTag("add_consumer_fab")
        )
    }
}

@Composable
private fun <T> FilterDropdownChip(
    label: String,
    selected: Boolean,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = selected,
            onClick = { expanded = true },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else null,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            label = {
                Text(
                    text = label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun PaginationControlsBar(
    pageIndex: Int,
    totalPages: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val sem = LocalBijliColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, sem.cardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onPrev,
            enabled = pageIndex > 0,
            modifier = Modifier.testTag("pagination_prev")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Page"
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text("Prev")
        }

        Text(
            text = "Page ${pageIndex + 1} of $totalPages",
            style = MaterialTheme.typography.titleSmall.copy(fontFamily = MonoFontFamily),
            color = MaterialTheme.colorScheme.onSurface
        )

        TextButton(
            onClick = onNext,
            enabled = pageIndex < totalPages - 1,
            modifier = Modifier.testTag("pagination_next")
        ) {
            Text("Next")
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Page"
            )
        }
    }
}
