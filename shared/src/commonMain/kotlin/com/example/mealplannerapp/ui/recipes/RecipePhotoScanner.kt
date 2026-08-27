package com.example.mealplannerapp.ui.recipes

import androidx.compose.runtime.Composable

/**
 * Returns a launcher: invoking it opens a photo picker, runs on-device text recognition
 * (no network, no AI) on the chosen image, and reports the recognized text back via
 * [onTextRecognized] — or [onError] if the user cancels or recognition fails.
 */
@Composable
expect fun rememberRecipePhotoScanner(
    onTextRecognized: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit
