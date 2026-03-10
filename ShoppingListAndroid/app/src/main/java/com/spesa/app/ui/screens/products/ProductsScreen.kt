package com.spesa.app.ui.screens.products

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
import com.spesa.app.viewmodel.ProductViewModel

@Composable
fun ProductsScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSupermarkets: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var deleteId by remember { mutableStateOf<Int?>(null) }

    if (deleteId != null) {
        ConfirmDeleteDialog(
            title = "Elimina prodotto",
            message = "Vuoi eliminare questo prodotto?",
            onConfirm = {
                viewModel.deleteProduct(deleteId!!)
                deleteId = null
            },
            onDismiss = { deleteId = null }
        )
    }

    Scaffold(
        topBar = {
            SpesaTopBar(
                title = "Prodotti",
                actions = {
                    IconButton(onClick = onNavigateToCategories) {
                        Icon(Icons.Default.Category, "Categorie", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onNavigateToSupermarkets) {
                        Icon(Icons.Default.Store, "Supermercati", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreate,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nuovo prodotto") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchProducts(it)
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Cerca prodotto...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.loadAll()
                        }) { Icon(Icons.Default.Clear, null) }
                    }
                },
                singleLine = true
            )

            if (state.isLoading) {
                LoadingOverlay()
            } else if (state.error != null) {
                ErrorMessage(state.error!!)
            } else if (state.products.isEmpty()) {
                EmptyState("Nessun prodotto trovato.\nAggiungi il tuo primo prodotto!")
            } else {
                LazyColumn {
                    items(state.products, key = { it.id }) { product ->
                        ItemCard(
                            title = product.description,
                            subtitle = buildString {
                                if (product.code.isNotBlank()) append("Codice: ${product.code}  ")
                                if (product.categoryName != null) append("• ${product.categoryName}")
                                if (product.packageWeight != null) append("  • ${product.packageWeight}${product.weightUnit ?: ""}")
                                if (product.supermarketNames.isNotEmpty()) {
                                    append("\n${product.supermarketNames.joinToString(", ")}")
                                }
                            }.trim(),
                            badge = product.categoryName,
                            onClick = { onNavigateToDetail(product.id) },
                            onDelete = { deleteId = product.id }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}
