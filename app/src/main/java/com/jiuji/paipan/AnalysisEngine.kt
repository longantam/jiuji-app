package com.jiuji.paipan

object AnalysisEngine {

    // 天干克逆對（乙-辛, 丙-壬, 丁-癸, 甲-庚）
    private val TG_KE_PAIRS = setOf(
        setOf("乙", "辛"), setOf("丙", "壬"), setOf("丁", "癸"), setOf("甲", "庚")
    )

    // 地支克逆對（子-午, 丑-未, 寅-申, 卯-酉, 辰-戌, 巳-亥）
    private val DZ_KE_PAIRS = setOf(
        setOf("子", "午"), setOf("丑", "未"), setOf("寅", "申"),
        setOf("卯", "酉"), setOf("辰", "戌"), setOf("巳", "亥")
    )

    private fun isTgKe(a: String, b: String) = TG_KE_PAIRS.any { it == setOf(a, b) }
    private fun isDzKe(a: String, b: String) = DZ_KE_PAIRS.any { it == setOf(a, b) }

    // 地支藏干
    private fun cangGan(dz: String): List<String> = when (dz) {
        "子" -> listOf("癸")
        "丑" -> listOf("己", "辛", "癸")
        "寅" -> listOf("甲", "丙", "戊")
        "卯" -> listOf("乙")
        "辰" -> listOf("戊", "乙", "癸")
        "巳" -> listOf("丙", "庚", "戊")
        "午" -> listOf("丁", "己")
        "未" -> listOf("己", "丁", "乙")
        "申" -> listOf("庚", "壬", "戊")
        "酉" -> listOf("辛")
        "戌" -> listOf("戊", "辛", "丁")
        "亥" -> listOf("壬", "甲")
        else -> emptyList()
    }

    data class AnalysisResult(
        val bingYin: String,
        val bingZheng: String,
        val bingJi: String,
        val bingWei: String,
        val zhiZe: String,
        val fangBian: String
    )

