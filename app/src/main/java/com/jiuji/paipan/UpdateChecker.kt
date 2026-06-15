package com.jiuji.paipan

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object UpdateChecker {

    private const val CHECK_URL = "https://example.com/jiuji/version.json"

    fun checkSilently(context: Context) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(CHECK_URL).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body?.string() ?: return
                        val json = JSONObject(body)
                        val remoteVersion = json.getString("version")
                        val currentVersion = context.packageManager
                            .getPackageInfo(context.packageName, 0).versionName
                        if (compareVersion(remoteVersion, currentVersion) > 0) {
                            val downloadUrl = json.getString("url")
                            val notes = json.optString("notes", "")
                            val title = "發現新版本 v" + remoteVersion
                            val message = "更新內容：" + notes
                            val activity = context as? android.app.Activity
                            activity?.runOnUiThread {
                                AlertDialog.Builder(context)
                                    .setTitle(title)
                                    .setMessage(message)
                                    .setPositiveButton("立即更新") { _, _ ->
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                                        context.startActivity(intent)
                                    }
                                    .setNegativeButton("稍後再說", null)
                                    .show()
                            }
                        }
                    } catch (e: Exception) {}
                }
            })
        } catch (e: Exception) {}
    }

    private fun compareVersion(a: String, b: String): Int {
        val av = a.split(".").map { it.toIntOrNull() ?: 0 }
        val bv = b.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0..2) {
            val diff = (av.getOrElse(i) { 0 }) - (bv.getOrElse(i) { 0 })
            if (diff != 0) return diff
        }
        return 0
    }
}
