package com.jiuji.paipan

/**
 * 應象時空分析引擎 v2
 *
 * 分析次序（由近及遠）：
 *   人極：刻極 → 時極天干 → 時極地支 → 日極天干 → 日極地支
 *   輻射：段極 → 月極天干 → 月極地支 → 年極天干 → 年極地支
 *
 * 判定：
 *   人極天干與外極天干發生關聯 → 應時失常
 *   人極地支與外極地支發生關聯 → 應時不足
 *
 * 調治：
 *   同干宜養 / 異干宜化 / 同支宜固 / 異支宜平 / 克逆宜生
 */
object AnalysisEngine {

    // ── 基礎常量 ──────────────────────────────────────────
    private val TG = listOf("甲","乙","丙","丁","戊","己","庚","辛","壬","癸")
    private val DZ = listOf("子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥")

    /** 天干六合 */
    private val STEM_HE: Map<String,String> = mapOf(
        "甲" to "己","己" to "甲",
        "乙" to "庚","庚" to "乙",
        "丙" to "辛","辛" to "丙",
        "丁" to "壬","壬" to "丁",
        "戊" to "癸","癸" to "戊"
    )

    /** 地支六合 */
    private val BRANCH_HE: Map<String,String> = mapOf(
        "子" to "丑","丑" to "子",
        "寅" to "亥","亥" to "寅",
        "卯" to "戌","戌" to "卯",
        "辰" to "酉","酉" to "辰",
        "巳" to "申","申" to "巳",
        "午" to "未","未" to "午"
    )

