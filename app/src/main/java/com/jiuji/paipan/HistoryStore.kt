package com.jiuji.paipan

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryRecord(
    val id: Long,
    val savedAt: String,
    val inputLabel: String,   // e.g. "2026年6月16日  10:36"
    val summary: String,      // full nine-char summary string
    val nianJi: String,
    val yueJi: String,
    val riJi: String,
    val shiJi: String?,
    val lunarStr: String
)

object HistoryStore {
    private const val PREFS = "jiuji_history"
    private const val KEY = "records"
    private const val MAX = 100

    fun save(ctx: Context, year: Int, month: Int, day: Int, hour: Int?, minute: Int?,
             result: PaipanEngine.PaipanResult) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = loadJson(prefs)
        val timeStr = if (hour != null) {
            val min = minute ?: 0
            "%02d:%02d".format(hour, min)
        } else ""
        val inputLabel = "${year}年${month}月${day}日" + if (timeStr.isNotEmpty()) "  $timeStr" else ""
        val savedAt = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date())
        val obj = JSONObject().apply {
            put("id", System.currentTimeMillis())
            put("savedAt", savedAt)
            put("inputLabel", inputLabel)
            put("summary", result.summary())
            put("nianJi", result.nianJi)
            put("yueJi", result.yueJi)
            put("riJi", result.riJi)
            put("shiJi", result.shiJi ?: "")
            put("lunarStr", result.lunarStr)
        }
        // insert at front
        val newArr = JSONArray()
        newArr.put(obj)
        for (i in 0 until minOf(arr.length(), MAX - 1)) newArr.put(arr.get(i))
        prefs.edit().putString(KEY, newArr.toString()).apply()
    }

    fun load(ctx: Context): List<HistoryRecord> {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = loadJson(prefs)
        val list = mutableListOf<HistoryRecord>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(HistoryRecord(
                id = o.getLong("id"),
                savedAt = o.getString("savedAt"),
                inputLabel = o.getString("inputLabel"),
                summary = o.getString("summary"),
                nianJi = o.getString("nianJi"),
                yueJi = o.getString("yueJi"),
                riJi = o.getString("riJi"),
                shiJi = o.getString("shiJi").ifEmpty { null },
                lunarStr = o.getString("lunarStr")
            ))
        }
        return list
    }

    fun delete(ctx: Context, id: Long) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = loadJson(prefs)
        val newArr = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getLong("id") != id) newArr.put(o)
        }
        prefs.edit().putString(KEY, newArr.toString()).apply()
    }

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }

    private fun loadJson(prefs: android.content.SharedPreferences): JSONArray {
        val str = prefs.getString(KEY, null) ?: return JSONArray()
        return try { JSONArray(str) } catch (e: Exception) { JSONArray() }
    }
}
