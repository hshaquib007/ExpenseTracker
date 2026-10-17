package com.qasioon.expensetracker.ui.accounts

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.qasioon.expensetracker.data.entity.AccountEntity
import com.qasioon.expensetracker.data.model.AccountType
import com.qasioon.expensetracker.ui.components.EmptyState
import com.qasioon.expensetracker.ui.components.accountIcon
import com.qasioon.expensetracker.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    onOpenAccount: (Long) -> Unit,
    viewModel: AccountsViewModel = viewModel(
        factory = AccountsViewModel.factory(LocalContext.current.applicationContext as ExpenseApp)
    )
) {
    val accounts by viewModel.accounts.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var deletingAccount by remember { mutableStateOf<AccountEntity?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Accounts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
            Text(
                text = "Payment sources: Cash, Bank accounts and Credit Cards",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (accounts.isEmpty()) {
                EmptyState("No accounts yet. Tap + to add one.")
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                    AccountType.entries.forEach { type ->
                        val ofType = accounts.filter { it.type == type }
                        if (ofType.isNotEmpty()) {
                            item(key = "header_${type.name}") {
                                Text(
                                    text = type.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(ofType, key = { it.id }) { account ->
                                AccountItem(
                                    account = account,
                                    onClick = { onOpenAccount(account.id) },
                                    onEdit = {
                                        editingAccount = account
                                        showDialog = true
                                    },
                                    onDelete = { deletingAccount = account }
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingAccount = null
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add account")
        }
    }

    if (showDialog) {
        AccountDialog(
            initial = editingAccount,
            onDismiss = { showDialog = false },
            onSave = { name, type, limit, dueDay ->
                val current = editingAccount
                if (current == null) {
                    viewModel.add(name, type, limit, dueDay)
                } else {
                    viewModel.update(current.id, name, type, limit, dueDay)
                }
                showDialog = false
            }
        )
    }

    deletingAccount?.let { account ->
        AlertDialog(
            onDismissRequest = { deletingAccount = null },
            title = { Text("Delete ${account.name}?") },
            text = {
                Text("All transactions linked to this account will also be deleted.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(account)
                        deletingAccount = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingAccount = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountItem(
    account: AccountEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = accountIcon(account.type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                val subtitle = buildString {
                    if (account.type == AccountType.CREDIT_CARD) {
                        account.creditLimit?.let { append("Limit ${Money.format(it)}") }
                        if (account.creditLimit != null) append(" • ")
                        account.billDueDay?.let { append("Bill due day $it") }
                    } else {
                        append(account.type.label)
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDialog(
    initial: AccountEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, type: AccountType, limit: Double?, dueDay: Int?) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: AccountType.CASH) }
    var limitText by remember {
        mutableStateOf(
            initial?.creditLimit?.toBigDecimal()?.toPlainString() ?: ""
        )
    }
    var dueDayText by remember { mutableStateOf(initial?.billDueDay?.toString() ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Account" else "Edit Account") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Account name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountType.entries.forEach { entry ->
                        FilterChip(
                            selected = type == entry,
                            onClick = { type = entry; error = null },
                            label = {
                                Text(
                                    text = when (entry) {
                                        AccountType.CASH -> "Cash"
                                        AccountType.BANK -> "Bank"
                                        AccountType.CREDIT_CARD -> "Credit"
                                    }
                                )
                            }
                        )
                    }
                }
                if (type == AccountType.CREDIT_CARD) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) {
                                limitText = input
                                error = null
                            }
                        },
                        label = { Text("Credit limit (optional)") },
                        prefix = { Text("₹ ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dueDayText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 2) {
                                dueDayText = input
                                error = null
                            }
                        },
                        label = { Text("Bill due day (1-31, optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val limit = limitText.toDoubleOrNull()
                    val dueDay = dueDayText.toIntOrNull()
                    when {
                        name.trim().isEmpty() -> error = "Enter a name"
                        dueDay != null && (dueDay < 1 || dueDay > 31) ->
                            error = "Due day must be between 1 and 31"

                        else -> onSave(name, type, limit, dueDay)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
