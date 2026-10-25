package com.sajdatime.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle

/** The Today strip widget (step 8). See [NextPrayerWidgetProvider]: the three behave identically. */
class TodayStripWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) =
        WidgetUpdater.refreshFromProvider(this, context)

    override fun onEnabled(context: Context) = WidgetUpdater.refreshFromProvider(this, context)

    /** The last instance of this widget was removed: redraw finds none of the three and cancels the alarm. */
    override fun onDisabled(context: Context) = WidgetUpdater.refreshFromProvider(this, context)

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) = WidgetUpdater.refreshFromProvider(this, context)
}
