package com.sss.constructionroster.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.datetime.LocalDate
import com.sss.constructionroster.DayEntry
import kotlinx.datetime.Month
import org.json.JSONObject
import com.sss.constructionroster.MonthImageSelection
import com.sss.constructionroster.ImageItem
import android.net.Uri

class PaymentDataManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "PaymentData",
        Context.MODE_PRIVATE
    )
    private val imagePreferences = context.getSharedPreferences("captured_images", Context.MODE_PRIVATE)

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
            put("imageUri", monthImage.imageItem.imageUri?.toString())
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
        val jsonString = sharedPreferences.getString(key, null) ?: return null
        return try {
            val json = JSONObject(jsonString)
            val month = Month.valueOf(json.getString("month"))
            val imageRes = json.getInt("imageRes")
            val imageTitle = json.getString("imageTitle")
            val imageUriString = json.optString("imageUri")
            val imageUri = if (imageUriString.isNotEmpty()) Uri.parse(imageUriString) else null
            val amountPerDay = json.getString("amountPerDay")
            
            val dayEntries = mutableMapOf<Int, DayEntry>()
            val entriesJson = json.getJSONObject("dayEntries")
            entriesJson.keys().forEach { dayKey ->
                val dayJson = entriesJson.getJSONObject(dayKey)
                dayEntries[dayKey.toInt()] = DayEntry(
                    date = LocalDate.parse(dayJson.getString("date")),
                    amountPaid = dayJson.getString("amountPaid"),
                    isAbsent = dayJson.getBoolean("isAbsent")
                )
            }
            
            MonthImageSelection(
                month = month,
                imageItem = ImageItem(imageRes, imageTitle, imageUri),
                amountPerDay = amountPerDay,
                dayEntries = dayEntries
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearData() {
        sharedPreferences.edit().clear().apply()
    }

    fun saveCapturedImage(imageItem: ImageItem) {
        val editor = imagePreferences.edit()
        editor.putString("image_uri_${imageItem.imageRes}", imageItem.imageUri.toString())
        editor.putString("image_title_${imageItem.imageRes}", imageItem.title)
        editor.apply()
    }

    fun loadCapturedImages(): List<ImageItem> {
        val capturedImages = mutableListOf<ImageItem>()
        imagePreferences.all.keys.filter { it.startsWith("image_uri_") }.forEach { key ->
            val imageRes = key.removePrefix("image_uri_").toInt()
            val imageUri = Uri.parse(imagePreferences.getString(key, ""))
            val title = imagePreferences.getString("image_title_$imageRes", "")
            if (imageUri != null && title != null) {
                capturedImages.add(ImageItem(imageRes, title, imageUri))
            }
        }
        return capturedImages
    }
} 