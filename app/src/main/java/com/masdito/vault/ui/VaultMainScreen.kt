package com.masdito.vault.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masdito.vault.data.Credential

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultMainScreen(credentials: List<Credential>, onAddClick: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }
    var selectedCredential by remember { mutableStateOf<Credential?>(null) }

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF121212) else Color(0xFFFAFAFA)
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color(0xFFF5F5F5) else Color(0xFF1F2937)
    val borderColor = if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)

    val filteredData = credentials.filter { 
        (selectedCategory == "all" || it.category == selectedCategory) &&
        (it.title.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true))
    }

    Row(modifier = Modifier.fillMaxSize().background(bgColor)) {
        
        // SIDEBAR
        Column(
            modifier = Modifier.width(260.dp).fillMaxHeight().background(surfaceColor).border(1.dp, borderColor).padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Logo", tint = textColor)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Vault", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            Text("KATEGORI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            SidebarItem("Semua Sandi", Icons.Default.List, selectedCategory == "all") { selectedCategory = "all"; selectedCredential = null }
            SidebarItem("Sosial Media", Icons.Default.Person, selectedCategory == "sosmed") { selectedCategory = "sosmed"; selectedCredential = null }
            SidebarItem("Pekerjaan", Icons.Default.Build, selectedCategory == "kerja") { selectedCategory = "kerja"; selectedCredential = null }
        }

        // MAIN CONTENT
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight().padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari kredensial...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = surfaceColor,
                        unfocusedContainerColor = surfaceColor,
                        focusedBorderColor = textColor,
                        unfocusedBorderColor = borderColor
                    ),
                    modifier = Modifier.weight(1f).padding(end = 16.dp)
                )

                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = textColor, contentColor = bgColor),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Data Baru", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            if (filteredData.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = "Empty", tint = borderColor, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tidak ada data di kategori ini.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    items(filteredData) { item ->
                        CredentialCard(item, selectedCredential?.id == item.id, isDark) { selectedCredential = item }
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarItem(title: String, icon: ImageVector, isActive: Boolean, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val activeBg = if (isDark) Color(0xFF2D2D2D) else Color(0xFFF3F4F6)
    val activeText = if (isDark) Color.White else Color.Black
    val inactiveText = Color.Gray

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.background(if (isActive) activeBg else Color.Transparent, RoundedCornerShape(8.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = if (isActive) activeText else inactiveText, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = if (isActive) activeText else inactiveText, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun CredentialCard(item: Credential, isSelected: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)
    val activeBorder = if (isDark) Color.White else Color.Black
    
    Row(
        modifier = Modifier.fillMaxWidth().background(surfaceColor, RoundedCornerShape(12.dp))
            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) activeBorder else borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background(if (isDark) Color(0xFF2D2D2D) else Color(0xFFF3F4F6), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AccountBox, contentDescription = null, tint = if (isDark) Color.White else Color.Black)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isDark) Color.White else Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.username, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
