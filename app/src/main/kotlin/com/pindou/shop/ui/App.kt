package com.pindou.shop.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.pindou.shop.data.Store
import com.pindou.shop.ui.screens.BillsScreen
import com.pindou.shop.ui.screens.DepositsScreen
import com.pindou.shop.ui.screens.PackagesScreen
import com.pindou.shop.ui.screens.SettingsScreen
import com.pindou.shop.ui.screens.TablesScreen
import com.pindou.shop.ui.screens.TasksScreen
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class NavItem(val tab: String, val label: String, val icon: ImageVector)

val navItems = listOf(
    NavItem("tables", "桌台", Icons.Filled.TableRestaurant),
    NavItem("packages", "套餐", Icons.Filled.Inventory2),
    NavItem("bills", "账单", Icons.Filled.ReceiptLong),
    NavItem("deposits", "存豆", Icons.Filled.Inbox),
    NavItem("tasks", "事务", Icons.Filled.Checklist),
    NavItem("settings", "设置", Icons.Filled.Settings),
)

@Composable
fun App() {
    val orientation = LocalConfiguration.current.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        Row {
            NavigationRail(expanded = true, header = { Text("游艺文创") }) {
                navItems.forEach { item ->
                    NavigationRailItem(
                        selected = item.tab == Store.currentTab,
                        onClick = { Store.currentTab = item.tab },
                        icon = item.icon,
                        label = item.label
                    )
                }
            }
            Box(Modifier.weight(1f)) { Content() }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = "游艺文创",
                    color = MiuixTheme.colorScheme.surface,
                )
            },
            bottomBar = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .shadow(10.dp, RoundedCornerShape(30.dp))
                        .clip(RoundedCornerShape(30.dp))
                        .background(MiuixTheme.colorScheme.surfaceContainer)
                ) {
                    NavigationBar(
                        color = Color.Transparent,
                        showDivider = false,
                        defaultWindowInsetsPadding = false
                    ) {
                        navItems.forEach { item ->
                            NavigationBarItem(
                                selected = item.tab == Store.currentTab,
                                onClick = { Store.currentTab = item.tab },
                                icon = item.icon,
                                label = item.label
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) { Content() }
        }
    }
}

@Composable
fun Content() {
    when (Store.currentTab) {
        "tables" -> TablesScreen()
        "packages" -> PackagesScreen()
        "bills" -> BillsScreen()
        "deposits" -> DepositsScreen()
        "tasks" -> TasksScreen()
        "settings" -> SettingsScreen()
    }
}
