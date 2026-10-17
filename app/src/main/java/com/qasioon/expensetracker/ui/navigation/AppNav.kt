package com.qasioon.expensetracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qasioon.expensetracker.data.model.TransactionType
import com.qasioon.expensetracker.ui.accounts.AccountDetailScreen
import com.qasioon.expensetracker.ui.accounts.AccountsScreen
import com.qasioon.expensetracker.ui.addedit.AddEditScreen
import com.qasioon.expensetracker.ui.categories.CategoriesScreen
import com.qasioon.expensetracker.ui.home.HomeScreen
import com.qasioon.expensetracker.ui.reports.ReportsScreen
import com.qasioon.expensetracker.ui.search.SearchScreen

private data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomTabs = listOf(
    BottomTab("home", "Home", Icons.Filled.Home),
    BottomTab("reports", "Reports", Icons.Filled.BarChart),
    BottomTab("categories", "Categories", Icons.Filled.Label),
    BottomTab("accounts", "Accounts", Icons.Filled.AccountBalanceWallet)
)

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomTabs.map { it.route }) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.navigateToTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable("home") {
                HomeScreen(
                    onAdd = { type, accountId ->
                        navController.navigate(
                            "txn?txnId=-1&type=${type.name.lowercase()}&accountId=$accountId"
                        )
                    },
                    onOpenTransaction = { txnId ->
                        navController.navigate("txn?txnId=$txnId&type=expense&accountId=-1")
                    },
                    onOpenAccount = { accountId ->
                        navController.navigate("account/$accountId")
                    },
                    onSearch = { navController.navigate("search") }
                )
            }

            composable("reports") {
                ReportsScreen(
                    onOpenTransaction = { txnId ->
                        navController.navigate("txn?txnId=$txnId&type=expense&accountId=-1")
                    }
                )
            }

            composable("categories") {
                CategoriesScreen()
            }

            composable("accounts") {
                AccountsScreen(
                    onOpenAccount = { accountId ->
                        navController.navigate("account/$accountId")
                    }
                )
            }

            composable(
                route = "txn?txnId={txnId}&type={type}&accountId={accountId}",
                arguments = listOf(
                    navArgument("txnId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("type") {
                        type = NavType.StringType
                        defaultValue = "expense"
                    },
                    navArgument("accountId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val txnId = entry.arguments?.getLong("txnId") ?: -1L
                val type = entry.arguments?.getString("type") ?: "expense"
                val accountId = entry.arguments?.getLong("accountId") ?: -1L
                AddEditScreen(
                    transactionId = txnId,
                    initialType = if (type == "income") TransactionType.INCOME else TransactionType.EXPENSE,
                    initialAccountId = accountId,
                    onDone = { navController.popBackStack() }
                )
            }

            composable(
                route = "account/{accountId}",
                arguments = listOf(
                    navArgument("accountId") { type = NavType.LongType }
                )
            ) { entry ->
                val accountId = entry.arguments?.getLong("accountId") ?: -1L
                AccountDetailScreen(
                    accountId = accountId,
                    onBack = { navController.popBackStack() },
                    onOpenTransaction = { txnId ->
                        navController.navigate("txn?txnId=$txnId&type=expense&accountId=-1")
                    }
                )
            }

            composable("search") {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onOpenTransaction = { txnId ->
                        navController.navigate("txn?txnId=$txnId&type=expense&accountId=-1")
                    }
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
