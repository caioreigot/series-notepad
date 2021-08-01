package com.github.caioreigot.seriesnotepad.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class SharedPrefs(context: Context) {

    companion object {
        val STORE_FILE_NAME_KEY = "series_notepad_data"
    }

    private val sharedPreferences =
        context.getSharedPreferences(STORE_FILE_NAME_KEY, Context.MODE_PRIVATE)

    private val editor = sharedPreferences.edit()

    fun <T> setList(key: String?, list: List<T>?) {
        val gson = Gson()
        val json = gson.toJson(list)
        set(key, json)
    }

    private operator fun set(key: String?, value: String?) {
        editor.putString(key, value)
        editor.commit()
    }

    fun getList(): MutableList<MainInformation?>? {
        val arrayItems: MutableList<MainInformation?>
        val serializedObject = sharedPreferences.getString(STORE_FILE_NAME_KEY, null)

        if (serializedObject != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<MutableList<MainInformation?>?>() {}.type

            arrayItems = gson.fromJson(serializedObject, type)
            return arrayItems
        }

        return null
    }
}