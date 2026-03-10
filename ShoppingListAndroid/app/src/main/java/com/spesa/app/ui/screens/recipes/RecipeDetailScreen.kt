package com.spesa.app.ui.screens.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spesa.app.data.models.CreateRecipeDto
import com.spesa.app.data.models.CreateRecipeIngredientDto
import com.spesa.app.ui.components.LoadingOverlay
import com.spesa.app.ui.components.SpesaTopBar
import com.spesa.app.viewmodel.RecipeViewModel

data class IngredientEntry(
    val productId: Int = -1,
    val productName: String = "",
    val quantity: String = "",
    val unit: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Int?,
    onBack: () -> Unit,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isEditing = recipeId != null

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("1") }
    var ingredients by remember { mutableStateOf(listOf<IngredientEntry>()) }
    var initialized by remember { mutableStateOf(false) }
    var showProductPicker by remember { mutableStateOf<Int?>(null) }

    val units = listOf("g", "kg", "ml", "l", "pz", "cucchiai", "cucchiaini", "tazze", "q.b.")

    LaunchedEffect(recipeId) {
        if (isEditing) viewModel.loadRecipe(recipeId!!)
    }

    LaunchedEffect(state.selectedRecipe) {
        val r = state.selectedRecipe
        if (r != null && isEditing && !initialized) {
            name = r.name
            description = r.description ?: ""
            servings = r.servings.toString()
            ingredients = r.ingredients.map { ing ->
                IngredientEntry(
                    productId = ing.productId,
                    productName = ing.productDescription,
                    quantity = ing.quantity.toString(),
                    unit = ing.unit
                )
            }
            initialized = true
        }
    }

    // Product picker dialog
    if (showProductPicker != null) {
        val idx = showProductPicker!!
        var search by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showProductPicker = null },
            title = { Text("Seleziona prodotto") },
            text = {
                Column {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Cerca...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        val filtered = state.products.filter {
                            search.isBlank() || it.description.contains(search, ignoreCase = true)
                        }
                        items(filtered.size) { i ->
                            val p = filtered[i]
                            TextButton(
                                onClick = {
                                    ingredients = ingredients.toMutableList().also { list ->
                                        list[idx] = list[idx].copy(productId = p.id, productName = p.description)
                                    }
                                    showProductPicker = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "${p.description}${if (p.code.isNotBlank()) " (${p.code})" else ""}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProductPicker = null }) { Text("Chiudi") }
            }
        )
    }

    Scaffold(
        topBar = {
            SpesaTopBar(
                title = if (isEditing) "Modifica Ricetta" else "Nuova Ricetta",
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
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome ricetta *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrizione") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            OutlinedTextField(
                value = servings,
                onValueChange = { servings = it },
                label = { Text("Porzioni") },
                modifier = Modifier.fillMaxWidth(0.4f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Ingredients section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ingredienti", style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(
                    onClick = { ingredients = ingredients + IngredientEntry() }
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Aggiungi")
                }
            }

            ingredients.forEachIndexed { idx, ingredient ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ingrediente ${idx + 1}", style = MaterialTheme.typography.labelMedium)
                            IconButton(
                                onClick = { ingredients = ingredients.toMutableList().also { it.removeAt(idx) } }
                            ) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        OutlinedTextField(
                            value = ingredient.productName.ifBlank { "Seleziona prodotto..." },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Prodotto *") },
                            trailingIcon = {
                                IconButton(onClick = { showProductPicker = idx }) {
                                    Icon(Icons.Default.Search, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isError = ingredient.productId == -1 && ingredient.productName.isBlank()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = ingredient.quantity,
                                onValueChange = { v ->
                                    ingredients = ingredients.toMutableList().also { it[idx] = it[idx].copy(quantity = v) }
                                },
                                label = { Text("Quantità *") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )

                            var unitExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = unitExpanded,
                                onExpandedChange = { unitExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = ingredient.unit,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Unità *") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = unitExpanded,
                                    onDismissRequest = { unitExpanded = false }
                                ) {
                                    units.forEach { u ->
                                        DropdownMenuItem(
                                            text = { Text(u) },
                                            onClick = {
                                                ingredients = ingredients.toMutableList().also { it[idx] = it[idx].copy(unit = u) }
                                                unitExpanded = false
                                            }
                                        )
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
                    val dto = CreateRecipeDto(
                        name = name.trim(),
                        description = description.ifBlank { null },
                        servings = servings.toIntOrNull() ?: 1,
                        ingredients = ingredients.mapNotNull { ing ->
                            if (ing.productId == -1 || ing.quantity.isBlank() || ing.unit.isBlank()) null
                            else CreateRecipeIngredientDto(ing.productId, ing.quantity.toDoubleOrNull() ?: return@mapNotNull null, ing.unit)
                        }
                    )
                    if (isEditing) viewModel.updateRecipe(recipeId!!, dto) { onBack() }
                    else viewModel.createRecipe(dto) { onBack() }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = name.isNotBlank() && !state.isLoading
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) "Aggiorna ricetta" else "Salva ricetta")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
