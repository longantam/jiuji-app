package com.jiuji.paipan

/**
 * 應象時空分析引擎
 *
 * 分析次序（由近及遠）：
 *   人極：刻極 → 時極天干 → 時極地支 → 日極天干 → 日極地支
 *   輻射：段極 → 月極天干 → 月極地支 → 年極天干 → 年極地支
 *
 * 判定：
 *   發生兩兩氣感關聯的人極天干 → 應時失常
 *   發生兩兩氣感關聯的人極地支 → 應時不足
 *
 * 調治：
 *   同干宜養 / 異干宜化 / 同支宜固 / 異支宜平 / 克逆宜生
 */
object AnalysisEngine {

    // ── 基礎常量 ──────────────────────────────────────────
    private val TG = listOf("甲","乙","丙","丁","戊","己","庚","辛","壬","癸")
    private val DZ = listOf("子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥")

    /** 天干六合（甲己、乙庚、丙辛、丁壬、戊癸） */
    private val STEM_HE: Map<String,String> = mapOf(
        "甲" to "己","己" to "甲",
        "乙" to "庚","庚" to "乙",
        "丙" to "辛","辛" to "丙",
        "丁" to "壬","壬" to "丁",
        "戊" to "癸","癸" to "戊"
    )

    /** 地支六合（子丑、寅亥、卯戌、辰酉、巳申、午未） */
    private val BRANCH_HE: Map<String,String> = mapOf(
        "子" to "丑","丑" to "子",
        "寅" to "亥","亥" to "寅",
        "卯" to "戌","戌" to "卯",
        "辰" to "酉","酉" to "辰",
        "巳" to "申","申" to "巳",
        "午" to "未","未" to "午"
    )

    /** 地支六沖（子午、丑未、寅申、卯酉、辰戌、巳亥） */
    private val BRANCH_CHONG: Map<String,String> = mapOf(
        "子" to "午","午" to "子",
        "丑" to "未","未" to "丑",
        "寅" to "申","申" to "寅",
        "卯" to "酉","酉" to "卯",
        "辰" to "戌","戌" to "辰",
        "巳" to "亥","亥" to "巳"
    )

    /** 五行相克（克者 → 被克者） */
    private val STEM_KE: Map<String,String> = mapOf(
        "甲" to "戊","乙" to "己",  // 木克土
        "丙" to "庚","丁" to "辛",  // 火克金
        "戊" to "壬","己" to "癸",  // 土克水
        "庚" to "甲","辛" to "乙",  // 金克木
        "壬" to "丙","癸" to "丁"   // 水克火
    )

    /** 四行療法對照表（天干/地支 → 宜調治之干） */
    val FOUR_LINE_MAP: Map<String, String> = mapOf(
        "甲" to "己、庚",
        "乙" to "己、丁",
        "丙" to "辛、庚",
        "丁" to "壬、己",
        "庚" to "壬、辛",
        "辛" to "戊、癸",
        "壬" to "甲、丁",
        "癸" to "戊、乙",
        "子" to "己、癸",
        "寅" to "壬、甲",
        "卯" to "癸、乙",
        "巳" to "庚、丙",
        "午" to "己、丁",
        "申" to "丙、庚",
        "酉" to "戊、辛",
        "亥" to "甲、壬"
    )

    // ── 資料類 ────────────────────────────────────────────
    data class GanZhi(val stem: String?, val branch: String?)

    enum class RelationType { TONG_GAN, YI_GAN, TONG_ZHI, YI_ZHI, KE_NI }

    data class Relation(
        val type: RelationType,
        val humanPole: String,   // 人極哪個干/支
        val otherPole: String,   // 對應的天極/地極哪個干/支
        val poleLabel: String    // 月極/年極/段極
    )

    data class AnalysisResult(
        val bingYin: String,
        val bingZheng: String,
        val bingJi: String,
        val bingWei: String,
        val zhiZe: String,
        val fangBian: String,
        val relations: List<Relation> = emptyList(),
        val shichang: List<String> = emptyList(),   // 應時失常（天干）
        val buzu: List<String> = emptyList(),        // 應時不足（地支）
        val fourLineHints: List<String> = emptyList()
    )

    // ── 核心工具函數 ──────────────────────────────────────
    fun splitGZ(gz: String?): GanZhi {
        if (gz.isNullOrBlank() || gz == "-" || gz.uppercase() == "X") return GanZhi(null, null)
        val chars = gz.trim()
        val stem   = if (chars.isNotEmpty() && TG.contains(chars[0].toString())) chars[0].toString() else null
        val branch = if (chars.length > 1 && DZ.contains(chars[1].toString())) chars[1].toString() else null
        return GanZhi(stem, branch)
    }

