package com.jiuji.paipan

object AnalysisEngine {

    // ── 天干關係表 ──────────────────────────────────────────────────────────
    // 相逆（互為逆）
    private val TG_NI = mapOf(
        "甲" to "乙", "乙" to "甲",
        "丙" to "丁", "丁" to "丙",
        "戊" to "己", "己" to "戊",
        "庚" to "辛", "辛" to "庚",
        "壬" to "癸", "癸" to "壬"
    )

    // 相克（A 克 B）
    private val TG_KE = mapOf(
        "甲" to "庚", "乙" to "辛",
        "丙" to "壬", "丁" to "癸",
        "戊" to "戊", "己" to "己",
        "庚" to "甲", "辛" to "乙",
        "壬" to "丙", "癸" to "丁"
    )

    // 相合（六合：甲己、乙庚、丙辛、丁壬、戊癸）
    private val TG_HE = mapOf(
        "甲" to "己", "己" to "甲",
        "乙" to "庚", "庚" to "乙",
        "丙" to "辛", "辛" to "丙",
        "丁" to "壬", "壬" to "丁",
        "戊" to "癸", "癸" to "戊"
    )

    // 四行療法：天干宜調治對象（兩字字串，第一字和第二字分別為兩個調治對象）
    private val TG_SIHANG = mapOf(
        "甲" to "己庚", "乙" to "己丁",
        "丙" to "辛庚", "丁" to "壬己",
        "庚" to "壬辛", "辛" to "戊癸",
        "壬" to "甲丁", "癸" to "戊乙"
    )

    // ── 地支關係表 ──────────────────────────────────────────────────────────
    // 相逆（可一對多）
    private val DZ_NI = mapOf(
        "子"  to listOf("亥"),
        "丑"  to listOf("戌", "辰"),
        "寅"  to listOf("卯"),
        "卯"  to listOf("寅"),
        "辰"  to listOf("丑", "未"),
        "巳"  to listOf("午"),
        "午"  to listOf("巳"),
        "未"  to listOf("辰", "戌"),
        "申"  to listOf("酉"),
        "酉"  to listOf("申"),
        "戌"  to listOf("未", "丑"),
        "亥"  to listOf("子")
    )

    // 相克（A 克 B）
    private val DZ_KE = mapOf(
        "子" to "午", "丑" to "未", "寅" to "申",
        "卯" to "酉", "辰" to "戌", "巳" to "亥",
        "午" to "子", "未" to "丑", "申" to "寅",
        "酉" to "卯", "戌" to "辰", "亥" to "巳"
    )

    // 四行療法：地支宜調治對象（兩字字串）
    private val DZ_SIHANG = mapOf(
        "子" to "己癸", "寅" to "壬甲",
        "卯" to "癸乙", "巳" to "庚丙",
        "午" to "己丁", "申" to "丙庚",
        "酉" to "戊辛", "亥" to "甲壬"
    )

    // ── 地支藏干 ────────────────────────────────────────────────────────────
    private fun cangGan(dz: String): List<String> = when (dz) {
        "子" -> listOf("癸")
        "丑" -> listOf("己", "辛", "癸")
        "寅" -> listOf("甲", "丙", "戊")
        "卯" -> listOf("乙")
        "辰" -> listOf("戊", "癸", "乙")
        "巳" -> listOf("丙", "庚", "戊")
        "午" -> listOf("乙")
        "未" -> listOf("己", "丁", "乙")
        "申" -> listOf("庚", "戊", "壬")
        "酉" -> listOf("辛")
        "戌" -> listOf("戊", "辛", "丁")
        "亥" -> listOf("壬", "甲")
        else -> emptyList()
    }

    // 天干顯象地支（哪些地支能「顯」此天干）
    private val TG_XIAN_ZHI = mapOf(
        "甲" to listOf("寅", "亥"),
        "乙" to listOf("辰", "未", "卯"),
        "丙" to listOf("巳", "寅"),
        "丁" to listOf("戌", "未"),
        "戊" to listOf("辰", "戌", "巳", "寅", "申"),
        "己" to listOf("丑", "未", "午"),
        "庚" to listOf("申", "巳"),
        "辛" to listOf("丑", "酉", "戌"),
        "壬" to listOf("亥", "申"),
        "癸" to listOf("丑", "辰", "子")
    )

