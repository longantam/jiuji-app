package com.jiuji.paipan

object ZiwuEngine {

    private val TG = arrayOf("甲","乙","丙","丁","戊","己","庚","辛","壬","癸")
    private val DZ = arrayOf("子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥")

    data class NaziResult(
        val dzName: String, val meridian: String, val organ: String,
        val allPts: List<String>, val buPt: String, val xiePt: String, val benPt: String
    )
    data class NaJiaResult(
        val mainPt: String?, val hePt: String?, val yuanPt: String?, val note: String
    )
    data class LingGuiResult(
        val mainPt: String, val gua: String, val pairPt: String, val note: String
    )
    data class FeitengResult(
        val shiTg: String, val mainPt: String, val gua: String, val pairPt: String
    )
    data class ZiwuResult(
        val nazi: NaziResult,
        val naJia: NaJiaResult?,
        val lingGui: LingGuiResult?,
        val feiteng: FeitengResult?
    )

    private val NAZI: Map<String, Array<String>> = mapOf(
        "子" to arrayOf("足少陰腎經","腎","湧泉,然谷,太谿,復溜,陰谷","復溜","湧泉","太谿"),
        "丑" to arrayOf("足厥陰肝經","肝","大敦,行間,太衝,中封,曲泉","曲泉","行間","太衝"),
        "寅" to arrayOf("手太陰肺經","肺","少商,魚際,太淵,經渠,尺澤","太淵","尺澤","太淵"),
        "卯" to arrayOf("手陽明大腸經","大腸","商陽,二間,三間,陽谿,曲池","曲池","二間","合谷"),
        "辰" to arrayOf("足陽明胃經","胃","厲兌,內庭,陷谷,解谿,足三里","解谿","厲兌","衝陽"),
        "巳" to arrayOf("足太陰脾經","脾","隱白,大都,太白,商丘,陰陵泉","大都","商丘","太白"),
        "午" to arrayOf("手少陰心經","心","少衝,少府,神門,靈道,少海","少衝","神門","神門"),
        "未" to arrayOf("手太陽小腸經","小腸","少澤,前谷,後谿,陽谷,小海","後谿","小海","腕骨"),
        "申" to arrayOf("足太陽膀胱經","膀胱","至陰,通谷,束骨,崑崙,委中","至陰","束骨","京骨"),
        "酉" to arrayOf("足少陽膽經","膽","竅陰,俠谿,足臨泣,陽輔,陽陵泉","俠谿","陽輔","丘墟"),
        "戌" to arrayOf("手厥陰心包經","心包","中衝,勞宮,大陵,間使,曲澤","中衝","大陵","大陵"),
        "亥" to arrayOf("手少陽三焦經","三焦","關衝,液門,中渚,支溝,天井","中渚","天井","陽池")
    )

