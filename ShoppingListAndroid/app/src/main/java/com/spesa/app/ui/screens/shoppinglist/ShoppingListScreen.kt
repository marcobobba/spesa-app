package com.spesa.app.ui.screens.shoppinglist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spesa.app.data.models.ShoppingListItemDto
import com.spesa.app.ui.components.EmptyState
import com.spesa.app.ui.components.ErrorMessage
import com.spesa.app.ui.components.LoadingOverlay
import com.spesa.app.ui.components.SpesaTopBar
import com.spesa.app.viewmodel.WeeklyPlanViewModel

@Composable
fun ShoppingListScreen(
    planId: Int,
    onBack: () -> Unit,
    viewModel: WeeklyPlanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val checkedItems = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(planId) {
        viewModel.loadShoppingList(planId)
    }

    Scaffold(
        topBar = {
            SpesaTopBar(
                title = state.shoppingList?.let { "Lista: ${it.weeklyPlanName}" } ?: "Lista della Spesa",
                onBack = onBack,
                actions = {
                    if (state.shoppingList != null) {
                        IconButton(onClick = { checkedItems.clear() }) {
                            Icon(Icons.Default.Refresh, "Reset", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingOverlay()
            state.error != null -> ErrorMessage(state.error!!, modifier = Modifier.padding(padding))
            state.shoppingList == null -> EmptyState("Lista non disponibile", modifier = Modifier.padding(padding))
            else -> {
                val list = state.shoppingList!!
                val grouped = list.items.groupBy { it.categoryName ?: "Senza categoria" }

                Column(modifier = Modifier.padding(padding)) {
                    // Summary
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${list.items.size} prodotti",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            val checkedCount = checkedItems.count { it.value }
                            Text(
                                "$checkedCount/${list.items.size} acquistati",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        LinearProgressIndicator(
                            progress = { if (list.items.isEmpty()) 0f else checkedItems.count { it.value }.toFloat() / list.items.size },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp)
                        )
                    }

                    if (list.items.isEmpty()) {
                        EmptyState("Nessun ingrediente nei pasti selezionati")
                    } else {
                        LazyColumn {
                            grouped.forEach { (category, items) ->
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Category,
                                            null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            category,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        HorizontalDivider(modifier = Modifier.weight(1f))
                                    }
                                }
                                items(items, key = { "${it.productId}_${it.unit}" }) { item ->
                                    ShoppingItemRow(
                                        item = item,
                                        isChecked = checkedItems[item.productId] == true,
                                        onCheckedChange = { checked -> checkedItems[item.productId] = checked }
                                    )
                                }
                            }
                            item { Spacer(Modifier.height(32.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingListItemDto,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val alpha = if (isChecked) 0.4f else 1f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        onClick = { onCheckedChange(!isChecked) },
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isChecked) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productDescription,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                    ),
                    fontWeight = if (!isChecked) FontWeight.Medium else FontWeight.Normal
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.productCode.isNotBlank()) {
                        Text(
                            "Cod: ${item.productCode}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                        )
                    }
                    if (item.supermarkets.isNotEmpty()) {
                        Text(
                            item.supermarkets.joinToString(", "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = alpha)
                        )
                    }
                }
                // Packages info
                if (item.packageWeight != null) {
                    val packages = Math.ceil(item.totalQuantity / item.packageWeight).toInt()
                    Text(
                        "≈ $packages confezioni da ${item.packageWeight}${item.weightUnit ?: ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = alpha)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            // Quantity badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isChecked) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${formatQuantity(item.totalQuantity)} ${item.unit}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatQuantity(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        "%.2f".format(value).trimEnd('0').trimEnd('.')
    }
}
