package com.pindou.shop.data

import org.json.JSONArray
import org.json.JSONObject

data class Session(
    val customer: String,
    val pkgName: String,
    val price: Double,
    val start: Long,
    val end: Long,
    var alertedPre: Boolean = false,
    var alertedEnd: Boolean = false
) {
    fun toJson() = JSONObject().apply {
        put("customer", customer); put("pkgName", pkgName); put("price", price)
        put("start", start); put("end", end)
        put("alertedPre", alertedPre); put("alertedEnd", alertedEnd)
    }
    companion object {
        fun fromJson(o: JSONObject) = Session(
            o.optString("customer", "客"), o.optString("pkgName", ""), o.optDouble("price", 0.0),
            o.optLong("start"), o.optLong("end"), o.optBoolean("alertedPre"), o.optBoolean("alertedEnd")
        )
    }
}

data class Seat(val no: Int, var session: Session? = null)

data class Table(val id: Int, val name: String, val seats: MutableList<Seat>)

data class HourlyPackage(val id: String, val name: String, val pricePerHour: Double)
data class PeriodPackage(val id: String, val name: String, val hours: Double? = null, val endTime: String? = null, val price: Double)
data class DayPackage(val id: String, val name: String, val price: Double)

data class Bill(
    val id: String, val type: String, val amount: Double, val note: String,
    val customer: String, val time: Long, var struck: Boolean, val duration: Long?
)

data class Deposit(
    val id: String, val name: String, val phone: String, val cabinet: String,
    val time: Long, var done: Boolean
)

data class TaskItem(val id: String, val text: String, var done: Boolean, val time: Long)

data class OpenHistory(
    val id: String, val time: Long, val table: String, val seat: String,
    val customer: String, val pkg: String, val manual: Boolean
)
