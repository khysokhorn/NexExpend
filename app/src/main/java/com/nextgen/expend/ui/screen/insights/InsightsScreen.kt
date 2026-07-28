package com.nextgen.expend.ui.screen.insights

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.nextgen.expend.data.CategoryStat
import com.nextgen.expend.data.model.Transaction
import com.nextgen.expend.data.model.TransactionType
import com.nextgen.expend.ui.components.BottomNavBar
import com.nextgen.expend.ui.components.NavTab
import java.util.Locale
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.Shape as VicoShape

private enum class Period(val label: String) { WEEKLY("Weekly"), MONTHLY("Monthly"), YEARLY("Yearly") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    transactions: List<Transaction>,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onHistory: () -> Unit,
    onAdd: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    val totalExpense = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val dynamicStats = remember(transactions, totalExpense) {
        transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .map { (category, txList) ->
                val amount = txList.sumOf { it.amount }
                val percent = if (totalExpense > 0.0) ((amount / totalExpense) * 100).toFloat() else 0.0f
                CategoryStat(category, amount, percent)
            }
            .sortedByDescending { it.amount }
    }
    var period by remember { mutableStateOf(Period.WEEKLY) }
    val context = LocalContext.current

    // ── Vico model producer ──────────────────────────────────────────────────
    // Transactions only carry a display label (dateLabel), not a real timestamp,
    // so a true day/month time series isn't available yet — show one real bar
    // sized to actual total spend instead of a fabricated per-period trend.
    val modelProducer = remember { CartesianChartModelProducer() }
    val dataForPeriod = listOf(totalExpense.toFloat().coerceAtLeast(0f))

    LaunchedEffect(period, totalExpense) {
        modelProducer.runTransaction {
            columnSeries { series(dataForPeriod) }
        }
    }

    val xLabels = listOf(period.label)

    val totalSpend = "$%,.2f".format(Locale.US, totalExpense)

    // Vico column layer built with rememberLineComponent (correct 2.x API)
    val columnLayer = rememberColumnCartesianLayer(
        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
            rememberLineComponent(
                fill = fill(scheme.primary),
                thickness = if (period == Period.WEEKLY) 20.dp else 12.dp,
                shape = VicoShape.Rectangle,
            )
        )
    )

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
                        "Insights",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Export coming soon", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Export")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.surface,
                    titleContentColor = scheme.primary,
                    navigationIconContentColor = scheme.primary,
                    actionIconContentColor = scheme.primary,
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                selected = NavTab.INSIGHTS,
                onHome = onHome,
                onHistory = onHistory,
                onAdd = onAdd,
                onInsights = {},
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

            // ── Period Toggle ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .border(1.dp, scheme.outlineVariant, MaterialTheme.shapes.large)
                    .background(scheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Period.entries.forEach { p ->
                    val isSelected = p == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.medium)
                            .background(if (isSelected) scheme.primary else Color.Transparent)
                            .clickable { period = p }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            p.label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Bento Stats ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val topCategory = dynamicStats.firstOrNull()
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "TOP CATEGORY",
                    value = topCategory?.category?.label ?: "—",
                    sub = topCategory?.let { "%.0f%% of total spend".format(it.percent) }
                        ?: "No expenses yet",
                    subIsError = false,
                    scheme = scheme,
                )
                AnimatedContent(
                    targetState = totalSpend,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "totalSpend"
                ) { spend ->
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "TOTAL SPEND",
                        value = spend,
                        sub = "Stable trend",
                        subIsError = false,
                        scheme = scheme,
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Vico Bar Chart ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .border(1.dp, scheme.outlineVariant, MaterialTheme.shapes.large)
                    .background(scheme.surface)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "Spending Analysis",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            "Breakdown of total expenditures",
                            style = MaterialTheme.typography.labelMedium.copy(color = scheme.onSurfaceVariant)
                        )
                    }
                    Text(
                        totalSpend,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(Modifier.height(20.dp))

                CartesianChartHost(
                    chart = rememberCartesianChart(
                        columnLayer,
                        bottomAxis = HorizontalAxis.rememberBottom(
                            valueFormatter = CartesianValueFormatter { _, x, _ ->
                                xLabels.getOrElse(x.toInt()) { "" }
                            }
                        ),
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Category Breakdown ───────────────────────────────────────────
            Text(
                "CATEGORY BREAKDOWN",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = scheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp
                )
            )
            Spacer(Modifier.height(12.dp))

            if (dynamicStats.isEmpty()) {
                Text(
                    "No expenses recorded yet.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = scheme.onSurfaceVariant),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            dynamicStats.forEach { stat ->
                CategoryBreakdownRow(stat = stat, scheme = scheme)
                HorizontalDivider(
                    color = scheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Export Report Banner ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(scheme.primary)
                    .clickable {
                        Toast.makeText(context, "Export coming soon", Toast.LENGTH_SHORT).show()
                    }
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Export PDF Report",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = scheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        "Full financial analysis for this period",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = scheme.onPrimary.copy(alpha = 0.7f)
                        )
                    )
                }
                Icon(
                    Icons.Outlined.Download,
                    contentDescription = null,
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Small helpers ────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    modifier: Modifier,
    label: String,
    value: String,
    sub: String,
    subIsError: Boolean,
    scheme: ColorScheme,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .border(1.dp, scheme.outlineVariant, MaterialTheme.shapes.large)
            .background(scheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = scheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        )
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                if (subIsError) Icons.Outlined.TrendingUp else Icons.Outlined.TableChart,
                contentDescription = null,
                tint = if (subIsError) scheme.error else scheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                sub,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (subIsError) scheme.error else scheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun CategoryBreakdownRow(stat: CategoryStat, scheme: ColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .border(1.dp, scheme.outlineVariant, MaterialTheme.shapes.medium)
                    .background(scheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    stat.category.icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = scheme.primary
                )
            }
            Column {
                Text(
                    stat.category.label,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(4.dp))
                // Mini progress bar
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(scheme.outlineVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(stat.percent / 100f)
                            .clip(CircleShape)
                            .background(scheme.primary)
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "\$${"%.2f".format(stat.amount)}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "${"%.1f".format(stat.percent)}%",
                style = MaterialTheme.typography.labelMedium.copy(color = scheme.onSurfaceVariant)
            )
        }
    }
}
