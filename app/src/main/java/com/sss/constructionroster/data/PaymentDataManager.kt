package com.sss.constructionroster.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.datetime.LocalDate
import com.sss.constructionroster.DayEntry
import kotlinx.datetime.Month
import org.json.JSONObject
import com.sss.constructionroster.MonthImageSelection
import com.sss.constructionroster.ImageItem

class PaymentDataManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "PaymentData",
        Context.MODE_PRIVATE
    )

    private fun getKey(imageRes: Int, month: Month): String {
        return "monthData_${imageRes}_${month.name}"
    }

    fun saveMonthImageSelection(monthImage: MonthImageSelection) {
        val editor = sharedPreferences.edit()
        val key = getKey(monthImage.imageItem.imageRes, monthImage.month)
        
        // Create JSON object for the month data
        val monthData = JSONObject().apply {
            put("month", monthImage.month.name)
            put("imageRes", monthImage.imageItem.imageRes)
            put("imageTitle", monthImage.imageItem.title)
            put("amountPerDay", monthImage.amountPerDay)
            
            // Convert day entries to JSON
            val entriesJson = JSONObject()
            monthImage.dayEntries.forEach { (day, entry) ->
                entriesJson.put(day.toString(), JSONObject().apply {
                    put("date", entry.date.toString())
                    put("amountPaid", entry.amountPaid)
                    put("isAbsent", entry.isAbsent)
                })
            }
            put("dayEntries", entriesJson)
        }

        editor.putString(key, monthData.toString())
        editor.apply()
    }

    fun loadMonthImageSelection(imageRes: Int? = null, month: Month? = null): MonthImageSelection? {
        if (imageRes == null && month == null) {
            // Load last used data
            val lastKey = sharedPreferences.all.keys.firstOrNull { it.startsWith("monthData_") }
                ?: return null
            return loadMonthImageSelectionByKey(lastKey)
        } else if (imageRes != null && month != null) {
            // Load specific month data for specific image
            val key = getKey(imageRes, month)
            return loadMonthImageSelectionByKey(key)
        } else if (imageRes != null) {
            // Load latest month data for specific image
            val imageKeys = sharedPreferences.all.keys.filter { 
                it.startsWith("monthData_${imageRes}_") 
            }
            return imageKeys.lastOrNull()?.let { loadMonthImageSelectionByKey(it) }
        }
        return null
    }

    fun loadAllMonthsForImage(imageRes: Int): List<MonthImageSelection> {
        return sharedPreferences.all.keys
            .filter { it.startsWith("monthData_${imageRes}_") }
            .mapNotNull { loadMonthImageSelectionByKey(it) }
    }

    private fun loadMonthImageSelectionByKey(key: String): MonthImageSelection? {
        val monthDataString = sharedPreferences.getString(key, null) ?: return null
        
        try {
            val monthData = JSONObject(monthDataString)
            
            // Parse basic data
            val month = Month.valueOf(monthData.getString("month"))
            val imageRes = monthData.getInt("imageRes")
            val imageTitle = monthData.getString("imageTitle")
            val amountPerDay = monthData.getString("amountPerDay")
            
            // Parse day entries
            val entriesJson = monthData.getJSONObject("dayEntries")
            val dayEntries = mutableMapOf<Int, DayEntry>()
            
            entriesJson.keys().forEach { dayKey ->
                val entryJson = entriesJson.getJSONObject(dayKey)
                val date = LocalDate.parse(entryJson.getString("date"))
                val entry = DayEntry(
                    date = date,
                    amountPaid = entryJson.getString("amountPaid"),
                    isAbsent = entryJson.getBoolean("isAbsent")
                )
                dayEntries[dayKey.toInt()] = entry
            }
            
            return MonthImageSelection(
                month = month,
                imageItem = ImageItem(imageRes, imageTitle),
                amountPerDay = amountPerDay,
                dayEntries = dayEntries
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun clearData() {
        sharedPreferences.edit().clear().apply()
    }
} 