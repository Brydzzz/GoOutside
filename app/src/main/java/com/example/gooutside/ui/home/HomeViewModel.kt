package com.example.gooutside.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gooutside.data.DiaryEntriesRepository
import com.example.gooutside.data.DiaryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val diaryEntriesRepository: DiaryEntriesRepository) :
    ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    val homeUiState: StateFlow<HomeUiState> =
        diaryEntriesRepository.getRecentEntries().map { HomeUiState(it) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = HomeUiState()
        )
}

data class HomeUiState(
    val recentDiaryEntries: List<DiaryEntry> = emptyList(),
    // TODO Properties for statistics
)


fun DiaryEntry.formattedDate(): String {
    val formatter = DateTimeFormatter.ofPattern("EE, dd MMM yyyy", Locale.getDefault())
    val formatted = creationDate.format(formatter)
    // Capitalize day names, because for some locales it is lowercase
    return formatted.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}


fun DiaryEntry.formattedLocation(): String {
    val cityPart = city?.takeIf { it.isNotBlank() }.orEmpty()
    val countryPart = country?.takeIf { it.isNotBlank() }.orEmpty()
    val streetNumberPart = streetNumber?.takeIf { it.isNotBlank() }.orEmpty()
    val streetPart = street?.takeIf { it.isNotBlank() }?.let {
        if (cityPart.isNotEmpty() || countryPart.isNotEmpty()) "$it $streetNumberPart,\n" else "$it $streetNumberPart"
    }.orEmpty()

    return if (streetPart.isEmpty() && cityPart.isEmpty() && countryPart.isEmpty()) {
        "No location"
    } else {
        if (cityPart.isNotEmpty() && countryPart.isNotEmpty()) {
            "$streetPart$cityPart • $countryPart"
        } else {
            streetPart + cityPart + countryPart
        }
    }
}


