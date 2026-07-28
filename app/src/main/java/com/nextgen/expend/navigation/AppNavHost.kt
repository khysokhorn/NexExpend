package com.nextgen.expend.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nextgen.expend.ui.screen.addexpense.AddExpenseScreen
import com.nextgen.expend.ui.screen.dashboard.DashboardScreen
import com.nextgen.expend.ui.screen.history.TransactionHistoryScreen
import com.nextgen.expend.ui.screen.insights.InsightsScreen
import com.nextgen.expend.ui.screen.search.SearchScreen
import com.nextgen.expend.ui.viewmodel.TransactionViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: TransactionViewModel
) {
    val transactions by viewModel.transactions.collectAsState()
    val smartTip by viewModel.smartTip.collectAsState()
    val llmStatus by viewModel.llmStatus.collectAsState()
    val modelDownloadProgress by viewModel.modelDownloadProgress.collectAsState()

    val modelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.loadModelFromUri(it) }
    }

    NavHost(
        navController    = navController,
        startDestination = Routes.DASHBOARD,
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                transactions = transactions,
                smartTip = smartTip,
                llmStatus = llmStatus,
                modelDownloadProgress = modelDownloadProgress,
                onRefreshTip = { viewModel.generateSmartTip() },
                onLoadModel = { modelPickerLauncher.launch(arrayOf("*/*")) },
                onDownloadModel = { url -> viewModel.downloadModelFromUrl(url) },
                onAddExpense = { navController.navigate(Routes.ADD_EXPENSE) },
                onHistory    = { navController.navigate(Routes.TRANSACTION_HISTORY) },
                onInsights   = { navController.navigate(Routes.INSIGHTS) },
                onSearch     = { navController.navigate(Routes.SEARCH) },
            )
        }

        composable(Routes.ADD_EXPENSE) {
            AddExpenseScreen(
                onBack = { navController.popBackStack() },
                onSaveExpense = { amount, category, note, currency ->
                    viewModel.addExpense(amount, category, note, currency)
                }
            )
        }

        composable(Routes.TRANSACTION_HISTORY) {
            TransactionHistoryScreen(
                transactions = transactions,
                onBack   = { navController.popBackStack() },
                onSearch = { navController.navigate(Routes.SEARCH) },
                onAdd    = { navController.navigate(Routes.ADD_EXPENSE) },
                onInsights = { navController.navigate(Routes.INSIGHTS) },
                onHome   = { navController.popBackStack(Routes.DASHBOARD, false) },
            )
        }

        composable(Routes.INSIGHTS) {
            InsightsScreen(
                transactions = transactions,
                onBack    = { navController.popBackStack() },
                onHome    = { navController.popBackStack(Routes.DASHBOARD, false) },
                onHistory = { navController.navigate(Routes.TRANSACTION_HISTORY) },
                onAdd     = { navController.navigate(Routes.ADD_EXPENSE) },
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                transactions = transactions,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
