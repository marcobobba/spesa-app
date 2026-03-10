package com.spesa.app.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spesa.app.ui.components.*
import com.spesa.app.viewmodel.ProductViewModel

@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf("") }
    var deleteId by remember { mutableStateOf<Int?>(null) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false; editingName = "" },
            title = { Text("Nuova categoria") },
            text = {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    label = { Text("Nome categoria") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editingName.isNotBlank()) {
                            viewModel.createCategory(editingName.trim())
                            showDialog = false
                            editingName = ""
                        }
                    }
                ) { Text("Aggiungi") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false; editingName = "" }) { Text("Annulla") }
            }
        )
    }

    if (deleteId != null) {
        ConfirmDeleteDialog(
            title = "Elimina categoria",
            message = "Vuoi eliminare questa categoria?",
            onConfirm = {
                viewModel.deleteCategory(deleteId!!)
                deleteId = null
            },
            onDismiss = { deleteId = null }
        )
    }

    Scaffold(
        topBar = { SpesaTopBar(title = "Categorie", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, "Aggiungi categoria")
            }
        }
    ) { padding ->
        if (state.categories.isEmpty()) {
            EmptyState("Nessuna categoria", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(state.categories, key = { it.id }) { cat ->
                    ItemCard(
                        title = cat.name,
                        onClick = {},
                        onDelete = { deleteId = cat.id }
                    )
                }
            }
        }
    }
}
