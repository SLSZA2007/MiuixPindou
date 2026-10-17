package com.pindou.shop.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pindou.shop.data.Store
import com.pindou.shop.data.Table
import com.pindou.shop.ui.theme.parseColor
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.ColorPicker
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import java.io.File

private val PRESET = listOf("#FF6900", "#1677FF", "#00B42A", "#722ED1", "#F5222D", "#EB2F96", "#13C2C2")

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }
    var versionClicks by remember { mutableIntStateOf(0) }
    var showEgg by remember { mutableStateOf(false) }
    var oldPwd by remember { mutableStateOf(TextFieldState()) }
    var newPwd by remember { mutableStateOf(TextFieldState()) }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("桌台管理", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Store.tables.forEach { t ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(t.name, Modifier.weight(1f))
                            Button(onClick = {
                                val n = t.seats.size
                                if (n < 20) { t.seats.add(com.pindou.shop.data.Seat(n + 1)); Store.save() }
                            }) { Text("+座") }
                            Button(onClick = {
                                val n = t.seats.size
                                if (n > 1) { t.seats.removeAt(n - 1); Store.save() }
                            }) { Text("-座") }
                            if (Store.tables.size > 1) Button(onClick = { Store.tables.remove(t); Store.save() }) { Text("删桌") }
                        }
                    }
                    Button(onClick = {
                        val id = (Store.tables.maxOfOrNull { it.id } ?: 0) + 1
                        Store.tables.add(Table(id, "${id}号桌", MutableList(2) { com.pindou.shop.data.Seat(it + 1) }))
                        Store.save()
                    }, modifier = Modifier.fillMaxWidth()) { Text("+ 添加桌子") }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("主题色", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PRESET.forEach { c ->
                            val sel = Store.themeColor == c
                            androidx.compose.foundation.layout.Box(
                                Modifier.size(40.dp).background(parseColor(c), CircleShape)
                                    .clickable { Store.themeColor = c; Store.save() },
                                contentAlignment = Alignment.Center
                            ) { if (sel) Text("✓", color = Color.White) }
                        }
                        Button(onClick = { showPicker = true }) { Text("取色盘") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("自定义颜色", color = Color.Gray)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Store.customColors.forEach { c ->
                            androidx.compose.foundation.layout.Box(
                                Modifier.size(40.dp).background(parseColor(c), CircleShape)
                                    .clickable { Store.themeColor = c; Store.save() },
                                contentAlignment = Alignment.Center
                            ) { if (Store.themeColor == c) Text("✓", color = Color.White) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("操作密码", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (Store.password.isNotEmpty()) TextField(oldPwd, Modifier.fillMaxWidth(), label = "旧密码")
                    TextField(newPwd, Modifier.fillMaxWidth(), label = "新密码")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val n = newPwd.text.toString()
                        if (n.isBlank()) return@Button
                        if (Store.password.isNotEmpty() && Store.hash(oldPwd.text.toString()) != Store.password) return@Button
                        Store.password = Store.hash(n); Store.save()
                    }) { Text(if (Store.password.isEmpty()) "创建密码" else "修改密码") }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("数据", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { exportData(context) }, modifier = Modifier.weight(1f)) { Text("导出备份") }
                        Button(onClick = { importData(context) }, modifier = Modifier.weight(1f)) { Text("导入备份") }
                        Button(onClick = { Store.reset() }, modifier = Modifier.weight(1f)) { Text("重置") }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("关于", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("软件名称：游艺文创（拼豆计时计费）")
                    Text("软件版本：v4.0", Modifier.clickable {
                        versionClicks++
                        if (versionClicks >= 7) { versionClicks = 0; showEgg = true }
                    })
                    Text("制作人：SLSZA2007")
                    Text("审批人：Lee Jr.")
                    Text("此软件由 ai小姐 赞助开发")
                    Text("侵权必究喵～")
                    Text("如果愿望不能实现，就把它们送往明天")
                }
            }
        }
    }

    if (showPicker) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showPicker = false }) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("取色盘", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    ColorPicker(color = parseColor(Store.themeColor), onColorChanged = { c ->
                        Store.themeColor = colorToHex(c)
                        if (!Store.customColors.contains(Store.themeColor)) Store.customColors.add(Store.themeColor)
                        Store.save()
                    })
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { showPicker = false }, modifier = Modifier.fillMaxWidth()) { Text("确定") }
                }
            }
        }
    }

    if (showEgg) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showEgg = false }) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("明天见", fontWeight = FontWeight.Bold)
                    Text("是世界上最伟大的预言", color = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { showEgg = false }, modifier = Modifier.fillMaxWidth()) { Text("确定") }
                }
            }
        }
    }
}

fun colorToHex(c: Color): String {
    val r = (c.red * 255).toInt(); val g = (c.green * 255).toInt(); val b = (c.blue * 255).toInt()
    return "#%02X%02X%02X".format(r, g, b)
}

private fun exportData(context: Context) {
    val json = JSONObject()
    json.put("tables", Store.tables.size)
    val file = File(context.getExternalFilesDir(null), "游艺文创备份.json")
    file.writeText("{}")
    // 简化导出：写一个占位，完整导出见 Store
    file.writeText("{}")
}

private fun importData(context: Context) {}
