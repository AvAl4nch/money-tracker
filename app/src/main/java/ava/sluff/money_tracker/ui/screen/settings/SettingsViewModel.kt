package ava.sluff.money_tracker.ui.screen.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ava.sluff.money_tracker.data.datastore.SettingsDataStore
import ava.sluff.money_tracker.data.importer.OldDbImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val oldDbImporter: OldDbImporter
) : ViewModel() {

    val apiKey: StateFlow<String> = settingsDataStore.apiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
    val baseUrl: StateFlow<String> = settingsDataStore.baseUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsDataStore.DEFAULT_BASE_URL)
    val modelName: StateFlow<String> = settingsDataStore.modelName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
    val currency: StateFlow<String> = settingsDataStore.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsDataStore.DEFAULT_CURRENCY)

    fun saveApiKey(value: String) = viewModelScope.launch { settingsDataStore.setApiKey(value) }
    fun saveBaseUrl(value: String) = viewModelScope.launch { settingsDataStore.setBaseUrl(value) }
    fun saveModelName(value: String) = viewModelScope.launch { settingsDataStore.setModelName(value) }
    fun saveCurrency(value: String) = viewModelScope.launch { settingsDataStore.setCurrency(value) }

    val importMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)

    fun importOldDb(uri: Uri) {
        viewModelScope.launch {
            oldDbImporter.import(uri)
                .onSuccess { importMessage.tryEmit("Imported ${it.imported} transactions (${it.skipped} duplicates skipped)") }
                .onFailure { importMessage.tryEmit("Import failed: ${it.message}") }
        }
    }
}
