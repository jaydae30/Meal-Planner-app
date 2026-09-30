package com.example.mealplannerapp.ui.planner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mealplannerapp.data.local.IngredientNutrition
import com.example.mealplannerapp.data.local.Recipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipePickerSheet(
    recipes: List<Recipe>,
    storedIngredients: List<IngredientNutrition>,
    onDismiss: () -> Unit,
    onConfirm: (Recipe, Double) -> Unit,
    onConfirmIngredient: (IngredientNutrition, Double) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var selectedIngredient by remember { mutableStateOf<IngredientNutrition?>(null) }
    var servingsText by remember { mutableStateOf("1") }
    var ingredientQuantityText by remember { mutableStateOf("1") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Add to meal", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Recipe") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Ingredient") }
                )
            }

            Spacer(Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Recipe selection UI
                if (recipes.isEmpty()) {
                    Text("No recipes yet — add one from the Recipes tab first.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(recipes) { recipe ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedRecipe = recipe }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedRecipe?.id == recipe.id,
                                    onClick = { selectedRecipe = recipe }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(recipe.name)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = servingsText,
                        onValueChange = { servingsText = it },
                        label = { Text("Servings") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val servings = servingsText.toDoubleOrNull() ?: 1.0
                            selectedRecipe?.let { onConfirm(it, servings) }
                        },
                        enabled = selectedRecipe != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add recipe")
                    }
                }
            } else {
                // Stored ingredient selection UI
                if (storedIngredients.isEmpty()) {
                    Text("No ingredients yet — add one from the Ingredients tab first.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(storedIngredients) { ingredient ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedIngredient = ingredient }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedIngredient?.id == ingredient.id,
                                    onClick = { selectedIngredient = ingredient }
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(ingredient.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        buildString {
                                            append("${ingredient.caloriesPerServing.toInt()} kcal")
                                            if (ingredient.servingQuantity == 1.0) {
                                                append(" per ${ingredient.servingUnit}")
                                            } else {
                                                val qty = if (ingredient.servingQuantity == ingredient.servingQuantity.toInt().toDouble()) {
                                                    ingredient.servingQuantity.toInt().toString()
                                                } else {
                                                    ingredient.servingQuantity.toString()
                                                }
                                                append(" per $qty ${ingredient.servingUnit}")
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    selectedIngredient?.let { ingredient ->
                        Text(
                            buildString {
                                append("How many ")
                                if (ingredient.servingQuantity == 1.0) {
                                    append("${ingredient.servingUnit}s")
                                } else {
                                    append("${ingredient.servingUnit}")
                                }
                                append("?")
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    OutlinedTextField(
                        value = ingredientQuantityText,
                        onValueChange = { ingredientQuantityText = it },
                        label = {
                            val ingredient = selectedIngredient
                            Text(
                                if (ingredient != null) {
                                    "Quantity in ${ingredient.servingUnit}"
                                } else {
                                    "Select an ingredient first"
                                }
                            )
                        },
                        placeholder = {
                            val ingredient = selectedIngredient
                            if (ingredient != null) {
                                Text(
                                    if (ingredient.servingQuantity == 1.0) {
                                        "e.g., 1, 2, 3"
                                    } else {
                                        "e.g., ${ingredient.servingQuantity.toInt()}, ${(ingredient.servingQuantity * 2).toInt()}"
                                    }
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedIngredient != null
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val quantity = ingredientQuantityText.toDoubleOrNull() ?: 1.0
                            selectedIngredient?.let { onConfirmIngredient(it, quantity) }
                        },
                        enabled = selectedIngredient != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add ingredient")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
