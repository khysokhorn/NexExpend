package com.nextgen.expend.ui.screen.search

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextgen.expend.data.model.Category
import com.nextgen.expend.data.model.Transaction
import com.nextgen.expend.data.model.TransactionType

private val filterCategories = listOf(null) + Category.entries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    transactions: List<Transaction>,
    onBack: () -> Unit
) {
    val scheme       = MaterialTheme.colorScheme
    var query        by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf<Category?>(null) }
    val focusRequester = remember { FocusRequester() }

    // Auto-focus keyboard on launch
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val results: List<Transaction> = remember(query, activeFilter, transactions) {
        transactions.filter { tx ->
            val matchesQuery = query.isBlank() ||
                tx.merchant.contains(query, ignoreCase = true) ||
                tx.category.label.contains(query, ignoreCase = true)
            val matchesFilter = activeFilter == null || tx.category == activeFilter
            matchesQuery && matchesFilter
        }
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
                    // Inline search field in the app bar
                    TextField(
                        value         = query,
                        onValueChange = { query = it },
                        placeholder   = {
                            Text(
                                "Search transactions…",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = scheme.onSurfaceVariant
                                )
                            )
                        },
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        colors        = TextFieldDefaults.colors(
                            focusedContainerColor   = scheme.surface,
                            unfocusedContainerColor = scheme.surface,
                            focusedIndicatorColor   = scheme.primary,
                            unfocusedIndicatorColor = scheme.outlineVariant,
                            cursorColor             = scheme.primary,
                            focusedTextColor        = scheme.onSurface,
                            unfocusedTextColor      = scheme.onSurface,
                        ),
                        modifier      = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        trailingIcon  = {
                            AnimatedVisibility(
                                visible = query.isNotEmpty(),
                                enter = fadeIn(),
                                exit  = fadeOut()
                            ) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(
                                        Icons.Outlined.Cancel,
                                        contentDescription = "Clear",
                                        tint = scheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = scheme.surface,
                    navigationIconContentColor = scheme.primary,
                )
            )
        },
        containerColor = scheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Filter chips ─────────────────────────────────────────────────
            Row(
                modifier            = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip
                val allSelected = activeFilter == null
                FilterChip(
                    selected = allSelected,
                    onClick  = { activeFilter = null },
                    label    = { Text("All", style = MaterialTheme.typography.labelMedium) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor    = scheme.primary,
                        selectedLabelColor        = scheme.onPrimary,
                        containerColor            = scheme.surface,
                        labelColor                = scheme.onSurfaceVariant,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled          = true,
                        selected         = allSelected,
                        borderColor      = scheme.outlineVariant,
                        selectedBorderColor = scheme.primary,
                        borderWidth      = 1.dp,
                        selectedBorderWidth = 0.dp,
                    )
                )

                Category.entries.forEach { cat ->
                    val isSelected = activeFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick  = { activeFilter = if (isSelected) null else cat },
                        label    = {
                            Text(cat.label, style = MaterialTheme.typography.labelMedium)
                        },
                        leadingIcon = {
                            Icon(
                                cat.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor    = scheme.primary,
                            selectedLabelColor        = scheme.onPrimary,
                            selectedLeadingIconColor  = scheme.onPrimary,
                            containerColor            = scheme.surface,
                            labelColor                = scheme.onSurfaceVariant,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = isSelected,
                            borderColor         = scheme.outlineVariant,
                            selectedBorderColor = scheme.primary,
                            borderWidth         = 1.dp,
                            selectedBorderWidth = 0.dp,
                        )
                    )
                }
            }

            HorizontalDivider(color = scheme.outlineVariant, thickness = 0.5.dp)

            // ── Results ──────────────────────────────────────────────────────
            AnimatedContent(
                targetState = results.isEmpty(),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "emptyState"
            ) { isEmpty ->
                if (isEmpty) {
                    // Empty state
                    Column(
                        modifier            = Modifier
                            .fillMaxSize()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint     = scheme.outlineVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No results found",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = scheme.onSurfaceVariant
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Try a different search term or clear the filter.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = scheme.onSurfaceVariant
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        // Result count label
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${results.size} RESULT${if (results.size != 1) "S" else ""}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color         = scheme.onSurfaceVariant,
                                    letterSpacing = 1.5.sp
                                )
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        items(results, key = { it.id }) { tx ->
                            SearchResultRow(tx = tx, query = query, scheme = scheme)
                            HorizontalDivider(
                                color     = scheme.outlineVariant.copy(alpha = 0.5f),
                                thickness = 0.5.dp
                            )
                        }

                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    tx: Transaction,
    query: String,
    scheme: ColorScheme,
) {
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
                    ),
                    maxLines = 1
                )
                Text(
                    "${tx.dateLabel} • ${tx.category.label}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = scheme.onSurfaceVariant
                    )
                )
            }
        }

        val sign  = if (tx.type == TransactionType.INCOME) "+" else "-"
        val color = if (tx.type == TransactionType.INCOME) scheme.primary else scheme.onSurface
        Text(
            "$sign$${"%.2f".format(tx.amount)}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color      = color
            )
        )
    }
}
