package com.pindou.shop.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.pindou.shop.data.Store
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun PindouTheme(content: @Composable () -> Unit) {
    val keyColor = parseColor(Store.themeColor)
    val controller = remember(keyColor) {
        ThemeController(ColorSchemeMode.MonetSystem, keyColor = keyColor)
    }
    MiuixTheme(controller = controller, content = content)
}

fun parseColor(hex: String): Color = try {
    Color(("FF" + hex.removePrefix("#").trim()).toLong(16))
} catch (e: Exception) {
    Color(0xFFFF6900)
}
