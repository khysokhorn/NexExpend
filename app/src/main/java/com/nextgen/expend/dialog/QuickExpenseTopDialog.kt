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
private val AccentCyan = Color(0xFF00E5CC)
private val AccentTeal = Color(0xFF00B4D8)
private val TextPrimary = Color(0xFFF0F0F5)
private val TextSecondary = Color(0xFFA0A0B8)
private val BorderColor = Color(0xFF2E2F4A)
private val BorderFocused = Color(0xFF00E5CC)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickExpenseTopPopup(
    onSave: (amount: Double, category: Category, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Category.DINING) }
    var note by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var showCategorySelector by remember { mutableStateOf(false) }

    val amountFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        visible = true
        // Auto-request focus for amount input to trigger system keyboard
        amountFocusRequester.requestFocus()
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
                .border(1.dp, BorderColor, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = GlassBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentCyan)
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

                Spacer(Modifier.height(12.dp))

                // Input Controls Row
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
                            .border(1.5.dp, Color.White , CircleShape)
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

                    // Amount Text Input (Using system numeric keyboard)
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { input ->
                            // Numeric validation
                            if (input.isEmpty() || input.toDoubleOrNull() != null) {
                                amountStr = input
                            }
                        },
                        label = { Text("Amount ($)", style = TextStyle(fontSize = 11.sp)) },
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

                    // Note Text Input
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Remark", style = TextStyle(fontSize = 11.sp)) },
                        modifier = Modifier.weight(1.5f),
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
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (amountStr.toDoubleOrNull() != null && amountStr.toDoubleOrNull()!! > 0.0) {
                                    Brush.horizontalGradient(listOf(AccentCyan, AccentTeal))
                                } else {
                                    Brush.linearGradient(listOf(BorderColor, BorderColor))
                                }
                            )
                            .clickable(
                                enabled = amountStr.toDoubleOrNull() != null && amountStr.toDoubleOrNull()!! > 0.0
                            ) {
                                val amountVal = amountStr.toDoubleOrNull() ?: 0.0
                                onSave(amountVal, selectedCategory, note)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = "Save",
                            tint = if (amountStr.toDoubleOrNull() != null && amountStr.toDoubleOrNull()!! > 0.0) GlassBg else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Smoothly animated Category selector drawer/row
                AnimatedVisibility(visible = showCategorySelector) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(quickCategories) { cat ->
                                val isSelected = cat == selectedCategory
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) AccentCyan.copy(alpha = 0.15f)
                                            else Color.Transparent
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) AccentCyan else BorderColor,
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
                                            tint = if (isSelected) AccentCyan else TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = cat.label,
                                            color = if (isSelected) AccentCyan else TextSecondary,
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
