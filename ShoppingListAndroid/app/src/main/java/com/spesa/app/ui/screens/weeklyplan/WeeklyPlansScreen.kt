package com.spesa.app.ui.screens.weeklyplan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spesa.app.ui.components.*
import com.spesa.app.viewmodel.WeeklyPlanViewModel

@Composable
fun WeeklyPlansScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToShopping: (Int) -> Unit,
    viewModel: WeeklyPlanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var deleteId by remember { mutableStateOf<Int?>(null) }
    var duplicateId by remember { mutableStateOf<Int?>(null) }
    var duplicateName by remember { mutableStateOf("") }

    if (deleteId != null) {
        ConfirmDeleteDialog(
            title = "Elimina piano",
            message = "Vuoi eliminare questo piano settimanale?",
            onConfirm = {
                viewModel.deletePlan(deleteId!!)
                deleteId = null
            },
            onDismiss = { deleteId = null }
        )
    }

    if (duplicateId != null) {
        AlertDialog(
            onDismissRequest = { duplicateId = null },
            title = { Text("Duplica piano") },
            text = {
                OutlinedTextField(
                    value = duplicateName,
                    onValueChange = { duplicateName = it },
                    label = { Text("Nome nuovo piano") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (duplicateName.isNotBlank()) {
                            viewModel.duplicatePlan(duplicateId!!, duplicateName.trim())
                            duplicateId = null
                            duplicateName = ""
                        }
                    }
                ) { Text("Duplica") }
            },
            dismissButton = {
                TextButton(onClick = { duplicateId = null }) { Text("Annulla") }
            }
        )
    }

    Scaffold(
        topBar = { SpesaTopBar(title = "Piani Settimanali") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreate,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nuovo piano") }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingOverlay()
        } else if (state.error != null) {
            ErrorMessage(state.error!!, modifier = Modifier.padding(padding))
        } else if (state.plans.isEmpty()) {
            EmptyState(
                "Nessun piano settimanale.\nCrea il tuo primo piano!",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(state.plans, key = { it.id }) { plan ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        onClick = { onNavigateToDetail(plan.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(plan.name, style = MaterialTheme.typography.titleMedium)
                                    if (plan.description != null) {
                                        Text(
                                            plan.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row {
                                    // Shopping list button
                                    IconButton(onClick = { onNavigateToShopping(plan.id) }) {
                                        Icon(
                                            Icons.Default.ShoppingCart,
                                            contentDescription = "Lista spesa",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    // Duplicate button
                                    IconButton(onClick = {
                                        duplicateId = plan.id
                                        duplicateName = "Copia di ${plan.name}"
                                    }) {
                                        Icon(
                                            Icons.Default.CopyAll,
                                            contentDescription = "Duplica",
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    // Delete button
                                    IconButton(onClick = { deleteId = plan.id }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Elimina",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
