package com.jiuji.paipan

/**
 * 應象時空調治分析引擎 v6
 *
 * 治則合併規則：
 * 1. 時支關聯小段極/大段極→合併為一句
 *    「時支 X 關聯 A/B 不足，則時干 Y 亦應時失常。
 *    故分別以『同支宜固』和『克逆宜生』調治。」
 * 2. 時干關聯月/年干→合併為一句
 *    「時干 Y 關聯月/年干 A/B 失常，則時支 X 亦應月/年支 a/b 不足。
 *    故分別以『...』和『...』調治。」
 * 3. 日干關聯年干
 *    「日干 X 應年干 Y 失常，則日支 a 亦應年支 b 不足。
 *    故以『同干宜養』調治。」
 */
object AnalysisEngine {

    // 天干克逆：對（乙-辛, 丙-壬, 丁-癸, 甲-庚）
    private val TG_CHONG = setOf(
        setOf("乙", "辛"), setOf("丙", "壬"), setOf("丁", "癸"), setOf("甲", "庚")
    )

    // 地支克逆：對（子-午, 丑-未, 寅-申, 卯-酉, 辰-戌, 巳-亥）
    private val DZ_CHONG = setOf(
        setOf("子", "午"), setOf("丑", "未"), setOf("寅", "申"),
        setOf("卯", "酉"), setOf("辰", "戌"), setOf("巳", "亥")
    )

    private fun isTgKe(a: String, b: String) = TG_CHONG.any { it == setOf(a, b) }
    private fun isDzKe(a: String, b: String) = DZ_CHONG.any { it == setOf(a, b) }
    private fun isTgSame(a: String, b: String) = a.isNotEmpty() && a == b
    private fun isDzSame(a: String, b: String) = a.isNotEmpty() && a == b

    // 地支藏干
    private fun cangGan(dz: String): List<String> = when (dz) {
        "子" -> listOf("癸")
        "丑" -> listOf("巹", "辛", "癸")
        "寅" -> listOf("甲", "丙", "戊")
        "卯" -> listOf("乙")
        "辰" -> listOf("戊", "癸", "乙")
        "巳" -> listOf("丙", "庚", "戊")
        "午" -> listOf("丁", "巹")
        "未" -> listOf("巹", "丁", "乙")
        "申" -> listOf("庚", "戊", "壬")
        "酉" -> listOf("辛")
        "戌" -> listOf("戊", "辛", "丁")
        "亥" -> listOf("壬", "甲")
        else -> emptyList()
    }

    // 天干顯現地支表
    private val TG_XIAN: Map<String, List<String>> = mapOf(
        "甲" to listOf("寅", "亥"),
        "乙" to listOf("辰", "未", "卯"),
        "丙" to listOf("巳", "寅"),
        "丁" to listOf("戌", "未"),
        "戊" to listOf("辰", "戌", "巳", "寅", "申"),
        "巹" to listOf("丑", "未", "午"),
        "庚" to listOf("申", "巳"),
        "辛" to listOf("丑", "酉", "戌"),
        "壬" to listOf("亥", "申"),
        "癸" to listOf("丑", "辰", "子")
    )

    enum class RelationType { TONG_GAN, YI_GAN, TONG_ZHI, YI_ZHI, KE_NI_GAN, KE_NI_ZHI }

    data class Relation(
        val type: RelationType,
        val humanPole: String,
        val outerPole: String,
        val detail: String
    )

    data class AnalysisResult(
        val bingYin: String,
        val bingZheng: String,
        val bingJi: String,
        val bingWei: String,
        val zhiZe: String,
        val fangBian: String,
        val shichang: List<String> = emptyList(),
        val buzu: List<String> = emptyList(),
        val fourLineHints: List<String> = emptyList(),
        val relations: List<Relation> = emptyList()
    )