    // ── 判斷函數 ─────────────────────────────────────────────────────────────

    /** 天干「克逆」：包括相克和相逆 */
    private fun isTgKeNi(a: String, b: String): Boolean =
        TG_KE[a] == b || TG_KE[b] == a || TG_NI[a] == b

    /** 天干「同干」：相同或相合 */
    private fun isTgTong(a: String, b: String): Boolean =
        a == b || TG_HE[a] == b

    /** 地支「克逆」：包括相克和相逆 */
    private fun isDzKeNi(a: String, b: String): Boolean =
        DZ_KE[a] == b || DZ_KE[b] == a || DZ_NI[a]?.contains(b) == true

    /** 地支「同支」：相同或相逆，且 a 的藏干皆未顯於三極任何地支 */
    private fun isDzTong(a: String, b: String, allZhi: Set<String>): Boolean {
        val sameOrNi = a == b || DZ_NI[a]?.contains(b) == true
        if (!sameOrNi) return false
        val cg = cangGan(a)
        for (g in cg) {
            val xian = TG_XIAN_ZHI[g] ?: emptyList()
            if (xian.any { it in allZhi }) return false
        }
        return true
    }

    // ── 枚舉：五種關係/治則 ──────────────────────────────────────────────────
    enum class RelationType { TONG_GAN, YI_GAN, TONG_ZHI, YI_ZHI, KE_NI }

    data class Relation(
        val poleLabel: String,
        val humanPole: String,
        val otherPole: String,
        val type: RelationType
    )

    data class AnalysisResult(
        val bingYin: String,
        val bingZheng: String,
        val bingJi: String,
        val bingWei: String,
        val zhiZe: String,
        val fangBian: String,
        val relations: List<Relation> = emptyList(),
        val fourLineHints: List<String> = emptyList()
    )

