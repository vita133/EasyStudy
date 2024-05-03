package com.example.easystudy

import android.app.Application
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.easystudy.R
import com.example.easystudy.entities.Event
import com.example.easystudy.entities.EventType
import com.example.easystudy.entities.RepeatType
import com.example.easystudy.viewmodels.EventViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ExamAppWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context.packageName, ExamAppWidget::class.java.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val eventViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application).create(EventViewModel::class.java)
            val allEvents = eventViewModel.getAllEvents()
            allEvents.observeForever { events ->
                Log.d("AllEvents", events.toString())
                events?.forEach { event ->
                    Log.d("Event 1", event.toString())
                }
            }

                allEvents.observeForever { events ->
                val closestEvent = getClosestEvent(events)
                if (closestEvent != null) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, closestEvent)
                } else {
                    val views = RemoteViews(context.packageName, R.layout.exam_app_widget)
                    views.setTextViewText(R.id.textView_event_title, "У вас немає подій")
                    views.setTextViewText(R.id.textView_time, "")
                    views.setTextViewText(R.id.textView_location, "")
                    views.setTextViewText(R.id.textView_type, "")
                    views.setImageViewBitmap(R.id.imageView_ellipse, null)
                    views.setInt(
                        R.id.event_linear_layout,
                        "setBackgroundResource",
                        R.drawable.rounded_corners
                    )
                    views.setImageViewBitmap(R.id.imageView_map, null)
                    views.setTextColor(
                        R.id.textView_event_title,
                        ContextCompat.getColor(context, R.color.black)
                    )
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            event: Event
        ) {
            val views = RemoteViews(context.packageName, R.layout.exam_app_widget)

            val formattedTime = "${event.startTime}-${event.endTime}"
            val textColor = getTextColor(event.type)
            val backgroundDrawable = getBackgroundDrawable(event.type)
            val ellipseImage = getEllipseImage(context, event.type)
            val mapImage = getMapImage(context, event.type)

            views.setImageViewBitmap(R.id.imageView_map, mapImage)
            views.setImageViewBitmap(R.id.imageView_ellipse, ellipseImage)
            views.setInt(R.id.event_linear_layout, "setBackgroundResource", backgroundDrawable)
            views.setTextViewText(R.id.textView_event_title, event.title)
            views.setTextViewText(R.id.textView_time, formattedTime)
            views.setTextViewText(R.id.textView_location, event.location)
            views.setTextViewText(R.id.textView_type, event.type.toString())
            views.setTextColor(R.id.textView_event_title, ContextCompat.getColor(context, textColor))
            views.setTextColor(R.id.textView_time, ContextCompat.getColor(context, textColor))
            views.setTextColor(R.id.textView_location, ContextCompat.getColor(context, textColor))
            views.setTextColor(R.id.textView_type, ContextCompat.getColor(context, textColor))

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getClosestEvent(events: List<Event>): Event? {
            val currentDate = LocalDate.now()
            val currentTime = LocalTime.now()

            var closestEvent: Event? = null
            var closestTimeDifference: Long = Long.MAX_VALUE

            for (event in events) {
                Log.d("Event 2", event.toString())
                val eventDateTime = LocalDateTime.of(event.date, event.startTime)
                if (event.date.isAfter(currentDate) ||
                    (event.date == currentDate && event.startTime.isAfter(currentTime)) ||
                    checkForRecurringEvents(currentDate, events).contains(event)
                ) {
                    Log.d("EventDateTime", eventDateTime.toString())

                    val timeDifference = Duration.between(LocalDateTime.now(), eventDateTime).toMillis()

                    if (timeDifference in 1 until closestTimeDifference) {
                        closestTimeDifference = timeDifference
                        closestEvent = event
                    }
                }
            }

            return closestEvent
        }

        private fun checkForRecurringEvents(date: LocalDate, allEvents: List<Event>): List<Event> {
            val events: MutableList<Event> = mutableListOf()
            for (event in allEvents) {
                if (event.date.isAfter(date) && event.repeat != RepeatType.NEVER) {
                    val currentDate = LocalDate.now()
                    val startDate = event.date
                    val totalLessons = event.count.toInt()
                    val repeatType = event.repeat

                    val passedLessons = if (currentDate.isAfter(startDate)) {
                        when (repeatType) {
                            RepeatType.WEEKLY -> {
                                val weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startDate, currentDate)
                                (weeksBetween + 1).toInt()
                            }
                            RepeatType.BIWEEKLY -> {
                                val weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startDate, currentDate)
                                ((weeksBetween + 1) / 2).toInt()
                            }
                            RepeatType.NEVER -> {
                                1
                            }
                        }
                    } else {
                        0
                    }

                    if (passedLessons < totalLessons) {
                        events.add(event)
                    }
                }
            }
            return events
        }

        private fun getTextColor(eventType: EventType): Int {
            return when (eventType) {
                EventType.LECTURE -> R.color.dark_blue
                EventType.PRACTICE -> R.color.dark_green
                EventType.SEMINAR -> R.color.dark_purple
                EventType.EXAM -> R.color.dark_red
            }
        }

        private fun getBackgroundDrawable(eventType: EventType): Int {
            return when (eventType) {
                EventType.LECTURE -> R.drawable.rounded_corners_blue
                EventType.PRACTICE -> R.drawable.rounded_corners_green
                EventType.SEMINAR -> R.drawable.rounded_corners_purple
                EventType.EXAM -> R.drawable.rounded_corners_red
            }
        }

        private fun getEllipseImage(context: Context, eventType: EventType): Bitmap {
            return when (eventType) {
                EventType.LECTURE -> getBitmapFromDrawable(ContextCompat.getDrawable(context, R.drawable.ic_ellipse_blue))
                EventType.PRACTICE -> getBitmapFromDrawable(ContextCompat.getDrawable(context, R.drawable.ic_ellipse_green))
                EventType.SEMINAR -> getBitmapFromDrawable(ContextCompat.getDrawable(context, R.drawable.ic_ellipse_purple))
                EventType.EXAM -> getBitmapFromDrawable(ContextCompat.getDrawable(context, R.drawable.ic_ellipse_red))
            }!!
        }

        private fun getMapImage(context: Context, eventType: EventType): Bitmap {
            return when (eventType) {
                EventType.LECTURE -> getBitmapFromDrawable(ContextCompat.getDrawable(context, R.drawable.ic_map_blue))
                EventType.PRACTICE -> getBitmapFromDrawable(ContextCompat.getDrawable(context, R.drawable.ic_map_green))
                EventType.SEMINAR -> getBitmapFromDrawable(ContextCompat.getDrawable(context, R.drawable.ic_map_purple))
                EventType.EXAM -> getBitmapFromDrawable(ContextCompat.getDrawable(context, R.drawable.ic_map_red))
            }!!
        }

        private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            val bitmap = Bitmap.createBitmap(
                drawable?.intrinsicWidth ?: 1,
                drawable?.intrinsicHeight ?: 1,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable?.setBounds(0, 0, canvas.width, canvas.height)
            drawable?.draw(canvas)
            return bitmap
        }
    }
}
