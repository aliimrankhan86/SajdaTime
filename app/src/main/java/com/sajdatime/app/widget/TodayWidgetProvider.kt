package com.sajdatime.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle

/** The Today widget. See [NextPrayerWidgetProvider]: the two behave identically. */
class TodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) =
        WidgetUpdater.refreshFromProvider(this, context)

    override fun onEnabled(context: Context) = WidgetUpdater.refreshFromProvider(this, context)

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) = WidgetUpdater.refreshFromProvider(this, context)
}
