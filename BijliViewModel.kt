package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BijliDatabase
import com.example.data.local.ConsumerEntity
import com.example.data.local.SettingsPreferences
import com.example.data.local.UserPreferences
import com.example.data.model.BillingStatus
import com.example.data.model.CategoryBreakdown
import com.example.data.model.ConnectionStatus
import com.example.data.model.ConsumerCategory
import com.example.data.model.ConsumerFilterState
import com.example.data.model.DashboardSummary
import com.example.data.model.EmployeeProfile
import com.example.data.model.LanguageMode
import com.example.data.model.SortOption
import com.example.data.model.TariffSettings
import com.example.data.repository.ConsumerRepository
import com.example.data.repository.SaveConsumerResult
import com.example.util.CsvExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PagedConsumersUiState(
    val items: List<ConsumerEntity> = emptyList(),
    val totalFilteredCount: Int = 0,
    val pageIndex: Int = 0,
    val totalPages: Int = 1,
    val pageSize: Int = 25
)

class BijliViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BijliDatabase.getInstance(application)
    private val repository = ConsumerRepository(database.consumerDao())
    private val settingsPrefs = SettingsPreferences(application)

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    private val _filterState = MutableStateFlow(ConsumerFilterState())
    val filterState: StateFlow<ConsumerFilterState> = _filterState.asStateFlow()

    val allConsumers: StateFlow<List<ConsumerEntity>> = repository.allConsumers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userPreferences: StateFlow<UserPreferences> = settingsPrefs.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val dashboardSummary: StateFlow<DashboardSummary> = allConsumers
        .map { list -> computeDashboardSummary(list) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardSummary()
        )

    val categoryBreakdowns: StateFlow<List<CategoryBreakdown>> = allConsumers
        .map { list -> computeCategoryBreakdowns(list) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val priorityConsumers: StateFlow<List<ConsumerEntity>> = allConsumers
        .map { list ->
            val featured = list.take(4)
            val topUnpaid = list
                .filter { it.billingStatus != BillingStatus.PAID && it.id > 4 }
                .sortedByDescending { it.dueAmount }
                .take(4)
            (featured + topUnpaid).distinctBy { it.id }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dueQueueConsumers: StateFlow<List<ConsumerEntity>> = allConsumers
        .map { list ->
            list.filter { it.billingStatus != BillingStatus.PAID }
                .sortedByDescending { it.dueAmount }
                .take(50)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pagedConsumers: StateFlow<PagedConsumersUiState> = combine(
        allConsumers,
        _filterState
    ) { consumers, filter ->
        filterAndPaginate(consumers, filter)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagedConsumersUiState()
        )

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.ensureSeeded()
            }
            _isInitializing.value = false
        }
    }

    fun observeConsumer(id: Int) = repository.observeConsumer(id)

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query, pageIndex = 0)
    }

    fun setCategoryFilter(category: ConsumerCategory?) {
        _filterState.value = _filterState.value.copy(category = category, pageIndex = 0)
    }

    fun setConnectionStatusFilter(status: ConnectionStatus?) {
        _filterState.value = _filterState.value.copy(connectionStatus = status, pageIndex = 0)
    }

    fun setBillingStatusFilter(status: BillingStatus?) {
        _filterState.value = _filterState.value.copy(billingStatus = status, pageIndex = 0)
    }

    fun setSortOption(sort: SortOption) {
        _filterState.value = _filterState.value.copy(sortOption = sort, pageIndex = 0)
    }

    fun setPageSize(size: Int) {
        _filterState.value = _filterState.value.copy(pageSize = size, pageIndex = 0)
    }

    fun goToPage(page: Int) {
        _filterState.value = _filterState.value.copy(pageIndex = page.coerceAtLeast(0))
    }

    fun clearAllFilters() {
        _filterState.value = ConsumerFilterState(pageSize = _filterState.value.pageSize)
    }

    fun applyQuickFilter(
        category: ConsumerCategory? = null,
        billingStatus: BillingStatus? = null
    ) {
        _filterState.value = ConsumerFilterState(
            category = category,
            billingStatus = billingStatus,
            pageSize = _filterState.value.pageSize
        )
    }

    fun toggleLanguageMode() {
        val current = userPreferences.value.languageMode
        val next = when (current) {
            LanguageMode.BILINGUAL -> LanguageMode.ENGLISH
            LanguageMode.ENGLISH -> LanguageMode.HINDI
            LanguageMode.HINDI -> LanguageMode.BILINGUAL
        }
        viewModelScope.launch { settingsPrefs.setLanguageMode(next) }
    }

    fun setLanguageMode(mode: LanguageMode) {
        viewModelScope.launch { settingsPrefs.setLanguageMode(mode) }
    }

    fun toggleDarkTheme() {
        val next = !userPreferences.value.isDarkTheme
        viewModelScope.launch { settingsPrefs.setDarkTheme(next) }
    }

    fun updateTariffs(tariffs: TariffSettings) {
        viewModelScope.launch { settingsPrefs.updateTariffs(tariffs) }
    }

    fun updateEmployeeProfile(profile: EmployeeProfile) {
        viewModelScope.launch { settingsPrefs.updateEmployeeProfile(profile) }
    }

    fun togglePaidStatus(consumer: ConsumerEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.togglePaidStatus(consumer)
        }
    }

    fun updateMeterReading(
        consumer: ConsumerEntity,
        newCurrentReading: Int,
        tariffRate: Double,
        markUnpaidOnNewBill: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMeterReading(consumer, newCurrentReading, tariffRate, markUnpaidOnNewBill)
        }
    }

    suspend fun getSuggestedNumbers(): Pair<String, Pair<String, String>> {
        return withContext(Dispatchers.IO) {
            repository.getNextSuggestedNumbers()
        }
    }

    suspend fun saveConsumer(consumer: ConsumerEntity): SaveConsumerResult {
        return withContext(Dispatchers.IO) {
            repository.saveConsumer(consumer)
        }
    }

    fun deleteConsumer(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteConsumer(id)
        }
    }

    fun resetToSampleData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.resetToSampleData()
            }
            clearAllFilters()
            onComplete()
        }
    }

    suspend fun writeCsvToUri(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val consumers = allConsumers.value
            val resolver = getApplication<Application>().contentResolver
            resolver.openOutputStream(uri)?.use { out ->
                CsvExporter.writeToOutputStream(out, consumers)
            } ?: error("Unable to open output stream")
            consumers.size
        }
    }

    suspend fun createShareCsvUri(): Uri = withContext(Dispatchers.IO) {
        CsvExporter.createShareableCsvUri(getApplication(), allConsumers.value)
    }

    suspend fun createSingleConsumerCsvUri(consumer: ConsumerEntity): Uri = withContext(Dispatchers.IO) {
        CsvExporter.createShareableCsvUri(
            context = getApplication(),
            consumers = listOf(consumer),
            fileName = "bijli_${consumer.kNumber}.csv"
        )
    }

    suspend fun writeSingleConsumerCsvToUri(uri: Uri, consumer: ConsumerEntity): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val resolver = getApplication<Application>().contentResolver
                resolver.openOutputStream(uri)?.use { out ->
                    CsvExporter.writeToOutputStream(out, listOf(consumer))
                } ?: error("Unable to open output stream")
            }
        }

    private fun computeDashboardSummary(list: List<ConsumerEntity>): DashboardSummary {
        var active = 0
        var disconnected = 0
        var pending = 0
        var units = 0L
        var billed = 0.0
        var paid = 0.0
        var dues = 0.0
        var paidCount = 0
        var unpaidCount = 0
        var partialCount = 0
        var domestic = 0
        var commercial = 0
        var agri = 0
        var industrial = 0

        for (c in list) {
            when (c.connectionStatus) {
                ConnectionStatus.ACTIVE -> active++
                ConnectionStatus.DISCONNECTED -> disconnected++
                ConnectionStatus.PENDING -> pending++
            }
            units += c.unitsConsumed
            billed += c.lastBillAmount
            paid += when (c.billingStatus) {
                BillingStatus.PAID -> c.lastBillAmount
                BillingStatus.UNPAID -> 0.0
                BillingStatus.PARTIAL -> c.paidAmount
            }
            dues += c.dueAmount
            when (c.billingStatus) {
                BillingStatus.PAID -> paidCount++
                BillingStatus.UNPAID -> unpaidCount++
                BillingStatus.PARTIAL -> partialCount++
            }
            when (c.category) {
                ConsumerCategory.DOMESTIC -> domestic++
                ConsumerCategory.COMMERCIAL -> commercial++
                ConsumerCategory.AGRICULTURAL -> agri++
                ConsumerCategory.INDUSTRIAL -> industrial++
            }
        }

        return DashboardSummary(
            totalConsumers = list.size,
            activeCount = active,
            disconnectedCount = disconnected,
            pendingConnectionCount = pending,
            totalUnitsConsumed = units,
            totalBilledAmount = billed,
            totalPaidAmount = paid,
            totalPendingDues = dues,
            paidCount = paidCount,
            unpaidCount = unpaidCount,
            partialCount = partialCount,
            domesticCount = domestic,
            commercialCount = commercial,
            agriculturalCount = agri,
            industrialCount = industrial
        )
    }

    private fun computeCategoryBreakdowns(list: List<ConsumerEntity>): List<CategoryBreakdown> {
        return ConsumerCategory.entries.map { cat ->
            val matching = list.filter { it.category == cat }
            CategoryBreakdown(
                category = cat,
                count = matching.size,
                totalUnits = matching.sumOf { it.unitsConsumed.toLong() },
                totalBilled = matching.sumOf { it.lastBillAmount },
                totalDue = matching.sumOf { it.dueAmount }
            )
        }
    }

    private fun filterAndPaginate(
        consumers: List<ConsumerEntity>,
        filter: ConsumerFilterState
    ): PagedConsumersUiState {
        val q = filter.searchQuery.trim().lowercase()
        val filtered = consumers.filter { c ->
            val matchesQuery = if (q.isEmpty()) {
                true
            } else {
                c.name.lowercase().contains(q) ||
                    c.accountNumber.lowercase().contains(q) ||
                    c.kNumber.lowercase().contains(q) ||
                    c.meterNumber.lowercase().contains(q) ||
                    c.mobileNumber.contains(q) ||
                    c.addressArea.lowercase().contains(q) ||
                    c.id.toString() == q
            }
            val matchesCategory = filter.category == null || c.category == filter.category
            val matchesConn = filter.connectionStatus == null || c.connectionStatus == filter.connectionStatus
            val matchesBilling = filter.billingStatus == null || c.billingStatus == filter.billingStatus

            matchesQuery && matchesCategory && matchesConn && matchesBilling
        }

        val sorted = when (filter.sortOption) {
            SortOption.ID_ASC -> filtered.sortedBy { it.id }
            SortOption.HIGHEST_DUE -> filtered.sortedByDescending { it.dueAmount }
            SortOption.HIGHEST_UNITS -> filtered.sortedByDescending { it.unitsConsumed }
            SortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            SortOption.RECENTLY_UPDATED -> filtered.sortedByDescending { it.lastUpdatedMillis }
        }

        val totalCount = sorted.size
        val pageSize = filter.pageSize.coerceAtLeast(1)
        val totalPages = if (totalCount == 0) 1 else ((totalCount + pageSize - 1) / pageSize)
        val safePage = filter.pageIndex.coerceIn(0, totalPages - 1)
        val fromIndex = safePage * pageSize
        val toIndex = (fromIndex + pageSize).coerceAtMost(totalCount)
        val pageItems = if (fromIndex < totalCount) sorted.subList(fromIndex, toIndex) else emptyList()

        return PagedConsumersUiState(
            items = pageItems,
            totalFilteredCount = totalCount,
            pageIndex = safePage,
            totalPages = totalPages,
            pageSize = pageSize
        )
    }
}
