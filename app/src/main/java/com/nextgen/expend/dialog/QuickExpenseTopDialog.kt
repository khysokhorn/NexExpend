package com.nextgen.expend.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextgen.expend.data.model.Category

// --- Ultra-premium compact color tokens ---
private val GlassBg = Color(0xE60F101A) // Semi-transparent ultra dark
private val AccentCyan = Color.Black
private val TextPrimary = Color(0xFFF0F0F5)
private val TextSecondary = Color(0xFFA0A0B8)
private val BorderColor = Color(0xFF2E2F4A)
private val BorderFocused = Color.White
private val AiAccent = Color(0xFF7B68EE) // Soft purple to signal AI-generated data

private val quickCategories = listOf(
    Category.DINING,
    Category.SHOPPING,
    Category.TRANSPORT,
    Category.GROCERIES,
    Category.BILLS,
    Category.HEALTH,
    Category.FUN,
    Category.OTHER
)

/**
 * Bottom-sheet style overlay popup for quick expense entry.
 *
 * When [isFromNotification] is true, the popup is operating in **AI Preview mode**:
 * - The header shows a purple "Transaction Preview · AI" label.
 * - Fields pre-filled from the LLM are shown with a subtle AI badge.
 * - The category selector expands automatically if [initialCategory] is null or OTHER.
 * - Focus is set to the Amount field when [initialAmount] is null, otherwise to Remark
 *   when [initialNote] is blank.
 *
 * @param initialAmount   Amount extracted by the LLM, or null if unknown.
 * @param initialCategory Category inferred by the LLM, or null if unknown.
 * @param initialNote     Remark / merchant name extracted by the LLM.
 * @param isFromNotification True when this popup was triggered by a bank notification.
 * @param onSave   Callback invoked when the user confirms the transaction.
 * @param onDismiss Callback invoked when the user dismisses the popup.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickExpenseTopPopup(
    initialAmount: Double? = null,
    initialCategory: Category? = null,
    initialNote: String = "",
    isFromNotification: Boolean = false,
    onSave: (amount: Double, category: Category, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    // ---- State ----
    var amountStr by remember(initialAmount) {
        mutableStateOf(initialAmount?.let {
            if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
        } ?: "")
    }
    var selectedCategory by remember(initialCategory) {
        mutableStateOf(initialCategory ?: Category.DINING)
    }
    var note by remember(initialNote) { mutableStateOf(initialNote) }
    var visible by remember { mutableStateOf(false) }

    // Auto-expand category row if LLM couldn't determine a category
    val categoryMissing = initialCategory == null || initialCategory == Category.OTHER
    var showCategorySelector by remember(initialCategory) {
        mutableStateOf(isFromNotification && categoryMissing)
    }

    val amountFocusRequester = remember { FocusRequester() }
    val remarkFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        visible = true
    }

    // Request focus on the appropriate field based on what data is missing
    LaunchedEffect(isFromNotification, initialAmount, initialNote) {
        if (isFromNotification) {
            when {
                initialAmount == null -> amountFocusRequester.requestFocus()
                initialNote.isBlank() -> remarkFocusRequester.requestFocus()
                else -> amountFocusRequester.requestFocus()
            }
        } else {
            amountFocusRequester.requestFocus()
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = AccentCyan,
        focusedBorderColor = BorderFocused,
        unfocusedBorderColor = BorderColor,
        focusedLabelColor = AccentCyan,
        unfocusedLabelColor = TextSecondary,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
            initialOffsetY = { it }
        ) + fadeIn(animationSpec = tween(200)),
        exit = slideOutVertically(
            animationSpec = tween(150),
            targetOffsetY = { it }
        ) + fadeOut(animationSpec = tween(100))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    color = if (isFromNotification) AiAccent.copy(alpha = 0.5f) else BorderColor,
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // ---- Header Row ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isFromNotification) {
                            // AI Preview indicator
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = "AI Preview",
                                tint = AiAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Transaction Preview · AI",
                                color = AiAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.2.sp
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Quick Expense",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // AI hint banner — tells user to verify the parsed data
                if (isFromNotification) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Review and confirm the details below before saving.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.1.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ---- Input Controls Row ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category circular selector button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GlassBg.copy(alpha = 0.12f))
                            .border(
                                width = 1.5.dp,
                                color = if (categoryMissing && isFromNotification) AiAccent else Color.White,
                                shape = CircleShape
                            )
                            .clickable { showCategorySelector = !showCategorySelector },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            selectedCategory.icon,
                            contentDescription = selectedCategory.label,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Amount Text Input
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.toDoubleOrNull() != null) {
                                amountStr = input
                            }
                        },
                        label = {
                            Text(
                                text = if (isFromNotification && initialAmount == null) "Amount (⚠ Missing)" else "Amount ($)",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = if (isFromNotification && initialAmount == null) AiAccent else TextSecondary
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .focusRequester(amountFocusRequester),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    )

                    // Note / Remark Text Input
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = {
                            Text(
                                text = if (isFromNotification && initialNote.isBlank()) "Remark (⚠ Missing)" else "Remark",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = if (isFromNotification && initialNote.isBlank()) AiAccent else TextSecondary
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .focusRequester(remarkFocusRequester),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val amountVal = amountStr.toDoubleOrNull() ?: 0.0
                                if (amountVal > 0.0) {
                                    onSave(amountVal, selectedCategory, note)
                                }
                            }
                        ),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextPrimary
                        )
                    )

                    // Quick Save FAB style button
                    val canSave = amountStr.toDoubleOrNull()?.let { it > 0.0 } == true
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (canSave) {
                                    Brush.horizontalGradient(listOf(Color.Black, Color.White))
                                } else {
                                    Brush.linearGradient(listOf(BorderColor, BorderColor))
                                }
                            )
                            .clickable(enabled = canSave) {
                                val amountVal = amountStr.toDoubleOrNull() ?: 0.0
                                onSave(amountVal, selectedCategory, note)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = "Save",
                            tint = if (canSave) GlassBg else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // ---- Category selector drawer/row ----
                AnimatedVisibility(visible = showCategorySelector) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White, thickness = 0.5.dp)
                        if (isFromNotification && categoryMissing) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "AI couldn't determine the category — please select one:",
                                color = AiAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(quickCategories) { cat ->
                                val isSelected = cat == selectedCategory
                                val selectedColor = Color.White
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) selectedColor.copy(alpha = 0.15f)
                                            else Color.Transparent
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) selectedColor else BorderColor,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedCategory = cat
                                            showCategorySelector = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            cat.icon,
                                            contentDescription = cat.label,
                                            tint = if (isSelected) selectedColor else TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = cat.label,
                                            color = if (isSelected) selectedColor else TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
