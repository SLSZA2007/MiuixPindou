package com.pindou.shop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pindou.shop.data.HourlyPackage
import com.pindou.shop.data.PeriodPackage
import com.pindou.shop.data.Session
import com.pindou.shop.data.Store
import com.pindou.shop.data.Table
import com.pindou.shop.service.ReminderService
import com.pindou.shop.ui.endTimeOf
import com.pindou.shop.ui.fmtDur
import com.pindou.shop.ui.fmtTime
import com.pindou.shop.ui.money
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text

fun fmtCD(ms: Long): String {
    val neg = ms < 0
    val s = (kotlin.math.abs(ms) / 1000)
    val h = s / 3600; val m = (s % 3600) / 60; val sec = s % 60
    val body = if (h > 0) "%d:%02d".format(h, m) else "%d:%02d".format(m, sec)
    return (if (neg) "-" else "") + body
}

@Composable
fun TablesScreen() {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(1000); tick++ } }

    var openSeat by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var detailSeat by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val used = Store.usedSeats()
    val total = Store.totalSeats()

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBox(Store.todayOpenCount().toString(), "今日开台", Modifier.weight(1f))
                StatBox(total.toString(), "总座位", Modifier.weight(1f))
                StatBox(used.toString(), "使用中", Modifier.weight(1f))
                StatBox((total - used).toString(), "剩余座位", Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { ReminderService.stopAlarm() }, modifier = Modifier.fillMaxWidth()) {
                Text("强制停止所有响铃")
            }
            Spacer(Modifier.height(12.dp))
        }
        items(Store.tables.toList()) { table ->
            TableCard(table, tick) { no -> openSeat = table.id to no }
        }
    }

    openSeat?.let { (tid, no) ->
        OpenSeatDialog(tid, no, onDismiss = { openSeat = null })
    }
    detailSeat?.let { (tid, no) ->
        val table = Store.tables.find { it.id == tid } ?: return@let
        val seat = table.seats.find { it.no == no } ?: return@let
        seat.session?.let { sess ->
            DetailDialog(table, no, sess, onDismiss = { detailSeat = null })
        }
    }
}

@Composable
fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold)
            Text(label, color = Color.Gray)
        }
    }
}

@Composable
fun TableCard(table: Table, tick: Int, onOpen: (Int) -> Unit) {
    Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(table.name, fontWeight = FontWeight.Bold)
                Text("${table.seats.count { it.session != null }}/${table.seats.size} 使用中", color = Color.Gray)
            }
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                table.seats.forEach { seat ->
                    val sess = seat.session
                    if (sess == null) {
                        Box(
                            Modifier.width(96.dp).height(72.dp)
                                .background(Color(0xFFE8F7EE), RoundedCornerShape(12.dp))
                                .clickable { onOpen(seat.no) },
                            contentAlignment = Alignment.Center
                        ) { Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${table.id}-${seat.no}座", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            Text("空", color = Color(0xFF16A34A))
                        } }
                    } else {
                        val overdue = System.currentTimeMillis() > sess.end
                        val bg = if (overdue) Color(0xFFE02020) else Color(0xFFFFE9E9)
                        val fg = if (overdue) Color.White else Color(0xFFE02020)
                        Box(
                            Modifier.width(96.dp).height(72.dp).background(bg, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) { Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${table.id}-${seat.no}座", color = fg)
                            Text(sess.customer, fontWeight = FontWeight.Bold, color = fg)
                            Text(fmtCD(sess.end - System.currentTimeMillis()), color = fg)
                        } }
                    }
                }
            }
        }
    }
}

