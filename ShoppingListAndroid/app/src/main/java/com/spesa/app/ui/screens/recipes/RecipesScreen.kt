package com.spesa.app.ui.screens.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spesa.app.ui.components.*
import com.spesa.app.viewmodel.RecipeViewModel

@Composable
fun RecipesScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var deleteId by remember { mutableStateOf<Int?>(null) }

    if (deleteId != null) {
        ConfirmDeleteDialog(
            title = "Elimina ricetta",
            message = "Vuoi eliminare questa ricetta?",
            onConfirm = {
                viewModel.deleteRecipe(deleteId!!)
                deleteId = null
            },
            onDismiss = { deleteId = null }
        )
    }

    Scaffold(
        topBar = { SpesaTopBar(title = "Ricette") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreate,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nuova ricetta") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Cerca ricetta...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                singleLine = true
            )

            if (state.isLoading) {
                LoadingOverlay()
            } else if (state.error != null) {
                ErrorMessage(state.error!!)
            } else {
                val filtered = state.recipes.filter {
                    searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                }
                if (filtered.isEmpty()) {
                    EmptyState("Nessuna ricetta trovata.\nCrea la tua prima ricetta!")
                } else {
                    LazyColumn {
                        items(filtered, key = { it.id }) { recipe ->
                            ItemCard(
                                title = recipe.name,
                                subtitle = buildString {
                                    if (recipe.description != null) appendLine(recipe.description)
                                    append("${recipe.servings} porzioni • ${recipe.ingredients.size} ingredienti")
                                }.trim(),
                                badge = "${recipe.servings}p",
                                onClick = { onNavigateToDetail(recipe.id) },
                                onDelete = { deleteId = recipe.id }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}
