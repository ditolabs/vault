package com.ditolabs.pwvault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ditolabs.pwvault.data.Categories
import com.ditolabs.pwvault.data.Entry
import com.ditolabs.pwvault.i18n.LocalStrings
import com.ditolabs.pwvault.ui.components.BrutalCard
import com.ditolabs.pwvault.ui.components.BrutalIconSlot
import com.ditolabs.pwvault.ui.components.BrutalTextField
import com.ditolabs.pwvault.ui.components.BrutalTopBar
import com.ditolabs.pwvault.ui.components.EmptyState
import com.ditolabs.pwvault.ui.theme.CategoryColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultListScreen(
    entries: List<Entry>,
    onOpenEntry: (Entry) -> Unit,
    onAddEntry: () -> Unit,
    onOpenSettings: () -> Unit,
    onCopyPassword: (Entry) -> Unit,
    onCleanEmptyPasswords: () -> Unit,
) {
    val s = LocalStrings.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var activeCategory by remember { mutableStateOf("semua") }
    var query by remember { mutableStateOf("") }
    var copiedToast by remember { mutableStateOf<String?>(null) }

    val filtered = entries.filter { e ->
        (activeCategory == "semua" || e.category == activeCategory) &&
            (query.isBlank() || e.title.contains(query, true) || e.username.contains(query, true))
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            val colors = MaterialTheme.colorScheme
            Column(
                Modifier
                    .fillMaxHeight()
                    .width(260.dp)
                    .background(colors.tertiary)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp).background(colors.tertiary),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("VAULT.", fontWeight = FontWeight.Black, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                    BrutalIconSlot(onClick = { scope.launch { drawerState.close() } }, background = colors.surface) {
                        Icon(Icons.Filled.Menu, null, modifier = Modifier.fillMaxSize())
                    }
                }
                Box(Modifier.fillMaxWidth().height(4.dp).background(colors.outline))
                Column(Modifier.fillMaxWidth().padding(14.dp).weight(1f)) {
                    Text(s["categories"].uppercase(), fontWeight = FontWeight.Black, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Categories.all.forEach { (id, labels) ->
                        val count = if (id == "semua") entries.size else entries.count { it.category == id }
                        val active = activeCategory == id
                        BrutalCard(
                            onClick = { activeCategory = id; scope.launch { drawerState.close() } },
                            background = if (active) colors.outline else colors.surface,
                            cornerRadius = 4.dp,
                            borderWidth = 2.dp,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        ) {
                            Text(
                                "${labels.first}  ($count)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (active) colors.background else colors.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            )
                        }
                    }
                    BrutalCard(
                        onClick = { onOpenSettings(); scope.launch { drawerState.close() } },
                        cornerRadius = 4.dp, borderWidth = 2.dp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Settings, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(s["settings"], fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
                if (entries.any { it.password.isBlank() }) {
                    BrutalCard(
                        onClick = { onCleanEmptyPasswords(); scope.launch { drawerState.close() } },
                        background = colors.error, cornerRadius = 0.dp, borderWidth = 0.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, null, tint = colors.onError, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(s["clean_empty"], fontWeight = FontWeight.Black, fontSize = 12.sp, color = colors.onError)
                        }
                    }
                }
            }
        },
    ) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).navigationBarsPadding()) {
            BrutalTopBar(
                title = Categories.all.firstOrNull { it.first == activeCategory }?.second?.first ?: s["all"],
                leading = {
                    BrutalIconSlot(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Filled.Menu, null, modifier = Modifier.fillMaxSize())
                    }
                },
                trailing = {
                    BrutalIconSlot(onClick = onAddEntry, background = MaterialTheme.colorScheme.surface) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.fillMaxSize())
                    }
                },
            )
            Column(Modifier.padding(horizontal = 16.dp)) {
                BrutalTextField(
                    label = s["search_hint"],
                    value = query,
                    onValueChange = { query = it },
                    trailing = { Icon(Icons.Filled.Search, null, modifier = Modifier.size(18.dp)) },
                )
                Text(
                    s["tap_edit_hold_copy"],
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                )
            }
            Box(Modifier.fillMaxSize()) {
                if (filtered.isEmpty()) {
                    EmptyState(
                        icon = { Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.onSecondary) },
                        title = s["empty_vault"],
                        body = s["empty_vault_body"],
                        ctaLabel = if (activeCategory == "semua") "+ ${s["add_entry"]}" else null,
                        onCta = if (activeCategory == "semua") onAddEntry else null,
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        items(filtered, key = { it.id }) { entry ->
                            EntryRow(
                                entry = entry,
                                onTap = { onOpenEntry(entry) },
                                onLongPressComplete = {
                                    onCopyPassword(entry)
                                    copiedToast = entry.title
                                },
                            )
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }

                copiedToast?.let { title ->
                    LaunchedEffect(title) { delay(1600); copiedToast = null }
                    BrutalCard(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp),
                        cornerRadius = 4.dp,
                        borderWidth = 2.dp,
                        background = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("${s["password_label"]} '$title' ${s["copied"]}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: Entry, onTap: () -> Unit, onLongPressComplete: () -> Unit) {
    var holding by remember { mutableStateOf(false) }
    val lineColor = MaterialTheme.colorScheme.outline

    LaunchedEffect(holding) {
        if (holding) {
            delay(3000)
            if (holding) { onLongPressComplete(); holding = false }
        }
    }

    // Manual offset-shadow (not BrutalCard) so the row keeps its own
    // detectTapGestures — BrutalCard's built-in clickable would fight the
    // custom tap/long-press-to-copy gesture below.
    Box(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Box(
            Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 3.dp)
                .background(lineColor, RoundedCornerShape(12.dp)),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .border(2.5.dp, lineColor, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .pointerInput(entry.id) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onPress = {
                            holding = true
                            tryAwaitRelease()
                            holding = false
                        },
                    )
                }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(38.dp)
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (entry.password.isBlank()) {
                    Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                } else {
                    Text(
                        entry.title.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(),
                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(entry.username, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val catColor = CategoryColors[entry.category] ?: MaterialTheme.colorScheme.surfaceVariant
            Text(
                entry.category.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = androidx.compose.ui.graphics.Color.Black,
                modifier = Modifier
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .background(catColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
            )
        }
    }
}
