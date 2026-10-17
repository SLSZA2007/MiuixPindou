package com.pindou.shop.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

object Store {
    private lateinit var prefs: SharedPreferences

    val tables = mutableStateListOf<Table>()
    val hourlyPkgs = mutableStateListOf<HourlyPackage>()
    val periodPkgs = mutableStateListOf<PeriodPackage>()
    val dayPkgs = mutableStateListOf<DayPackage>()

    var overtimeRate by mutableStateOf(10.0)
    var halfHourRate by mutableStateOf(5.0)
    var closingTime by mutableStateOf("22:00")
    var password by mutableStateOf("")
    var themeColor by mutableStateOf("#FF6900")
    val customColors = mutableStateListOf<String>()

    val bills = mutableStateListOf<Bill>()
    val deposits = mutableStateListOf<Deposit>()
    val tasks = mutableStateListOf<TaskItem>()
    val openHistory = mutableStateListOf<OpenHistory>()

    var currentTab by mutableStateOf("tables")
    var billSubTab by mutableStateOf("bill")

    fun init(context: Context) {
        prefs = context.getSharedPreferences("pindou_shop_v4", Context.MODE_PRIVATE)
        load()
    }

    fun load() {
        val raw = prefs.getString("data", null) ?: run { reset(); return }
        try {
            val o = JSONObject(raw)
            tables.clear(); hourlyPkgs.clear(); periodPkgs.clear(); dayPkgs.clear()
            bills.clear(); deposits.clear(); tasks.clear(); openHistory.clear(); customColors.clear()

            val ta = o.optJSONArray("tables") ?: JSONArray()
            for (i in 0 until ta.length()) tables.add(tableFromJson(ta.getJSONObject(i)))
            if (tables.isEmpty()) resetTables()

            val ha = o.optJSONArray("hourly") ?: JSONArray()
            for (i in 0 until ha.length()) { val x = ha.getJSONObject(i); hourlyPkgs.add(HourlyPackage(x.optString("id"), x.optString("name"), x.optDouble("pricePerHour"))) }
            if (hourlyPkgs.isEmpty()) hourlyPkgs.add(HourlyPackage("h1", "单小时", 10.0))

            val pa = o.optJSONArray("period") ?: JSONArray()
            for (i in 0 until pa.length()) { val x = pa.getJSONObject(i); periodPkgs.add(PeriodPackage(x.optString("id"), x.optString("name"), if (x.has("hours")) x.optDouble("hours") else null, if (x.has("endTime")) x.optString("endTime") else null, x.optDouble("price"))) }
            if (periodPkgs.isEmpty()) { periodPkgs.add(PeriodPackage("p1", "2小时", 2.0, null, 18.0)); periodPkgs.add(PeriodPackage("p2", "3小时", 3.0, null, 25.0)) }

            val da = o.optJSONArray("day") ?: JSONArray()
            for (i in 0 until da.length()) { val x = da.getJSONObject(i); dayPkgs.add(DayPackage(x.optString("id"), x.optString("name"), x.optDouble("price"))) }
            if (dayPkgs.isEmpty()) dayPkgs.add(DayPackage("d1", "整天", 50.0))

            overtimeRate = o.optDouble("overtimeRate", 10.0)
            halfHourRate = o.optDouble("halfHourRate", 5.0)
            closingTime = o.optString("closingTime", "22:00")
            password = o.optString("password", "")
            themeColor = o.optString("themeColor", "#FF6900")
            val ca = o.optJSONArray("customColors") ?: JSONArray()
            for (i in 0 until ca.length()) customColors.add(ca.optString(i))

            val ba = o.optJSONArray("bills") ?: JSONArray()
            for (i in 0 until ba.length()) { val x = ba.getJSONObject(i); bills.add(Bill(x.optString("id"), x.optString("type"), x.optDouble("amount"), x.optString("note"), x.optString("customer"), x.optLong("time"), x.optBoolean("struck"), if (x.has("duration")) x.optLong("duration") else null)) }
            val de = o.optJSONArray("deposits") ?: JSONArray()
            for (i in 0 until de.length()) { val x = de.getJSONObject(i); deposits.add(Deposit(x.optString("id"), x.optString("name"), x.optString("phone"), x.optString("cabinet"), x.optLong("time"), x.optBoolean("done"))) }
            val tk = o.optJSONArray("tasks") ?: JSONArray()
            for (i in 0 until tk.length()) { val x = tk.getJSONObject(i); tasks.add(TaskItem(x.optString("id"), x.optString("text"), x.optBoolean("done"), x.optLong("time"))) }
            val oh = o.optJSONArray("openHistory") ?: JSONArray()
            for (i in 0 until oh.length()) { val x = oh.getJSONObject(i); openHistory.add(OpenHistory(x.optString("id"), x.optLong("time"), x.optString("table"), x.optString("seat"), x.optString("customer"), x.optString("pkg"), x.optBoolean("manual"))) }
        } catch (e: Exception) {
            reset()
        }
    }