@Composable
fun OpenSeatDialog(tid: Int, no: Int, onDismiss: () -> Unit) {
    val table = Store.tables.find { it.id == tid } ?: return
    var step by remember { mutableStateOf("type") } // type -> hourly/period/day/custom
    var customer by remember { mutableStateOf(TextFieldState()) }
    var selPkgId by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf(TextFieldState()) }
    var customMin by remember { mutableStateOf(TextFieldState()) }
    var customPrice by remember { mutableStateOf(TextFieldState()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("开台 · ${table.name} ${tid}-${no}座", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                if (step == "type") {
                    val types = listOf("hourly" to "单小时套餐", "period" to "时段套餐", "day" to "整天套餐", "custom" to "自定义")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        types.forEach { (t, label) ->
                            Button(onClick = { step = t }, modifier = Modifier.width(140.dp)) { Text(label) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("取消") }
                } else {
                    Text("顾客（姓氏或手机尾号）", color = Color.Gray)
                    Spacer(Modifier.height(6.dp))
                    androidx.compose.material3.TextField(value = customer.text.toString(), onValueChange = {}, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))

                    when (step) {
                        "hourly" -> {
                            Store.hourlyPkgs.forEach { p ->
                                val sel = p.id == selPkgId
                                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selPkgId = p.id }) {
                                    Text("${p.name} · ${p.pricePerHour}元/小时", Modifier.padding(12.dp))
                                }
                            }
                        }
                        "period" -> {
                            Store.periodPkgs.forEach { p ->
                                val sel = p.id == selPkgId
                                val desc = if (p.endTime != null) "到 ${p.endTime} 结束" else "${p.hours} 小时"
                                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selPkgId = p.id }) {
                                    Text("${p.name} · ${p.price}元", Modifier.padding(12.dp))
                                    Text(desc, color = Color.Gray, modifier = Modifier.padding(start = 12.dp, bottom = 8.dp))
                                }
                            }
                        }
                        "day" -> {
                            Store.dayPkgs.forEach { p ->
                                val sel = p.id == selPkgId
                                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selPkgId = p.id }) {
                                    Text("${p.name} · ${p.price}元 · 到打烊 ${Store.closingTime}", Modifier.padding(12.dp))
                                }
                            }
                        }
                        "custom" -> {
                            androidx.compose.material3.TextField(value = customMin.text.toString(), onValueChange = {}, placeholder = { Text("游玩时长（分钟）") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            androidx.compose.material3.TextField(value = customPrice.text.toString(), onValueChange = {}, placeholder = { Text("金额（元）") }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { step = "type" }, modifier = Modifier.weight(1f)) { Text("返回") }
                        Button(onClick = {
                            val cust = customer.text.toString().ifBlank { "客" }
                            val start = System.currentTimeMillis()
                            var end = 0L; var price = 0.0; var name = ""
                            var ok = true
                            when (step) {
                                "hourly" -> { val p = Store.hourlyPkgs.find { it.id == selPkgId }; if (p == null) ok = false else { val h = (hours.text.toString().toDoubleOrNull() ?: 1.0).coerceAtLeast(1.0); end = start + (h*3600000).toLong(); price = p.pricePerHour*h; name = "${p.name}×${h.toInt()}h" } }
                                "period" -> { val p = Store.periodPkgs.find { it.id == selPkgId }; if (p == null) ok = false else { price = p.price; name = p.name; end = if (p.endTime != null) endTimeOf(p.endTime) else start + (p.hours!!*3600000).toLong() } }
                                "day" -> { val p = Store.dayPkgs.find { it.id == selPkgId }; if (p == null) ok = false else { price = p.price; name = p.name; end = endTimeOf(Store.closingTime) } }
                                "custom" -> { val m = customMin.text.toString().toIntOrNull() ?: 0; val pr = customPrice.text.toString().toDoubleOrNull() ?: 0.0; if (m <= 0 || pr <= 0) ok = false else { end = start + (m*60000L); price = pr; name = "自定义" } }
                            }
                            if (ok) {
                                table.seats.find { it.no == no }?.session = Session(cust, name, price, start, end)
                                Store.save()
                                onDismiss()
                            }
                        }, modifier = Modifier.weight(1f)) { Text("确认开台") }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailDialog(table: Table, no: Int, sess: Session, onDismiss: () -> Unit) {
    val remain = sess.end - System.currentTimeMillis()
    val overdue = remain < 0
    val overHours = if (overdue) kotlin.math.ceil(kotlin.math.abs(remain) / 3600000.0).toInt() else 0
    val overFee = overHours * Store.overtimeRate
    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("${table.name} ${table.id}-${no}座", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("顾客：${sess.customer}")
                Text("套餐：${sess.pkgName}")
                Text("开始：${fmtTime(sess.start)}")
                Text("应结束：${fmtTime(sess.end)}")
                Text("已用时长：${fmtDur(System.currentTimeMillis() - sess.start)}")
                Text("基础金额：${money(sess.price)}")
                if (overHours > 0) Text("超时 ${overHours}h：+${money(overFee)}", color = Color(0xFFE02020))
                Text("应收：${money(sess.price + overFee)}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { /* 加时 */ }, modifier = Modifier.weight(1f)) { Text("加时") }
                    Button(onClick = {
                        val total = sess.price + overFee
                        Store.bills.add(0, com.pindou.shop.data.Bill("b${System.currentTimeMillis()}", "income", total, "${table.name} ${table.id}-${no}座 · ${sess.pkgName}", sess.customer, System.currentTimeMillis(), false, System.currentTimeMillis() - sess.start))
                        table.seats.find { it.no == no }?.session = null
                        Store.save(); onDismiss()
                    }, modifier = Modifier.weight(1f)) { Text("结账") }
                }
            }
        }
    }
}