    /** 判斷兩天干是否「同干」（相同或相合） */
    fun isTongGan(a: String, b: String) = a == b || STEM_HE[a] == b

    /** 判斷兩天干是否「克逆」（相克） */
    fun isKeNiStem(a: String, b: String) = STEM_KE[a] == b || STEM_KE[b] == a

    /** 判斷兩地支是否「同支」（相同或六合） */
    fun isTongZhi(a: String, b: String) = a == b || BRANCH_HE[a] == b

    /** 判斷兩地支是否「克逆」（六沖） */
    fun isKeNiBranch(a: String, b: String) = BRANCH_CHONG[a] == b

    /**
     * 比對一個「人極干支」與一個「外極干支」，返回所有關聯類型。
     * 順序：先判克逆，再判同/異。
     */
    fun detectRelations(
        human: GanZhi,
        other: GanZhi,
        poleLabel: String
    ): List<Relation> {
        val result = mutableListOf<Relation>()

        // 天干比對
        val hs = human.stem; val os = other.stem
        if (hs != null && os != null) {
            when {
                isKeNiStem(hs, os) -> result.add(Relation(RelationType.KE_NI, hs, os, poleLabel))
                isTongGan(hs, os)  -> result.add(Relation(RelationType.TONG_GAN, hs, os, poleLabel))
                else               -> result.add(Relation(RelationType.YI_GAN, hs, os, poleLabel))
            }
        }

        // 地支比對
        val hb = human.branch; val ob = other.branch
        if (hb != null && ob != null) {
            when {
                isKeNiBranch(hb, ob) -> result.add(Relation(RelationType.KE_NI, hb, ob, poleLabel))
                isTongZhi(hb, ob)    -> result.add(Relation(RelationType.TONG_ZHI, hb, ob, poleLabel))
                else                 -> result.add(Relation(RelationType.YI_ZHI, hb, ob, poleLabel))
            }
        }

        return result
    }

    /**
     * 將 RelationType 轉為調治原則文字
     */
    fun relTypeToZhiZe(type: RelationType): String = when(type) {
        RelationType.TONG_GAN -> "同干宜養（育也，畜也，長也）"
        RelationType.YI_GAN   -> "異干宜化（因時而化）"
        RelationType.TONG_ZHI -> "同支宜固（坚固，稳固）"
        RelationType.YI_ZHI   -> "異支宜平（平者，和也）"
        RelationType.KE_NI    -> "克逆宜生（從干支本源起也）"
    }