    /**
     * 輸入格式（以「/」分隔，共7段）：
     * 年極干支 / 月極干支 / 大段地支 / 中段地支 / 小段地支 / 日極干支 / 時極干支
     * 例：乙未 / 丙戌 / 亥 / 午 / 火 / 乙酉 / 辛巳
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

        val nGZ  = parts[0]; val yGZ = parts[1]
        val ddRaw = parts[2]; val zdRaw = parts[3]; val xdRaw = parts[4]
        val rGZ  = parts[5]
        val sGZ  = parts.getOrNull(6)?.trim()
            ?.let { if (it.isBlank() || it == "X" || it == "-") null else it }
        val dkRaw = parts.getOrNull(7)?.trim()
            ?.let { if (it.isBlank() || it == "X" || it == "-") null else it }

        val nG = g(nGZ); val nZ = z(nGZ)
        val yG = g(yGZ); val yZ = z(yGZ)
        val rG = g(rGZ); val rZ = z(rGZ)
        val sG = sGZ?.let { g(it) } ?: ""
        val sZ = sGZ?.let { z(it) } ?: ""
        val ddZ = if (ddRaw.length == 1) ddRaw else z(ddRaw)
        val zdZ = if (zdRaw.length == 1) zdRaw else z(zdRaw)
        val xdZ = if (xdRaw.length == 1) xdRaw else z(xdRaw)

        // 三極全部地支
        val allZhi = setOf(nZ, yZ, ddZ, zdZ, xdZ, rZ, sZ).filter { it.isNotEmpty() }.toSet()
        // 三極全部天干
        val allGan = setOf(nG, yG, rG, sG).filter { it.isNotEmpty() }.toSet()

        val zhiZeLines = mutableListOf<String>()
        val bingYinParts = mutableListOf<String>()
        val relations = mutableListOf<Relation>()
        var counter = 1

        // 輔助：天干關係類型
        fun ganRelType(human: String, other: String): RelationType? {
            if (other.isEmpty()) return null
            if (isTgTong(human, other)) return RelationType.TONG_GAN
            if (isTgKeNi(human, other)) return RelationType.KE_NI
            return RelationType.YI_GAN
        }

        // 輔助：地支關係類型
        fun zhiRelType(human: String, other: String): RelationType? {
            if (other.isEmpty()) return null
            if (isDzTong(human, other, allZhi)) return RelationType.TONG_ZHI
            if (isDzKeNi(human, other)) return RelationType.KE_NI
            return RelationType.YI_ZHI
        }

        // 治則標籤
        fun relLabel(rt: RelationType) = when (rt) {
            RelationType.TONG_GAN -> "同干宜養"
            RelationType.YI_GAN   -> "異干宜化"
            RelationType.TONG_ZHI -> "同支宜固"
            RelationType.YI_ZHI   -> "異支宜平"
            RelationType.KE_NI    -> "克逆宜生"
        }

        // ═══════════════════════════════════════════════════════════════════════
        // 規則 A：時極天干 → 輻射 月極天干、年極天干
        // ═══════════════════════════════════════════════════════════════════════
        if (sG.isNotEmpty()) {
            val hits = mutableListOf<Pair<String, RelationType>>()
            for ((og, oLabel) in listOf(yG to "月干${yG}", nG to "年干${nG}")) {
                val rt = ganRelType(sG, og)
                if (rt != null) {
                    hits.add(og to rt)
                    relations.add(Relation(oLabel, "時干${sG}", oLabel, rt))
                    bingYinParts.add("時干${sG}與${oLabel}${relLabel(rt)}")
                }
            }
            if (hits.isNotEmpty()) {
                val mStr = hits.map { relLabel(it.second) }.distinct()
                    .joinToString("」和「", "「", "」")
                val oStr = listOf(
                    if (hits.any { it.first == yG }) "月干${yG}" else "",
                    if (hits.any { it.first == nG }) "年干${nG}" else ""
                ).filter { it.isNotEmpty() }.joinToString("/")
                zhiZeLines.add("${counter}.時干${sG}關聯${oStr}失常，則時支${sZ}亦應對應地支不足，以${mStr}調治。")
                counter += 1
            }
        }

        // ═══════════════════════════════════════════════════════════════════════
        // 規則 B：時極地支 → 輻射 小段極、中段極、大段極地支
        // ═══════════════════════════════════════════════════════════════════════
        if (sZ.isNotEmpty()) {
            val hits = mutableListOf<Pair<String, RelationType>>()
            for ((oz, oLabel) in listOf(
                xdZ to "小段極${xdZ}",
                zdZ to "中段極${zdZ}",
                ddZ to "大段極${ddZ}"
            )) {
                if (oz.isEmpty()) continue
                val rt = zhiRelType(sZ, oz)
                if (rt != null) {
                    hits.add(oz to rt)
                    relations.add(Relation(oLabel, "時支${sZ}", oLabel, rt))
                    bingYinParts.add("時支${sZ}與${oLabel}${relLabel(rt)}")
                }
            }
            if (hits.isNotEmpty()) {
                val mStr = hits.map { relLabel(it.second) }.distinct()
                    .joinToString("」和「", "「", "」")
                val oStr = hits.map { (oz, _) ->
                    when (oz) {
                        xdZ -> "小段極${xdZ}"
                        zdZ -> "中段極${zdZ}"
                        else -> "大段極${ddZ}"
                    }
                }.joinToString("/")
                zhiZeLines.add("${counter}.時支${sZ}關聯${oStr}不足，則時干${sG}亦應時失常，以${mStr}調治。")
                counter += 1
            }
        }

        // ═══════════════════════════════════════════════════════════════════════
        // 規則 C：日極天干 → 輻射 月極天干、年極天干
        // ═══════════════════════════════════════════════════════════════════════
        if (rG.isNotEmpty()) {
            val hits = mutableListOf<Pair<String, RelationType>>()
            for ((og, oLabel) in listOf(yG to "月干${yG}", nG to "年干${nG}")) {
                val rt = ganRelType(rG, og)
                if (rt != null) {
                    hits.add(og to rt)
                    relations.add(Relation(oLabel, "日干${rG}", oLabel, rt))
                    bingYinParts.add("日干${rG}與${oLabel}${relLabel(rt)}")
                }
            }
            if (hits.isNotEmpty()) {
                val mStr = hits.map { relLabel(it.second) }.distinct()
                    .joinToString("」和「", "「", "」")
                val oStr = listOf(
                    if (hits.any { it.first == yG }) "月干${yG}" else "",
                    if (hits.any { it.first == nG }) "年干${nG}" else ""
                ).filter { it.isNotEmpty() }.joinToString("/")
                zhiZeLines.add("${counter}.日干${rG}關聯${oStr}失常，則日支${rZ}亦應地支不足，以${mStr}調治。")
                counter += 1
            }
        }

        // ═══════════════════════════════════════════════════════════════════════
        // 規則 D：日極地支 → 輻射 月極地支、年極地支
        // ═══════════════════════════════════════════════════════════════════════
        if (rZ.isNotEmpty()) {
            val hits = mutableListOf<Pair<String, RelationType>>()
            for ((oz, oLabel) in listOf(yZ to "月支${yZ}", nZ to "年支${nZ}")) {
                if (oz.isEmpty()) continue
                val rt = zhiRelType(rZ, oz)
                if (rt != null) {
                    hits.add(oz to rt)
                    relations.add(Relation(oLabel, "日支${rZ}", oLabel, rt))
                    bingYinParts.add("日支${rZ}與${oLabel}${relLabel(rt)}")
                }
            }
            if (hits.isNotEmpty()) {
                val mStr = hits.map { relLabel(it.second) }.distinct()
                    .joinToString("」和「", "「", "」")
                val oStr = listOf(
                    if (hits.any { it.first == yZ }) "月支${yZ}" else "",
                    if (hits.any { it.first == nZ }) "年支${nZ}" else ""
                ).filter { it.isNotEmpty() }.joinToString("/")
                zhiZeLines.add("${counter}.日支${rZ}關聯${oStr}不足，則日干${rG}亦應天干失常，以${mStr}調治。")
                @Suppress("UNUSED_VALUE")
                counter += 1
            }
        }

        // ── 四行療法提示 ──────────────────────────────────────────────────────
        val fourLineHints = mutableListOf<String>()
        for (g in listOf(sG, rG).filter { it.isNotEmpty() }.distinct()) {
            TG_SIHANG[g]?.let { t ->
                fourLineHints.add("天干${g} → 宜調治「${t[0]}」和「${t[1]}」之應象")
            }
        }
        for (z in listOf(sZ, rZ).filter { it.isNotEmpty() }.distinct()) {
            DZ_SIHANG[z]?.let { t ->
                fourLineHints.add("地支${z} → 宜調治「${t[0]}」和「${t[1]}」之應象")
            }
        }

        // ── 防變提示 ──────────────────────────────────────────────────────────
        val fangBianList = mutableListOf<String>()
        for (zh in allZhi) {
            for (cg in cangGan(zh)) {
                val xian = TG_XIAN_ZHI[cg] ?: emptyList()
                if (xian.any { it in allZhi && it != zh }) {
                    val msg = "${cg}藏於${zh}，已顯於地支，謹防傳變"
                    if (msg !in fangBianList) fangBianList.add(msg)
                }
                if (cg in allGan) {
                    val msg = "${cg}藏於${zh}，已顯於天干，謹防傳變"
                    if (msg !in fangBianList) fangBianList.add(msg)
                }
            }
        }
        val fangBian = fangBianList.joinToString("；").ifEmpty { "暫無明顯傳變風險" }

        val bingYin   = bingYinParts.distinct().joinToString("；").ifEmpty { "未發現干支關聯失常" }
        val bingZheng = if (sGZ != null) "${sGZ}時應象失常" else "${rGZ}日本症"
        val bingJi    = if (sGZ != null) "時支${sZ}段不足，時干${sG}失常為病機" else "${rGZ}為本，${rZ}位不足"
        val bingWei   = dkRaw?.let { "${it}位" } ?: (if (sZ.isNotEmpty()) "${sZ}位" else "${rZ}位")
        val zhiZe     = zhiZeLines.joinToString("\n").ifEmpty { "無明顯克逆，平調即可" }

        return AnalysisResult(
            bingYin       = bingYin,
            bingZheng     = bingZheng,
            bingJi        = bingJi,
            bingWei       = bingWei,
            zhiZe         = zhiZe,
            fangBian      = fangBian,
            relations     = relations,
            fourLineHints = fourLineHints
        )
    }
}
