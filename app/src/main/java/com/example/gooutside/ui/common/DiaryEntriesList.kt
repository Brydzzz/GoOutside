package com.example.gooutside.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gooutside.data.DiaryEntry

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
                    .clickable { onDiaryEntryClick(entry) })
        }
    }
}