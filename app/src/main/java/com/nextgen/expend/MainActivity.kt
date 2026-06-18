package com.nextgen.expend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.nextgen.expend.navigation.AppNavHost
import com.nextgen.expend.ui.theme.NexExpendTheme
import com.nextgen.expend.ui.viewmodel.TransactionViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val transactionViewModel: TransactionViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexExpendTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController, viewModel = transactionViewModel)
            }
        }
    }
}