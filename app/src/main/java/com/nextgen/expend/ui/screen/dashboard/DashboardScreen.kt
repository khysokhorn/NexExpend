package com.nextgen.expend.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextgen.expend.data.model.Transaction
import com.nextgen.expend.data.model.TransactionType
import com.nextgen.expend.ui.components.BottomNavBar
import com.nextgen.expend.ui.components.NavTab
import java.util.Locale

private data class CategoryItem(val label: String, val icon: ImageVector)

private val categories = listOf(
    CategoryItem("Food",      Icons.Outlined.Restaurant),
    CategoryItem("Transport", Icons.Outlined.DirectionsCar),
    CategoryItem("Bills",     Icons.Outlined.ReceiptLong),
    CategoryItem("Shopping",  Icons.Outlined.ShoppingBag),
    CategoryItem("Health",    Icons.Outlined.FitnessCenter),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    transactions: List<Transaction>,
    onAddExpense: () -> Unit,
    onHistory:    () -> Unit,
    onInsights:   () -> Unit,
    onSearch:     () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    val totalIncome = remember(transactions) {
        transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val totalSpent = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val totalBalance = remember(totalIncome, totalSpent) {
        10000.0 + totalIncome - totalSpent
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Wallet",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = scheme.surface,
                    titleContentColor      = scheme.primary,
                    actionIconContentColor = scheme.primary
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                selected   = NavTab.HOME,
                onHome     = {},
                onHistory  = onHistory,
                onAdd      = onAddExpense,
                onInsights = onInsights
            )
        },
        containerColor = scheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Total Balance Card ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(scheme.primary)
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text  = "TOTAL BALANCE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color         = scheme.onPrimary.copy(alpha = 0.7f),
                                    letterSpacing = 1.5.sp
                                )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text  = "$%,.2f".format(Locale.US, totalBalance),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    color         = scheme.onPrimary,
                                    fontSize      = 36.sp,
                                    fontWeight    = FontWeight.Bold,
                                    letterSpacing = (-1).sp
                                )
                            )
                        }
                        Icon(
                            imageVector        = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint               = scheme.onPrimary.copy(alpha = 0.2f),
                            modifier           = Modifier.size(40.dp)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        Column {
                            Text(
                                "MONTHLY INCOME",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color         = scheme.onPrimary.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                "+$%,.2f".format(Locale.US, totalIncome),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color      = scheme.onPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Column {
                            Text(
                                "MONTHLY SPENT",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color         = scheme.onPrimary.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                "-$%,.2f".format(Locale.US, totalSpent),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color      = scheme.onPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── Categories ──────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Categories", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = {}) {
                    Text(
                        "See All",
                        style = MaterialTheme.typography.bodyMedium.copy(color = scheme.onSurfaceVariant)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding        = PaddingValues(horizontal = 0.dp)
            ) {
                items(categories) { cat ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.width(64.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(MaterialTheme.shapes.large)
                                .border(1.dp, scheme.outlineVariant, MaterialTheme.shapes.large)
                                .background(scheme.surface)
                                .clickable {},
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                cat.icon,
                                contentDescription = cat.label,
                                modifier           = Modifier.size(24.dp),
                                tint               = scheme.primary
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            cat.label,
                            style    = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── Recent Transactions ──────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Recent Transactions", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onHistory) {
                    Text(
                        "View History",
                        style = MaterialTheme.typography.bodyMedium.copy(color = scheme.onSurfaceVariant)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            transactions.take(3).forEach { tx ->
                HorizontalDivider(
                    color     = scheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .clickable {}
                        .padding(vertical = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(scheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                tx.category.icon,
                                contentDescription = null,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                tx.merchant,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                "${tx.dateLabel}, ${tx.timeLabel}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = scheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        val sign  = if (tx.type == TransactionType.INCOME) "+" else "-"
                        val color = if (tx.type == TransactionType.INCOME) scheme.primary else scheme.onSurface
                        Text(
                            "$sign\$${"%.2f".format(tx.amount)}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color      = color
                            )
                        )
                        Text(
                            tx.category.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = scheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            HorizontalDivider(
                color     = scheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )

            Spacer(Modifier.height(40.dp))

            // ── Smart Tip ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .border(1.dp, scheme.outlineVariant, MaterialTheme.shapes.large)
                    .background(scheme.surfaceVariant.copy(alpha = 0.4f))
                    .clickable {}
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier              = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint               = scheme.primary
                    )
                    Column {
                        Text(
                            "Smart Tip",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            "You spent 15% less on food this week.",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = scheme.onSurfaceVariant
                            )
                        )
                    }
                }
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint               = scheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