    fun save() {
        val o = JSONObject()
        val ta = JSONArray(); tables.forEach { ta.put(tableToJson(it)) }; o.put("tables", ta)
        val ha = JSONArray(); hourlyPkgs.forEach { ha.put(JSONObject().put("id", it.id).put("name", it.name).put("pricePerHour", it.pricePerHour)) }; o.put("hourly", ha)
        val pa = JSONArray(); periodPkgs.forEach { val j = JSONObject().put("id", it.id).put("name", it.name).put("price", it.price); it.hours?.let { h -> j.put("hours", h) }; it.endTime?.let { e -> j.put("endTime", e) }; pa.put(j) }; o.put("period", pa)
        val da = JSONArray(); dayPkgs.forEach { da.put(JSONObject().put("id", it.id).put("name", it.name).put("price", it.price)) }; o.put("day", da)
        o.put("overtimeRate", overtimeRate); o.put("halfHourRate", halfHourRate); o.put("closingTime", closingTime)
        o.put("password", password); o.put("themeColor", themeColor)
        val ca = JSONArray(); customColors.forEach { ca.put(it) }; o.put("customColors", ca)
        val ba = JSONArray(); bills.forEach { ba.put(JSONObject().put("id", it.id).put("type", it.type).put("amount", it.amount).put("note", it.note).put("customer", it.customer).put("time", it.time).put("struck", it.struck).put("duration", it.duration ?: JSONObject.NULL)) }; o.put("bills", ba)
        val de = JSONArray(); deposits.forEach { de.put(JSONObject().put("id", it.id).put("name", it.name).put("phone", it.phone).put("cabinet", it.cabinet).put("time", it.time).put("done", it.done)) }; o.put("deposits", de)
        val tk = JSONArray(); tasks.forEach { tk.put(JSONObject().put("id", it.id).put("text", it.text).put("done", it.done).put("time", it.time)) }; o.put("tasks", tk)
        val oh = JSONArray(); openHistory.forEach { oh.put(JSONObject().put("id", it.id).put("time", it.time).put("table", it.table).put("seat", it.seat).put("customer", it.customer).put("pkg", it.pkg).put("manual", it.manual)) }; o.put("openHistory", oh)
        prefs.edit().putString("data", o.toString()).apply()
    }

    fun reset() {
        tables.clear(); hourlyPkgs.clear(); periodPkgs.clear(); dayPkgs.clear()
        bills.clear(); deposits.clear(); tasks.clear(); openHistory.clear(); customColors.clear()
        overtimeRate = 10.0; halfHourRate = 5.0; closingTime = "22:00"; password = ""; themeColor = "#FF6900"
        resetTables()
        hourlyPkgs.add(HourlyPackage("h1", "单小时", 10.0))
        periodPkgs.add(PeriodPackage("p1", "2小时", 2.0, null, 18.0))
        periodPkgs.add(PeriodPackage("p2", "3小时", 3.0, null, 25.0))
        dayPkgs.add(DayPackage("d1", "整天", 50.0))
        save()
    }

    private fun resetTables() {
        for (i in 1..6) {
            tables.add(Table(i, "${i}号桌", MutableList(4) { Seat(it + 1) }))
        }
    }

    private fun tableToJson(t: Table): JSONObject {
        val j = JSONObject().put("id", t.id).put("name", t.name)
        val sa = JSONArray()
        t.seats.forEach { s ->
            val sj = JSONObject().put("no", s.no)
            s.session?.let { sj.put("session", it.toJson()) }
            sa.put(sj)
        }
        j.put("seats", sa)
        return j
    }

    private fun tableFromJson(o: JSONObject): Table {
        val id = o.optInt("id")
        val name = o.optString("name", "${id}号桌")
        val sa = o.optJSONArray("seats") ?: JSONArray()
        val seats = mutableListOf<Seat>()
        for (i in 0 until sa.length()) {
            val sj = sa.getJSONObject(i)
            val seat = Seat(sj.optInt("no", i + 1))
            if (sj.has("session")) seat.session = Session.fromJson(sj.getJSONObject("session"))
            seats.add(seat)
        }
        return Table(id, name, seats)
    }

    fun usedSeats() = tables.sumOf { t -> t.seats.count { it.session != null } }
    fun totalSeats() = tables.sumOf { t -> t.seats.size }

    fun hash(s: String): String {
        var h = 5381
        for (c in s) h = ((h shl 5) + h + c.code) and 0x7fffffff
        return "p" + h.toString(36)
    }

    fun todayOpenCount(): Int {
        val t0 = todayStart()
        return openHistory.count { it.time >= t0 }
    }

    fun todayStart(): Long {
        val c = java.util.Calendar.getInstance()
        c.set(java.util.Calendar.HOUR_OF_DAY, 0); c.set(java.util.Calendar.MINUTE, 0)
        c.set(java.util.Calendar.SECOND, 0); c.set(java.util.Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}
