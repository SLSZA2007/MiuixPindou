package com.pindou.shop.ui.screens

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
import com.pindou.shop.data.Deposit
import com.pindou.shop.data.Store
import com.pindou.shop.ui.fmtDate
import com.pindou.shop.ui.fmtTime
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField

@Composable
fun DepositsScreen() {
    var name by remember { mutableStateOf(TextFieldState()) }
    var phone by remember { mutableStateOf(TextFieldState()) }
    var cabinet by remember { mutableStateOf(TextFieldState()) }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("新增存豆", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    TextField(name, Modifier.fillMaxWidth(), label = "姓名")
                    TextField(phone, Modifier.fillMaxWidth(), label = "电话")
                    TextField(cabinet, Modifier.fillMaxWidth(), label = "柜号")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val n = name.text.toString()
                        if (n.isNotBlank()) {
                            Store.deposits.add(0, Deposit("d${System.currentTimeMillis()}", n, phone.text.toString(), cabinet.text.toString(), System.currentTimeMillis(), false))
                            Store.save()
                        }
                    }) { Text("寄存") }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        val active = Store.deposits.filter { !it.done }
        val done = Store.deposits.filter { it.done }
        item { Text("寄存中（${active.size}）", fontWeight = FontWeight.Bold) }
        items(active) { d -> DepositItem(d) }
        item { Spacer(Modifier.height(12.dp)); Text("已取走（${done.size}）", fontWeight = FontWeight.Bold) }
        items(done) { d -> DepositItem(d) }
    }
}

@Composable
fun DepositItem(d: Deposit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(d.name, fontWeight = FontWeight.Bold)
                Text("📞 ${d.phone.ifBlank { "—" }} · 柜号 ${d.cabinet.ifBlank { "—" }} · ${fmtDate(d.time)} ${fmtTime(d.time)}", color = Color.Gray)
            }
            if (!d.done) Button(onClick = { d.done = true; Store.save() }) { Text("已取走") }
            else Button(onClick = { d.done = false; Store.save() }) { Text("恢复") }
            Button(onClick = { Store.deposits.remove(d); Store.save() }) { Text("删除") }
        }
    }
}
