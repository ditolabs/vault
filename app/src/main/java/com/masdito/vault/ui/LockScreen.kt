package com.masdito.vault.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LockScreen(onUnlockSuccess: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF121212) else Color(0xFFFAFAFA)
    val textColor = if (isDark) Color(0xFFF5F5F5) else Color(0xFF1F2937)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = "Vault", tint = textColor, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Vault Offline", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
        Text("Masukkan PIN untuk membuka", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { 
                if (it.length <= 6) pin = it 
                errorMsg = ""
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.width(200.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = textColor,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Hardcoded PIN untuk Prototype (Sesuai dengan spesifikasi)
                if (pin == "123456") {
                    onUnlockSuccess()
                } else {
                    errorMsg = "PIN salah. Coba lagi."
                    pin = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = textColor, contentColor = bgColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.width(200.dp).height(50.dp)
        ) {
            Text("Buka Brankas", fontWeight = FontWeight.Bold)
        }

        if (errorMsg.isNotEmpty()) {
            Text(errorMsg, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp))
        }
    }
}
