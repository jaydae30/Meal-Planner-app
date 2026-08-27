package com.example.mealplannerapp.ui.grocery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mealplannerapp.di.LocalAppContainer
import com.example.mealplannerapp.domain.GroceryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen() {
    val container = LocalAppContainer.current
    val viewModel: GroceryListViewModel = viewModel { GroceryListViewModel(container.mealPlanRepository) }

    val rangeStart by viewModel.rangeStart.collectAsState()
    val items by viewModel.groceryItems.collectAsState()
    val checkedKeys by viewModel.checkedKeys.collectAsState()
    val rangeEnd by viewModel.rangeEnd.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Grocery List") }) }) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = true,
                    onClick = { viewModel.selectThisWeek() },
                    label = { Text("This week") }
                )
                FilterChip(
                    selected = false,
                    onClick = { viewModel.selectNextWeek() },
                    label = { Text("Next week") }
                )
            }
            Text(
                "${rangeStart} – ${rangeEnd}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No meals planned for this range yet.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items, key = { it.key }) { item ->
                        GroceryRow(
                            item = item,
                            checked = item.key in checkedKeys,
                            onToggle = { viewModel.toggleChecked(item.key) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroceryRow(item: GroceryItem, checked: Boolean, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
            Column {
                Text(
                    "${formatQuantity(item.totalQuantity)} ${item.unit} ${item.displayName}".trim(),
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                )
                if (item.sourceRecipeNames.isNotEmpty()) {
                    Text(
                        "from: ${item.sourceRecipeNames.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

private fun formatQuantity(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
