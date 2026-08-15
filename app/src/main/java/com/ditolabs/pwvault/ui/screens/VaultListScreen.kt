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
            ModalDrawerSheet {
                Text(s["categories"], style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                Categories.all.forEach { (id, labels) ->
                    val count = if (id == "semua") entries.size else entries.count { it.category == id }
                    NavigationDrawerItem(
                        label = { Text("${labels.first}  ($count)") },
                        selected = activeCategory == id,
                        onClick = { activeCategory = id; scope.launch { drawerState.close() } },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
                if (entries.any { it.password.isBlank() }) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(
                        label = { Text(s["clean_empty"]) },
                        selected = false,
                        icon = { Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { onCleanEmptyPasswords(); scope.launch { drawerState.close() } },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text(Categories.all.firstOrNull { it.first == activeCategory }?.second?.first ?: s["all"]) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Filled.Menu, null) }
                        },
                        actions = {
                            IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, null) }
                        }
                    )
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text(s["search_hint"]) },
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                    Text(
                        s["tap_edit_hold_copy"],
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddEntry) { Icon(Icons.Filled.Add, null) }
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
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
                        item { Spacer(Modifier.height(80.dp)) }
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
