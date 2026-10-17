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
import com.pindou.shop.data.DayPackage
import com.pindou.shop.data.HourlyPackage
import com.pindou.shop.data.PeriodPackage
import com.pindou.shop.data.Store
import com.pindou.shop.ui.money
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField

@Composable
fun PackagesScreen() {
    var hName by remember { mutableStateOf(TextFieldState()) }
    var hPrice by remember { mutableStateOf(TextFieldState()) }
    var pName by remember { mutableStateOf(TextFieldState()) }
    var pHours by remember { mutableStateOf(TextFieldState()) }
    var pEnd by remember { mutableStateOf(TextFieldState()) }
    var pPrice by remember { mutableStateOf(TextFieldState()) }
    var dName by remember { mutableStateOf(TextFieldState()) }
    var dPrice by remember { mutableStateOf(TextFieldState()) }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("单小时套餐（按小时计费）", fontWeight = FontWeight.Bold) }
        items(Store.hourlyPkgs.toList()) { p ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${p.name} · ${p.pricePerHour}元/小时", Modifier.weight(1f))
                Text(money(p.pricePerHour), color = Color(0xFFFF6900), fontWeight = FontWeight.Bold)
                Button(onClick = { Store.hourlyPkgs.remove(p); Store.save() }) { Text("删") }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(hName, Modifier.weight(1f), label = "名称")
                TextField(hPrice, Modifier.weight(1f), label = "元/小时")
                Button(onClick = {
                    val n = hName.text.toString(); val pr = hPrice.text.toString().toDoubleOrNull() ?: 0.0
                    if (n.isNotBlank() && pr > 0) { Store.hourlyPkgs.add(HourlyPackage("h${System.currentTimeMillis()}", n, pr)); Store.save() }
                }) { Text("添加") }
            }
        }

        item { Spacer(Modifier.height(16.dp)); Text("时段套餐（时长或固定时间段）", fontWeight = FontWeight.Bold) }
        items(Store.periodPkgs.toList()) { p ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                val d = if (p.endTime != null) "到 ${p.endTime} 结束" else "${p.hours} 小时"
                Text("${p.name} · $d", Modifier.weight(1f))
                Text(money(p.price), color = Color(0xFFFF6900), fontWeight = FontWeight.Bold)
                Button(onClick = { Store.periodPkgs.remove(p); Store.save() }) { Text("删") }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(pName, Modifier.weight(1f), label = "名称")
                TextField(pHours, Modifier.weight(1f), label = "时长(时)")
                TextField(pEnd, Modifier.weight(1f), label = "结束时间")
                TextField(pPrice, Modifier.weight(1f), label = "价格")
                Button(onClick = {
                    val n = pName.text.toString(); val pr = pPrice.text.toString().toDoubleOrNull() ?: 0.0
                    val h = pHours.text.toString().toDoubleOrNull()
                    val e = pEnd.text.toString()
                    if (n.isNotBlank() && pr > 0 && (h != null || e.isNotBlank())) {
                        Store.periodPkgs.add(PeriodPackage("p${System.currentTimeMillis()}", n, h, e.ifBlank { null }, pr)); Store.save()
                    }
                }) { Text("添加") }
            }
        }

        item { Spacer(Modifier.height(16.dp)); Text("整天套餐（一口价玩到打烊）", fontWeight = FontWeight.Bold) }
        items(Store.dayPkgs.toList()) { p ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${p.name} · 到打烊", Modifier.weight(1f))
                Text(money(p.price), color = Color(0xFFFF6900), fontWeight = FontWeight.Bold)
                Button(onClick = { Store.dayPkgs.remove(p); Store.save() }) { Text("删") }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(dName, Modifier.weight(1f), label = "名称")
                TextField(dPrice, Modifier.weight(1f), label = "价格")
                Button(onClick = {
                    val n = dName.text.toString(); val pr = dPrice.text.toString().toDoubleOrNull() ?: 0.0
                    if (n.isNotBlank() && pr > 0) { Store.dayPkgs.add(DayPackage("d${System.currentTimeMillis()}", n, pr)); Store.save() }
                }) { Text("添加") }
            }
        }
    }
}
