package com.jiuji.paipan

/**
 * 應象時空調治分析引擎 v5
 * 修正規則：
 * - 病因：時/日干支 關聯 年/月/大/中/小段極干支 失常（克逆）或同干/同支
 *   格式：「時干辛與月干丙失常（克逆）；日干乙與年干乙同干」
 * - 病症：日極干支症（如：乙酉症）
 * - 病機：時極干支病機（如：辛巳病機）—— 對應時極完整干支
 * - 病位：大刻完整干支位（如：辛巳位）—— 對應大刻位置
 */
object AnalysisEngine {

    // 天干克逆表：key 被 value 所克
    private val TG_KE: Map<String, String> = mapOf(
        "甲" to "庚", "乙" to "辛", "丙" to "壬", "丁" to "癸",
        "戊" to "戊", "己" to "己",
        "庚" to "甲", "辛" to "乙", "壬" to "丙", "癸" to "丁"
    )

    // 地支克逆表：key 被 value 所克
    private val DZ_KE: Map<String, String> = mapOf(
        "子" to "午", "丑" to "未", "寅" to "申", "卯" to "酉",
        "辰" to "戌", "巳" to "亥", "午" to "子", "未" to "丑",
        "申" to "寅", "酉" to "卯", "戌" to "辰", "亥" to "巳"
    )

