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
fun SupermarketsScreen(
    onBack: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var deleteId by remember { mutableStateOf<Int?>(null) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nuovo supermercato") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nome *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Indirizzo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.createSupermarket(nameInput.trim(), addressInput.ifBlank { null })
                            showDialog = false
                            nameInput = ""
                            addressInput = ""
                        }
                    }
                ) { Text("Aggiungi") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Annulla") }
            }
        )
    }

    if (deleteId != null) {
        ConfirmDeleteDialog(
            title = "Elimina supermercato",
            message = "Vuoi eliminare questo supermercato?",
            onConfirm = {
                viewModel.deleteSupermarket(deleteId!!)
                deleteId = null
            },
            onDismiss = { deleteId = null }
        )
    }

    Scaffold(
        topBar = { SpesaTopBar(title = "Supermercati", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        if (state.supermarkets.isEmpty()) {
            EmptyState("Nessun supermercato", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(state.supermarkets, key = { it.id }) { s ->
                    ItemCard(
                        title = s.name,
                        subtitle = s.address,
                        onClick = {},
                        onDelete = { deleteId = s.id }
                    )
                }
            }
        }
    }
}
