package com.orbit.worldtime

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("orbit_world_time", Context.MODE_PRIVATE) }
    private lateinit var list: LinearLayout
    private lateinit var search: EditText
    private lateinit var selectedLabel: TextView
    private var query = ""
    private val handler = Handler(Looper.getMainLooper())
    private val tick = object : Runnable { override fun run() { updateTimeLabels(); handler.postDelayed(this, 1000) } }
    private val zones: List<String> by lazy { TimeZone.getAvailableIDs().filter { it.contains('/') && !it.startsWith("Etc/") }.distinct().sorted() }
    private val selected: MutableSet<String> get() = prefs.getStringSet("selected_zones", setOf("Africa/Lagos", "Europe/London", "America/New_York", "Asia/Tokyo"))!!.toMutableSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!prefs.contains("selected_zones")) prefs.edit().putStringSet("selected_zones", setOf("Africa/Lagos", "Europe/London", "America/New_York", "Asia/Tokyo")).apply()
        buildUi(); renderList(); handler.post(tick)
    }
    override fun onDestroy() { handler.removeCallbacks(tick); super.onDestroy() }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun rounded(color: Int, radius: Int, stroke: Int = 0): GradientDrawable = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat(); if (stroke != 0) setStroke(dp(1), stroke) }
    private fun text(value: String, size: Float, color: Int, bold: Boolean = false): TextView = TextView(this).apply { text = value; textSize = size; setTextColor(color); if (bold) setTypeface(null, Typeface.BOLD) }

    private fun buildUi() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(18), dp(20), dp(8)); setBackgroundColor(Color.rgb(16,21,43)) }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val eyebrow = text("ORBIT  /  TIME INTELLIGENCE", 11f, Color.rgb(114,240,192), true).apply { letterSpacing = .12f }
        content.addView(eyebrow, matchWrap())
        content.addView(text("Everywhere,\nright now.", 34f, Color.WHITE, true).apply { setPadding(0, dp(12), 0, dp(8)); setLineSpacing(dp(1).toFloat(), 1f) }, matchWrap())
        content.addView(text("A little window into every time zone on Earth.", 14f, Color.rgb(171,183,211)), matchWrap())

        val hero = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)); background = rounded(Color.rgb(28,43,68), 20, Color.rgb(53,66,99)) }
        hero.addView(text("YOUR LOCAL REFERENCE", 10f, Color.rgb(114,240,192), true), matchWrap())
        val localZone = TimeZone.getDefault().id
        hero.addView(text(cityName(localZone), 20f, Color.WHITE, true).apply { setPadding(0, dp(6), 0, dp(2)) }, matchWrap())
        val localTime = text("", 30f, Color.WHITE, true).apply { typeface = Typeface.MONOSPACE }
        hero.addView(localTime, matchWrap())
        val localDate = text("", 12f, Color.rgb(171,183,211))
        hero.addView(localDate, matchWrap())
        val localTick = object : Runnable { override fun run() { val now = Date(); localTime.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now); localDate.text = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(now); handler.postDelayed(this, 1000) } }
        handler.post(localTick)
        content.addView(hero, matchWrap().apply { topMargin = dp(18); bottomMargin = dp(18) })

        val heading = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        heading.addView(text("Pinned clocks", 20f, Color.WHITE, true), LinearLayout.LayoutParams(0, dp(34), 1f))
        selectedLabel = text("4 ON WIDGET", 10f, Color.rgb(114,240,192), true)
        heading.addView(selectedLabel)
        content.addView(heading, matchWrap())
        content.addView(text("Choose any city below. Your first four pinned clocks appear on your home-screen widget.", 12f, Color.rgb(171,183,211)).apply { setPadding(0, dp(4), 0, dp(12)) }, matchWrap())

        search = EditText(this).apply { hint = "Search city or time zone…"; setHintTextColor(Color.rgb(123,137,168)); setTextColor(Color.WHITE); textSize = 15f; singleLine = true; setPadding(dp(14), 0, dp(14), 0); background = rounded(Color.rgb(27,34,60), 14, Color.rgb(53,66,99)) }
        content.addView(search, LinearLayout.LayoutParams(-1, dp(50)).apply { bottomMargin = dp(12) })
        search.addTextChangedListener(object : TextWatcher { override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {} ; override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { query = s?.toString()?.trim()?.lowercase(Locale.ROOT) ?: ""; renderList() }; override fun afterTextChanged(s: Editable?) {} })
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(list, matchWrap())
        content.addView(text("TIME ZONES PROVIDED BY YOUR DEVICE · OFFLINE READY", 9f, Color.rgb(123,137,168), true).apply { gravity = Gravity.CENTER; setPadding(0, dp(20), 0, dp(20)) }, matchWrap())
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }
    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)

    private fun cityName(id: String): String = id.substringAfterLast('/').replace('_', ' ')
    private fun countryRegion(id: String): String = when (id.substringBefore('/')) { "Africa" -> "AFRICA"; "America" -> "AMERICAS"; "Antarctica" -> "ANTARCTICA"; "Asia" -> "ASIA"; "Atlantic" -> "ATLANTIC"; "Australia" -> "OCEANIA"; "Europe" -> "EUROPE"; "Indian" -> "INDIAN OCEAN"; "Pacific" -> "PACIFIC"; else -> id.substringBefore('/') .uppercase(Locale.ROOT) }
    private fun timeFor(id: String): String { val f = SimpleDateFormat("HH:mm", Locale.getDefault()); f.timeZone = TimeZone.getTimeZone(id); return f.format(Date()) }
    private fun updateTimeLabels() {
        if (!::list.isInitialized) return
        for (i in 0 until list.childCount) {
            val row = list.getChildAt(i) as? LinearLayout ?: continue
            val id = row.tag as? String ?: continue
            if (row.childCount < 2) continue
            val right = row.getChildAt(1) as? LinearLayout ?: continue
            val time = if (right.childCount > 0) right.getChildAt(0) as? TextView else null
            time?.text = timeFor(id)
        }
    }

    private fun renderList() {
        if (!::list.isInitialized) return
        list.removeAllViews()
        val pins = selected
        selectedLabel.text = "${pins.size} PINNED"
        val filtered = zones.asSequence().filter { query.isBlank() || it.lowercase(Locale.ROOT).contains(query) || cityName(it).lowercase(Locale.ROOT).contains(query) || countryRegion(it).lowercase(Locale.ROOT).contains(query) }.toList()
        if (filtered.isEmpty()) { list.addView(text("No matching time zones. Try a city name such as Paris or Tokyo.", 14f, Color.LTGRAY).apply { setPadding(dp(8), dp(20), dp(8), dp(20)) }); return }
        var lastRegion = ""
        filtered.forEach { id ->
            val region = countryRegion(id)
            if (region != lastRegion) { list.addView(text(region, 10f, Color.rgb(114,240,192), true).apply { setPadding(dp(4), dp(12), 0, dp(7)); letterSpacing = .12f }); lastRegion = region }
            val pinned = pins.contains(id)
            val row = LinearLayout(this).apply { tag = id; orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(13), dp(11), dp(13), dp(11)); background = rounded(if (pinned) Color.rgb(35,55,70) else Color.rgb(25,31,54), 15, if (pinned) Color.rgb(66,127,115) else Color.rgb(42,51,78)) }
            val left = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            left.addView(text(cityName(id), 15f, Color.WHITE, true), matchWrap())
            left.addView(text(id, 10f, Color.rgb(153,168,198)), matchWrap())
            row.addView(left, LinearLayout.LayoutParams(0, -2, 1f))
            val right = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.END }
            right.addView(text(timeFor(id), 19f, Color.WHITE, true).apply { typeface = Typeface.MONOSPACE; tag = "time" }, matchWrap())
            right.addView(text(if (pinned) "● PINNED" else "+ PIN", 9f, if (pinned) Color.rgb(114,240,192) else Color.rgb(171,183,211), true), matchWrap())
            row.addView(right)
            row.setOnClickListener {
                val current = selected
                if (current.contains(id)) current.remove(id) else current.add(id)
                prefs.edit().putStringSet("selected_zones", current).apply()
                updateWidgets(); renderList()
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(search.windowToken, 0)
            }
            list.addView(row, matchWrap().apply { bottomMargin = dp(7) })
        }
    }
    private fun updateWidgets() { val manager = AppWidgetManager.getInstance(this); val ids = manager.getAppWidgetIds(ComponentName(this, WorldClockWidget::class.java)); ids.forEach { WorldClockWidget.update(this, manager, it) } }
}
