package com.nextgen.expend.ui.screen.addexpense

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextgen.expend.data.model.Category
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    onBack: () -> Unit, onSaveExpense: (Double, Category, String, String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var selectedCategory by remember { mutableStateOf(Category.DINING) }
    var amountStr by remember { mutableStateOf("0") }
    var note by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(Currency.KHR) }
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "cursorAlpha"
    )

    fun formatAmount(
        value: String,
        currency: Currency
    ): String {
        val number = value.toBigDecimalOrNull() ?: return value

        return when (currency) {
            Currency.USD ->
                DecimalFormat("#,##0.00").format(number)

            Currency.KHR ->
                DecimalFormat("#,##0").format(number)
        }
    }

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
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.background,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = scheme.onSurface,
                    titleContentColor = scheme.onSurface,
                    actionIconContentColor = Color.Unspecified
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
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Clickable Currency Selector (USD/KHR Switch)
                        CurrencySelector(
                            modifier = Modifier.width(240.dp),
                            selected = currency,
                            onSelected = {
                                currency = it
                            }
                        )
                        Spacer(Modifier.height(32.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                formatAmount(amountStr, currency = currency),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.primary,
                                    letterSpacing = (-1.5).sp
                                )
                            )
                            Text(
                                "|", style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Light,
                                    color = scheme.primary.copy(alpha = 0.5f)
                                ), modifier = Modifier
                                    .padding(start = 2.dp)
                                    .alpha(cursorAlpha)
                            )
                        }
                    }
                }
                // ── Category Grid ───────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
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
                        val today = remember {
                            "Today, " + SimpleDateFormat("MMM d yyyy", Locale.US).format(Date())
                        }
                        Text(
                            today,
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
                                onSaveExpense(amountVal, selectedCategory, note, currency.name)
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

enum class Currency {
    KHR, USD
}

@Composable
fun CurrencySelector(
    modifier: Modifier = Modifier,
    selected: Currency,
    onSelected: (Currency) -> Unit
) {
    val items = Currency.entries

    val scheme = MaterialTheme.colorScheme

    BoxWithConstraints(
        modifier = modifier
            .wrapContentWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(scheme.surfaceVariant)
            .padding(3.dp)
    ) {
        val itemWidth = maxWidth / items.size

        val offset by animateDpAsState(
            targetValue = itemWidth * items.indexOf(selected),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = ""
        )

        Card(
            modifier = Modifier
                .offset(x = offset)
                .width(itemWidth)
                .fillMaxHeight(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = scheme.primary
            )
        ) {}

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            items.forEach { currency ->

                val textColor by animateColorAsState(
                    if (currency == selected)
                        scheme.onPrimary
                    else
                        scheme.onSurfaceVariant,
                    label = ""
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(
                            indication = ripple(
                                bounded = true,
                                radius = 24.dp
                            ),
                            interactionSource = remember {
                                MutableInteractionSource()
                            }
                        ) {
                            onSelected(currency)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currency.name,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}