package com.example.gooutside.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gooutside.R
import com.example.gooutside.data.DiaryEntry
import com.example.gooutside.ui.theme.GoOutsideTheme
import java.time.LocalDate

@Composable
fun HomeScreen(
    navigateToStatsPage: () -> Unit,
    navigateToDiaryPage: () -> Unit,
    navigateToDiaryEntry: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel<HomeViewModel>()
) {
    val homeUiState: HomeUiState by viewModel.homeUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatsSection(onStatsSectionHeaderClick = navigateToStatsPage)
        DiarySection(
            onDiarySectionHeaderClick = navigateToDiaryPage,
            onDiaryEntryClick = navigateToDiaryEntry,
            entriesList = homeUiState.recentDiaryEntries
        )
    }
}

@Composable
fun SectionHeader(headerText: String, onIconClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        Text(headerText, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.size(10.dp))
        Icon(
            painterResource(R.drawable.ic_arrow_forward_24),
            contentDescription = stringResource(R.string.arrow_forward_icon_content_description),
            modifier = Modifier.clickable { onIconClick })
    }
}

@Composable
fun StatsSection(onStatsSectionHeaderClick: () -> Unit) {
    SectionHeader(
        headerText = stringResource(R.string.stats_section_header),
        onIconClick = onStatsSectionHeaderClick
    )
    // TODO: Calendar composable for stats section
}

@Composable
fun DiarySection(
    onDiarySectionHeaderClick: () -> Unit,
    onDiaryEntryClick: (Int) -> Unit,
    entriesList: List<DiaryEntry>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeader(
            headerText = stringResource(R.string.diary_section_header),
            onIconClick = onDiarySectionHeaderClick
        )
        if (entriesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_entries_prompt),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            DiaryEntriesList(
                entriesList = entriesList,
                onDiaryEntryClick = { onDiaryEntryClick(it.id) },
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
fun DiaryEntriesList(
    entriesList: List<DiaryEntry>,
    onDiaryEntryClick: (DiaryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp), modifier = modifier

    ) {
        entriesList.forEach { entry ->
            DiaryEntryCard(
                entry,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
                    .clickable { onDiaryEntryClick(entry) })
        }
    }
}

@Composable
fun DiaryEntryCard(entry: DiaryEntry, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(22.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.diary_test_image), // TODO replace with entry image
            contentDescription = "Image for diary entry from ${entry.formattedDate()}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Column {
            Text(entry.formattedDate(), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.size(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.ic_location_24), contentDescription = stringResource(
                        R.string.location_icon_content_description
                    )
                )
                Spacer(Modifier.size(4.dp))
                Text(entry.formattedLocation(), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SectionHeaderPreview() {
    GoOutsideTheme { SectionHeader("Photo Diary") {} }
}

@Preview(showBackground = true)
@Composable
fun DiarySectionPreview() {
    val entry1 = DiaryEntry(
        1,
        LocalDate.of(2025, 5, 21),
        "content://media/external/images/media/12345",
        "Marszałkowska 14",
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
    GoOutsideTheme { DiarySection({}, {}, listOf(entry1, entry2, entry3, entry4, entry5)) }
}

@Preview(showBackground = true)
@Composable
fun DiarySectionNoEntriesPreview() {
    GoOutsideTheme { DiarySection({}, {}, emptyList(), modifier = Modifier.fillMaxSize()) }
}

@Preview(showBackground = true)
@Composable
fun DiaryEntryPreview() {
    val entry1 = DiaryEntry(
        1,
        LocalDate.of(2025, 5, 21),
        "content://media/external/images/media/12345",
        "Marszałkowska 14",
        "Warsaw",
        "Poland",
        123.456,
        789.012
    )
    GoOutsideTheme { DiaryEntryCard(entry1) }
}