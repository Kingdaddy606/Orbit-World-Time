package com.orbit.worldtime

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import java.util.TimeZone

class WorldClockWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) { ids.forEach { update(context, manager, it) } }
    override fun onEnabled(context: Context) { super.onEnabled(context); val m = AppWidgetManager.getInstance(context); m.getAppWidgetIds(ComponentName(context, WorldClockWidget::class.java)).forEach { update(context, m, it) } }
    companion object {
        fun update(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences("orbit_world_time", Context.MODE_PRIVATE)
            val defaults = linkedSetOf("Africa/Lagos", "Europe/London", "America/New_York", "Asia/Tokyo")
            val saved = prefs.getStringSet("selected_zones", defaults) ?: defaults
            val zones = saved.toList().take(4)
            val views = RemoteViews(context.packageName, R.layout.world_clock_widget)
            val cityIds = intArrayOf(R.id.city1, R.id.city2, R.id.city3, R.id.city4)
            val timeIds = intArrayOf(R.id.time1, R.id.time2, R.id.time3, R.id.time4)
            val slotIds = intArrayOf(R.id.slot1, R.id.slot2, R.id.slot3, R.id.slot4)
            for (i in 0..3) {
                if (i < zones.size) {
                    val zone = zones[i]
                    val city = zone.substringAfterLast('/').replace('_', ' ')
                    views.setViewVisibility(slotIds[i], android.view.View.VISIBLE)
                    views.setTextViewText(cityIds[i], "$city  ·  ${zone.replace('_', ' ')}")
                    views.setString(timeIds[i], "setTimeZone", zone)
                } else views.setViewVisibility(slotIds[i], android.view.View.GONE)
            }
            manager.updateAppWidget(appWidgetId, views)
        }
    }
}
