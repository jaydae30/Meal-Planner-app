package com.example.mealplannerapp.ui.recipes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mealplannerapp.data.local.RecipeWithIngredients
import com.example.mealplannerapp.di.LocalAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onAddRecipe: () -> Unit,
    onEditRecipe: (Long) -> Unit,
    onOpenIngredientCatalog: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: RecipeViewModel = viewModel { RecipeViewModel(container.recipeRepository) }

    val recipes by viewModel.recipes.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    val filtered = remember(recipes, query) {
        if (query.isBlank()) {
            recipes
        } else {
            recipes.filter { item ->
                item.recipe.name.contains(query, ignoreCase = true) ||
                    item.recipe.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipes") },
                actions = {
                    IconButton(onClick = onOpenIngredientCatalog) {
                        Icon(Icons.Filled.Scale, contentDescription = "Ingredient nutrition catalog")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecipe) {
                Icon(Icons.Filled.Add, contentDescription = "Add recipe")
            }
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setSearchQuery,
                label = { Text("Search recipes or tags") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (recipes.isEmpty()) "No recipes yet. Tap + to add one." else "No matches.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.recipe.id }) { item ->
                        RecipeRow(item, onClick = { onEditRecipe(item.recipe.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeRow(item: RecipeWithIngredients, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(12.dp)) {
            Text(item.recipe.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${item.recipe.servings} servings · ${item.recipe.caloriesPerServing.toInt()} kcal/serving",
                style = MaterialTheme.typography.bodySmall
            )
            if (item.recipe.tags.isNotEmpty()) {
                Text(item.recipe.tags.joinToString(", "), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