    // ── 主分析函數 ────────────────────────────────────────
    /**
     * 按標準格式輸入模型字串：
     * 年極 / 月極 / 大段 / 中段 / 小段(五行) / 日極 / 時極 / 大刻 / 小刻
     *
     * 分析次序：
     *   人極（日極、時極）依次與段極、月極、年極兩兩比對
     */
    fun analyze(model: String): AnalysisResult {
        val parts = model.split(Regex("\\s*/\\s*"))
        if (parts.size < 6) return AnalysisResult(
            "輸入格式錯誤（需至少6段：年/月/大段/中段/五行/日極）",
            "","","","","")

        val yearGZ  = splitGZ(parts.getOrNull(0))
        val monthGZ = splitGZ(parts.getOrNull(1))
        val daduanS = parts.getOrNull(2)?.removeSuffix("大段")
        val daduanGZ= GanZhi(null, if (DZ.contains(daduanS)) daduanS else null)
        val dayGZ   = splitGZ(parts.getOrNull(5))
        val timeGZ  = splitGZ(parts.getOrNull(6))
        val keGZ    = splitGZ(parts.getOrNull(7))

        val allRelations = mutableListOf<Relation>()

        // 分析順序：刻極→時極→日極，各自向段極→月極→年極輻射
        val humanPoles = listOfNotNull(
            if (keGZ.stem != null || keGZ.branch != null) keGZ to "刻極" else null,
            if (timeGZ.stem != null || timeGZ.branch != null) timeGZ to "時極" else null,
            if (dayGZ.stem != null || dayGZ.branch != null) dayGZ to "日極" else null
        )

        val outerPoles = listOf(
            daduanGZ  to "段極",
            monthGZ   to "月極",
            yearGZ    to "年極"
        )

        for ((human, humanLabel) in humanPoles) {
            for ((outer, outerLabel) in outerPoles) {
                val rels = detectRelations(human, outer, "$humanLabel↔$outerLabel")
                allRelations.addAll(rels)
            }
        }

        // 應時失常（天干有關聯）與應時不足（地支有關聯）
        val shichang = allRelations
            .filter { it.type != RelationType.YI_GAN && TG.contains(it.humanPole) }
            .map { "${it.humanPole}（${it.poleLabel}）" }
            .distinct()

        val buzu = allRelations
            .filter { it.type != RelationType.YI_ZHI && DZ.contains(it.humanPole) }
            .map { "${it.humanPole}（${it.poleLabel}）" }
            .distinct()

        // 整合調治原則（去重）
        val zhizeSet = allRelations.map { relTypeToZhiZe(it.type) }.distinct()

        // 四行療法提示
        val fourLineHints = mutableListOf<String>()
        listOf(dayGZ, timeGZ).forEach { gz ->
            gz.stem?.let { s -> FOUR_LINE_MAP[s]?.let { fourLineHints.add("天干${s} → 宜調治 $it 之應象") } }
            gz.branch?.let { b -> FOUR_LINE_MAP[b]?.let { fourLineHints.add("地支${b} → 宜調治 $it 之應象") } }
        }

        // 病因：遠層（年極/月極）天干失常
        val bingYin = allRelations
            .filter { it.poleLabel.contains("年極") || it.poleLabel.contains("月極") }
            .filter { it.type == RelationType.TONG_GAN || it.type == RelationType.KE_NI }
            .joinToString("；") { "${it.humanPole}關聯${it.otherPole}（${it.poleLabel}）失常" }
            .ifBlank { "遠層天干關聯待進一步確認" }

        // 病症：日極干支症
        val bingZheng = buildString {
            dayGZ.stem?.let { append(it) }
            dayGZ.branch?.let { append(it) }
            if (isNotEmpty()) append("症") else append("日極症（需補充日極干支）")
        }

        // 病機：時極干支機
        val bingJi = buildString {
            val base = timeGZ.stem ?: dayGZ.stem ?: ""
            val br   = timeGZ.branch ?: dayGZ.branch ?: ""
            append("$base$br 病機")
            allRelations.filter {
                (it.humanPole == timeGZ.stem || it.humanPole == timeGZ.branch) &&
                it.type == RelationType.KE_NI
            }.forEach { append("（${it.humanPole}↔${it.otherPole} 克逆）") }
        }

        // 病位：日極地支定位
        val bingWei = dayGZ.branch?.let { "${it}位" } ?: "病位未明確（需補充日極地支）"

        // 防變：藏干透出提示
        val cangGan = mapOf(
            "子" to listOf("癸"),
            "丑" to listOf("己","癸","辛"),
            "寅" to listOf("甲","丙","戊"),
            "卯" to listOf("乙"),
            "辰" to listOf("戊","癸","乙"),
            "巳" to listOf("丙","庚","戊"),
            "午" to listOf("丁","己"),
            "未" to listOf("己","丁","乙"),
            "申" to listOf("庚","壬","戊"),
            "酉" to listOf("辛"),
            "戌" to listOf("戊","辛","丁"),
            "亥" to listOf("壬","甲")
        )
        val allBranches = listOfNotNull(yearGZ.branch, monthGZ.branch, dayGZ.branch, timeGZ.branch)
        val fangBianHints = allBranches.mapNotNull { br ->
            val hidden = cangGan[br] ?: return@mapNotNull null
            val triggered = hidden.filter { h ->
                allBranches.any { b -> b != br && (cangGan[b]?.contains(h) == true) } ||
                listOfNotNull(yearGZ.stem, monthGZ.stem, dayGZ.stem, timeGZ.stem).contains(h)
            }
            if (triggered.isNotEmpty()) "${h} 中藏干 ${triggered.joinToString("、")} 已顯，謹防傳變"
                .replace("${h} 中", "${br} 中")
            else null
        }
        val fangBian = if (fangBianHints.isNotEmpty()) fangBianHints.joinToString("；")
                       else "請留意藏干透出，逢相應時空則易傳變"

        val zhiZeText = if (zhizeSet.isEmpty()) "待補充干支後自動輸出調治原則"
                        else zhizeSet.joinToString("\n")

        return AnalysisResult(
            bingYin  = bingYin,
            bingZheng= bingZheng,
            bingJi   = bingJi,
            bingWei  = bingWei,
            zhiZe    = zhiZeText,
            fangBian = fangBian,
            relations= allRelations,
            shichang = shichang,
            buzu     = buzu,
            fourLineHints = fourLineHints.distinct()
        )
    }
}
