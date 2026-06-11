package ava.sluff.money_tracker.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val apiKeyKey = stringPreferencesKey("api_key")
    private val baseUrlKey = stringPreferencesKey("base_url")
    private val modelNameKey = stringPreferencesKey("model_name")
    private val currencyKey = stringPreferencesKey("currency")

    val apiKey: Flow<String> = context.dataStore.data.map { it[apiKeyKey] ?: "" }
    val baseUrl: Flow<String> = context.dataStore.data.map {
        it[baseUrlKey]?.takeIf { v -> v.isNotBlank() } ?: DEFAULT_BASE_URL
    }
    val modelName: Flow<String> = context.dataStore.data.map { it[modelNameKey] ?: "" }
    val currency: Flow<String> = context.dataStore.data.map {
        it[currencyKey]?.takeIf { v -> v.isNotBlank() } ?: DEFAULT_CURRENCY
    }

    suspend fun setApiKey(value: String) = context.dataStore.edit { it[apiKeyKey] = value }
    suspend fun setBaseUrl(value: String) = context.dataStore.edit { it[baseUrlKey] = value }
    suspend fun setModelName(value: String) = context.dataStore.edit { it[modelNameKey] = value }
    suspend fun setCurrency(value: String) = context.dataStore.edit { it[currencyKey] = value }

    companion object {
        const val DEFAULT_BASE_URL = "https://openrouter.ai/api"
        const val DEFAULT_CURRENCY = "JOD"
    }
}
