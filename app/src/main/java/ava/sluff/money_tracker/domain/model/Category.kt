package ava.sluff.money_tracker.domain.model

data class Category(
    val id: Long = 0L,
    val name: String,
    val icon: String,
    val color: Long,
    val isDefault: Boolean = true,
    val sortOrder: Int = 0
)
