package com.jiuji.paipan

/**
 * 應象時空調治分析引擎 v3 - 正確版
 * 依據九極時空模型進行兩兩氣感關聯判定
 * 分析順序：人極（刻極→時極→日極）→ 地極（段極→月極）→ 天極（年極）
 */
object AnalysisEngine {

    private val TG = arrayOf("甲","乙","丙","丁","戊","己","庚","辛","壬","癸")
    private val DZ = arrayOf("子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥")

    // 天干合：甲己、乙庚、丙辛、丁壬、戊癸
    private fun tianGanHe(g1: String, g2: String): Boolean {
        return (g1 == "甲" && g2 == "己") || (g1 == "己" && g2 == "甲") ||
               (g1 == "乙" && g2 == "庚") || (g1 == "庚" && g2 == "乙") ||
               (g1 == "丙" && g2 == "辛") || (g1 == "辛" && g2 == "丙") ||
               (g1 == "丁" && g2 == "壬") || (g1 == "壬" && g2 == "丁") ||
               (g1 == "戊" && g2 == "癸") || (g1 == "癸" && g2 == "戊")
    }

    // 地支合：子丑、寅亥、卯戌、辰酉、巳申、午未
    private fun dizhiHe(z1: String, z2: String): Boolean {
        return (z1 == "子" && z2 == "丑") || (z1 == "丑" && z2 == "子") ||
               (z1 == "寅" && z2 == "亥") || (z1 == "亥" && z2 == "寅") ||
               (z1 == "卯" && z2 == "戌") || (z1 == "戌" && z2 == "卯") ||
               (z1 == "辰" && z2 == "酉") || (z1 == "酉" && z2 == "辰") ||
               (z1 == "巳" && z2 == "申") || (z1 == "申" && z2 == "巳") ||
               (z1 == "午" && z2 == "未") || (z1 == "未" && z2 == "午")
    }

    // 地支藏干
    private fun dizhiCangGan(dz: String): List<String> {
        return when(dz) {
            "子" -> listOf("癸")
            "丑" -> listOf("己", "癸", "辛")
            "寅" -> listOf("甲", "丙", "戊")
            "卯" -> listOf("乙")
            "辰" -> listOf("戊", "乙", "癸")
            "巳" -> listOf("丙", "戊", "庚")
            "午" -> listOf("丁", "己")
            "未" -> listOf("己", "丁", "乙")
            "申" -> listOf("庚", "壬", "戊")
            "酉" -> listOf("辛")
            "戌" -> listOf("戊", "辛", "丁")
            "亥" -> listOf("壬", "甲")
            else -> listOf()
        }
    }

    // 地支相沖
    private fun dizhiChong(z1: String, z2: String): Boolean {
        return (z1 == "子" && z2 == "午") || (z1 == "午" && z2 == "子") ||
               (z1 == "丑" && z2 == "未") || (z1 == "未" && z2 == "丑") ||
               (z1 == "寅" && z2 == "申") || (z1 == "申" && z2 == "寅") ||
               (z1 == "卯" && z2 == "酉") || (z1 == "酉" && z2 == "卯") ||
               (z1 == "辰" && z2 == "戌") || (z1 == "戌" && z2 == "辰") ||
               (z1 == "巳" && z2 == "亥") || (z1 == "亥" && z2 == "巳")
    }

    // 四行療法對照表
    private val SI_XING = mapOf(
        "甲" to listOf("己", "庚"),
        "乙" to listOf("己", "丁"),
        "丙" to listOf("辛", "庚"),
        "丁" to listOf("壬", "己"),
        "庚" to listOf("壬", "辛"),
        "辛" to listOf("戊", "癸"),
        "壬" to listOf("甲", "丁"),
        "癸" to listOf("戊", "乙"),
        "子" to listOf("己", "癸"),
        "寅" to listOf("壬", "甲"),
        "卯" to listOf("癸", "乙"),
        "巳" to listOf("庚", "丙"),
        "午" to listOf("己", "丁"),
        "申" to listOf("丙", "庚"),
        "酉" to listOf("戊", "辛"),
        "亥" to listOf("甲", "壬")
    )

    data class AnalysisResult(
        val bingYin: String,
        val bingZheng: String,
        val bingJi: String,
        val bingWei: String,
        val zhiZe: String,
        val fangBian: String
    )

