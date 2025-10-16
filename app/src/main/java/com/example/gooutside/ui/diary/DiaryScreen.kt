package com.example.gooutside.ui.diary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gooutside.R
import com.example.gooutside.data.DiaryEntry
import com.example.gooutside.ui.common.DiaryEntriesList
import com.example.gooutside.ui.theme.GoOutsideTheme
import java.time.LocalDate

@Composable
fun DiaryScreen(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit,
    onDiaryEntryClick: (Int) -> Unit,
    viewModel: DiaryScreenViewModel = hiltViewModel<DiaryScreenViewModel>()
) {
    val uiState: DiaryScreenUiState by viewModel.uiState.collectAsState()

    DiaryScreenBody(
        onNavigateUp = navigateToHome,
        modifier = modifier,
        checkedFilter = uiState.dateRangeFilter,
        onFilterChange = viewModel::onDateRangeFilterChanged,
        entriesList = uiState.diaryEntries,
        onDiaryEntryClick = onDiaryEntryClick,
        showDateRangePicker = uiState.showDateRangePicker,
        initialDateRange = uiState.startDate.toEpochMillis() to uiState.endDate.toEpochMillis(),
        onDateRangeSelected = viewModel::onRangeConfirmed,
        onDatePickerDismiss = viewModel::hideDateRangePicker,
    )
}

@Composable
fun DiaryScreenBody(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    checkedFilter: DateRangeFilter,
    onFilterChange: (DateRangeFilter) -> Unit,
    entriesList: List<DiaryEntry>,
    onDiaryEntryClick: (Int) -> Unit,
    showDateRangePicker: Boolean,
    initialDateRange: Pair<Long?, Long?>,
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDatePickerDismiss: () -> Unit
) {
    if (showDateRangePicker) {
        DateRangePickerModal(initialDateRange, onDateRangeSelected, onDismiss = onDatePickerDismiss)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.ic_arrow_back_24),
                contentDescription = stringResource(R.string.arrow_back_icon_content_description),
                modifier = Modifier.clickable { onNavigateUp() })
            Spacer(Modifier.size(10.dp))
            Text(
                stringResource(R.string.diary_section_header),
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DateFilter(
                checkedFilter = checkedFilter,
                onFilterChange = onFilterChange
            )
        }
        DiaryEntriesList(
            entriesList = entriesList,
            onDiaryEntryClick = { onDiaryEntryClick(it.id) })
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DateFilter(checkedFilter: DateRangeFilter, onFilterChange: (DateRangeFilter) -> Unit) {
    ButtonGroup(
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        }
    ) {
        enumValues<DateRangeFilter>().forEach { filter ->
            toggleableItem(
                onCheckedChange = {
                    onFilterChange(filter)
                },
                label = filter.uiText,
                checked = filter == checkedFilter,
                icon = { Icon(painter = painterResource(filter.icon), contentDescription = null) },
            )
        }
    }
}

@Composable
fun DateRangePickerModal(
    initialDateRange: Pair<Long?, Long?>,
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState =
        rememberDateRangePickerState(initialDateRange.first, initialDateRange.second)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateRangeSelected(
                        Pair(
                            dateRangePickerState.selectedStartDateMillis,
                            dateRangePickerState.selectedEndDateMillis
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = "Select date range"
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_8")
@Composable
fun DiaryScreenPreview() {
    val entry1 = DiaryEntry(
        1,
        LocalDate.of(2025, 5, 21),
        "content://media/external/images/media/12345",
        "Marszałkowska",
        "14",
        "Warsaw",
        "Poland",
        123.456,
        789.012
    )
    val entry2 = entry1.copy(
        id = 2,
        creationDate = LocalDate.of(2024, 8, 21),
        street = "Street",
        city = null,
        country = null
    )
    val entry3 = entry1.copy(
        id = 2,
        creationDate = LocalDate.of(2024, 12, 21),
        street = "Street",
        city = "City",
        country = null
    )
    val entry4 = entry1.copy(
        id = 2,
        creationDate = LocalDate.of(2024, 10, 21),
        street = null,
        city = null,
        country = null
    )
    val entry5 = entry1.copy(
        id = 2,
        creationDate = LocalDate.of(2024, 4, 21),
        street = null,
        city = "City",
        country = "Country"
    )
    var selectedFilter by remember { mutableStateOf(DateRangeFilter.WEEK) }
    GoOutsideTheme {
        DiaryScreenBody(
            modifier = Modifier.fillMaxSize(),
            onNavigateUp = {},
            checkedFilter = selectedFilter,
            onFilterChange = { selectedFilter = it },
            entriesList = listOf(entry1, entry2, entry3, entry4, entry5),
            onDiaryEntryClick = {},
            showDateRangePicker = false,
            initialDateRange = LocalDate.now().toEpochDay() to LocalDate.now().toEpochDay(),
            onDateRangeSelected = { },
            onDatePickerDismiss = {}
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_8")
@Composable
fun DiaryScreenWithDatePickerPreview() {
    val entry1 = DiaryEntry(
        1,
        LocalDate.of(2025, 5, 21),
        "content://media/external/images/media/12345",
        "Marszałkowska",
        "14",
        "Warsaw",
        "Poland",
        123.456,
        789.012
    )
    val entry2 = entry1.copy(
        id = 2,
        creationDate = LocalDate.of(2024, 8, 21),
        street = "Street",
        city = null,
        country = null
    )
    val entry3 = entry1.copy(
        id = 2,
        creationDate = LocalDate.of(2024, 12, 21),
        street = "Street",
        city = "City",
        country = null
    )
    var selectedFilter by remember { mutableStateOf(DateRangeFilter.CUSTOM) }
    GoOutsideTheme {
        DiaryScreenBody(
            modifier = Modifier.fillMaxSize(),
            onNavigateUp = {},
            checkedFilter = selectedFilter,
            onFilterChange = { selectedFilter = it },
            entriesList = listOf(entry1, entry2, entry3),
            onDiaryEntryClick = {},
            showDateRangePicker = true,
            initialDateRange = LocalDate.now().toEpochDay() to LocalDate.now().toEpochDay(),
            onDateRangeSelected = { },
            onDatePickerDismiss = {}
        )
    }
}