package com.pindou.shop.ui

import java.util.Calendar

fun fmtTime(ts: Long): String {
    val c = Calendar.getInstance(); c.timeInMillis = ts
    return "%02d:%02d".format(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
}

fun fmtDate(ts: Long): String {
    val c = Calendar.getInstance(); c.timeInMillis = ts
    return "%d-%02d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}

fun fmtDur(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "%02d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
}

fun fmtDurHm(ms: Long): String {
    val m = ms / 60000
    val h = m / 60; val mm = m % 60
    return if (h > 0) "${h}小时${mm}分" else "${mm}分钟"
}

fun money(n: Double): String = "¥%.1f".format(n)

fun todayStart(): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

fun endTimeOf(timeStr: String): Long {
    val p = timeStr.split(":").map { it.toInt() }
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, p[0]); c.set(Calendar.MINUTE, p[1]); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
    if (c.timeInMillis <= System.currentTimeMillis()) c.add(Calendar.DAY_OF_MONTH, 1)
    return c.timeInMillis
}