    fun analyze(model: String): AnalysisResult {
        // 解析模型：年干支/月干支/段極/中段/小段/日干支/時干支/大刻/小刻
        val parts = model.split(" / ").map { it.trim() }
        if (parts.size < 7) {
            return AnalysisResult("輸入格式錯誤", "", "", "", "", "")
        }

        val nianGZ = parts[0]
        val yueGZ = parts[1]
        val duanJi = parts[2].replace("大段", "")
        val zhongDuan = parts[3].replace("中段", "")
        val riGZ = parts[5]
        val shiGZ = if (parts.size > 6 && parts[6] != "-" && parts[6] != "X") parts[6] else null

        // 提取干支
        val nianGan = if (nianGZ.isNotEmpty()) nianGZ[0].toString() else ""
        val nianZhi = if (nianGZ.length >= 2) nianGZ[1].toString() else ""
        val yueGan = if (yueGZ.isNotEmpty()) yueGZ[0].toString() else ""
        val yueZhi = if (yueGZ.length >= 2) yueGZ[1].toString() else ""
        val riGan = if (riGZ.isNotEmpty()) riGZ[0].toString() else ""
        val riZhi = if (riGZ.length >= 2) riGZ[1].toString() else ""
        val shiGan = if (shiGZ != null && shiGZ.isNotEmpty()) shiGZ[0].toString() else null
        val shiZhi = if (shiGZ != null && shiGZ.length >= 2) shiGZ[1].toString() else null

        val bingYinList = mutableListOf<String>()
        val bingJiList = mutableListOf<String>()
        val zhiZeList = mutableListOf<String>()

        // 第一步：分析日極（人極核心）
        // 日干與年干、月幹關聯
        if (tianGanHe(riGan, nianGan) || riGan == nianGan) {
            bingYinList.add("日幹" + riGan + "與年幹" + nianGan + "同幹")
            zhiZeList.add("同幹宜養（日極與年極）")
        }
        if (tianGanHe(riGan, yueGan) || riGan == yueGan) {
            bingYinList.add("日幹" + riGan + "與月幹" + yueGan + "同幹")
            zhiZeList.add("同幹宜養（日極與月極）")
        }

        // 日支與段極、月支關聯
        if (dizhiHe(riZhi, duanJi) || riZhi == duanJi) {
            val cangGan = dizhiCangGan(riZhi)
            val allGan = listOf(nianGan, yueGan, riGan, shiGan ?: "")
            val cangBuXian = cangGan.filter { it !in allGan }
            if (cangBuXian.isNotEmpty()) {
                bingYinList.add("日支" + riZhi + "與段極" + duanJi + "同支")
                zhiZeList.add("同支宜固（日極與段極）")
            }
        }
        if (dizhiHe(riZhi, yueZhi) || riZhi == yueZhi) {
            bingYinList.add("日支" + riZhi + "與月支" + yueZhi + "同支")
            zhiZeList.add("同支宜固（日極與月極）")
        }

        // 克逆判定
        if (dizhiChong(riZhi, nianZhi)) {
            bingYinList.add("日支" + riZhi + "沖年支" + nianZhi)
            zhiZeList.add("克逆宜生（日極與年極）")
        }

        // 第二步：分析時極（若有）
        if (shiGan != null && shiZhi != null) {
            if (tianGanHe(shiGan, nianGan) || shiGan == nianGan) {
                bingJiList.add("時幹" + shiGan + "與年幹" + nianGan + "失常")
                zhiZeList.add("同幹宜養（時極與年極）")
            }
            if (tianGanHe(shiGan, yueGan) || shiGan == yueGan) {
                bingJiList.add("時幹" + shiGan + "與月幹" + yueGan + "失常")
                zhiZeList.add("同幹宜養（時極與月極）")
            }

            if (dizhiHe(shiZhi, duanJi) || shiZhi == duanJi) {
                bingJiList.add("時支" + shiZhi + "與段極" + duanJi + "不足")
                zhiZeList.add("同支宜固（時極與段極）")
            }
            if (dizhiHe(shiZhi, zhongDuan) || shiZhi == zhongDuan) {
                bingJiList.add("時支" + shiZhi + "與中段" + zhongDuan + "不足")
                zhiZeList.add("同支宜固（時極與中段）")
            }

            if (dizhiChong(shiZhi, yueZhi)) {
                bingJiList.add("時支" + shiZhi + "沖月支" + yueZhi)
                zhiZeList.add("克逆宜生（時極與月極）")
            }
        }

        // 組合結果
        val bingYin = if (bingYinList.isNotEmpty()) bingYinList.joinToString("；") else "未發現明顯病因"
        val bingJi = if (bingJiList.isNotEmpty()) bingJiList.joinToString("；") else riGZ + "為本"
        val bingZheng = riGZ + "症"
        val bingWei = if (shiGZ != null) shiGZ else "未明確"
        val zhiZe = if (zhiZeList.isNotEmpty()) zhiZeList.distinct().joinToString("\n") else "平調即可"

        // 防變：檢查藏幹
        val fangBianList = mutableListOf<String>()
        val riCang = dizhiCangGan(riZhi)
        val yueCang = dizhiCangGan(yueZhi)
        val nianCang = dizhiCangGan(nianZhi)

        for (cg in riCang) {
            if (cg in yueCang || cg in nianCang) {
                fangBianList.add(cg + "藏於地支，謹防傳變")
            }
        }

        // 檢查地支合
        if (dizhiHe(yueZhi, riZhi) || dizhiHe(nianZhi, riZhi)) {
            fangBianList.add("謹防病症隨地支合化而傳變")
        }

        val fangBian = if (fangBianList.isNotEmpty()) 
            fangBianList.joinToString("；") 
        else 
            "無明顯傳變風險"

        return AnalysisResult(
            bingYin = bingYin,
            bingZheng = bingZheng,
            bingJi = bingJi,
            bingWei = bingWei,
            zhiZe = zhiZe,
            fangBian = fangBian
        )
    }
}
