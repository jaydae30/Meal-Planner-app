package com.example.mealplannerapp.ui.recipes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.example.mealplannerapp.data.local.IngredientNutrition
import com.example.mealplannerapp.di.LocalAppContainer

// Predefined units for ingredients
private val INGREDIENT_UNITS = listOf(
    "g",           // grams
    "oz",          // ounces
    "lb",          // pounds
    "kg",          // kilograms
    "ml",          // milliliters
    "cup",         // cups
    "tbsp",        // tablespoons
    "tsp",         // teaspoons
    "item"         // per item (e.g., biscuit, egg)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientCatalogScreen(onDone: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: IngredientNutritionViewModel = viewModel {
        IngredientNutritionViewModel(container.ingredientNutritionRepository)
    }

    val items by viewModel.items.collectAsState()
    var editingItem by remember { mutableStateOf<IngredientNutrition?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingredient Nutrition") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add ingredient nutrition")
            }
        }
    ) { innerPadding ->
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(
                    "No ingredients yet. Tap + to add one\n(e.g. \"chicken breast\": 165 kcal per 100 g).\n\n" +
                        "When a recipe's ingredients match by name + unit,\ncalories can be calculated for you.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { editingItem = item }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${item.caloriesPerServing.formatNumber()} kcal per " +
                                    "${item.servingQuantity.formatNumber()} ${item.servingUnit}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Protein ${item.proteinGramsPerServing.formatNumber()}g · " +
                                    "Carbs ${item.carbsGramsPerServing.formatNumber()}g · " +
                                    "Fat ${item.fatGramsPerServing.formatNumber()}g",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        IngredientNutritionEditDialog(
            existing = null,
            onDismiss = { showAddDialog = false },
            onSave = { item ->
                viewModel.save(item)
                showAddDialog = false
            },
            onDelete = null
        )
    }

    editingItem?.let { item ->
        IngredientNutritionEditDialog(
            existing = item,
            onDismiss = { editingItem = null },
            onSave = { updated ->
                viewModel.save(updated)
                editingItem = null
            },
            onDelete = {
                viewModel.delete(item)
                editingItem = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientNutritionEditDialog(
    existing: IngredientNutrition?,
    onDismiss: () -> Unit,
    onSave: (IngredientNutrition) -> Unit,
    onDelete: (() -> Unit)?
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var servingQuantity by remember { mutableStateOf(existing?.servingQuantity?.formatNumber() ?: "100") }
    var servingUnit by remember { mutableStateOf(existing?.servingUnit ?: "g") }
    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var calories by remember { mutableStateOf(existing?.caloriesPerServing?.formatNumber() ?: "0") }
    var protein by remember { mutableStateOf(existing?.proteinGramsPerServing?.formatNumber() ?: "0") }
    var carbs by remember { mutableStateOf(existing?.carbsGramsPerServing?.formatNumber() ?: "0") }
    var fat by remember { mutableStateOf(existing?.fatGramsPerServing?.formatNumber() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add ingredient" else "Edit ingredient") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ingredient name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = servingQuantity,
                        onValueChange = { servingQuantity = it },
                        label = { Text("Serving qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = unitDropdownExpanded,
                        onExpandedChange = { unitDropdownExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = servingUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            INGREDIENT_UNITS.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        servingUnit = unit
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
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
                if (onDelete != null) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        IngredientNutrition(
                            id = existing?.id ?: 0,
                            name = name.trim(),
                            servingQuantity = servingQuantity.toDoubleOrNull() ?: 0.0,
                            servingUnit = servingUnit.trim(),
                            caloriesPerServing = calories.toDoubleOrNull() ?: 0.0,
                            proteinGramsPerServing = protein.toDoubleOrNull() ?: 0.0,
                            carbsGramsPerServing = carbs.toDoubleOrNull() ?: 0.0,
                            fatGramsPerServing = fat.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                enabled = name.isNotBlank() && servingUnit.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun Double.formatNumber(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
