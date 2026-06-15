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

    private const val API_URL =
        "https://api.github.com/repos/longantam/jiuji-app/releases/latest"

    fun checkSilently(context: Context) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(API_URL).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body?.string() ?: return
                        val json = JSONObject(body)
                        val latestTag = json.getString("tag_name") // e.g. "v1.0.2"

                        val currentVersion = context.packageManager
                            .getPackageInfo(context.packageName, 0).versionName
                        val currentTag = if (currentVersion.startsWith("v")) currentVersion
                                         else "v$currentVersion"

                        if (latestTag != currentTag) {
                            val assets = json.getJSONArray("assets")
                            var apkUrl: String? = null
                            for (i in 0 until assets.length()) {
                                val asset = assets.getJSONObject(i)
                                if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                                    apkUrl = asset.getString("browser_download_url")
                                    break
                                }
                            }
                            if (apkUrl == null) return

                            val notes = json.optString("body", "").let {
                                if (it.isBlank()) "點擊下載安裝最新版本。" else it
                            }
                            val downloadUrl = apkUrl
                            val activity = context as? android.app.Activity
                            activity?.runOnUiThread {
                                AlertDialog.Builder(context)
                                    .setTitle("🆕 發現新版本 $latestTag")
                                    .setMessage(notes)
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
}
