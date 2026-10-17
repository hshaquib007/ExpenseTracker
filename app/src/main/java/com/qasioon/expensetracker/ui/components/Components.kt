package com.qasioon.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ArrowDropDownCircle
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qasioon.expensetracker.data.model.AccountType
import com.qasioon.expensetracker.data.model.PeriodMode
import com.qasioon.expensetracker.data.model.TransactionType
import com.qasioon.expensetracker.ui.model.TxnItem
import com.qasioon.expensetracker.util.Dates
import com.qasioon.expensetracker.util.Money

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = Money.format(amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TransactionRow(
    item: TxnItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = item.txn.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) ExpenseRedRow else IncomeGreenRow
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isExpense) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isExpense) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = amountColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.categoryName
                    ?: if (isExpense) "Uncategorized" else "Income",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.accountName} • ${Dates.formatDay(item.txn.date)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.size(8.dp))
        Text(
            text = (if (isExpense) "- " else "+ ") + Money.format(item.txn.amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = amountColor
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PeriodSelector(
    label: String,
    mode: PeriodMode,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onModeChange: (PeriodMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Previous")
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ArrowForwardIos, contentDescription = "Next")
        }
        Spacer(Modifier.size(4.dp))
        FilterChip(
            selected = mode == PeriodMode.MONTHLY,
            onClick = { onModeChange(PeriodMode.MONTHLY) },
            label = { Text("Monthly") }
        )
        Spacer(Modifier.size(6.dp))
        FilterChip(
            selected = mode == PeriodMode.YEARLY,
            onClick = { onModeChange(PeriodMode.YEARLY) },
            label = { Text("Yearly") }
        )
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

fun accountIcon(type: AccountType): ImageVector = when (type) {
    AccountType.CASH -> Icons.Filled.Payments
    AccountType.BANK -> Icons.Filled.AccountBalance
    AccountType.CREDIT_CARD -> Icons.Filled.CreditCard
}

fun categoryIcon(name: String): ImageVector {
    val n = name.lowercase()
    return when {
        listOf("food", "eat", "dining", "restaurant").any { n.contains(it) } -> Icons.Filled.Restaurant
        listOf("grocery", "vegetable", "market").any { n.contains(it) } -> Icons.Filled.LocalGroceryStore
        listOf("entertain", "movie", "music", "game").any { n.contains(it) } -> Icons.Filled.Theaters
        listOf("transport", "travel", "car", "fuel", "petrol").any { n.contains(it) } -> Icons.Filled.DirectionsCar
        listOf("medical", "health", "doctor").any { n.contains(it) } -> Icons.Filled.HealthAndSafety
        listOf("bill", "utility", "recharge").any { n.contains(it) } -> Icons.Filled.Receipt
        listOf("shop", "clothes").any { n.contains(it) } -> Icons.Filled.ShoppingBag
        listOf("salary", "income", "interest").any { n.contains(it) } -> Icons.Filled.Payments
        else -> Icons.Filled.Label
    }
}

private val ExpenseRedRow = androidx.compose.ui.graphics.Color(0xFFD32F2F)
private val IncomeGreenRow = androidx.compose.ui.graphics.Color(0xFF2E7D32)
