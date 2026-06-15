package com.jiuji.paipan

object AnalysisEngine {
    data class AnalysisResult(
        val bingYin: String,
        val bingZheng: String,
        val bingJi: String,
        val bingWei: String,
        val zhiZe: String,
        val fangBian: String
    )

    fun analyze(model: String): AnalysisResult {
        val parts = model.split(" / ")
        if (parts.size < 7) {
            return AnalysisResult("輸入格式錯誤", "", "", "", "", "")
        }
        
        val riGZ = parts[5]
        val shiGZ = if (parts.size > 6 && parts[6] != "-" && parts[6] != "X") parts[6] else null
        
        return AnalysisResult(
            bingYin = "時空關聯分析中",
            bingZheng = riGZ + "症",
            bingJi = (shiGZ ?: riGZ) + "為本",
            bingWei = shiGZ ?: "未明確",
            zhiZe = "同干宜養\n異干宜化\n同支宜固\n異支宜平\n克逆宜生",
            fangBian = "請注意藏干傳變"
        )
    }
}
