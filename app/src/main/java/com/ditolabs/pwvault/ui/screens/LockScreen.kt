package com.ditolabs.pwvault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ditolabs.pwvault.data.AppLanguage
import com.ditolabs.pwvault.data.ThemeMode
import com.ditolabs.pwvault.i18n.LocalStrings
import com.ditolabs.pwvault.ui.components.IconBackspace
import com.ditolabs.pwvault.ui.components.IconFingerprint
import com.ditolabs.pwvault.ui.components.IconMoon
import com.ditolabs.pwvault.ui.components.IconShield
import com.ditolabs.pwvault.ui.components.IconSun
import com.ditolabs.pwvault.ui.components.BrutalCard
import com.ditolabs.pwvault.ui.components.VaultLogo

enum class LockTab { PIN, PASSWORD, BIOMETRIC }

@Composable
fun LockScreen(
    isCreatingVault: Boolean,
    pinEnabled: Boolean,
    biometricEnabled: Boolean,
    onUnlockPassword: (String) -> Boolean,
    onUnlockPin: (String) -> Boolean,
    onUnlockBiometric: () -> Unit,
    pinAttemptsLeft: Int,
    pinLockedUntil: Long,
    language: AppLanguage,
    themeMode: ThemeMode,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
) {
    val s = LocalStrings.current
    var tab by remember { mutableStateOf(if (pinEnabled) LockTab.PIN else LockTab.PASSWORD) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        // Quick language/theme toggles — reachable without going into Settings,
        // since both are things people flip once and forget, not tune repeatedly.
        Row(
            Modifier.align(Alignment.TopEnd).padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BrutalCard(
                onClick = onToggleLanguage, cornerRadius = 4.dp, borderWidth = 2.dp,
                background = androidx.compose.ui.graphics.Color.Transparent,
            ) {
                Text(
                    language.name, fontWeight = FontWeight.Bold, fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            BrutalCard(
                onClick = onToggleTheme, cornerRadius = 4.dp, borderWidth = 2.dp,
                background = androidx.compose.ui.graphics.Color.Transparent,
            ) {
                    Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                    val showSun = themeMode == ThemeMode.DARK
                    if (showSun) IconSun(modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurface)
                    else IconMoon(modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Spacer(Modifier.height(48.dp))
        VaultLogo(modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(10.dp))
        Text(s["app_name"], style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(20.dp))

        val tabs = buildList {
            if (pinEnabled) add(LockTab.PIN)
            add(LockTab.PASSWORD)
            if (biometricEnabled) add(LockTab.BIOMETRIC)
        }
        TabRow(selectedTabIndex = tabs.indexOf(tab).coerceAtLeast(0), containerColor = androidx.compose.ui.graphics.Color.Transparent) {
            tabs.forEach { t ->
                Tab(
                    selected = tab == t,
                    onClick = { tab = t; errorText = null },
                    text = { Text(when (t) { LockTab.PIN -> s["tab_pin"]; LockTab.PASSWORD -> s["tab_password"]; LockTab.BIOMETRIC -> s["tab_biometric"] }) }
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        when (tab) {
            LockTab.PASSWORD -> PasswordTab(isCreatingVault, errorText) { pw ->
                if (!onUnlockPassword(pw)) errorText = s["wrong_password"]
            }
            LockTab.PIN -> PinTab(pinAttemptsLeft, pinLockedUntil, errorText) { pin ->
                if (!onUnlockPin(pin)) errorText = s["wrong_pin"]
            }
            LockTab.BIOMETRIC -> BiometricTab(onUnlockBiometric)
        }

        Spacer(Modifier.weight(1f))
        BrutalCard(cornerRadius = 999.dp, borderWidth = 2.dp) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconShield(modifier = Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text(s["offline_mode"], fontSize = 10.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PasswordTab(isCreating: Boolean, error: String?, onSubmit: (String) -> Unit) {
    val s = LocalStrings.current
    var pw by remember { mutableStateOf("") }
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = pw,
            onValueChange = { pw = it },
            label = { Text(if (isCreating) s["new_master_password_hint"] else s["master_password_hint"]) },
            visualTransformation = PasswordVisualTransformation(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)) }
        Spacer(Modifier.height(14.dp))
        BrutalCard(
            onClick = { onSubmit(pw) },
            enabled = pw.isNotEmpty(),
            background = MaterialTheme.colorScheme.primary,
            cornerRadius = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (isCreating) s["create_vault"] else s["unlock_vault"],
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            )
        }
    }
}

@Composable
private fun PinTab(attemptsLeft: Int, lockedUntil: Long, error: String?, onSubmit: (String) -> Unit) {
    val s = LocalStrings.current
    var pin by remember { mutableStateOf("") }
    val isLocked = lockedUntil > System.currentTimeMillis()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(6) { i ->
                Box(
                    Modifier
                        .size(width = 36.dp, height = 44.dp)
                        .border(2.dp, if (error != null) MaterialTheme.colorScheme.error else if (i < pin.length) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    if (i < pin.length) Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.onSurface, CircleShape))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        when {
            isLocked -> Text(s["locked_try_again"], fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
            error != null -> Text("$error — $attemptsLeft ${s["attempts_left"]}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
            else -> Spacer(Modifier.height(16.dp))
        }
        Spacer(Modifier.height(8.dp))

        val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.width(260.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(keys) { k ->
                when (k) {
                    "" -> Spacer(Modifier.size(56.dp))
                    "⌫" -> Box(
                        Modifier.size(56.dp).clickable(enabled = !isLocked) { pin = pin.dropLast(1) },
                        contentAlignment = Alignment.Center,
                    ) { IconBackspace(tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
                    else -> Box(
                        Modifier
                            .size(56.dp)
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .clickable(enabled = !isLocked && pin.length < 6) { pin += k },
                        contentAlignment = Alignment.Center,
                    ) { Text(k, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        BrutalCard(
            onClick = { onSubmit(pin); pin = "" },
            enabled = pin.length == 6 && !isLocked,
            background = MaterialTheme.colorScheme.primary,
            cornerRadius = 4.dp,
            modifier = Modifier.width(260.dp),
        ) {
            Text(
                s["unlock_with_pin"],
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            )
        }
    }
}

@Composable
private fun BiometricTab(onTrigger: () -> Unit) {
    val s = LocalStrings.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
        Box(
            Modifier
                .size(84.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .clickable { onTrigger() },
            contentAlignment = Alignment.Center,
        ) { IconFingerprint(tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) }
        Spacer(Modifier.height(14.dp))
        Text(s["unlock_with_biometric"], fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
