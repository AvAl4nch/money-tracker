package ava.sluff.money_tracker.di

import ava.sluff.money_tracker.ai.LlmClient
import ava.sluff.money_tracker.ai.QueryRunner
import ava.sluff.money_tracker.data.datastore.SettingsDataStore
import ava.sluff.money_tracker.data.remote.LlmApiService
import ava.sluff.money_tracker.data.remote.Message
import ava.sluff.money_tracker.data.repository.AnalyticsRepository
import ava.sluff.money_tracker.data.repository.QueryResult
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AskModule {

    @Provides
    @Singleton
    fun provideLlmClient(
        api: LlmApiService,
        settings: SettingsDataStore
    ): LlmClient = object : LlmClient {
        override suspend fun complete(messages: List<Message>, temperature: Double): String? =
            api.chatCompletion(
                baseUrl = settings.baseUrl.first(),
                apiKey = settings.apiKey.first(),
                model = settings.modelName.first(),
                messages = messages,
                temperature = temperature
            )
    }

    @Provides
    @Singleton
    fun provideQueryRunner(repository: AnalyticsRepository): QueryRunner = object : QueryRunner {
        override suspend fun run(sql: String): QueryResult = repository.runReadOnlyQuery(sql)
    }
}