    /**
     * 輸入格式（以「/」分隔，共7-8段）：
     * 年極干支 / 月極干支 / 大段地支 / 中段地支 / 小段地支 / 日極干支 / 時極干支 [/ 大刻干支]
     * 例：乙未 / 丙戌 / 亥 / 午 / 火 / 乙酉 / 辛巳
     * 亦接受「年極：乙未 / 月極：丙戌 / ...」格式
     */
    fun analyze(input: String): AnalysisResult {
        val cleaned = input
            .replace("：", ":")
            .replace("年極:", "").replace("月極:", "").replace("大段:", "")
            .replace("中段:", "").replace("小段:", "").replace("日極:", "").replace("時極:", "")
        val parts = cleaned.split("/").map { it.trim() }

        if (parts.size < 6) return AnalysisResult("輸入不足六段", "", "", "", "", "")

        fun g(s: String) = if (s.isNotEmpty()) s[0].toString() else ""
        fun z(s: String) = if (s.length >= 2) s[1].toString() else if (s.length == 1) s else ""

        val nGZ = parts[0]; val yGZ = parts[1]
        val ddRaw = parts[2]; val zdRaw = parts[3]; val xdRaw = parts[4]
        val rGZ = parts[5]
        val sGZ = parts.getOrNull(6)?.trim()
            ?.let { if (it.isBlank() || it == "X" || it == "-") null else it }
        val dkRaw = parts.getOrNull(7)?.trim()
            ?.let { if (it.isBlank() || it == "X" || it == "-") null else it }

        val nG = g(nGZ); val nZ = z(nGZ)
        val yG = g(yGZ); val yZ = z(yGZ)
        val rG = g(rGZ); val rZ = z(rGZ)
        val sG = sGZ?.let { g(it) } ?: ""
        val sZ = sGZ?.let { z(it) } ?: ""

        // 段極地支（單字直接用，雙字取第二字）
        val ddZ = z(ddRaw)
        val zdZ = z(zdRaw)
        val xdZ = z(xdRaw)

        val zhiZeLines = mutableListOf<String>()
        var counter = 1
        val bingYinParts = mutableListOf<String>()

        // ---- 規則1：時支 關聯 小段極/大段極 ----
        if (sZ.isNotEmpty()) {
            val targets = mutableListOf<String>()
            val methods = mutableListOf<String>()

            if (xdZ.isNotEmpty() && sZ == xdZ) {
                targets.add(xdZ)
                methods.add("同支宜固")
                bingYinParts.add("時支${sZ}與小段極${xdZ}同支")
            }
            if (xdZ.isNotEmpty() && isDzKe(sZ, xdZ)) {
                if (xdZ !in targets) targets.add(xdZ)
                if ("克逆宜生" !in methods) methods.add("克逆宜生")
                bingYinParts.add("時支${sZ}與小段極${xdZ}克逆")
            }
            if (ddZ.isNotEmpty() && isDzKe(sZ, ddZ)) {
                targets.add("${ddZ}段極")
                if ("克逆宜生" !in methods) methods.add("克逆宜生")
                bingYinParts.add("時支${sZ}與大段極${ddZ}克逆")
            }

            if (targets.isNotEmpty()) {
                val mStr = methods.joinToString("」和「", "「", "」")
                zhiZeLines.add(
                    "${counter}.時支${sZ}關聯${targets.joinToString("/")}不足，則時干${sG}亦應時失常。" +
                    "故分別以${mStr}調治。"
                )
                counter++
            }
        }

        // ---- 規則2：時干 關聯 月干/年干 ----
        if (sG.isNotEmpty()) {
            val ganTargets = mutableListOf<String>()
            val zhiTargets = mutableListOf<String>()
            val methods2 = mutableListOf<String>()

            if (yG.isNotEmpty() && sG == yG) {
                ganTargets.add("月干${yG}")
                zhiTargets.add("月支${yZ}")
                if ("同干宜養" !in methods2) methods2.add("同干宜養")
                bingYinParts.add("時干${sG}與月干${yG}同干")
            }
            if (nG.isNotEmpty() && sG == nG) {
                ganTargets.add("年干${nG}")
                zhiTargets.add("年支${nZ}")
                if ("同干宜養" !in methods2) methods2.add("同干宜養")
                bingYinParts.add("時干${sG}與年干${nG}同干")
            }
            if (yG.isNotEmpty() && isTgKe(sG, yG)) {
                if ("月干${yG}" !in ganTargets) { ganTargets.add("月干${yG}"); zhiTargets.add("月支${yZ}") }
                if ("克逆宜生" !in methods2) methods2.add("克逆宜生")
                bingYinParts.add("時干${sG}與月干${yG}克逆")
            }
            if (nG.isNotEmpty() && isTgKe(sG, nG)) {
                if ("年干${nG}" !in ganTargets) { ganTargets.add("年干${nG}"); zhiTargets.add("年支${nZ}") }
                if ("克逆宜生" !in methods2) methods2.add("克逆宜生")
                bingYinParts.add("時干${sG}與年干${nG}克逆")
            }

            if (ganTargets.isNotEmpty()) {
                val mStr = methods2.joinToString("」和「", "「", "」")
                zhiZeLines.add(
                    "${counter}.時干${sG}關聯${ganTargets.joinToString("/")}失常，則時支${sZ}亦應${zhiTargets.joinToString("/")}不足。" +
                    "故分別以${mStr}調治。"
                )
                counter++
            }
        }

        // ---- 規則3：日干 關聯 年干 ----
        if (rG.isNotEmpty() && nG.isNotEmpty()) {
            val methods3 = mutableListOf<String>()
            var hit = false
            if (rG == nG) {
                methods3.add("同干宜養"); hit = true
                bingYinParts.add("日干${rG}與年干${nG}同干")
            }
            if (isTgKe(rG, nG)) {
                if ("克逆宜生" !in methods3) methods3.add("克逆宜生"); hit = true
                bingYinParts.add("日干${rG}與年干${nG}克逆")
            }
            if (hit) {
                val mStr = methods3.joinToString("」和「", "「", "」")
                zhiZeLines.add(
                    "${counter}.日干${rG}應年干${nG}失常，則日支${rZ}亦應年支${nZ}不足。故以${mStr}調治。"
                )
            }
        }

        val bingYin   = bingYinParts.distinct().joinToString("；").ifEmpty { "未發現克逆失常" }
        val bingZheng = if (rGZ.isNotEmpty()) "${rGZ}症" else "未定"
        val bingJi    = if (sGZ != null) "${sGZ}病機" else "${rGZ}為本"
        val bingWei   = dkRaw?.let { "${it}位" } ?: "未定"
        val zhiZe     = zhiZeLines.joinToString("\n").ifEmpty { "平調即可" }

        // 防變
        val allZhi = listOf(rZ, sZ, nZ, yZ, ddZ, zdZ, xdZ).filter { it.length == 1 }.distinct()
        val fangBianList = mutableListOf<String>()
        for (zh in allZhi) {
            for (cg in cangGan(zh)) {
                val xianZhiList = mapOf(
                    "甲" to listOf("寅", "亥"), "乙" to listOf("辰", "未", "卯"),
                    "丙" to listOf("巳", "寅"), "丁" to listOf("戌", "未"),
                    "戊" to listOf("辰", "戌", "巳", "寅", "申"), "己" to listOf("丑", "未", "午"),
                    "庚" to listOf("申", "巳"), "辛" to listOf("丑", "酉", "戌"),
                    "壬" to listOf("亥", "申"), "癸" to listOf("丑", "辰", "子")
                )[cg] ?: emptyList()
                if (xianZhiList.any { it in allZhi }) {
                    val msg = "${cg}藏於地支${zh}，已顯於地支，謹防傳變"
                    if (msg !in fangBianList) fangBianList.add(msg)
                }
            }
        }
        val fangBian = fangBianList.joinToString("；").ifEmpty { "暫無明顯傳變風險" }

        return AnalysisResult(
            bingYin   = bingYin,
            bingZheng = bingZheng,
            bingJi    = bingJi,
            bingWei   = bingWei,
            zhiZe     = zhiZe,
            fangBian  = fangBian
        )
    }
}
