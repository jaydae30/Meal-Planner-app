package com.example.mealplannerapp.ui.recipes

import androidx.compose.runtime.Composable

@Composable
actual fun rememberRecipePhotoScanner(
    onTextRecognized: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    // iOS implementation: photo scanning with text recognition is not yet implemented
    // For now, return a no-op function that shows an error message
    return {
        onError("Photo scanning is not yet available on iOS")
    }
}
