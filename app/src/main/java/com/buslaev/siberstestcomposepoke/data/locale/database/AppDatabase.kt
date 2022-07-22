package com.buslaev.siberstestcomposepoke.data.locale.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.buslaev.siberstestcomposepoke.data.locale.converters.Converters
import com.buslaev.siberstestcomposepoke.data.locale.dao.AppDao
import com.buslaev.siberstestcomposepoke.data.models.Pokemon

@Database(
    entities = [Pokemon::class],
    version = 1,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}