    // 輸入格式：「年極：乙未 / 月極：丙戌 / 大段：亥 / 中段：午 / 小段：火 / 日極：乙酉 / 時極：辛巳」
    // 或簡化：「乙未 / 丙戌 / 亥 / 午 / 火 / 乙酉 / 辛巳」
    fun analyze(input: String): AnalysisResult {
        // 支持兩種格式：「年極：乙未」 或 「乙未」
        val raw = input.replace("：", ":").replace("「", "").replace("」", "")
        val parts = raw.split("/", "/ ").map {
            it.replace("年極:", "").replace("月極:", "").replace("大段:", "")
              .replace("中段:", "").replace("小段:", "").replace("日極:", "").replace("時極:", "")
              .trim()
        }
        if (parts.size < 6) return AnalysisResult("輸入不足六段", "", "", "", "", "")

        fun g(s: String) = if (s.length >= 1) s[0].toString() else ""
        fun z(s: String) = if (s.length >= 2) s[1].toString() else ""
        // 對於只有一個字符的段極（如「火」「亥」），直接當地支處理
        fun zOnly(s: String) = if (s.length == 1) s else z(s)

        val nGZ = parts[0]; val yGZ = parts[1]
        val ddRaw = parts[2]; val zdRaw = parts[3]; val xdRaw = parts[4]
        val rGZ  = parts[5]
        val sGZ  = parts.getOrNull(6)?.let { if (it.isBlank() || it == "X" || it == "-") null else it }
        val dkRaw = parts.getOrNull(7)?.let { if (it.isBlank() || it == "X" || it == "-") null else it }

        val nG = g(nGZ); val nZ = z(nGZ)
        val yG = g(yGZ); val yZ = z(yGZ)
        val rG = g(rGZ); val rZ = z(rGZ)
        val sG = sGZ?.let { g(it) } ?: ""
        val sZ = sGZ?.let { z(it) } ?: ""

        val ddZ = zOnly(ddRaw)   // 大段地支
        val zdZ = zOnly(zdRaw)   // 中段地支
        val xdZ = xdRaw.let {    // 小段地支（如「火」 → 對應巳/午 —— 这裡直接用字面）
            if (it.length == 1) it else zOnly(it)
        }

        // ===================== 治則生成 =====================
        val zhiZeLines = mutableListOf<String>()
        var counter = 1
        val bingYinParts = mutableListOf<String>()
        val relationList = mutableListOf<Relation>()

        // ---- 規則 1：時支 Z 關聯 小段極/大段極 ----
        if (sZ.isNotEmpty()) {
            val zhiTargets = mutableListOf<String>()  // 外極地支名稱
            val methods   = mutableListOf<String>()   // 調治方法

            // 小段極：同支宜固
            if (xdZ.isNotEmpty() && isDzSame(sZ, xdZ)) {
                zhiTargets.add(xdZ)
                methods.add("同支宜固")
                bingYinParts.add("時支${sZ}與小段極${xdZ}同支")
                relationList.add(Relation(RelationType.TONG_ZHI, "時極", "小段", "時支${sZ}與小段極${xdZ}同支"))
            }
            // 小段極：克逆宜生
            if (xdZ.isNotEmpty() && isDzKe(sZ, xdZ)) {
                zhiTargets.add(xdZ)
                if ("克逆宜生" !in methods) methods.add("克逆宜生")
                bingYinParts.add("時支${sZ}與小段極${xdZ}克逆")
                relationList.add(Relation(RelationType.KE_NI_ZHI, "時極", "小段", "時支${sZ}與小段極${xdZ}克逆"))
            }
            // 大段極：克逆宜生
            if (ddZ.isNotEmpty() && isDzKe(sZ, ddZ)) {
                zhiTargets.add("${ddZ}段極")
                if ("克逆宜生" !in methods) methods.add("克逆宜生")
                bingYinParts.add("時支${sZ}與大段極${ddZ}克逆")
                relationList.add(Relation(RelationType.KE_NI_ZHI, "時極", "大段", "時支${sZ}與大段極${ddZ}克逆"))
            }

            if (zhiTargets.isNotEmpty()) {
                val mStr = methods.joinToString("』和『", "『", "』")
                zhiZeLines.add(
                    "${counter}.時支${sZ}關聯${zhiTargets.joinToString("/")}不足，則時干${sG}亦應時失常。" +
                    "故分別以${mStr}調治。"
                )
                counter++
            }
        }

        // ---- 規則 2：時干 G 關聯 月干/年干 ----
        if (sG.isNotEmpty()) {
            val ganTargets    = mutableListOf<String>()   // 外極干名稱（如「月干丙」）
            val outerZhiNames = mutableListOf<String>()   // 外極支名稱（如「月支戌」）
            val methods2      = mutableListOf<String>()

            // 月干：同干宜養
            if (yG.isNotEmpty() && isTgSame(sG, yG)) {
                ganTargets.add("月干${yG}")
                outerZhiNames.add("月支${yZ}")
                if ("同干宜養" !in methods2) methods2.add("同干宜養")
                bingYinParts.add("時干${sG}與月干${yG}同干")
                relationList.add(Relation(RelationType.TONG_GAN, "時極", "月極", "時干${sG}與月干${yG}同干"))
            }
            // 年干：同干宜養
            if (nG.isNotEmpty() && isTgSame(sG, nG)) {
                ganTargets.add("年干${nG}")
                outerZhiNames.add("年支${nZ}")
                if ("同干宜養" !in methods2) methods2.add("同干宜養")
                bingYinParts.add("時干${sG}與年干${nG}同干")
                relationList.add(Relation(RelationType.TONG_GAN, "時極", "年極", "時干${sG}與年干${nG}同干"))
            }
            // 月干：克逆宜生
            if (yG.isNotEmpty() && isTgKe(sG, yG)) {
                if ("月干${yG}" !in ganTargets) ganTargets.add("月干${yG}")
                if ("月支${yZ}" !in outerZhiNames) outerZhiNames.add("月支${yZ}")
                if ("克逆宜生" !in methods2) methods2.add("克逆宜生")
                bingYinParts.add("時干${sG}與月干${yG}失常（克逆）")
                relationList.add(Relation(RelationType.KE_NI_GAN, "時極", "月極", "時干${sG}與月干${yG}克逆"))
            }
            // 年干：克逆宜生
            if (nG.isNotEmpty() && isTgKe(sG, nG)) {
                if ("年干${nG}" !in ganTargets) ganTargets.add("年干${nG}")
                if ("年支${nZ}" !in outerZhiNames) outerZhiNames.add("年支${nZ}")
                if ("克逆宜生" !in methods2) methods2.add("克逆宜生")
                bingYinParts.add("時干${sG}與年干${nG}失常（克逆）")
                relationList.add(Relation(RelationType.KE_NI_GAN, "時極", "年極", "時干${sG}與年干${nG}克逆"))
            }

            if (ganTargets.isNotEmpty()) {
                val mStr = methods2.joinToString("』和『", "『", "』")
                zhiZeLines.add(
                    "${counter}.時干${sG}關聯${ganTargets.joinToString("/")}失常，則時支${sZ}亦應${outerZhiNames.joinToString("/")}不足。" +
                    "故分別以${mStr}調治。"
                )
                counter++
            }
        }

        // ---- 規則 3：日干 G 關聯 年干 ----
        if (rG.isNotEmpty() && nG.isNotEmpty()) {
            val r3methods = mutableListOf<String>()
            var triggered = false
            if (isTgSame(rG, nG)) { r3methods.add("同干宜養"); triggered = true; bingYinParts.add("日干${rG}與年干${nG}同干"); relationList.add(Relation(RelationType.TONG_GAN, "日極", "年極", "日干${rG}與年干${nG}同干")) }
            if (isTgKe(rG, nG)) { if ("克逆宜生" !in r3methods) r3methods.add("克逆宜生"); triggered = true; bingYinParts.add("日干${rG}與年干${nG}失常（克逆）"); relationList.add(Relation(RelationType.KE_NI_GAN, "日極", "年極", "日干${rG}與年干${nG}克逆")) }
            if (triggered) {
                val mStr = r3methods.joinToString("』和『", "『", "』")
                zhiZeLines.add(
                    "${counter}.日干${rG}應年干${nG}失常，則日支${rZ}亦應年支${nZ}不足。故以${mStr}調治。"
                )
                counter++
            }
        }

        // ===================== 其他字段 =====================
        val bingYin   = bingYinParts.distinct().joinToString("；").ifEmpty { "未發現克逆失常" }
        val bingZheng = if (rGZ.isNotEmpty()) "${rGZ}症" else "未定"
        val bingJi    = if (sGZ != null) "${sGZ}病機" else if (rGZ.isNotEmpty()) "${rGZ}為本" else "未定"
        val bingWei   = if (dkRaw != null) "${dkRaw}位" else "未定"
        val zhiZe     = zhiZeLines.joinToString("\n").ifEmpty { "平調即可" }

        // 防變
        val fangBianList = mutableListOf<String>()
        val allZhi = listOf(rZ, sZ, nZ, yZ, ddZ, zdZ, xdZ).filter { it.isNotEmpty() && it.length == 1 }
        for (zh in allZhi.distinct()) {
            for (cg in cangGan(zh)) {
                val xianZhi = TG_XIAN[cg] ?: emptyList()
                if (xianZhi.any { it in allZhi }) {
                    val msg = "${cg}藏於地支${zh}，已顯於地支，謹防傳變"
                    if (msg !in fangBianList) fangBianList.add(msg)
                }
            }
        }
        val fangBian = fangBianList.distinct().joinToString("；").ifEmpty { "暫無明顯傳變風險" }

        return AnalysisResult(
            bingYin   = bingYin,
            bingZheng = bingZheng,
            bingJi    = bingJi,
            bingWei   = bingWei,
            zhiZe     = zhiZe,
            fangBian  = fangBian,
            relations = relationList
        )
    }
}
