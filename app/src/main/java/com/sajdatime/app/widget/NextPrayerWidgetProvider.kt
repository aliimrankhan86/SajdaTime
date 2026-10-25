package com.sajdatime.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle

/**
 * The Next prayer widget. Every lifecycle callback does the same thing, redraw all three widgets from the
 * current settings, because the widgets are stateless: a launcher that restarts, a resize, or a first
 * placement all need exactly what any other trigger needs.
 */
class NextPrayerWidgetProvider : AppWidgetProvider() {

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
