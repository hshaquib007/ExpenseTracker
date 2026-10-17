package com.qasioon.expensetracker.ui.reports

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qasioon.expensetracker.ExpenseApp
import com.qasioon.expensetracker.export.CsvExporter
import com.qasioon.expensetracker.export.PdfExporter
import com.qasioon.expensetracker.export.ShareUtil
import com.qasioon.expensetracker.ui.components.BarChart
import com.qasioon.expensetracker.ui.components.EmptyState
import com.qasioon.expensetracker.ui.components.PeriodSelector
import com.qasioon.expensetracker.ui.components.PieChart
import com.qasioon.expensetracker.ui.components.PieSlice
import com.qasioon.expensetracker.ui.components.SectionHeader
import com.qasioon.expensetracker.ui.components.SummaryCard
import com.qasioon.expensetracker.ui.components.chartPalette
import com.qasioon.expensetracker.util.Money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onOpenTransaction: (Long) -> Unit,
    viewModel: ReportsViewModel = viewModel(
        factory = ReportsViewModel.factory(LocalContext.current.applicationContext as ExpenseApp)
    )
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var exportStatus by remember { mutableStateOf<String?>(null) }

    val slices = state.categoryShares.mapIndexed { index, share ->
        PieSlice(
            label = share.name,
            fraction = share.fraction,
            color = chartPalette[index % chartPalette.size],
            amount = share.amount
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Reports",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        item {
            PeriodSelector(
                label = state.periodLabel,
                mode = state.mode,
                onPrevious = viewModel::previousPeriod,
                onNext = viewModel::nextPeriod,
                onModeChange = viewModel::setMode
            )
        }

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

        item { SectionHeader("Spending by Category") }

        if (slices.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expenses in ${state.periodLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        PieChart(
                            slices = slices,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        slices.forEach { slice ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(slice.color, CircleShape)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = slice.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                Text(
                                    text = "${(slice.fraction * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = Money.format(slice.amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        item { SectionHeader("Last 6 Months") }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    BarChart(bars = state.monthBars)
                    HorizontalDivider(Modifier.padding(top = 4.dp))
                    Text(
                        text = "Expenses per month",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        if (state.budgetStatus.isNotEmpty()) {
            item { SectionHeader("Category Budgets • ${state.budgetStatusMonth}") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.budgetStatus.forEach { status ->
                            val budget = status.category.monthlyBudget ?: 0.0
                            val progress = (status.spent / budget).toFloat().coerceIn(0f, 1f)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = status.category.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${Money.format(status.spent)} / ${Money.format(budget)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (status.spent > budget) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { SectionHeader("Export ${state.periodLabel}") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            try {
                                val file = withContext(Dispatchers.IO) {
                                    CsvExporter.export(context, state.items, state.periodLabel)
                                }
                                withContext(Dispatchers.Main) {
                                    ShareUtil.shareFile(context, file, "text/csv")
                                }
                                exportStatus = null
                            } catch (e: Exception) {
                                exportStatus = "CSV export failed: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.TableChart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text("CSV")
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            try {
                                val file = withContext(Dispatchers.IO) {
                                    PdfExporter.export(
                                        context = context,
                                        items = state.items,
                                        periodLabel = state.periodLabel,
                                        income = state.income,
                                        expenses = state.expenses
                                    )
                                }
                                withContext(Dispatchers.Main) {
                                    ShareUtil.shareFile(context, file, "application/pdf")
                                }
                                exportStatus = null
                            } catch (e: Exception) {
                                exportStatus = "PDF export failed: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text("PDF")
                }
            }
        }

        exportStatus?.let { message ->
            item {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (!state.hasAnyData) {
            item {
                EmptyState("No data in ${state.periodLabel}")
            }
        }
    }
}
