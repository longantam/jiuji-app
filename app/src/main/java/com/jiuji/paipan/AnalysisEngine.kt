package com.jiuji.paipan
object AnalysisEngine {
    private val WUXING = mapOf("甲" to "木", "乙" to "木", "丙" to "火", "丁" to "火", "戊" to "土", "己" to "土", "庚" to "金", "辛" to "金", "壬" to "水", "癸" to "水", "巳" to "火", "午" to "火", "亥" to "水", "未" to "土", "酉" to "金", "戌" to "土", "火" to "火")
    private fun tianGanHe(g1: String, g2: String) = (g1 == "丙" && g2 == "辛") || (g1 == "辛" && g2 == "丙") || (g1 == "乙" && g2 == "庚") || (g1 == "庚" && g2 == "乙")
    private fun dizhiChong(z1: String, z2: String) = (z1 == "巳" && z2 == "亥") || (z1 == "亥" && z2 == "巳")
    private fun wuxingKe(f: String, t: String) = (WUXING[f] == "金" && WUXING[t] == "木") || (WUXING[f] == "火" && WUXING[t] == "金")
    private fun dizhiCangGan(dz: String) = when(dz) { "酉" -> listOf("辛"); "未" -> listOf("己", "丁", "乙"); else -> listOf() }
    data class AnalysisResult(val bingYin: String, val bingZheng: String, val bingJi: String, val bingWei: String, val zhiZe: String, val fangBian: String)
    fun analyze(m: String): AnalysisResult {
        val p = m.split("/").map { it.trim() }
        if (p.size < 7) return AnalysisResult("", "", "", "", "", "")
        val nG = p[0][0].toString(); val nZ = p[0][1].toString(); val yG = p[1][0].toString(); val yZ = p[1][1].toString()
        val dJ = p[2].replace("段極", ""); val xD = if (p.size >= 5) p[4].replace("小段", "") else ""
        val rG = p[5][0].toString(); val rZ = p[5][1].toString()
        val sG = if (p.size >= 7 && p[6] != "-") p[6][0].toString() else null
        val sZ = if (p.size >= 7 && p[6] != "-") p[6][1].toString() else null
        val zl = mutableListOf<String>(); var c = 1
        if (sG != null && sZ != null) {
            val r1 = mutableListOf<String>(); val m1 = mutableListOf<String>()
            if (xD.isNotEmpty() && WUXING[sZ] == WUXING[xD]) { r1.add(xD); m1.add("同支宜固") }
            if (dizhiChong(sZ, dJ)) { r1.add("${dJ}段極"); m1.add("克逆宜生") }
            if (r1.isNotEmpty()) { zl.add("${c}.時支${sZ}關聯${r1.joinToString("/")}不足，則時干${sG}亦應時失常。故分別以「${m1.joinToString("」和「")}」調治。"); c++ }
            val r2 = mutableListOf<String>(); val m2 = mutableListOf<String>(); val r3 = mutableListOf<String>()
            if (tianGanHe(sG, yG)) { r2.add("月干${yG}"); m2.add("同干宜養"); r3.add("月支${yZ}") }
            if (tianGanHe(sG, nG)) { r2.add("年干${nG}"); if ("同干宜養" !in m2) m2.add("同干宜養"); r3.add("年支${nZ}") }
            if (wuxingKe(sG, yG)) { if ("克逆宜生" !in m2) m2.add("克逆宜生") }
            if (wuxingKe(sG, nG)) { if ("克逆宜生" !in m2) m2.add("克逆宜生") }
            if (r2.isNotEmpty()) { zl.add("${c}.時干${sG}關聯${r2.joinToString("/")}失常，則時支${sZ}亦應${r3.joinToString("/")}不足。故分別以「${m2.joinToString("」和「")}」調治。"); c++ }
        }
        if (rG == nG || tianGanHe(rG, nG)) { zl.add("${c}.日干${rG}應年干${nG}失常，則日支${rZ}亦應年支${nZ}不足。故以「同干宜養」調治。") }
        val fb = mutableListOf<String>(); for (cg in dizhiCangGan(rZ)) { if (cg in dizhiCangGan(nZ)) fb.add("${cg}藏於地支，謹防傳變") }
        return AnalysisResult("應時異常", p[5], sG?.let { p[6] } ?: p[5], if (p.size >= 8) p[7] else "未定", zl.joinToString("\n"), if (fb.isNotEmpty()) fb.joinToString("；") else "無特殊防變")
    }
}