    // 地支藏干
    private fun cangGan(dz: String): List<String> = when (dz) {
        "子" -> listOf("癸")
        "丑" -> listOf("己", "辛", "癸")
        "寅" -> listOf("甲", "丙", "戊")
        "卯" -> listOf("乙")
        "辰" -> listOf("戊", "癸", "乙")
        "巳" -> listOf("丙", "庚", "戊")
        "午" -> listOf("丁", "己")
        "未" -> listOf("己", "丁", "乙")
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
        "己" to listOf("丑", "未", "午"),
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

    private fun ganRelType(h: String, o: String): String? {
        if (h.isEmpty() || o.isEmpty()) return null
        if (h == o) return "同干"
        if (TG_KE[h] == o || TG_KE[o] == h) return "克逆"
        return "異干"
    }

    private fun zhiRelType(h: String, o: String): String? {
        if (h.isEmpty() || o.isEmpty()) return null
        if (h == o) return "同支"
        if (DZ_KE[h] == o || DZ_KE[o] == h) return "克逆"
        return "異支"
    }

    private fun zhiZeFor(relType: String, humanPole: String, outerPole: String): String {
        return when (relType) {
            "同干" -> "同干宜養（${humanPole}與${outerPole}）"
            "同支" -> "同支宜固（${humanPole}與${outerPole}）"
            "克逆" -> "克逆宜生（${humanPole}與${outerPole}）"
            "異干" -> "異干宜通（${humanPole}與${outerPole}）"
            "異支" -> "異支宜通（${humanPole}與${outerPole}）"
            else -> ""
        }
    }

    fun analyze(model: String): AnalysisResult {
        val parts = model.split(" / ").map { it.trim() }
        if (parts.size < 6) {
            return AnalysisResult("輸入格式錯誤", "", "", "", "", "")
        }

        fun stripSuffix(s: String, vararg suffixes: String): String {
            var r = s; for (suf in suffixes) r = r.replace(suf, ""); return r.trim()
        }

        val nianGZ       = parts[0]
        val yueGZ        = parts[1]
        val daDuanRaw    = parts.getOrNull(2) ?: "X"
        val zhongDuanRaw = parts.getOrNull(3) ?: "X"
        val xiaoDuanRaw  = parts.getOrNull(4) ?: "X"
        val riGZ         = parts.getOrNull(5) ?: ""
        val shiGZ        = parts.getOrNull(6)?.trim()
            ?.let { if (it == "X" || it == "-" || it.isEmpty()) null else it }
        val daKeRaw      = parts.getOrNull(7)?.trim()
            ?.let { if (it == "X" || it == "-" || it.isEmpty()) null else it }

        fun parseDuan(raw: String): String? {
            val s = stripSuffix(raw, "大段", "中段", "小段")
            return if (s == "X" || s.isEmpty()) null else s
        }

        val daDuan    = parseDuan(daDuanRaw)
        val zhongDuan = parseDuan(zhongDuanRaw)
        val xiaoDuan  = parseDuan(xiaoDuanRaw)

        fun gan(gz: String?) = gz?.takeIf { it.length >= 1 }?.get(0)?.toString() ?: ""
        fun zhi(gz: String?) = gz?.takeIf { it.length >= 2 }?.get(1)?.toString() ?: ""
        fun zhiOf(s: String?): String {
            if (s == null) return ""
            return if (s.length == 1) s else s[1].toString()
        }

        val nGan = gan(nianGZ); val nZhi = zhi(nianGZ)
        val yGan = gan(yueGZ);  val yZhi = zhi(yueGZ)
        val rGan = gan(riGZ);   val rZhi = zhi(riGZ)
        val sGan = gan(shiGZ);  val sZhi = zhi(shiGZ)

        data class OuterPole(val name: String, val gan: String, val zhi: String)
        val outerPoles = mutableListOf<OuterPole>()
        outerPoles.add(OuterPole("年極", nGan, nZhi))
        outerPoles.add(OuterPole("月極", yGan, yZhi))
        daDuan?.let    { outerPoles.add(OuterPole("大段", "", zhiOf(it))) }
        zhongDuan?.let { outerPoles.add(OuterPole("中段", "", zhiOf(it))) }
        xiaoDuan?.let  { outerPoles.add(OuterPole("小段", "", zhiOf(it))) }

        data class HumanPole(val name: String, val gan: String, val zhi: String)
        val humanPoles = mutableListOf<HumanPole>()
        if (rGan.isNotEmpty() || rZhi.isNotEmpty()) humanPoles.add(HumanPole("日極", rGan, rZhi))
        if (sGan.isNotEmpty() || sZhi.isNotEmpty()) humanPoles.add(HumanPole("時極", sGan, sZhi))

        val bingYinParts = mutableListOf<String>()
        val zhiZeList    = mutableListOf<String>()
        val relationList = mutableListOf<Relation>()

        for (hp in humanPoles) {
            for (op in outerPoles) {
                if (hp.gan.isNotEmpty() && op.gan.isNotEmpty()) {
                    val gr = ganRelType(hp.gan, op.gan)
                    if (gr != null) {
                        val desc = when (gr) {
                            "同干" -> "${hp.name}干${hp.gan}與${op.name}干${op.gan}同干"
                            "克逆" -> "${hp.name}干${hp.gan}與${op.name}干${op.gan}失常（克逆）"
                            else   -> "${hp.name}干${hp.gan}與${op.name}干${op.gan}異干"
                        }
                        val rt = when (gr) {
                            "同干" -> RelationType.TONG_GAN
                            "克逆" -> RelationType.KE_NI_GAN
                            else  -> RelationType.YI_GAN
                        }
                        if (gr == "克逆" || gr == "同干") bingYinParts.add(desc)
                        zhiZeList.add(zhiZeFor(gr, hp.name, op.name))
                        relationList.add(Relation(rt, hp.name, op.name, desc))
                    }
                }
                if (hp.zhi.isNotEmpty() && op.zhi.isNotEmpty()) {
                    val zr = zhiRelType(hp.zhi, op.zhi)
                    if (zr != null) {
                        val desc = when (zr) {
                            "同支" -> "${hp.name}支${hp.zhi}與${op.name}支${op.zhi}同支"
                            "克逆" -> "${hp.name}支${hp.zhi}與${op.name}支${op.zhi}失常（克逆）"
                            else   -> "${hp.name}支${hp.zhi}與${op.name}支${op.zhi}異支"
                        }
                        val rt = when (zr) {
                            "同支" -> RelationType.TONG_ZHI
                            "克逆" -> RelationType.KE_NI_ZHI
                            else  -> RelationType.YI_ZHI
                        }
                        if (zr == "克逆" || zr == "同支") bingYinParts.add(desc)
                        zhiZeList.add(zhiZeFor(zr, hp.name, op.name))
                        relationList.add(Relation(rt, hp.name, op.name, desc))
                    }
                }
            }
        }

        val bingYin = bingYinParts.distinct().joinToString("；")
            .ifEmpty { "未發現克逆失常" }

        val bingZheng = if (riGZ.isNotEmpty()) "${riGZ}症" else "未定"

        val bingJi = when {
            shiGZ != null && shiGZ.isNotEmpty() -> "${shiGZ}病機"
            riGZ.isNotEmpty() -> "${riGZ}為本"
            else -> "未定"
        }

        val bingWei = if (daKeRaw != null && daKeRaw.isNotEmpty())
            "${daKeRaw}位"
        else
            "未定"

        val zhiZe = zhiZeList.distinct().filter { it.isNotEmpty() }.joinToString("\n")
            .ifEmpty { "平調即可" }

        val fangBianList = mutableListOf<String>()
        val allZhi = listOfNotNull(rZhi, sZhi, nZhi, yZhi)
            .filter { it.isNotEmpty() } +
            listOfNotNull(daDuan, zhongDuan, xiaoDuan)
                .map { zhiOf(it) }.filter { it.isNotEmpty() }

        for (z in allZhi.distinct()) {
            for (cg in cangGan(z)) {
                val xianZhi = TG_XIAN[cg] ?: emptyList()
                if (xianZhi.any { it in allZhi }) {
                    val msg = "${cg}藏於地支${z}，已顯於地支，謹防傳變"
                    if (!fangBianList.contains(msg)) fangBianList.add(msg)
                }
            }
        }
        val fangBian = fangBianList.distinct().joinToString("；")
            .ifEmpty { "暫無明顯傳變風險" }

        return AnalysisResult(
            bingYin   = bingYin,
            bingZheng = bingZheng,
            bingJi    = bingJi,
            bingWei   = bingWei,
            zhiZe     = zhiZe,
            fangBian  = fangBian,
            shichang  = emptyList(),
            buzu      = emptyList(),
            fourLineHints = emptyList(),
            relations = relationList
        )
    }
}
