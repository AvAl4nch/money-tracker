package ava.sluff.money_tracker.data.repository

import ava.sluff.money_tracker.data.local.dao.CategoryDao
import ava.sluff.money_tracker.data.local.entity.CategoryEntity
import ava.sluff.money_tracker.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {

    fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { list -> list.map { it.toDomain() } }

    suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)?.toDomain()

    suspend fun getCategoryByName(name: String): CategoryEntity? = categoryDao.getCategoryByName(name)
}

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    color = color,
    isDefault = isDefault,
    sortOrder = sortOrder
)
