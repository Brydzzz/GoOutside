package com.example.gooutside.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.AsyncImage
import com.example.gooutside.R
import com.example.gooutside.data.DiaryEntry
import com.example.gooutside.ui.home.formattedDate
import com.example.gooutside.ui.home.formattedLocation
import com.example.gooutside.ui.theme.GoOutsideTheme
import java.time.LocalDate

@Composable
fun DiaryEntryCard(entry: DiaryEntry, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(22.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // todo: add fallback when imagepath fails
        AsyncImage(
            model = entry.imagePath,
            contentDescription = "Image for diary entry from ${entry.formattedDate()}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Column(Modifier.padding(end = 8.dp)) {
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
fun DiaryEntryCardPreview() {
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
    GoOutsideTheme { DiaryEntryCard(entry1) }
}