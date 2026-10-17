package com.qasioon.expensetracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qasioon.expensetracker.ExpenseApp
import com.qasioon.expensetracker.data.model.TransactionType
import com.qasioon.expensetracker.ui.components.EmptyState
import com.qasioon.expensetracker.ui.components.PeriodSelector
import com.qasioon.expensetracker.ui.components.SectionHeader
import com.qasioon.expensetracker.ui.components.SummaryCard
import com.qasioon.expensetracker.ui.components.TransactionRow
import com.qasioon.expensetracker.ui.components.accountIcon
import com.qasioon.expensetracker.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAdd: (TransactionType, Long) -> Unit,
    onOpenTransaction: (Long) -> Unit,
    onOpenAccount: (Long) -> Unit,
    onSearch: () -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(LocalContext.current.applicationContext as ExpenseApp)
    )
) {
    val state by viewModel.uiState.collectAsState()
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "Expense Tracker",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onSearch) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            }

            PeriodSelector(
                label = state.periodLabel,
                mode = state.mode,
                onPrevious = viewModel::previousPeriod,
                onNext = viewModel::nextPeriod,
                onModeChange = viewModel::setMode
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryCard(
                            title = "Income",
                            amount = state.income,
                            icon = Icons.Filled.ArrowDownward,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Expenses",
                            amount = state.expenses,
                            icon = Icons.Filled.ArrowUpward,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    BudgetCard(
                        spent = state.expenses,
                        budget = state.budget,
                        balance = state.balance,
                        onClick = { showBudgetDialog = true }
                    )
                }

                item {
                    SectionHeader("Accounts")
                }

                item {
                    if (state.accounts.isEmpty()) {
                        Text(
                            text = "No accounts yet. Add one in the Accounts tab.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.accounts, key = { it.account.id }) { summary ->
                                AccountCard(
                                    summary = summary,
                                    onClick = { onOpenAccount(summary.account.id) }
                                )
                            }
                        }
                    }
                }

                item {
                    SectionHeader("Transactions")
                }

                if (state.items.isEmpty()) {
                    item {
                        EmptyState("No transactions in ${state.periodLabel}")
                    }
                } else {
                    items(state.items, key = { it.txn.id }) { item ->
                        TransactionRow(
                            item = item,
                            onClick = { onOpenTransaction(item.txn.id) }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = { showAddMenu = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add") }
            )
            DropdownMenu(
                expanded = showAddMenu,
                onDismissRequest = { showAddMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Expense") },
                    leadingIcon = { Icon(Icons.Filled.ArrowUpward, contentDescription = null) },
                    onClick = {
                        showAddMenu = false
                        onAdd(TransactionType.EXPENSE, -1L)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Income") },
                    leadingIcon = { Icon(Icons.Filled.ArrowDownward, contentDescription = null) },
                    onClick = {
                        showAddMenu = false
                        onAdd(TransactionType.INCOME, -1L)
                    }
                )
            }
        }
    }

    if (showBudgetDialog) {
        BudgetDialog(
            current = state.budget,
            onDismiss = { showBudgetDialog = false },
            onSave = { amount ->
                viewModel.setBudget(amount)
                showBudgetDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetCard(
    spent: Double,
    budget: Double,
    balance: Double,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.SouthEast,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = "Monthly Budget",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (budget > 0) "Balance ${Money.format(balance)}" else "Set budget",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            if (budget > 0) {
                val progress = (spent / budget).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${Money.format(spent)} of ${Money.format(budget)} spent • " +
                        "Left ${Money.format((budget - spent).coerceAtLeast(0.0))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Tap to set a monthly spending budget",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountCard(
    summary: com.qasioon.expensetracker.ui.model.AccountSummary,
    onClick: () -> Unit
) {
    val account = summary.account
    Card(onClick = onClick, modifier = Modifier.width(170.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = accountIcon(account.type),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = Money.format(summary.spent),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (account.type == com.qasioon.expensetracker.data.model.AccountType.CREDIT_CARD && account.creditLimit != null) {
                    "Limit ${Money.format(account.creditLimit)}"
                } else {
                    "Spent in period"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BudgetDialog(
    current: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var text by remember {
        mutableStateOf(if (current > 0) current.toBigDecimal().toPlainString() else "")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly Budget") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) {
                        text = input
                    }
                },
                label = { Text("Amount") },
                prefix = { Text("₹ ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    text.toDoubleOrNull()?.let { if (it >= 0) onSave(it) }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
