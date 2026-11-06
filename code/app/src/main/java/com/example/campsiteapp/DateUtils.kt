package com.example.campsiteapp

import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * The date picker returns UTC midnight of the tapped day, so the
 * year/month/day are read in UTC and rebuilt in the local timezone.
 *
 * @param picked the date returned by the date picker
 * @return 00:00:00.000 local time of the picked day
 */
fun startOfPickedDay(picked: Date): Date {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = picked }
    return Calendar.getInstance().apply {
        clear()
        set(
            utc.get(Calendar.YEAR),
            utc.get(Calendar.MONTH),
            utc.get(Calendar.DAY_OF_MONTH),
            0, 0, 0
        )
    }.time
}

/**
 * @param picked the date returned by the date picker
 * @return 23:59:59.999 local time of the picked day
 */
fun endOfPickedDay(picked: Date): Date {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = picked }
    val nextMidnight = Calendar.getInstance().apply {
        clear()
        // Calendar is lenient, so day + 1 rolls into the next month if needed
        set(
            utc.get(Calendar.YEAR),
            utc.get(Calendar.MONTH),
            utc.get(Calendar.DAY_OF_MONTH) + 1,
            0, 0, 0
        )
    }
    return Date(nextMidnight.timeInMillis - 1)
}