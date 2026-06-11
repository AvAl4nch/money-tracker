package ava.sluff.money_tracker.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Atm
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector

fun categoryIcon(name: String?): ImageVector = when (name) {
    "shopping_cart" -> Icons.Default.ShoppingCart
    "directions_car" -> Icons.Default.DirectionsCar
    "restaurant" -> Icons.Default.Restaurant
    "movie" -> Icons.Default.Movie
    "shopping_bag" -> Icons.Default.ShoppingBag
    "local_hospital" -> Icons.Default.LocalHospital
    "receipt_long" -> Icons.Default.ReceiptLong
    "school" -> Icons.Default.School
    "swap_horiz" -> Icons.Default.SwapHoriz
    "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
    "atm" -> Icons.Default.Atm
    else -> Icons.Default.MoreHoriz
}
