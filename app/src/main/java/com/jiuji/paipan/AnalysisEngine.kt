package com.jiuji.paipan

object AnalysisEngine {
    private val WUXING = mapOf("甲" to "木", "乙" to "木", "丙" to "火", "丁" to "火", "戊" to "土", "己" to "土", "庚" to "金", "辛" to "金", "壬" to "水", "癸" to "水", "巳" to "火", "午" to "火", "亥" to "水", "未" to "土", "酉" to "金", "戌" to "土", "火" to "火", "子" to "水", "寅" to "木", "卯" to "木", "申" to "金", "丑" to "土", "辰" to "土")
    
    private fun tianGanHe(g1: String, g2: String) = (g1 == "丙" && g2 == "辛") || (g1 == "辛" && g2 == "丙") || (g1 == "乙" && g2 == "庚") || (g1 == "庚" && g2 == "乙") || (g1 == "甲" && g2 == "己") || (g1 == "己" && g2 == "甲") || (g1 == "丁" && g2 == "壬") || (g1 == "壬" && g2 == "丁") || (g1 == "戊" && g2 == "癸") || (g1 == "癸" && g2 == "戊")
    
    private fun dizhiChong(z1: String, z2: String) = (z1 == "巳" && z2 == "亥") || (z1 == "亥" && z2 == "巳") || (z1 == "子" && z2 == "午") || (z1 == "午" && z2 == "子") || (z1 == "丑" && z2 == "未") || (z1 == "未" && z2 == "丑") || (z1 == "寅" && z2 == "申") || (z1 == "申" && z2 == "寅") || (z1 == "卯" && z2 == "酉") || (z1 == "酉" && z2 == "卯") || (z1 == "辰" && z2 == "戌") || (z1 == "戌" && z2 == "辰")
    
    private fun wuxingKe(from: String, to: String): Boolean {
        val wx1 = WUXING[from] ?: return false
        val wx2 = WUXING[to] ?: return false
        return (wx1 == "金" && wx2 == "木") || (wx1 == "火" && wx2 == "金") || (wx1 == "木" && wx2 == "土") || (wx1 == "土" && wx2 == "水") || (wx1 == "水" && wx2 == "火")
    }
    
    private fun dizhiCangGan(dz: String) = when(dz) {
        "子" -> listOf("癸"); "丑" -> listOf("己", "癸", "辛"); "寅" -> listOf("甲", "丙", "戊")
        "卯" -> listOf("乙"); "辰" -> listOf("戊", "乙", "癸"); "巳" -> listOf("丙", "庚", "戊")
        "午" -> listOf("丁", "己"); "未" -> listOf("己", "丁", "乙"); "申" -> listOf("庚", "壬", "戊")
        "酉" -> listOf("辛"); "戌" -> listOf("戊", "辛", "丁"); "亥" -> listOf("壬", "甲")
        else -> listOf()
    }
    
    data class AnalysisResult(val bingYin: String, val bingZheng: String, val bingJi: String, val bingWei: String, val zhiZe: String, val fangBian: String)
    
    fun analyze(model: String): AnalysisResult {
        val parts = model.split("/").map { it.trim() }
        if (parts.size < 7) return AnalysisResult("", "", "", "", "", "")
        
        val nianGan = parts[0][0].toString(); val nianZhi = parts[0][1].toString()
        val yueGan = parts[1][0].toString(); val yueZhi = parts[1][1].toString()
        val duanJi = parts[2].replace("段極", "")
        val xiaoDuan = if (parts.size >= 5) parts[4].replace("小段", "") else ""
        val riGan = parts[5][0].toString(); val riZhi = parts[5][1].toString()
        val shiGan = if (parts.size >= 7 && parts[6] != "-" && parts[6] != "X") parts[6][0].toString() else null
        val shiZhi = if (parts.size >= 7 && parts[6] != "-" && parts[6] != "X") parts[6][1].toString() else null
        val daKe = if (parts.size >= 8 && parts[7] != "X" && parts[7] != "-") parts[7] else null
        
        val zhiZeList = mutableListOf<String>()
        var counter = 1
        
        if (shiGan != null && shiZhi != null) {
            val shizhiRels = mutableListOf<String>(); val shizhiMethods = mutableListOf<String>()
            if (xiaoDuan.isNotEmpty() && WUXING[shiZhi] == WUXING[xiaoDuan]) {
                shizhiRels.add(xiaoDuan); shizhiMethods.add("同支宜固")
            }
            if (dizhiChong(shiZhi, duanJi)) {
                shizhiRels.add(duanJi + "段極"); shizhiMethods.add("克逆宜生")
            }
            if (shizhiRels.isNotEmpty()) {
                zhiZeList.add(counter.toString() + ".時支" + shiZhi + "關聯" + shizhiRels.joinToString("/") + "不足，則時干" + shiGan + "亦應時失常。故分別以「" + shizhiMethods.joinToString("」和「") + "」調治。")
                counter++
            }
            
            val shiganRels = mutableListOf<String>(); val shiganMethods = mutableListOf<String>(); val shizhiRels2 = mutableListOf<String>()
            if (tianGanHe(shiGan, yueGan)) {
                shiganRels.add("月干" + yueGan); shiganMethods.add("同干宜養"); shizhiRels2.add("月支" + yueZhi)
            }
            if (tianGanHe(shiGan, nianGan)) {
                shiganRels.add("年干" + nianGan)
                if ("同干宜養" !in shiganMethods) shiganMethods.add("同干宜養")
                shizhiRels2.add("年支" + nianZhi)
            }
            if (wuxingKe(shiGan, yueGan)) {
                if ("克逆宜生" !in shiganMethods) shiganMethods.add("克逆宜生")
            }
            if (wuxingKe(shiGan, nianGan)) {
                if ("克逆宜生" !in shiganMethods) shiganMethods.add("克逆宜生")
            }
            if (shiganRels.isNotEmpty()) {
                zhiZeList.add(counter.toString() + ".時干" + shiGan + "關聯" + shiganRels.joinToString("/") + "失常，則時支" + shiZhi + "亦應" + shizhiRels2.joinToString("/") + "不足。故分別以「" + shiganMethods.joinToString("」和「") + "」調治。")
                counter++
            }
        }
        
        if (riGan == nianGan || tianGanHe(riGan, nianGan)) {
            zhiZeList.add(counter.toString() + ".日干" + riGan + "應年干" + nianGan + "失常，則日支" + riZhi + "亦應年支" + nianZhi + "不足。故以「同干宜養」調治。")
        }
        
        val fangBianList = mutableListOf<String>()
        val riCang = dizhiCangGan(riZhi)
        val nianCang = dizhiCangGan(nianZhi)
        for (cg in riCang) {
            if (cg in nianCang) fangBianList.add(cg + "藏於地支，謹防傳變")
        }
        
        return AnalysisResult(
            bingYin = "應時異常",
            bingZheng = parts[5],
            bingJi = if (shiGan != null) parts[6] else parts[5],
            bingWei = daKe ?: (if (shiGan != null) parts[6] else "未定"),
            zhiZe = zhiZeList.joinToString("
"),
            fangBian = if (fangBianList.isNotEmpty()) fangBianList.joinToString("；") else "無特殊防變"
        )
    }
}
