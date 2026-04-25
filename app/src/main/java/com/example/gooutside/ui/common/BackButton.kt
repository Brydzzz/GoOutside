package com.example.gooutside.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gooutside.R

@Composable
fun BackButton(onNavigateUp: () -> Unit) {
    val painter = painterResource(R.drawable.ic_arrow_back_24)
    val description = stringResource(R.string.arrow_back_icon_content_description)
    Icon(
        painter = painter,
        contentDescription = description,
        modifier = Modifier.clickable { onNavigateUp() }
    )
}