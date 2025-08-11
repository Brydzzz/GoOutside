package com.example.gooutside.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gooutside.R
import com.example.gooutside.ui.theme.GoOutsideTheme

// TODO: Prepare composables for getting data from viewmodel

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(stringResource(R.string.stats_section_header))
        // TODO: Calendar composable for stats section
        SectionHeader(stringResource(R.string.diary_section_header))
        DiaryEntriesList()
    }
}

@Composable
fun SectionHeader(headerText: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        Text(headerText, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.size(10.dp))
        Icon(
            painterResource(R.drawable.ic_arrow_forward_24),
            contentDescription = null,
            modifier = Modifier.clickable { onClick })
    }
}

@Composable
fun DiaryEntriesList() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
    ) {
        repeat(5) {
            DiaryEntryCard()
        }
    }
}

@Composable
fun DiaryEntryCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(22.dp))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.diary_test_image),
            contentDescription = "Diary Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Column {
            Text("Thursday, 17/04/2025", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.size(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_location_24), contentDescription = null)
                Spacer(Modifier.size(4.dp))
                Text("City • Country", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GoOutsideTheme { HomeScreen() }
}

@Preview(showBackground = true)
@Composable
fun SectionHeaderPreview() {
    GoOutsideTheme { SectionHeader("Photo Diary") }
}

@Preview(showBackground = true)
@Composable
fun DiaryEntryPreview() {
    GoOutsideTheme { DiaryEntryCard() }
}

@Preview(showBackground = true)
@Composable
fun DiaryEntriesListPreview() {
    GoOutsideTheme { DiaryEntriesList() }
}