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
import com.example.mealplannerapp.data.local.Recipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipePickerSheet(
    recipes: List<Recipe>,
    onDismiss: () -> Unit,
    onConfirm: (Recipe, Double) -> Unit
) {
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var servingsText by remember { mutableStateOf("1") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Add a meal", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
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
                    Text("Add to plan")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
