package com.masdito.vault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.masdito.vault.data.AppDatabase
import com.masdito.vault.data.Credential
import com.masdito.vault.ui.LockScreen
import com.masdito.vault.ui.VaultMainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inisialisasi Database
        val db = AppDatabase.getDatabase(this)
        
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VaultApp(db)
                }
            }
        }
    }
}

@Composable
fun VaultApp(db: AppDatabase) {
    // State Authentication
    var isAuthenticated by remember { mutableStateOf(false) }
    
    // Observers data dari Room
    val credentialsFlow = db.credentialDao().getAllCredentials()
    val credentialsList by credentialsFlow.collectAsState(initial = emptyList())

    if (!isAuthenticated) {
        LockScreen(
            onUnlockSuccess = { isAuthenticated = true }
        )
    } else {
        VaultMainScreen(
            credentials = credentialsList,
            onAddClick = {
                // TODO: Navigasi ke halaman form tambah data
            }
        )
    }
}
