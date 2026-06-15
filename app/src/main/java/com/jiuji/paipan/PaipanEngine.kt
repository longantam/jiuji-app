package com.jiuji.paipan

import java.util.GregorianCalendar

object PaipanEngine {
    private val TG = arrayOf("甲","乙","丙","丁","戊","己","庚","辛","壬","癸")
    private val DZ = arrayOf("子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥")

    data class PaipanResult(
        val nianJi: String, val yueJi: String,
        val daDuanJi: String?, val zhongDuanJi: String?, val xiaoDuanJi: String?,
        val riJi: String, val shiJi: String?,
        val daKeJi: String?, val xiaoKeJi: String?,
        val lunarStr: String, val lunarDay: Int?
    ) {
        fun summary() = listOf(nianJi,yueJi,daDuanJi?:"X",zhongDuanJi?:"X",
            xiaoDuanJi?:"X",riJi,shiJi?:"X",daKeJi?:"X",xiaoKeJi?:"X").joinToString(" / ")
    }

    private fun jd(y: Int, m: Int, d: Int): Long {
        var my = y.toDouble(); var mm = m.toDouble()
        if (mm <= 2) { my -= 1; mm += 12 }
        val a = (my / 100).toInt(); val b = 2 - a + a / 4
        return (365.25*(my+4716)).toLong() + (30.6001*(mm+1)).toLong() + d + b - 1524
    }

    private fun nextDate(y: Int, m: Int, d: Int): Triple<Int, Int, Int> {
        val cal = GregorianCalendar(y, m - 1, d)
        cal.add(GregorianCalendar.DAY_OF_MONTH, 1)
        return Triple(
            cal.get(GregorianCalendar.YEAR),
            cal.get(GregorianCalendar.MONTH) + 1,
            cal.get(GregorianCalendar.DAY_OF_MONTH)
        )
    }

    private fun effectiveDayForZiChu(y: Int, m: Int, d: Int, hour: Int?): Triple<Int, Int, Int> {
        return if (hour != null && hour >= 23) nextDate(y, m, d) else Triple(y, m, d)
    }

    fun yearGZ(y: Int): String {
        val o = ((y-1984)%60+60)%60
        return TG[o%10]+DZ[o%12]
    }

    fun dayGZ(y: Int, m: Int, d: Int): String {
        val diff = jd(y,m,d) - 2451545L
        val idx = ((54+diff)%60+60).toInt()%60
        return TG[idx%10]+DZ[idx%12]
    }

    fun monthGZ(y: Int, sm: Int, sd: Int): String {
        val jqd = intArrayOf(0,6,4,6,5,6,6,7,7,8,8,7,7)
        val jqb = intArrayOf(0,1,2,3,4,5,6,7,8,9,10,11,0)
        val mb = if (sd >= jqd[sm]) jqb[sm] else jqb[if(sm==1) 12 else sm-1]
        val ti = TG.indexOf(yearGZ(y)[0].toString())
        val ms = intArrayOf(2,4,6,8,0)
        return TG[(ms[ti%5]+(mb-2+12)%12)%10]+DZ[mb]
    }

    private fun shiIdx(h: Int) = ((h+1)%24)/2

    fun shiGZ(dgz: String, h: Int): String {
        val ti = TG.indexOf(dgz[0].toString())
        val sb = shiIdx(h)
        return TG[(intArrayOf(0,2,4,6,8)[ti%5]+sb)%10]+DZ[sb]
    }

    private fun daDuanSeg(totalMin: Int) = (2 + totalMin / 3600) % 12

    fun daDuan(ld: Int, h: Int, mn: Int): String {
        val total = (ld-1)*1440 + h*60 + mn
        return DZ[daDuanSeg(total)] + "大段"
    }

    fun daDuanRange(ld: Int): String {
        val dayStart = (ld-1)*1440
        val dayEnd   = ld*1440 - 1
        val segS = daDuanSeg(dayStart)
        val segE = daDuanSeg(dayEnd)
        return if (segS == segE) {
            DZ[segS] + "大段"
        } else {
            DZ[segS] + " / " + DZ[segE] + "大段"
        }
    }

    private fun zhongDuanSeg(totalMin: Int) = (2 + totalMin / 300) % 12

    fun zhongDuan(ld: Int, h: Int, mn: Int): String {
        val total = (ld-1)*1440 + h*60 + mn
        return DZ[zhongDuanSeg(total)] + "中段"
    }

    fun zhongDuanRange(ld: Int, h: Int): String {
        val base = (ld-1)*1440 + h*60
        val segS = zhongDuanSeg(base)
        val segE = zhongDuanSeg(base + 59)
        return if (segS == segE) {
            DZ[segS] + "中段"
        } else {
            DZ[segS] + " / " + DZ[segE] + "中段"
        }
    }

    fun xiaoDuan(ld: Int, h: Int, mn: Int): String {
        val total = (ld-1)*1440 + h*60 + mn
        return arrayOf("木","火","土","金","水")[(total%300)/60]
    }

    fun daKe(h: Int, mn: Int): String {
        val totalMin = ((h - 3 + 24) % 24) * 60 + mn
        val keIdx = totalMin / 10
        return DZ[(2 + keIdx) % 12] + "大刻"
    }

    fun daKeRange(h: Int): String {
        val base = ((h - 3 + 24) % 24) * 60
        val segS = (2 + base / 10) % 12
        val segE = (2 + (base + 59) / 10) % 12
        return if (segS == segE) {
            DZ[segS] + "大刻"
        } else {
            DZ[segS] + " / " + DZ[segE] + "大刻"
        }
    }

    fun xiaoKe(mn: Int) = if (mn % 10 < 5) "陽" else "陰"

    fun paipan(sy: Int, sm: Int, sd: Int, hour: Int?, minute: Int?): PaipanResult {
        val lunar = LunarCalendar.toLunar(sy, sm, sd)
        val ld = lunar?.day
        val ls = if (lunar != null)
            LunarCalendar.monthName(lunar.month, lunar.isLeap) + LunarCalendar.dayName(lunar.day)
        else "超出範圍"

        val ygz = if (lunar != null) yearGZ(lunar.year) else yearGZ(sy)
        val mgz = monthGZ(sy, sm, sd)

        val (dy, dm, dd) = effectiveDayForZiChu(sy, sm, sd, hour)
        val dgz = dayGZ(dy, dm, dd)
        val sgz = hour?.let { shiGZ(dgz, it) }

        val da = when {
            ld == null -> null
            hour != null -> daDuan(ld, hour, minute ?: 0)
            else -> daDuanRange(ld)
        }

        val zh = when {
            ld == null || hour == null -> null
            minute != null -> zhongDuan(ld, hour, minute)
            else -> zhongDuanRange(ld, hour)
        }

        val xi = if (ld != null && hour != null && minute != null)
            xiaoDuan(ld, hour, minute) else null

        val dk = if (hour != null && minute != null) daKe(hour, minute) else null
        val xk = if (minute != null) xiaoKe(minute) else null

        return PaipanResult(ygz, mgz, da, zh, xi, dgz, sgz, dk, xk, ls, ld)
    }
}
