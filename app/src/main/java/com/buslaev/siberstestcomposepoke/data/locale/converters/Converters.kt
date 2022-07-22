package com.buslaev.siberstestcomposepoke.data.locale.converters

import androidx.room.TypeConverter
import com.buslaev.siberstestcomposepoke.data.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun spiritesToString(spirites: Spirites): String = spirites.image

    @TypeConverter
    fun stringToSpirites(value: String): Spirites = Spirites(image = value)

    @TypeConverter
    fun statsToString(stats: List<Stat>): String = Gson().toJson(stats)

    @TypeConverter
    fun stringToStats(value: String): List<Stat> {
        val listType = object : TypeToken<List<Stat>>() {}.type
        return Gson().fromJson<List<Stat>?>(value, listType).toList()
    }

    @TypeConverter
    fun typeToString(stats: List<Type>): String = Gson().toJson(stats)

    @TypeConverter
    fun stringToType(value: String): List<Type> {
        val listType = object : TypeToken<List<Type>>() {}.type
        return Gson().fromJson<List<Type>?>(value, listType).toList()
    }


//    @TypeConverter
//    fun statToString(stat: Stat): String = "${stat.statNamed.name} ${stat.value}"
//
//    @TypeConverter
//    fun stringToStat(value: String): Stat {
//        val list = value.split(" ")
//        return Stat(statNamed = StatNamed(list[0]), value = list[1].toInt())
//    }
//
//    @TypeConverter
//    fun typeToString(type: Type): String = type.desctiption.name
//
//    @TypeConverter
//    fun stringToType(value: String): Type = Type(desctiption = Description(name = value))
}