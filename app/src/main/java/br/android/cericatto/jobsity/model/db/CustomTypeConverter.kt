package br.android.cericatto.jobsity.model.db

import androidx.room.TypeConverter
import br.android.cericatto.jobsity.model.api.Image
import br.android.cericatto.jobsity.model.api.Schedule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class CustomTypeConverter {
    var gson = Gson()
    inline fun <reified T> genericType() = object: TypeToken<T>() {}.type

    @TypeConverter
    fun stringToScheduleBean(data: String?): Schedule? {
        val type = genericType<Schedule>()
        return gson.fromJson(data, type)
    }

    @TypeConverter
    fun scheduleBeanToString(schedule: Schedule?): String? {
        val gson = Gson()
        return gson.toJson(schedule)
    }

    @TypeConverter
    fun stringToImageBean(data: String?): Image? {
        val type = genericType<Image>()
        return gson.fromJson(data, type)
    }

    @TypeConverter
    fun imageBeanToString(imageBean: Image?): String? {
        val gson = Gson()
        return gson.toJson(imageBean)
    }

    @TypeConverter
    fun fromString(value: String?): List<String>? {
        val listType: Type = object : TypeToken<ArrayList<String>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: List<String>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }
}