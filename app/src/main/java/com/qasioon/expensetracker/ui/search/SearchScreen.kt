package com.qasioon.expensetracker.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qasioon.expensetracker.ExpenseApp
import com.qasioon.expensetracker.data.model.TransactionType
import com.qasioon.expensetracker.ui.components.EmptyState
import com.qasioon.expensetracker.ui.components.TransactionRow
import com.qasioon.expensetracker.util.Dates
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenTransaction: (Long) -> Unit,
    viewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.factory(LocalContext.current.applicationContext as ExpenseApp)
    )
) {
    val filters by viewModel.filters.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val results by viewModel.results.collectAsState()

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Search & Filter",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (filters.isActive) {
                TextButton(onClick = viewModel::clearAll) {
                    Text("Clear")
                }
            }
        }

        OutlinedTextField(
            value = filters.query,
            onValueChange = viewModel::setQuery,
            label = { Text("Search note, category, account, amount") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (filters.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setQuery("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = filters.type == null,
                onClick = { viewModel.setType(null) },
                label = { Text("All") }
            )
            FilterChip(
                selected = filters.type == TransactionType.EXPENSE,
                onClick = { viewModel.setType(TransactionType.EXPENSE) },
                label = { Text("Expense") }
            )
            FilterChip(
                selected = filters.type == TransactionType.INCOME,
                onClick = { viewModel.setType(TransactionType.INCOME) },
                label = { Text("Income") }
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { showFromPicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = filters.from?.let { "From ${Dates.formatShortDay(it)}" } ?: "From date",
                    maxLines = 1
                )
            }
            OutlinedButton(
                onClick = { showToPicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = filters.to?.let { "To ${Dates.formatShortDay(it)}" } ?: "To date",
                    maxLines = 1
                )
            }
        }

        if (categories.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.forEach { category ->
                    val selected = filters.categoryId == category.id
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setCategory(category.id) },
                        label = { Text(category.name) }
                    )
                }
            }
        }

        if (accounts.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                accounts.forEach { account ->
                    val selected = filters.accountId == account.id
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setAccount(account.id) },
                        label = { Text(account.name) }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "${results.size} result${if (results.size == 1) "" else "s"}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (results.isEmpty()) {
            EmptyState("No transactions match your filters")
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                items(results, key = { it.txn.id }) { item ->
                    TransactionRow(
                        item = item,
                        onClick = { onOpenTransaction(item.txn.id) }
                    )
                }
            }
        }
    }

    if (showFromPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = filters.from
                ?: Dates.millisOf(java.time.LocalDate.now().minusDays(30))
        )
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            viewModel.setFrom(
                                Dates.millisOf(
                                    Instant.ofEpochMilli(millis)
                                        .atZone(ZoneOffset.UTC)
                                        .toLocalDate()
                                )
                            )
                        }
                        showFromPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showToPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = filters.to ?: Dates.millisOf(java.time.LocalDate.now())
        )
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            viewModel.setTo(
                                Dates.millisOf(
                                    Instant.ofEpochMilli(millis)
                                        .atZone(ZoneOffset.UTC)
                                        .toLocalDate()
                                )
                            )
                        }
                        showToPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