    private val NJY: Map<String, String> = mapOf(
        "甲甲" to "竅陰", "甲丙" to "前谷", "甲戊" to "陷谷", "甲庚" to "經渠", "甲壬" to "陰谷",
        "丙甲" to "少衝", "丙丙" to "魚際", "丙戊" to "太白", "丙庚" to "商丘", "丙壬" to "尺澤",
        "戊甲" to "大敦", "戊丙" to "行間", "戊戊" to "神門", "戊庚" to "靈道", "戊壬" to "曲泉",
        "庚甲" to "少澤", "庚丙" to "通谷", "庚戊" to "臨泣", "庚庚" to "崑崙", "庚壬" to "委中",
        "壬甲" to "至陰", "壬丙" to "俠谿", "壬戊" to "足臨泣", "壬庚" to "陽輔", "壬壬" to "陽陵泉"
    )
    private val NJI: Map<String, String> = mapOf(
        "乙乙" to "大敦", "乙丁" to "少府", "乙己" to "太白", "乙辛" to "中封", "乙癸" to "陰谷",
        "丁乙" to "少衝", "丁丁" to "少府", "丁己" to "大都", "丁辛" to "商丘", "丁癸" to "少海",
        "己乙" to "隱白", "己丁" to "大都", "己己" to "太白", "己辛" to "商丘", "己癸" to "陰陵泉",
        "辛乙" to "少商", "辛丁" to "魚際", "辛己" to "太淵", "辛辛" to "經渠", "辛癸" to "尺澤",
        "癸乙" to "湧泉", "癸丁" to "然谷", "癸己" to "太谿", "癸辛" to "復溜", "癸癸" to "陰谷"
    )
    private val HE: Map<String, String> = mapOf(
        "甲" to "己", "乙" to "庚", "丙" to "辛", "丁" to "壬", "戊" to "癸",
        "己" to "甲", "庚" to "乙", "辛" to "丙", "壬" to "丁", "癸" to "戊"
    )
    private val YUAN: Map<String, String> = mapOf(
        "乙" to "太衝", "丁" to "神門", "己" to "太白", "辛" to "太淵", "癸" to "太谿"
    )
    private val LG_TG = intArrayOf(10, 8, 9, 7, 10, 6, 9, 7, 5, 6)
    private val LG_DZ = intArrayOf(9, 8, 3, 4, 9, 6, 7, 2, 4, 9, 5, 6)
    private val GONG: Map<Int, Triple<String, String, String>> = mapOf(
        1 to Triple("申脈", "震", "後谿"),
        2 to Triple("照海", "坤", "列缺"),
        3 to Triple("外關", "震", "足臨泣"),
        4 to Triple("足臨泣", "巽", "外關"),
        5 to Triple("照海", "中宮", "列缺"),
        6 to Triple("公孫", "乾", "內關"),
        7 to Triple("後谿", "兌", "申脈"),
        8 to Triple("內關", "艮", "公孫"),
        9 to Triple("列缺", "離", "照海")
    )
    private val FT: Map<String, Pair<String, String>> = mapOf(
        "甲" to Pair("公孫", "乾"), "丙" to Pair("內關", "離"),
        "戊" to Pair("臨泣", "艮"), "庚" to Pair("外關", "兌"),
        "壬" to Pair("申脈", "坎"), "乙" to Pair("後谿", "巽"),
        "丁" to Pair("照海", "坤"), "己" to Pair("列缺", "坤"),
        "辛" to Pair("後谿", "巽"), "癸" to Pair("申脈", "坎")
    )
    private val FT_PAIR: Map<String, String> = mapOf(
        "公孫" to "內關", "內關" to "公孫", "臨泣" to "外關", "外關" to "臨泣",
        "申脈" to "後谿", "後谿" to "申脈", "照海" to "列缺", "列缺" to "照海"
    )

    fun compute(hour: Int, riGZ: String, shiGZ: String): ZiwuResult {
        val shiBranchIdx = ((hour + 1) % 24) / 2
        val dzName = DZ[shiBranchIdx]
        val data = NAZI[dzName]
        val nazi = if (data != null) {
            NaziResult(dzName, data[0], data[1], data[2].split(","), data[3], data[4], data[5])
        } else {
            NaziResult(dzName, "未知", "-", listOf(), "-", "-", "-")
        }

        val riTg = riGZ[0].toString()
        val shiTg = shiGZ[0].toString()
        val isYang = TG.indexOf(riTg) % 2 == 0
        val key = riTg + shiTg
        val mainPt = if (isYang) NJY[key] else NJI[key]
        val heTg = HE[riTg]
        val heKey = (heTg ?: "") + shiTg
        val hePt = if (isYang) NJY[heKey] else NJI[heKey]
        val yuanPt = if (!isYang) YUAN[riTg] else null
        val noteText = riGZ + "日 " + shiGZ + "時 — 合日互用：" + (hePt ?: "—") + "（" + (heTg ?: "?") + "日）"
        val naJia = NaJiaResult(mainPt, hePt, yuanPt, noteText)

        val ri0 = TG.indexOf(riTg)
        val rd = DZ.indexOf(riGZ[1].toString())
        val si0 = TG.indexOf(shiTg)
        val sd = DZ.indexOf(shiGZ[1].toString())
        val lingGui = if (ri0 >= 0 && rd >= 0 && si0 >= 0 && sd >= 0) {
            val sum = LG_TG[ri0] + LG_DZ[rd] + LG_TG[si0] + LG_DZ[sd]
            val divisor = if (isYang) 9 else 6
            val rem = sum % divisor
            val idx = if (rem == 0) divisor else rem
            val g = GONG[idx]
            if (g != null) {
                val yinYang = if (isYang) "陽" else "陰"
                val noteStr = "和數" + sum + "，" + yinYang + "日÷" + divisor + "，餘" + idx + "→" + g.second + "宮，主穴" + g.first + "，配" + g.third
                LingGuiResult(g.first, g.second, g.third, noteStr)
            } else null
        } else null

        val ft = FT[shiTg]
        val feiteng = if (ft != null) {
            FeitengResult(shiTg, ft.first, ft.second, FT_PAIR[ft.first] ?: "—")
        } else null

        return ZiwuResult(nazi, naJia, lingGui, feiteng)
    }
}
