package com.pindou.shop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pindou.shop.data.Bill
import com.pindou.shop.data.Store
import com.pindou.shop.ui.fmtDate
import com.pindou.shop.ui.fmtDurHm
import com.pindou.shop.ui.fmtTime
import com.pindou.shop.ui.money
import com.pindou.shop.ui.todayStart
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import java.util.Calendar

@Composable
fun BillsScreen() {
    var sub by remember { mutableStateOf("bill") }
    var amount by remember { mutableStateOf(TextFieldState()) }
    var customer by remember { mutableStateOf(TextFieldState()) }
    var note by remember { mutableStateOf(TextFieldState()) }

    val activeBills = Store.bills.filter { !it.struck }
    val now = Calendar.getInstance()
    val dayStart = todayStart()
    val monthStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val total = activeBills.filter { it.type == "income" }.sumOf { it.amount }
    val day = activeBills.filter { it.type == "income" && it.time >= dayStart }.sumOf { it.amount }
    val month = activeBills.filter { it.type == "income" && it.time >= monthStart }.sumOf { it.amount }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { sub = "bill" }, modifier = Modifier.weight(1f)) { Text("账单") }
                Button(onClick = { sub = "history" }, modifier = Modifier.weight(1f)) { Text("开台历史") }
            }
            Spacer(Modifier.height(12.dp))
        }
        if (sub == "bill") {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBox2(money(total), "总营收", Modifier.weight(1f))
                    StatBox2(money(day), "今日营收", Modifier.weight(1f))
                    StatBox2(money(month), "本月营收", Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("手动记账", fontWeight = FontWeight.Bold)
                        TextField(amount, Modifier.fillMaxWidth(), label = "金额")
                        TextField(customer, Modifier.fillMaxWidth(), label = "顾客")
                        TextField(note, Modifier.fillMaxWidth(), label = "备注")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            val a = amount.text.toString().toDoubleOrNull() ?: 0.0
                            if (a > 0) {
                                Store.bills.add(0, Bill("b${System.currentTimeMillis()}", "income", a, note.text.toString().ifBlank { "手动账单" }, customer.text.toString(), System.currentTimeMillis(), false, null))
                                Store.save()
                            }
                        }) { Text("添加") }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            items(Store.bills.toList()) { b -> BillItem(b) }
        } else {
            items(Store.openHistory.toList()) { h ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("${h.table} ${h.seat}座${if (h.manual) "" else " · ${h.customer}"}", fontWeight = FontWeight.Bold)
                        Text("${h.pkg} · ${fmtDate(h.time)} ${fmtTime(h.time)}", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox2(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, color = Color(0xFFFF6900))
            Text(label, color = Color.Gray)
        }
    }
}

@Composable
fun BillItem(b: Bill) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(b.note, color = if (b.struck) Color.Gray else Color.Black)
                Text("${b.customer} · ${if (b.type == "income") "收入" else "支出"} · ${fmtDate(b.time)} ${fmtTime(b.time)}${b.duration?.let { " · 游玩 " + fmtDurHm(it) } ?: ""}", color = if (b.struck) Color.LightGray else Color.Gray)
            }
            Text("${if (b.type == "income") "+" else "-"}${money(b.amount)}", color = if (b.struck) Color.Gray else if (b.type == "income") Color(0xFF16A34A) else Color(0xFFE02020), fontWeight = FontWeight.Bold)
            Button(onClick = { b.struck = !b.struck; Store.save() }) { Text(if (b.struck) "恢复" else "划去") }
            Button(onClick = { Store.bills.remove(b); Store.save() }) { Text("删除") }
        }
    }
}
