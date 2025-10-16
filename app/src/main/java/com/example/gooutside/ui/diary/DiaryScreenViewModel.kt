package com.example.gooutside.ui.diary

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gooutside.R
import com.example.gooutside.data.DiaryEntriesRepository
import com.example.gooutside.data.DiaryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class DiaryScreenViewModel @Inject constructor(private val diaryEntriesRepository: DiaryEntriesRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(DiaryScreenUiState())
    val uiState: StateFlow<DiaryScreenUiState> = _uiState.asStateFlow()


    fun hideDateRangePicker() {
        _uiState.value = _uiState.value.copy(showDateRangePicker = false)
    }

    fun onDateRangeFilterChanged(newFilter: DateRangeFilter) {
        when (newFilter) {
            DateRangeFilter.CUSTOM -> _uiState.update { it.copy(showDateRangePicker = true) }
            DateRangeFilter.WEEK -> {
                _uiState.update {
                    val startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY)
                    it.copy(
                        startDate = startOfWeek,
                        endDate = startOfWeek.plusDays(7)
                    )
                }
                updateDiaryEntries()

            }

            DateRangeFilter.MONTH -> {
                _uiState.update {
                    val startOfMonth = LocalDate.now().withDayOfMonth(1)
                    it.copy(
                        startDate = startOfMonth,
                        endDate = startOfMonth.plusMonths(1).minusDays(1)
                    )
                }
                updateDiaryEntries()
            }
        }
        _uiState.value = _uiState.value.copy(dateRangeFilter = newFilter)
    }


    fun onRangeConfirmed(newRange: Pair<Long?, Long?>) {
        updateCustomDateRange(newRange)
        updateDiaryEntries()
    }

    private fun updateCustomDateRange(newRange: Pair<Long?, Long?>) {
        if (newRange.first == null || newRange.second == null) return

        hideDateRangePicker()
        _uiState.update {
            it.copy(
                startDate = newRange.first!!.toLocalDate(),
                endDate = newRange.second!!.toLocalDate()
            )
        }
    }

    private fun updateDiaryEntries() {
        viewModelScope.launch {
            diaryEntriesRepository.getEntriesForDateRange(
                _uiState.value.startDate,
                _uiState.value.endDate
            ).collect { entries ->
                _uiState.update { currentState ->
                    currentState.copy(diaryEntries = entries)
                }
            }
        }
    }

    init {
        updateDiaryEntries()
    }
}

data class DiaryScreenUiState(
    var diaryEntries: List<DiaryEntry> = emptyList(),
    val dateRangeFilter: DateRangeFilter = DateRangeFilter.WEEK,
    val startDate: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY),
    val endDate: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY).plusDays(7),
    val showDateRangePicker: Boolean = false
)

enum class DateRangeFilter(val uiText: String, @DrawableRes val icon: Int) {
    WEEK("Week", R.drawable.ic_calendar_week_24),
    MONTH("Month", R.drawable.ic_calendar_month_24),
    CUSTOM("Custom", R.drawable.ic_date_range_24)
}

fun LocalDate.toEpochMillis(): Long {
    return this.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
}