package com.nextgen.expend.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(val label: String, val icon: ImageVector) {
    DINING("Dining",       Icons.Outlined.Restaurant),
    SHOPPING("Shopping",   Icons.Outlined.ShoppingBag),
    TRANSPORT("Transport", Icons.Outlined.DirectionsCar),
    GROCERIES("Groceries", Icons.Outlined.LocalGroceryStore),
    BILLS("Bills",         Icons.Outlined.ReceiptLong),
    HEALTH("Health",       Icons.Outlined.FitnessCenter),
    FUN("Fun",             Icons.Outlined.MusicNote),
    HOUSING("Housing",     Icons.Outlined.Home),
    INCOME("Income",       Icons.Outlined.AccountBalance),
    OTHER("Other",         Icons.Outlined.MoreHoriz),
}