    /** 地支六沖 */
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
        "甲" to "戊","乙" to "己",
        "丙" to "庚","丁" to "辛",
        "戊" to "壬","己" to "癸",
        "庚" to "甲","辛" to "乙",
        "壬" to "丙","癸" to "丁"
    )

    /** 天干 → 五行 */
    private val STEM_WX = mapOf(
        "甲" to "木","乙" to "木",
        "丙" to "火","丁" to "火",
        "戊" to "土","己" to "土",
        "庚" to "金","辛" to "金",
        "壬" to "水","癸" to "水"
    )

    /** 天干 → 臟腑（陽干=腑，陰干=臟） */
    private val STEM_ORGAN = mapOf(
        "甲" to "膽","乙" to "肝",
        "丙" to "小腸","丁" to "心",
        "戊" to "胃","己" to "脾",
        "庚" to "大腸","辛" to "肺",
        "壬" to "膀胱","癸" to "腎"
    )

    /** 地支 → 主臟腑 */
    private val BRANCH_ORGAN = mapOf(
        "子" to "腎/膀胱",
        "丑" to "脾/胃",
        "寅" to "肺/大腸",
        "卯" to "大腸/肺",
        "辰" to "胃/脾",
        "巳" to "心/小腸",
        "午" to "心/小腸",
        "未" to "脾/胃",
        "申" to "大腸/肺",
        "酉" to "肺/大腸",
        "戌" to "心包/三焦",
        "亥" to "腎/三焦"
    )

    /** 地支 → 主經絡 */
    private val BRANCH_MERIDIAN = mapOf(
        "子" to "足少陰腎經",
        "丑" to "足太陰脾經",
        "寅" to "手太陰肺經",
        "卯" to "手陽明大腸經",
        "辰" to "足陽明胃經",
        "巳" to "手少陰心經",
        "午" to "手太陽小腸經",
        "未" to "足太陰脾經",
        "申" to "手太陽小腸經",
        "酉" to "手太陰肺經",
        "戌" to "手厥陰心包經",
        "亥" to "手少陽三焦經"
    )

    /** 五行 → 對應病症提示 */
    private val WX_ZHENG = mapOf(
        "木" to "筋骨拘攣、目疾、情志抑鬱、脇肋脹痛",
        "火" to "心悸失眠、舌瘡口苦、神識躁動",
        "土" to "脘腹脹滿、納呆便溏、四肢倦怠、濕邪內蘊",
        "金" to "咳嗽喘息、皮膚乾燥、大便秘結、悲憂不散",
        "水" to "腰膝酸軟、耳鳴耳聾、夜尿頻數、寒象顯著"
    )

    /** 五行 → 對應病機 */
    private val WX_BINGJI = mapOf(
        "木" to "肝木失疏，氣機不暢；或木火相煽，陽亢風動",
        "火" to "心火亢盛，耗傷陰液；或心腎不交，虛火上炎",
        "土" to "脾土虛衰，運化失司；或濕熱困脾，中焦不運",
        "金" to "肺金宣降失調，津液不布；或燥邪傷肺，氣陰兩傷",
        "水" to "腎水虧虛，封藏失職；或寒水泛濫，陽氣被遏"
    )

    /** 調治原則 → 詳細治法 */
    private val ZHIZE_DETAIL = mapOf(
        RelationType.TONG_GAN  to "同干宜養\n──育也、畜也、長也\n宜補益其本氣，順其自然之性；選取同氣相投之穴位或藥物，緩養正氣",
        RelationType.YI_GAN    to "異干宜化\n──因時而化，順勢轉化\n宜調暢氣機，引異氣相化；選取調氣化滯之穴位或藥物，疏導轉化",
        RelationType.TONG_ZHI  to "同支宜固\n──堅固、穩固\n宜鞏固本位之氣，收斂固攝；選取固攝斂氣之穴位或藥物，穩固臟腑",
        RelationType.YI_ZHI    to "異支宜平\n──平者，和也\n宜調和陰陽，平衡氣血；選取調和氣血之穴位或藥物，寒熱並調",
        RelationType.KE_NI     to "克逆宜生\n──從干支本源起也\n宜扶助被克之氣的生源，以生化克；選取培元固本之穴位或藥物，以母養子"
    )

    /** 四行療法對照表 */
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

    /** 藏干表 */
    private val CANG_GAN = mapOf(
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

    // ── 資料類 ────────────────────────────────────────────
    data class GanZhi(val stem: String?, val branch: String?)

    enum class RelationType { TONG_GAN, YI_GAN, TONG_ZHI, YI_ZHI, KE_NI }

    data class Relation(
        val type: RelationType,
        val humanPole: String,
        val otherPole: String,
        val poleLabel: String
    )

    data class AnalysisResult(
        val bingYin: String,
        val bingZheng: String,
        val bingJi: String,
        val bingWei: String,
        val zhiZe: String,
        val fangBian: String,
        val relations: List<Relation> = emptyList(),
        val shichang: List<String> = emptyList(),
        val buzu: List<String> = emptyList(),
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

    fun isTongGan(a: String, b: String)    = a == b || STEM_HE[a] == b
    fun isKeNiStem(a: String, b: String)   = STEM_KE[a] == b || STEM_KE[b] == a
    fun isTongZhi(a: String, b: String)    = a == b || BRANCH_HE[a] == b
    fun isKeNiBranch(a: String, b: String) = BRANCH_CHONG[a] == b

    fun detectRelations(
        human: GanZhi,
        other: GanZhi,
        poleLabel: String
    ): List<Relation> {
        val result = mutableListOf<Relation>()
        val hs = human.stem; val os = other.stem
        if (hs != null && os != null) {
            when {
                isKeNiStem(hs, os) -> result.add(Relation(RelationType.KE_NI,    hs, os, poleLabel))
                isTongGan(hs, os)  -> result.add(Relation(RelationType.TONG_GAN, hs, os, poleLabel))
                else               -> result.add(Relation(RelationType.YI_GAN,   hs, os, poleLabel))
            }
        }
        val hb = human.branch; val ob = other.branch
        if (hb != null && ob != null) {
            when {
                isKeNiBranch(hb, ob) -> result.add(Relation(RelationType.KE_NI,    hb, ob, poleLabel))
                isTongZhi(hb, ob)    -> result.add(Relation(RelationType.TONG_ZHI, hb, ob, poleLabel))
                else                 -> result.add(Relation(RelationType.YI_ZHI,   hb, ob, poleLabel))
            }
        }
        return result
    }

    fun relTypeToZhiZe(type: RelationType): String = ZHIZE_DETAIL[type] ?: ""

    // ── 主分析函數 ────────────────────────────────────────
    fun analyze(model: String): AnalysisResult {
        val parts = model.split(Regex("\\s*/\\s*"))
        if (parts.size < 6) return AnalysisResult(
            "輸入格式錯誤（需至少6段：年/月/大段/中段/五行/日極）",
            "","","","","")

        val yearGZ   = splitGZ(parts.getOrNull(0))
        val monthGZ  = splitGZ(parts.getOrNull(1))
        val daduanS  = parts.getOrNull(2)?.removeSuffix("大段")
        val daduanGZ = GanZhi(null, if (DZ.contains(daduanS)) daduanS else null)
        val dayGZ    = splitGZ(parts.getOrNull(5))
        val timeGZ   = splitGZ(parts.getOrNull(6))
        val keGZ     = splitGZ(parts.getOrNull(7))

        val allRelations = mutableListOf<Relation>()

        val humanPoles = listOfNotNull(
            if (keGZ.stem   != null || keGZ.branch   != null) keGZ   to "刻極" else null,
            if (timeGZ.stem != null || timeGZ.branch != null) timeGZ to "時極" else null,
            if (dayGZ.stem  != null || dayGZ.branch  != null) dayGZ  to "日極" else null
        )

        val outerPoles = listOf(
            daduanGZ to "段極",
            monthGZ  to "月極",
            yearGZ   to "年極"
        )

        for ((human, humanLabel) in humanPoles) {
            for ((outer, outerLabel) in outerPoles) {
                val rels = detectRelations(human, outer, "$humanLabel↔$outerLabel")
                allRelations.addAll(rels)
            }
        }

        // ── 應時失常 / 應時不足 ──────────────────────────
        val shichang = allRelations
            .filter { it.type != RelationType.YI_GAN && TG.contains(it.humanPole) }
            .map { "${it.humanPole}（${it.poleLabel}）" }
            .distinct()

        val buzu = allRelations
            .filter { it.type != RelationType.YI_ZHI && DZ.contains(it.humanPole) }
            .map { "${it.humanPole}（${it.poleLabel}）" }
            .distinct()

        // ── 病因 ─────────────────────────────────────────
        // 天極（年/月）與人極天干的關聯性質
        val bingYinLines = mutableListOf<String>()
        allRelations
            .filter { it.poleLabel.contains("年極") || it.poleLabel.contains("月極") }
            .filter { it.type == RelationType.TONG_GAN || it.type == RelationType.KE_NI }
            .forEach { r ->
                val organ = STEM_ORGAN[r.humanPole] ?: ""
                val wx    = STEM_WX[r.humanPole] ?: ""
                val nature = if (r.type == RelationType.KE_NI) "克逆失常" else "同干過亢"
                bingYinLines.add("${r.poleLabel}：${r.humanPole}（${wx}/${organ}）${nature}，與${r.otherPole}關聯")
            }
        // 異干為遠層「宜化」之因
        allRelations
            .filter { it.poleLabel.contains("年極") || it.poleLabel.contains("月極") }
            .filter { it.type == RelationType.YI_GAN }
            .forEach { r ->
                val organ = STEM_ORGAN[r.humanPole] ?: ""
                bingYinLines.add("${r.poleLabel}：${r.humanPole}（${organ}）與${r.otherPole}異干，遠層氣化不和")
            }
        val bingYin = if (bingYinLines.isNotEmpty()) bingYinLines.joinToString("\n")
                      else "遠層（年/月極）天干與人極無直接衝突，病因多源自近層時空失調"

        // ── 病症 ─────────────────────────────────────────
        // 日極干支 → 五行臟腑 → 典型症狀
        val bingZhengLines = mutableListOf<String>()
        dayGZ.stem?.let { s ->
            val wx    = STEM_WX[s]  ?: ""
            val organ = STEM_ORGAN[s] ?: ""
            val zheng = WX_ZHENG[wx] ?: ""
            bingZhengLines.add("日極天干 ${s}（${wx}行，主${organ}）：${zheng}")
        }
        dayGZ.branch?.let { b ->
            val organ    = BRANCH_ORGAN[b]    ?: ""
            val meridian = BRANCH_MERIDIAN[b] ?: ""
            bingZhengLines.add("日極地支 ${b}（主${organ}，${meridian}）")
        }
        // 時極加強病症
        timeGZ.stem?.let { s ->
            val wx = STEM_WX[s] ?: ""
            WX_ZHENG[wx]?.let {
                bingZhengLines.add("時極天干 ${s}（${wx}行）兼症：${it}")
            }
        }
        val bingZheng = if (bingZhengLines.isNotEmpty()) bingZhengLines.joinToString("\n")
                        else "日極干支未填寫，症狀待確認"

        // ── 病機 ─────────────────────────────────────────
        // 時極與外極的克逆/同干關聯 → 病機傳變
        val bingJiLines = mutableListOf<String>()
        // 主病機來自時極五行
        timeGZ.stem?.let { s ->
            val wx = STEM_WX[s] ?: ""
            WX_BINGJI[wx]?.let { bingJiLines.add("時極 ${s}（${wx}行）：${it}") }
        }
        // 克逆關係點出傳變
        allRelations
            .filter { it.type == RelationType.KE_NI }
            .forEach { r ->
                val attacker = r.humanPole
                val victim   = r.otherPole
                val atkWx    = STEM_WX[attacker] ?: ""
                val vicWx    = STEM_WX[victim]   ?: ""
                val atkOrgan = STEM_ORGAN[attacker] ?: attacker
                val vicOrgan = STEM_ORGAN[victim]   ?: victim
                bingJiLines.add("克逆（${r.poleLabel}）：${attacker}${atkWx}（${atkOrgan}）克 ${victim}${vicWx}（${vicOrgan}），傳變之機")
            }
        if (bingJiLines.isEmpty()) {
            // 退而用日極
            dayGZ.stem?.let { s ->
                val wx = STEM_WX[s] ?: ""
                WX_BINGJI[wx]?.let { bingJiLines.add("日極 ${s}（${wx}行）：${it}") }
            }
        }
        val bingJi = if (bingJiLines.isNotEmpty()) bingJiLines.joinToString("\n")
                     else "病機待補充干支後自動生成"

        // ── 病位 ─────────────────────────────────────────
        val bingWeiLines = mutableListOf<String>()
        dayGZ.branch?.let { b ->
            val organ    = BRANCH_ORGAN[b]    ?: ""
            val meridian = BRANCH_MERIDIAN[b] ?: ""
            bingWeiLines.add("日極地支 ${b} → 主位：${organ}\n   經絡：${meridian}")
        }
        dayGZ.stem?.let { s ->
            val organ = STEM_ORGAN[s] ?: ""
            bingWeiLines.add("日極天干 ${s} → 輔位：${organ}")
        }
        timeGZ.branch?.let { b ->
            val organ    = BRANCH_ORGAN[b]    ?: ""
            val meridian = BRANCH_MERIDIAN[b] ?: ""
            bingWeiLines.add("時極地支 ${b} → 兼位：${organ}（${meridian}）")
        }
        val bingWei = if (bingWeiLines.isNotEmpty()) bingWeiLines.joinToString("\n")
                      else "病位未明確（需補充日極地支）"

        // ── 調治原則 ──────────────────────────────────────
        val zhizeTypes = allRelations.map { it.type }.distinct()
        val zhiZeText  = if (zhizeTypes.isEmpty()) "待補充干支後自動輸出調治原則"
                         else zhizeTypes.joinToString("\n\n") { ZHIZE_DETAIL[it] ?: "" }

        // ── 四行療法提示 ──────────────────────────────────
        val fourLineHints = mutableListOf<String>()
        listOf(dayGZ to "日極", timeGZ to "時極").forEach { (gz, label) ->
            gz.stem?.let   { s -> FOUR_LINE_MAP[s]?.let { fourLineHints.add("${label}天干 ${s} → 宜取 ${it} 應象調治") } }
            gz.branch?.let { b -> FOUR_LINE_MAP[b]?.let { fourLineHints.add("${label}地支 ${b} → 宜取 ${it} 應象調治") } }
        }

        // ── 防變提示 ──────────────────────────────────────
        val allStems    = listOfNotNull(yearGZ.stem, monthGZ.stem, dayGZ.stem, timeGZ.stem)
        val allBranches = listOfNotNull(yearGZ.branch, monthGZ.branch, dayGZ.branch, timeGZ.branch)
        val fangBianHints = mutableListOf<String>()

        allBranches.forEach { br ->
            val hidden = CANG_GAN[br] ?: return@forEach
            val triggered = hidden.filter { h ->
                // 藏干與透出之干相同，或與其他支的藏干同氣
                allStems.contains(h) ||
                allBranches.any { b -> b != br && (CANG_GAN[b]?.contains(h) == true) }
            }
            if (triggered.isNotEmpty()) {
                val organs = triggered.mapNotNull { STEM_ORGAN[it] }.joinToString("、")
                fangBianHints.add("${br} 中藏干 ${triggered.joinToString("、")} 已顯（主${organs}），謹防傳變")
            }
        }

        // 克逆鏈：被克者若藏干透出，易順勢傳至下一環
        allRelations
            .filter { it.type == RelationType.KE_NI }
            .forEach { r ->
                val victim    = r.otherPole
                val nextKe    = STEM_KE[victim]
                if (nextKe != null && (allStems.contains(nextKe) ||
                        allBranches.any { CANG_GAN[it]?.contains(nextKe) == true })) {
                    val organ1 = STEM_ORGAN[victim]  ?: victim
                    val organ2 = STEM_ORGAN[nextKe]  ?: nextKe
                    fangBianHints.add("克逆鏈：${victim}（${organ1}）→ 已具${nextKe}（${organ2}）傳變之機")
                }
            }

        val fangBian = if (fangBianHints.isNotEmpty()) fangBianHints.distinct().joinToString("\n")
                       else "請留意藏干透出：逢相應時空則易傳變，宜提早顧護"

        return AnalysisResult(
            bingYin       = bingYin,
            bingZheng     = bingZheng,
            bingJi        = bingJi,
            bingWei       = bingWei,
            zhiZe         = zhiZeText,
            fangBian      = fangBian,
            relations     = allRelations,
            shichang      = shichang,
            buzu          = buzu,
            fourLineHints = fourLineHints.distinct()
        )
    }
}
