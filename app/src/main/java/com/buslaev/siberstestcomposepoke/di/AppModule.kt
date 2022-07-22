package com.buslaev.siberstestcomposepoke.di

import android.content.Context
import androidx.room.Room
import com.buslaev.siberstestcomposepoke.data.locale.dao.AppDao
import com.buslaev.siberstestcomposepoke.data.locale.database.AppDatabase
import com.buslaev.siberstestcomposepoke.data.remote.PokeApi
import com.buslaev.siberstestcomposepoke.domain.repository.PokeRepository
import com.buslaev.siberstestcomposepoke.domain.repository.PokeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePokeApi(
        @Named("base_url") baseUrl: String
    ): PokeApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(PokeApi::class.java)

    @Provides
    @Singleton
    @Named("base_url")
    fun provideBaseUrl(): String = "https://pokeapi.co/api/v2/"

    @Provides
    @Singleton
    fun provideRepostiory(api: PokeApi, dao: AppDao): PokeRepository =
        PokeRepositoryImpl(api = api, dao = dao)

    @Provides
    @Singleton
    fun proviedDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_database"
    ).build()

    @Provides
    @Singleton
    fun provideDao(dataBase: AppDatabase): AppDao = dataBase.appDao()
}