package com.pindou.shop.ui.screens

import androidx.compose.foundation.clickable
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
import com.pindou.shop.data.Store
import com.pindou.shop.data.TaskItem
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField

@Composable
fun TasksScreen() {
    var text by remember { mutableStateOf(TextFieldState()) }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("新增事务", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    TextField(text, Modifier.fillMaxWidth(), label = "事务内容")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val t = text.text.toString()
                        if (t.isNotBlank()) { Store.tasks.add(0, TaskItem("t${System.currentTimeMillis()}", t, false, System.currentTimeMillis())); Store.save() }
                    }) { Text("添加") }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        val active = Store.tasks.filter { !it.done }
        val done = Store.tasks.filter { it.done }
        item { Text("待办（${active.size}）", fontWeight = FontWeight.Bold) }
        items(active) { t -> TaskItemRow(t) }
        item { Spacer(Modifier.height(12.dp)); Text("已完成（${done.size}）", fontWeight = FontWeight.Bold) }
        items(done) { t -> TaskItemRow(t) }
    }
}

@Composable
fun TaskItemRow(t: TaskItem) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        val bg = if (t.done) Color(0xFFFF6900) else Color.Transparent
        Card(Modifier.clickable { t.done = !t.done; Store.save() }) {
            Text(if (t.done) "✓" else "○", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = if (t.done) Color.White else Color.Gray)
        }
        Text(t.text, Modifier.weight(1f).padding(horizontal = 10.dp), color = if (t.done) Color.Gray else Color.Black)
        Button(onClick = { Store.tasks.remove(t); Store.save() }) { Text("删除") }
    }
}
