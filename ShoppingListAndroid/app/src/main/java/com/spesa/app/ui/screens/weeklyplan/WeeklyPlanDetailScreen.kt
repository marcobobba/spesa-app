package com.spesa.app.ui.screens.weeklyplan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spesa.app.data.models.CreateWeeklyPlanDayDto
import com.spesa.app.data.models.CreateWeeklyPlanDayRecipeDto
import com.spesa.app.data.models.CreateWeeklyPlanDto
import com.spesa.app.ui.components.LoadingOverlay
import com.spesa.app.ui.components.SpesaTopBar
import com.spesa.app.viewmodel.WeeklyPlanViewModel

data class MealEntry(
    val dayOfWeek: Int,
    val mealType: String,
    val recipes: MutableList<Pair<Int, Int>> = mutableListOf() // recipeId, servings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyPlanDetailScreen(
    planId: Int?,
    onBack: () -> Unit,
    onSuccess: (Int) -> Unit,
    viewModel: WeeklyPlanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isEditing = planId != null

    val dayNames = listOf("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica")
    val mealTypes = listOf("Colazione", "Pranzo", "Cena", "Spuntino")

    var planName by remember { mutableStateOf("") }
    var planDescription by remember { mutableStateOf("") }
    var meals by remember { mutableStateOf(mutableListOf<MealEntry>()) }
    var initialized by remember { mutableStateOf(false) }
    var showAddMeal by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf(0) }
    var selectedMealType by remember { mutableStateOf("Pranzo") }
    var dayExpanded by remember { mutableStateOf(false) }
    var mealExpanded by remember { mutableStateOf(false) }
    var showRecipePicker by remember { mutableStateOf<Int?>(null) } // index in meals

    LaunchedEffect(planId) {
        if (isEditing) viewModel.loadPlan(planId!!)
    }

    LaunchedEffect(state.selectedPlan) {
        val p = state.selectedPlan
        if (p != null && isEditing && !initialized) {
            planName = p.name
            planDescription = p.description ?: ""
            meals = p.days.groupBy { Pair(it.dayOfWeek, it.mealType) }
                .map { (key, days) ->
                    MealEntry(
                        dayOfWeek = key.first,
                        mealType = key.second,
                        recipes = days.flatMap { d -> d.recipes.map { Pair(it.recipeId, it.servings) } }.toMutableList()
                    )
                }.toMutableList()
            initialized = true
        }
    }

    if (showAddMeal) {
        AlertDialog(
            onDismissRequest = { showAddMeal = false },
            title = { Text("Aggiungi pasto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(expanded = dayExpanded, onExpandedChange = { dayExpanded = it }) {
                        OutlinedTextField(
                            value = dayNames[selectedDay],
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Giorno") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                            dayNames.forEachIndexed { i, d ->
                                DropdownMenuItem(text = { Text(d) }, onClick = { selectedDay = i; dayExpanded = false })
                            }
                        }
                    }
                    ExposedDropdownMenuBox(expanded = mealExpanded, onExpandedChange = { mealExpanded = it }) {
                        OutlinedTextField(
                            value = selectedMealType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pasto") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mealExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = mealExpanded, onDismissRequest = { mealExpanded = false }) {
                            mealTypes.forEach { mt ->
                                DropdownMenuItem(text = { Text(mt) }, onClick = { selectedMealType = mt; mealExpanded = false })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val newMeals = meals.toMutableList()
                    newMeals.add(MealEntry(selectedDay, selectedMealType))
                    meals = newMeals
                    showAddMeal = false
                }) { Text("Aggiungi") }
            },
            dismissButton = { TextButton(onClick = { showAddMeal = false }) { Text("Annulla") } }
        )
    }

    // Recipe picker
    if (showRecipePicker != null) {
        val mealIdx = showRecipePicker!!
        var servings by remember { mutableStateOf("1") }
        var selectedRecipeId by remember { mutableStateOf(-1) }
        var search by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRecipePicker = null },
            title = { Text("Aggiungi ricetta al pasto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = search, onValueChange = { search = it },
                        placeholder = { Text("Cerca ricetta...") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        val filtered = state.recipes.filter { search.isBlank() || it.name.contains(search, ignoreCase = true) }
                        items(filtered.size) { i ->
                            val r = filtered[i]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedRecipeId == r.id, onClick = { selectedRecipeId = r.id })
                                Text(r.name)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = servings, onValueChange = { servings = it },
                        label = { Text("Porzioni") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.4f)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedRecipeId != -1) {
                            val newMeals = meals.toMutableList()
                            val meal = newMeals[mealIdx]
                            val updatedRecipes = meal.recipes.toMutableList()
                            updatedRecipes.add(Pair(selectedRecipeId, servings.toIntOrNull() ?: 1))
                            newMeals[mealIdx] = meal.copy(recipes = updatedRecipes)
                            meals = newMeals
                        }
                        showRecipePicker = null
                    }
                ) { Text("Aggiungi") }
            },
            dismissButton = { TextButton(onClick = { showRecipePicker = null }) { Text("Annulla") } }
        )
    }

    Scaffold(
        topBar = {
            SpesaTopBar(
                title = if (isEditing) "Modifica Piano" else "Nuovo Piano",
                onBack = onBack
            )
        }
    ) { padding ->
        if (state.isLoading && isEditing && !initialized) {
            LoadingOverlay()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = planName, onValueChange = { planName = it },
                label = { Text("Nome piano *") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = planDescription, onValueChange = { planDescription = it },
                label = { Text("Descrizione") },
                modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pasti", style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(onClick = { showAddMeal = true }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Aggiungi pasto")
                }
            }

            val grouped = meals
                .sortedWith(compareBy({ it.dayOfWeek }, { mealTypes.indexOf(it.mealType) }))
                .groupBy { it.dayOfWeek }

            grouped.forEach { (day, dayMeals) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(dayNames[day], style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)

                        dayMeals.forEachIndexed { _, meal ->
                            val mealIdx = meals.indexOf(meal)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(meal.mealType, style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary)
                                        Row {
                                            IconButton(
                                                onClick = { showRecipePicker = mealIdx },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    val newMeals = meals.toMutableList()
                                                    newMeals.removeAt(mealIdx)
                                                    meals = newMeals
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }

                                    if (meal.recipes.isEmpty()) {
                                        Text("Nessuna ricetta - tappa + per aggiungere",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else {
                                        meal.recipes.forEachIndexed { rIdx, (recipeId, servingsCount) ->
                                            val recipeName = state.recipes.find { it.id == recipeId }?.name ?: "Ricetta $recipeId"
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("• $recipeName ($servingsCount p.)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.weight(1f))
                                                IconButton(
                                                    onClick = {
                                                        val newMeals = meals.toMutableList()
                                                        val updatedRecipes = meal.recipes.toMutableList()
                                                        updatedRecipes.removeAt(rIdx)
                                                        newMeals[mealIdx] = meal.copy(recipes = updatedRecipes)
                                                        meals = newMeals
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp),
                                                        tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val dto = CreateWeeklyPlanDto(
                        name = planName.trim(),
                        description = planDescription.ifBlank { null },
                        days = meals.map { meal ->
                            CreateWeeklyPlanDayDto(
                                dayOfWeek = meal.dayOfWeek,
                                mealType = meal.mealType,
                                recipes = meal.recipes.map { (rId, s) ->
                                    CreateWeeklyPlanDayRecipeDto(rId, s)
                                }
                            )
                        }
                    )
                    if (isEditing) {
                        viewModel.updatePlan(planId!!, dto) { onBack() }
                    } else {
                        viewModel.createPlan(dto) { id -> onSuccess(id) }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = planName.isNotBlank() && !state.isLoading
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) "Aggiorna piano" else "Salva piano")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
