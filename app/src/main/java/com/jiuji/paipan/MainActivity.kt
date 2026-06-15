package com.jiuji.paipan

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var etYear: EditText
    private lateinit var etMonth: EditText
    private lateinit var etDay: EditText
    private lateinit var etHour: EditText
    private lateinit var etMinute: EditText
    private lateinit var btnCalc: Button
    private lateinit var tvError: TextView
    private lateinit var tvLunar: TextView
    private lateinit var pagePaipan: ScrollView
    private lateinit var pageZiwu: ScrollView
    private lateinit var tabPaipan: View
    private lateinit var tabZiwu: View
    private lateinit var tabPaipanLabel: TextView
    private lateinit var tabZiwuLabel: TextView
    private lateinit var tvTabTitle: TextView
    private lateinit var resultSection: View
    private lateinit var tvSummary: TextView
    private lateinit var tvNian: TextView
    private lateinit var tvNianD: TextView
    private lateinit var tvYue: TextView
    private lateinit var tvYueD: TextView
    private lateinit var tvDaDuan: TextView
    private lateinit var tvDaDuanD: TextView
    private lateinit var tvZhDuan: TextView
    private lateinit var tvZhDuanD: TextView
    private lateinit var tvXiDuan: TextView
    private lateinit var tvXiDuanD: TextView
    private lateinit var tvRi: TextView
    private lateinit var tvRiD: TextView
    private lateinit var tvShi: TextView
    private lateinit var tvShiD: TextView
    private lateinit var tvDaKe: TextView
    private lateinit var tvDaKeD: TextView
    private lateinit var tvXiaoKe: TextView
    private lateinit var tvXiaoKeD: TextView
    private lateinit var ziwuEmptyHint: View
    private lateinit var ziwuContent: View
    private lateinit var tvZiwuContext: TextView
    private lateinit var tvNaziMeridian: TextView
    private lateinit var tvNaziAllPoints: TextView
    private lateinit var tvNaziBu: TextView
    private lateinit var tvNaziXie: TextView
    private lateinit var tvNaziBen: TextView
    private lateinit var tvNaJiaMain: TextView
    private lateinit var tvNaJiaHe: TextView
    private lateinit var tvNaJiaYuan: TextView
    private lateinit var tvNaJiaNote: TextView
    private lateinit var tvLingGui: TextView
    private lateinit var tvFeiteng: TextView

    private var lastResult: PaipanEngine.PaipanResult? = null
    private var currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        setupListeners()
        UpdateChecker.checkSilently(this)
    }

    private fun bindViews() {
        etYear = findViewById(R.id.etYear)
        etMonth = findViewById(R.id.etMonth)
        etDay = findViewById(R.id.etDay)
        etHour = findViewById(R.id.etHour)
        etMinute = findViewById(R.id.etMinute)
        btnCalc = findViewById(R.id.btnCalc)
        tvError = findViewById(R.id.tvError)
        tvLunar = findViewById(R.id.tvLunar)
        pagePaipan = findViewById(R.id.pagePaipan)
        pageZiwu = findViewById(R.id.pageZiwu)
        tabPaipan = findViewById(R.id.tabPaipan)
        tabZiwu = findViewById(R.id.tabZiwu)
        tabPaipanLabel = findViewById(R.id.tabPaipanLabel)
        tabZiwuLabel = findViewById(R.id.tabZiwuLabel)
        tvTabTitle = findViewById(R.id.tvTabTitle)
        resultSection = findViewById(R.id.resultSection)
        tvSummary = findViewById(R.id.tvSummary)
        tvNian = findViewById(R.id.tvNian)
        tvNianD = findViewById(R.id.tvNianD)
        tvYue = findViewById(R.id.tvYue)
        tvYueD = findViewById(R.id.tvYueD)
        tvDaDuan = findViewById(R.id.tvDaDuan)
        tvDaDuanD = findViewById(R.id.tvDaDuanD)
        tvZhDuan = findViewById(R.id.tvZhDuan)
        tvZhDuanD = findViewById(R.id.tvZhDuanD)
        tvXiDuan = findViewById(R.id.tvXiDuan)
        tvXiDuanD = findViewById(R.id.tvXiDuanD)
        tvRi = findViewById(R.id.tvRi)
        tvRiD = findViewById(R.id.tvRiD)
        tvShi = findViewById(R.id.tvShi)
        tvShiD = findViewById(R.id.tvShiD)
        tvDaKe = findViewById(R.id.tvDaKe)
        tvDaKeD = findViewById(R.id.tvDaKeD)
        tvXiaoKe = findViewById(R.id.tvXiaoKe)
        tvXiaoKeD = findViewById(R.id.tvXiaoKeD)
        ziwuEmptyHint = findViewById(R.id.ziwuEmptyHint)
        ziwuContent = findViewById(R.id.ziwuContent)
        tvZiwuContext = findViewById(R.id.tvZiwuContext)
        tvNaziMeridian = findViewById(R.id.tvNaziMeridian)
        tvNaziAllPoints = findViewById(R.id.tvNaziAllPoints)
        tvNaziBu = findViewById(R.id.tvNaziBu)
        tvNaziXie = findViewById(R.id.tvNaziXie)
        tvNaziBen = findViewById(R.id.tvNaziBen)
        tvNaJiaMain = findViewById(R.id.tvNaJiaMain)
        tvNaJiaHe = findViewById(R.id.tvNaJiaHe)
        tvNaJiaYuan = findViewById(R.id.tvNaJiaYuan)
        tvNaJiaNote = findViewById(R.id.tvNaJiaNote)
        tvLingGui = findViewById(R.id.tvLingGui)
        tvFeiteng = findViewById(R.id.tvFeiteng)
    }

    private fun setupListeners() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { refreshLunar() }
        }
        etYear.addTextChangedListener(watcher)
        etMonth.addTextChangedListener(watcher)
        etDay.addTextChangedListener(watcher)
        etHour.addTextChangedListener(watcher)
        btnCalc.setOnClickListener { calculate() }
        tabPaipan.setOnClickListener { switchTab(0) }
        tabZiwu.setOnClickListener { switchTab(1) }
    }

    private fun switchTab(tab: Int) {
        currentTab = tab
        if (tab == 0) {
            pagePaipan.visibility = View.VISIBLE
            pageZiwu.visibility = View.GONE
            tabPaipanLabel.setTextColor(Color.parseColor("#C9A227"))
            tabZiwuLabel.setTextColor(Color.parseColor("#4A4840"))
            tvTabTitle.text = "時空排盤"
        } else {
            pagePaipan.visibility = View.GONE
            pageZiwu.visibility = View.VISIBLE
            tabPaipanLabel.setTextColor(Color.parseColor("#4A4840"))
            tabZiwuLabel.setTextColor(Color.parseColor("#9ECFCE"))
            tvTabTitle.text = "子午流注針法"
            refreshZiwu()
        }
    }

    private fun refreshLunar() {
        val y = etYear.text.toString().toIntOrNull()
        val m = etMonth.text.toString().toIntOrNull()
        val d = etDay.text.toString().toIntOrNull()
        if (y != null && m != null && d != null && y in 1940..2100 && m in 1..12 && d in 1..31) {
            val lunar = LunarCalendar.toLunar(y, m, d)
            if (lunar != null) {
                val shiftNote = if ((etHour.text.toString().toIntOrNull() ?: -1) >= 23) "（23時後日極按次日）" else ""
                tvLunar.text = "農曆 " + LunarCalendar.monthName(lunar.month, lunar.isLeap) + LunarCalendar.dayName(lunar.day) + shiftNote
                tvLunar.visibility = View.VISIBLE
                return
            }
        }
        tvLunar.visibility = View.GONE
    }

    private fun calculate() {
        tvError.visibility = View.GONE
        val y = etYear.text.toString().toIntOrNull()
        val m = etMonth.text.toString().toIntOrNull()
        val d = etDay.text.toString().toIntOrNull()
        val h = etHour.text.toString().toIntOrNull()
        val mn = etMinute.text.toString().toIntOrNull()
        if (y == null || m == null || d == null) { showError("請輸入公曆年、月、日"); return }
        if (y !in 1940..2100) { showError("年份範圍：1940–2100"); return }
        if (m !in 1..12) { showError("月份：1–12"); return }
        if (d !in 1..31) { showError("日期：1–31"); return }
        if (h != null && h !in 0..23) { showError("小時：0–23"); return }
        if (mn != null && mn !in 0..59) { showError("分鐘：0–59"); return }
        val result = PaipanEngine.paipan(y, m, d, h, mn)
        lastResult = result
        displayResult(result)
        resultSection.visibility = View.VISIBLE
        pagePaipan.post { pagePaipan.smoothScrollTo(0, resultSection.top) }
        if (currentTab == 1) refreshZiwu()
    }

    private fun showError(msg: String) {
        tvError.text = msg
        tvError.visibility = View.VISIBLE
    }

    private fun displayResult(r: PaipanEngine.PaipanResult) {
        tvSummary.text = r.summary()
        setCard(tvNian, tvNianD, r.nianJi, "天極·年", false)
        setCard(tvYue, tvYueD, r.yueJi, "地極·月", false)
        val daDuanDesc = if (r.daDuanJi != null) r.lunarStr else "超出農曆範圍"
        setCard(tvDaDuan, tvDaDuanD, r.daDuanJi ?: "X", daDuanDesc, r.daDuanJi == null)
        val zhDesc = if (r.zhongDuanJi != null) "每5小時一地支" else "需輸入小時"
        setCard(tvZhDuan, tvZhDuanD, r.zhongDuanJi ?: "X", zhDesc, r.zhongDuanJi == null)
        val xiDesc = if (r.xiaoDuanJi != null) "每小時配五行" else "需輸入小時"
        setCard(tvXiDuan, tvXiDuanD, r.xiaoDuanJi ?: "X", xiDesc, r.xiaoDuanJi == null)
        setCard(tvRi, tvRiD, r.riJi, "人極·日", false)
        val shiDesc = if (r.shiJi != null) "時辰" else "需輸入小時"
        setCard(tvShi, tvShiD, r.shiJi ?: "X", shiDesc, r.shiJi == null)
        val daKeDesc = if (r.daKeJi != null) "每10分鐘" else "需輸入分鐘"
        setCard(tvDaKe, tvDaKeD, r.daKeJi ?: "X", daKeDesc, r.daKeJi == null)
        val xiaoKeDesc = if (r.xiaoKeJi != null) "前5分陽後5分陰" else "需輸入分鐘"
        setCard(tvXiaoKe, tvXiaoKeD, r.xiaoKeJi ?: "X", xiaoKeDesc, r.xiaoKeJi == null)
    }

    private fun setCard(v: TextView, d: TextView, value: String, desc: String, faded: Boolean) {
        v.text = value
        d.text = desc
        v.alpha = if (faded) 0.4f else 1f
        v.setTextColor(if (faded) Color.parseColor("#888888") else Color.parseColor("#C9A227"))
    }

    private fun refreshZiwu() {
        val r = lastResult
        if (r == null || r.shiJi == null) {
            ziwuEmptyHint.visibility = View.VISIBLE
            ziwuContent.visibility = View.GONE
            return
        }
        ziwuEmptyHint.visibility = View.GONE
        ziwuContent.visibility = View.VISIBLE

        tvZiwuContext.text = r.nianJi + "年  " + r.yueJi + "月  " + r.riJi + "日  " + r.shiJi + "時"

        val hour = etHour.text.toString().toIntOrNull() ?: 0
        val ziwu = ZiwuEngine.compute(hour, r.riJi, r.shiJi)
        val nazi = ziwu.nazi

        tvNaziMeridian.text = nazi.dzName + "時  " + nazi.meridian + "（" + nazi.organ + "）"
        tvNaziAllPoints.text = nazi.allPts.joinToString("  ")
        tvNaziBu.text = nazi.buPt
        tvNaziXie.text = nazi.xiePt
        tvNaziBen.text = nazi.benPt

        val naJia = ziwu.naJia
        if (naJia != null) {
            tvNaJiaMain.text = naJia.mainPt ?: "（無主穴）"
            tvNaJiaHe.text = naJia.hePt ?: "—"
            tvNaJiaYuan.text = naJia.yuanPt ?: "—"
            tvNaJiaNote.text = naJia.note
        }

        val lingGui = ziwu.lingGui
        if (lingGui != null) {
            val lgText = "主穴：" + lingGui.mainPt + "（" + lingGui.gua + "宮）  配穴：" + lingGui.pairPt + "\n" + lingGui.note
            tvLingGui.text = lgText
        }

        val feiteng = ziwu.feiteng
        if (feiteng != null) {
            val ftText = feiteng.shiTg + "時 → 主穴：" + feiteng.mainPt + "（" + feiteng.gua + "）  配穴：" + feiteng.pairPt
            tvFeiteng.text = ftText
        }
    }
}
