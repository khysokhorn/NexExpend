package com.nextgen.expend.ui.screen.history

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextgen.expend.data.model.Transaction
import com.nextgen.expend.data.model.TransactionType
import com.nextgen.expend.data.model.formattedAmount
import com.nextgen.expend.ui.components.BottomNavBar
import com.nextgen.expend.ui.components.NavTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    transactions: List<Transaction>,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onAdd: () -> Unit,
    onInsights: () -> Unit,
    onHome: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    // Group transactions by their dateLabel
    val grouped: Map<String, List<Transaction>> = remember(transactions) {
        transactions.groupBy { it.dateLabel }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        "History",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Outlined.FilterList, contentDescription = "Filter by category")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = scheme.surface,
                    titleContentColor          = scheme.primary,
                    navigationIconContentColor = scheme.primary,
                    actionIconContentColor     = scheme.primary,
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                selected   = NavTab.HISTORY,
                onHome     = onHome,
                onHistory  = {},
                onAdd      = onAdd,
                onInsights = onInsights,
            )
        },
        containerColor = scheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            grouped.forEach { (dateLabel, transactions) ->
                // ── Date header ──────────────────────────────────────────────
                item(key = "header_$dateLabel") {
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            dateLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color         = scheme.onSurfaceVariant,
                                fontWeight    = FontWeight.SemiBold,
                                letterSpacing = 1.5.sp
                            )
                        )
                        val dayTotal = transactions.sumOf {
                            if (it.type == TransactionType.INCOME) it.amount else -it.amount
                        }
                        val sign  = if (dayTotal >= 0) "+" else ""
                        val color = if (dayTotal >= 0) scheme.primary else scheme.onSurface
                        Text(
                            "$sign$${"%.2f".format(dayTotal)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color      = color,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    HorizontalDivider(
                        color     = scheme.outlineVariant,
                        thickness = 0.5.dp
                    )
                }

                // ── Transaction rows ─────────────────────────────────────────
                items(transactions, key = { it.id }) { tx ->
                    TransactionRow(tx = tx, scheme = scheme)
                    HorizontalDivider(
                        color     = scheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction, scheme: ColorScheme) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier              = Modifier.weight(1f)
        ) {
            // Icon bubble
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.large)
                    .border(1.dp, scheme.outlineVariant, MaterialTheme.shapes.large)
                    .background(scheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    tx.category.icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint     = scheme.primary
                )
            }

            Column {
                Text(
                    tx.merchant,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    "${tx.timeLabel} • ${tx.category.label}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = scheme.onSurfaceVariant
                    )
                )
            }
        }

        // Amount
        val sign  = if (tx.type == TransactionType.INCOME) "+" else "-"
        val color = if (tx.type == TransactionType.INCOME) scheme.primary else scheme.onSurface
        Text(
            "$sign${tx.formattedAmount()}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color      = color
            )
        )
    }
}
