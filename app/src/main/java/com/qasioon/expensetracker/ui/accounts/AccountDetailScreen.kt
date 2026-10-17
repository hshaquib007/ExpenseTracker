package com.qasioon.expensetracker.ui.accounts

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qasioon.expensetracker.ExpenseApp
import com.qasioon.expensetracker.data.model.AccountType
import com.qasioon.expensetracker.data.model.TransactionType
import com.qasioon.expensetracker.ui.components.EmptyState
import com.qasioon.expensetracker.ui.components.SectionHeader
import com.qasioon.expensetracker.ui.components.TransactionRow
import com.qasioon.expensetracker.ui.components.accountIcon
import com.qasioon.expensetracker.ui.model.TxnItem
import com.qasioon.expensetracker.util.Dates
import com.qasioon.expensetracker.util.Money
import java.time.YearMonth

@Composable
fun AccountDetailScreen(
    accountId: Long,
    onBack: () -> Unit,
    onOpenTransaction: (Long) -> Unit,
    viewModel: AccountDetailViewModel = viewModel(
        factory = AccountDetailViewModel.factory(
            LocalContext.current.applicationContext as ExpenseApp,
            accountId
        )
    )
) {
    val account by viewModel.account.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val app = LocalContext.current.applicationContext as ExpenseApp
    val categories by app.categoryRepository.observeAll()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val categoryById = categories.associateBy { it.id }

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
                text = account?.name ?: "Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        }

        val currentAccount = account
        if (currentAccount == null) {
            EmptyState("Account not found")
            return
        }

        val items = transactions.map { txn ->
            TxnItem(
                txn = txn,
                accountName = currentAccount.name,
                accountType = currentAccount.type,
                categoryName = txn.categoryId?.let { categoryById[it]?.name }
            )
        }

        val monthRange = Dates.monthRange(YearMonth.now())
        val monthSpent = transactions
            .filter { it.type == TransactionType.EXPENSE && Dates.contains(monthRange, it.date) }
            .sumOf { it.amount }
        val totalSpent = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = accountIcon(currentAccount.type),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.size(10.dp))
                            Column {
                                Text(
                                    text = currentAccount.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = currentAccount.type.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatBox(
                                label = "This month",
                                value = Money.format(monthSpent),
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                label = "All time spent",
                                value = Money.format(totalSpent),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (currentAccount.type == AccountType.CREDIT_CARD &&
                            currentAccount.creditLimit != null &&
                            currentAccount.creditLimit > 0
                        ) {
                            val limit = currentAccount.creditLimit
                            val usedFraction = (monthSpent / limit).toFloat().coerceIn(0f, 1f)
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Used ${Money.format(monthSpent)} of ${Money.format(limit)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Available ${Money.format((limit - monthSpent).coerceAtLeast(0.0))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { usedFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                            currentAccount.billDueDay?.let { day ->
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Bill due on day $day of every month",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item { SectionHeader("Transactions") }

            if (items.isEmpty()) {
                item { EmptyState("No transactions for this account") }
            } else {
                items(items, key = { it.txn.id }) { item ->
                    TransactionRow(
                        item = item,
                        onClick = { onOpenTransaction(item.txn.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
