package com.nextgen.expend.ui.screen.addexpense

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextgen.expend.data.model.Category

private val displayCategories = listOf(
    Category.DINING,
    Category.SHOPPING,
    Category.TRANSPORT,
    Category.GROCERIES,
    Category.BILLS,
    Category.HEALTH,
    Category.FUN,
    Category.OTHER
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBack: () -> Unit, onSaveExpense: (Double, Category, String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var selectedCategory by remember { mutableStateOf(Category.DINING) }
    var amountStr by remember { mutableStateOf("0") }
    var note by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }

    // Banking-style keypad digit handler
    fun onKey(key: String) {
        amountStr = when {
            key == "⌫" -> if (amountStr.length > 1) amountStr.dropLast(1) else "0"
            key == "." -> if ("." in amountStr) amountStr else "$amountStr."
            amountStr == "0" && key != "." -> key
            amountStr.substringAfter('.', "").length >= 2 && "." in amountStr -> amountStr
            else -> "$amountStr$key"
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(), containerColor = scheme.background, topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Add Expense",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }, navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = scheme.background,
                    titleContentColor = scheme.onSurface,
                    navigationIconContentColor = scheme.onSurface
                )
            )
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Big Input Display ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Clickable Currency Selector (USD/KHR Switch)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(scheme.primary)
                                .clickable { currency = if (currency == "USD") "KHR" else "USD" }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    if (currency == "USD") "$" else "៛",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold, color = scheme.onPrimary
                                    )
                                )
                                Text(
                                    currency, style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = scheme.onPrimary.copy(alpha = 0.8f)
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Switch currency",
                                    tint = scheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                amountStr, style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.primary,
                                    letterSpacing = (-1.5).sp
                                )
                            )
                            // Cursor |
                            Text(
                                "|", style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Light,
                                    color = scheme.primary.copy(alpha = 0.5f)
                                ), modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }

                // ── Category Grid ───────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "CATEGORY", style = MaterialTheme.typography.labelMedium.copy(
                            color = scheme.onSurfaceVariant, letterSpacing = 1.5.sp
                        )
                    )
                }
                Spacer(Modifier.height(12.dp))

                displayCategories.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { cat ->
                            val isSelected = cat == selectedCategory
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(MaterialTheme.shapes.large)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) scheme.primary else scheme.outlineVariant,
                                        shape = MaterialTheme.shapes.large
                                    )
                                    .background(
                                        if (isSelected) scheme.primary else scheme.surface
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    cat.icon,
                                    contentDescription = cat.label,
                                    modifier = Modifier.size(22.dp),
                                    tint = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    cat.label, style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ), maxLines = 1
                                )
                            }
                        }
                        if (rowItems.size < 4) repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(20.dp))

                // ── Date Row ────────────────────────────────────────────────
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "DATE", style = MaterialTheme.typography.labelMedium.copy(
                            color = scheme.onSurfaceVariant, letterSpacing = 1.5.sp
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Today, Jun 18 2026",
                            style = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface)
                        )
                    }
                    HorizontalDivider(color = scheme.outlineVariant)
                }

                Spacer(Modifier.height(20.dp))

            }

            // ── Sticky Bottom Panel (Layout from img.png) ─────────────────────
            Surface(
                color = scheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 16.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(scheme.surface)
                ) {
                    Spacer(Modifier.height(12.dp))

                    // 1. Remark Input Box
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = {
                            Text(
                                "Add Remark", style = MaterialTheme.typography.bodyLarge.copy(
                                    color = scheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = scheme.outlineVariant,
                            unfocusedBorderColor = scheme.outlineVariant,
                            cursorColor = scheme.primary,
                            focusedTextColor = scheme.onSurface,
                            unfocusedTextColor = scheme.onSurface,
                            focusedContainerColor = scheme.surface,
                            unfocusedContainerColor = scheme.surface,
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // 2. Action Button (Save Expense)
                    Button(
                        onClick = {
                            val amountVal = amountStr.toDoubleOrNull() ?: 0.0
                            if (amountVal > 0.0) {
                                onSaveExpense(amountVal, selectedCategory, note)
                            }
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = scheme.primary, contentColor = scheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            "Save Expense",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // 3. Grid Numeric Keypad with thin lines
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(scheme.surface)
                    ) {
                        // Top border of keypad
                        HorizontalDivider(
                            color = scheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp
                        )

                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf(".", "0", "⌫")
                        )

                        keys.forEachIndexed { rowIndex, rowKeys ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                rowKeys.forEachIndexed { colIndex, key ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable { onKey(key) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (key == "⌫") {
                                            Icon(
                                                Icons.Outlined.Backspace,
                                                contentDescription = "Backspace",
                                                tint = scheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Normal,
                                                    color = scheme.onSurface,
                                                    fontSize = 24.sp
                                                )
                                            )
                                        }
                                    }

                                    // Vertical divider
                                    if (colIndex < 2) {
                                        Box(
                                            modifier = Modifier
                                                .width(0.5.dp)
                                                .fillMaxHeight()
                                                .background(scheme.outlineVariant.copy(alpha = 0.4f))
                                        )
                                    }
                                }
                            }

                            // Horizontal divider
                            if (rowIndex < 3) {
                                HorizontalDivider(
                                    color = scheme.outlineVariant.copy(alpha = 0.4f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
