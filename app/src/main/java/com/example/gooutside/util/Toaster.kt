package com.example.gooutside.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToastManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    suspend fun show(message: String, duration: Int = Toast.LENGTH_SHORT) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, duration).show()
        }
    }

    suspend fun show(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, context.getString(messageRes), duration).show()
        }
    }
}