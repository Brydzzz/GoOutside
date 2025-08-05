package com.example.gooutside.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.gooutside.R

enum class MainDestination(
    val route: String,
    @StringRes val label: Int,
    @DrawableRes val iconFilled: Int,
    @DrawableRes val iconOutline: Int,
    @StringRes val contentDescription: Int
) {
    Home(
        route = "home",
        label = R.string.home_nav_label,
        iconFilled = R.drawable.ic_home_filled_24,
        iconOutline = R.drawable.ic_home_outline_24,
        contentDescription = R.string.home_nav_label
    ),
    PhotoMode(
        route = "photo_mode",
        label = R.string.photo_mode_nav_label,
        iconFilled = R.drawable.ic_photo_mode_filled_24,
        iconOutline = R.drawable.ic_photo_mode_outline_24,
        contentDescription = R.string.photo_mode_nav_label
    ),
    Settings(
        route = "settings",
        label = R.string.settings_nav_label,
        iconFilled = R.drawable.ic_settings_filled_24,
        iconOutline = R.drawable.ic_settings_outline_24,
        contentDescription = R.string.settings_nav_label
    )
}