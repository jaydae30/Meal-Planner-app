package com.example.mealplannerapp.ui.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mealplannerapp.data.local.MealSlot
import com.example.mealplannerapp.data.local.PlannedMealWithRecipeServings
import com.example.mealplannerapp.di.LocalAppContainer
import com.example.mealplannerapp.domain.datesUntilInclusive
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen() {
    val container = LocalAppContainer.current
    val viewModel: PlannerViewModel = viewModel {
        PlannerViewModel(
            container.mealPlanRepository,
            container.recipeRepository,
            container.settingsRepository,
            container.ingredientNutritionRepository
        )
    }

    val weekStart by viewModel.selectedWeekStart.collectAsState()
    val plannedMeals by viewModel.plannedMeals.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val storedIngredients by viewModel.storedIngredients.collectAsState()
    val dailyGoal by viewModel.dailyCalorieGoal.collectAsState()

    var pickerTarget by remember { mutableStateOf<Pair<LocalDate, MealSlot>?>(null) }
    var editingGoal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Meal Planner") }) }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            WeekHeader(
                weekStart = weekStart,
                onPrevious = viewModel::goToPreviousWeek,
                onNext = viewModel::goToNextWeek,
                onToday = viewModel::goToThisWeek
            )
            GoalRow(dailyGoal = dailyGoal, onEditGoal = { editingGoal = true })
            HorizontalDivider()
            val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
            val days = weekStart.datesUntilInclusive(weekEnd)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(days) { day ->
                    DayCard(
                        date = day,
                        mealsForDay = plannedMeals.filter {
                            LocalDate.fromEpochDays(it.plannedMeal.dateEpochDay.toInt()) == day
                        },
                        dailyGoal = dailyGoal,
                        onAddMeal = { slot -> pickerTarget = day to slot },
                        onRemoveMeal = { viewModel.removeMeal(it.plannedMeal) }
                    )
                }
            }
        }
    }

    pickerTarget?.let { (date, slot) ->
        RecipePickerSheet(
            recipes = recipes,
            storedIngredients = storedIngredients,
            onDismiss = { pickerTarget = null },
            onConfirm = { recipe, servings ->
                viewModel.addMeal(date, slot, recipe.id, servings)
                pickerTarget = null
            },
            onConfirmIngredient = { ingredient, quantity ->
                viewModel.addStoredIngredient(date, slot, ingredient, quantity)
                pickerTarget = null
            }
        )
    }

    if (editingGoal) {
        GoalEditDialog(
            currentGoal = dailyGoal,
            onDismiss = { editingGoal = false },
            onConfirm = { newGoal ->
                viewModel.setDailyCalorieGoal(newGoal)
                editingGoal = false
            }
        )
    }
}

@Composable
private fun GoalRow(dailyGoal: Double, onEditGoal: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Flag,
            contentDescription = null,
            modifier = Modifier.height(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            "Daily goal: ${dailyGoal.roundToInt()} kcal",
            style = MaterialTheme.typography.labelMedium
        )
        IconButton(onClick = onEditGoal, modifier = Modifier.height(32.dp)) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit daily calorie goal")
        }
    }
}

@Composable
private fun GoalEditDialog(
    currentGoal: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var text by remember { mutableStateOf(currentGoal.roundToInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily calorie goal") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Calories per day") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = {
                text.toDoubleOrNull()?.let(onConfirm)
            }) {
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

@Composable
private fun WeekHeader(
    weekStart: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous week")
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text(
                "${weekStart.formatShort()} – ${weekEnd.formatShort()}",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onToday) {
                Icon(Icons.Filled.Today, contentDescription = null, modifier = Modifier.height(16.dp))
                Spacer(Modifier.height(0.dp))
                Text("Today", style = MaterialTheme.typography.labelSmall)
            }
        }
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next week")
        }
    }
}

@Composable
private fun DayCard(
    date: LocalDate,
    mealsForDay: List<PlannedMealWithRecipeServings>,
    dailyGoal: Double,
    onAddMeal: (MealSlot) -> Unit,
    onRemoveMeal: (PlannedMealWithRecipeServings) -> Unit
) {
    val dayTotalCalories = mealsForDay.sumOf { it.totalCalories }
    val overGoal = dayTotalCalories > dailyGoal

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(date.formatFull(), style = MaterialTheme.typography.titleSmall)
                Text(
                    "${dayTotalCalories.roundToInt()} kcal total",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (overGoal) MaterialTheme.colorScheme.error else Color.Unspecified
                )
            }
            Spacer(Modifier.height(8.dp))
            MealSlot.entries.forEach { slot ->
                val mealsInSlot = mealsForDay.filter { it.plannedMeal.mealSlot == slot }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(slot.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelLarge)
                    IconButton(onClick = { onAddMeal(slot) }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add to $slot")
                    }
                }
                if (mealsInSlot.isEmpty()) {
                    Text(
                        "—",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                } else {
                    mealsInSlot.forEach { mealEntry ->
                        // Show meal with all its recipes
                        Column(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                val totalItems = mealEntry.recipeServings.size + mealEntry.mealIngredients.size
                                if (totalItems == 1 && mealEntry.recipeServings.size == 1) {
                                    // Single recipe meal - display inline as before
                                    val recipeServing = mealEntry.recipeServings.first()
                                    Text(
                                        "${recipeServing.recipe.name} (${recipeServing.servings.formatServings()} servings) · " +
                                            "${mealEntry.totalCalories.roundToInt()} kcal",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else if (totalItems == 1 && mealEntry.mealIngredients.size == 1) {
                                    // Single ingredient - display inline
                                    val ingredient = mealEntry.mealIngredients.first()
                                    Text(
                                        "${ingredient.name} (${ingredient.quantity.formatServings()} ${ingredient.unit}) · " +
                                            "${mealEntry.totalCalories.roundToInt()} kcal",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    // Multiple items - show summary
                                    Text(
                                        "$totalItems items · ${mealEntry.totalCalories.roundToInt()} kcal",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                IconButton(onClick = { onRemoveMeal(mealEntry) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove")
                                }
                            }
                            // For multiple items, show each on a separate line
                            if ((mealEntry.recipeServings.size + mealEntry.mealIngredients.size) > 1) {
                                mealEntry.recipeServings.forEach { recipeServing ->
                                    Text(
                                        "  • ${recipeServing.recipe.name} (${recipeServing.servings.formatServings()} servings)",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                                mealEntry.mealIngredients.forEach { ingredient ->
                                    Text(
                                        "  • ${ingredient.name} (${ingredient.quantity.formatServings()} ${ingredient.unit})",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val PlannedMealWithRecipeServings.totalCalories: Double
    get() {
        val recipeCalories = recipeServings.sumOf { it.servings * it.recipe.caloriesPerServing }
        val ingredientCalories = mealIngredients.sumOf { it.quantity * it.caloriesPerUnit }
        return recipeCalories + ingredientCalories
    }

private fun Double.formatServings(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()

private fun LocalDate.formatShort(): String = "${this.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${this.day}"

private fun LocalDate.formatFull(): String =
    "${this.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${this.formatShort()}"
