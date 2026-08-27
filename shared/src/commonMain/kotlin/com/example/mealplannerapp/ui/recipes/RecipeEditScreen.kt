package com.example.mealplannerapp.ui.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mealplannerapp.data.local.Ingredient
import com.example.mealplannerapp.data.local.Recipe
import com.example.mealplannerapp.di.LocalAppContainer
import com.example.mealplannerapp.domain.ParsedRecipeDraft
import com.example.mealplannerapp.domain.RecipeIngredientInput
import com.example.mealplannerapp.domain.RecipeNutritionCalculator
import com.example.mealplannerapp.domain.RecipeTextParser

private data class IngredientRow(val name: String = "", val quantity: String = "", val unit: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(recipeId: Long?, onDone: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: RecipeViewModel = viewModel { RecipeViewModel(container.recipeRepository) }

    var name by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("1") }
    var calories by remember { mutableStateOf("0") }
    var protein by remember { mutableStateOf("0") }
    var carbs by remember { mutableStateOf("0") }
    var fat by remember { mutableStateOf("0") }
    var tags by remember { mutableStateOf("") }
    var ingredientRows by remember { mutableStateOf(listOf(IngredientRow())) }
    var existingRecipe by remember { mutableStateOf<Recipe?>(null) }
    var loaded by remember { mutableStateOf(recipeId == null) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }
    var calculationNote by remember { mutableStateOf<String?>(null) }
    val nutritionCatalog by container.ingredientNutritionRepository.observeAll().collectAsState(initial = emptyList())

    fun applyDraft(draft: ParsedRecipeDraft) {
        draft.name?.let { name = it }
        draft.servings?.let { servings = it.toString() }
        draft.caloriesPerServing?.let { calories = it.toString() }
        draft.proteinGramsPerServing?.let { protein = it.toString() }
        draft.carbsGramsPerServing?.let { carbs = it.toString() }
        draft.fatGramsPerServing?.let { fat = it.toString() }
        if (draft.tags.isNotEmpty()) tags = draft.tags.joinToString(", ")
        if (draft.ingredients.isNotEmpty()) {
            ingredientRows = draft.ingredients.map {
                IngredientRow(it.name, it.quantity?.toString() ?: "", it.unit)
            }
        }
    }

    val launchPhotoScan = rememberRecipePhotoScanner(
        onTextRecognized = { text -> applyDraft(RecipeTextParser.parse(text)) },
        onError = { message -> scanError = message }
    )

    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            viewModel.loadRecipe(recipeId)?.let { loadedRecipe ->
                existingRecipe = loadedRecipe.recipe
                name = loadedRecipe.recipe.name
                servings = loadedRecipe.recipe.servings.toString()
                calories = loadedRecipe.recipe.caloriesPerServing.toString()
                protein = loadedRecipe.recipe.proteinGramsPerServing.toString()
                carbs = loadedRecipe.recipe.carbsGramsPerServing.toString()
                fat = loadedRecipe.recipe.fatGramsPerServing.toString()
                tags = loadedRecipe.recipe.tagsCsv
                val rows = loadedRecipe.ingredients
                    .sortedBy { it.sortOrder }
                    .map { IngredientRow(it.name, it.quantity.toString(), it.unit) }
                ingredientRows = rows.ifEmpty { listOf(IngredientRow()) }
            }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recipeId == null) "New Recipe" else "Edit Recipe") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    existingRecipe?.let { recipe ->
                        IconButton(onClick = {
                            viewModel.deleteRecipe(recipe)
                            onDone()
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete recipe")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!loaded) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { showPasteDialog = true }) {
                    Icon(Icons.Filled.ContentPaste, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Paste text")
                }
                TextButton(onClick = launchPhotoScan) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Scan photo")
                }
            }
            scanError?.let { message ->
                Text(
                    message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = servings,
                onValueChange = { servings = it },
                label = { Text("Servings") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nutrition per serving", style = MaterialTheme.typography.titleSmall)
                TextButton(onClick = {
                    val inputs = ingredientRows
                        .filter { it.name.isNotBlank() }
                        .map { RecipeIngredientInput(it.name, it.quantity.toDoubleOrNull() ?: 0.0, it.unit) }
                    val estimate = RecipeNutritionCalculator.calculate(
                        ingredients = inputs,
                        nutritionCatalog = nutritionCatalog,
                        recipeServings = servings.toIntOrNull() ?: 1
                    )
                    calories = estimate.caloriesPerServing.toString()
                    protein = estimate.proteinGramsPerServing.toString()
                    carbs = estimate.carbsGramsPerServing.toString()
                    fat = estimate.fatGramsPerServing.toString()
                    calculationNote = when {
                        inputs.isEmpty() -> "Add ingredients first."
                        estimate.unmatchedIngredientNames.isEmpty() ->
                            "Calculated from all ${estimate.matchedIngredientNames.size} ingredients."
                        estimate.matchedIngredientNames.isEmpty() ->
                            "No matching ingredients in the nutrition catalog."
                        else ->
                            "Calculated from ${estimate.matchedIngredientNames.size} of ${inputs.size} ingredients — " +
                                "no catalog match for: ${estimate.unmatchedIngredientNames.joinToString(", ")}."
                    }
                }) {
                    Icon(Icons.Filled.Calculate, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Calculate")
                }
            }
            calculationNote?.let { note ->
                Text(note, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
            }
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbs (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fat (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma-separated)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Text("Ingredients", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            ingredientRows.forEachIndexed { index, row ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = row.name,
                        onValueChange = { updated ->
                            ingredientRows = ingredientRows.toMutableList()
                                .also { it[index] = row.copy(name = updated) }
                        },
                        label = { Text("Ingredient") },
                        modifier = Modifier.weight(2f)
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        value = row.quantity,
                        onValueChange = { updated ->
                            ingredientRows = ingredientRows.toMutableList()
                                .also { it[index] = row.copy(quantity = updated) }
                        },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        value = row.unit,
                        onValueChange = { updated ->
                            ingredientRows = ingredientRows.toMutableList()
                                .also { it[index] = row.copy(unit = updated) }
                        },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        ingredientRows = ingredientRows.toMutableList()
                            .also { it.removeAt(index) }
                            .ifEmpty { listOf(IngredientRow()) }
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove ingredient")
                    }
                }
            }
            TextButton(onClick = { ingredientRows = ingredientRows + IngredientRow() }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add ingredient")
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val recipe = Recipe(
                        id = existingRecipe?.id ?: 0,
                        name = name.trim(),
                        servings = servings.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                        caloriesPerServing = calories.toDoubleOrNull() ?: 0.0,
                        proteinGramsPerServing = protein.toDoubleOrNull() ?: 0.0,
                        carbsGramsPerServing = carbs.toDoubleOrNull() ?: 0.0,
                        fatGramsPerServing = fat.toDoubleOrNull() ?: 0.0,
                        tagsCsv = tags
                    )
                    val ingredients = ingredientRows
                        .filter { it.name.isNotBlank() }
                        .map { row ->
                            Ingredient(
                                recipeId = recipe.id,
                                name = row.name.trim(),
                                quantity = row.quantity.toDoubleOrNull() ?: 0.0,
                                unit = row.unit.trim()
                            )
                        }
                    viewModel.saveRecipe(recipe, ingredients)
                    onDone()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showPasteDialog) {
        PasteRecipeTextDialog(
            onDismiss = { showPasteDialog = false },
            onParse = { text ->
                applyDraft(RecipeTextParser.parse(text))
                showPasteDialog = false
            }
        )
    }
}

@Composable
private fun PasteRecipeTextDialog(onDismiss: () -> Unit, onParse: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Paste recipe text") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Recipe text, caption, etc.") },
                minLines = 6,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onParse(text) }, enabled = text.isNotBlank()) {
                Text("Parse")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
