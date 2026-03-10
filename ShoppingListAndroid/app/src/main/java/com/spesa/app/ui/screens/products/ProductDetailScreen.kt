package com.spesa.app.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spesa.app.data.models.CreateProductDto
import com.spesa.app.ui.components.LoadingOverlay
import com.spesa.app.ui.components.SpesaTopBar
import com.spesa.app.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int?,
    onBack: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isEditing = productId != null

    var code by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var packageWeight by remember { mutableStateOf("") }
    var weightUnit by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedSupermarketIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    val weightUnits = listOf("g", "kg", "ml", "l", "pz")

    LaunchedEffect(productId) {
        if (isEditing) viewModel.loadProduct(productId!!)
    }

    LaunchedEffect(state.selectedProduct) {
        val p = state.selectedProduct
        if (p != null && isEditing && !initialized) {
            code = p.code
            description = p.description
            packageWeight = p.packageWeight?.toString() ?: ""
            weightUnit = p.weightUnit ?: ""
            selectedCategoryId = p.categoryId
            selectedSupermarketIds = p.supermarketIds.toSet()
            initialized = true
        }
    }

    Scaffold(
        topBar = {
            SpesaTopBar(
                title = if (isEditing) "Modifica Prodotto" else "Nuovo Prodotto",
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
            // Code
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Codice prodotto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrizione *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Package weight + unit
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = packageWeight,
                    onValueChange = { packageWeight = it },
                    label = { Text("Peso confezione") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier.weight(1f)
                ) {
                    var unitExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = weightUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unità") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            weightUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = { weightUnit = unit; unitExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                val catName = state.categories.find { it.id == selectedCategoryId }?.name ?: "Nessuna categoria"
                OutlinedTextField(
                    value = catName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Nessuna categoria") },
                        onClick = { selectedCategoryId = null; categoryExpanded = false }
                    )
                    state.categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = { selectedCategoryId = cat.id; categoryExpanded = false }
                        )
                    }
                }
            }

            // Supermarkets
            Text("Supermercati", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (state.supermarkets.isEmpty()) {
                        Text(
                            "Nessun supermercato disponibile",
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        state.supermarkets.forEach { supermarket ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = supermarket.id in selectedSupermarketIds,
                                    onCheckedChange = { checked ->
                                        selectedSupermarketIds = if (checked)
                                            selectedSupermarketIds + supermarket.id
                                        else
                                            selectedSupermarketIds - supermarket.id
                                    }
                                )
                                Text(supermarket.name)
                            }
                        }
                    }
                }
            }

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            // Save button
            Button(
                onClick = {
                    val dto = CreateProductDto(
                        code = code.trim(),
                        description = description.trim(),
                        packageWeight = packageWeight.toDoubleOrNull(),
                        weightUnit = weightUnit.ifBlank { null },
                        categoryId = selectedCategoryId,
                        supermarketIds = selectedSupermarketIds.toList()
                    )
                    if (isEditing) {
                        viewModel.updateProduct(productId!!, dto) { onBack() }
                    } else {
                        viewModel.createProduct(dto) { onBack() }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = description.isNotBlank() && !state.isLoading
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) "Aggiorna" else "Salva")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
