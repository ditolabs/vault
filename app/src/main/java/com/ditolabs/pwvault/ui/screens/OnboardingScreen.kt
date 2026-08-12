package com.ditolabs.pwvault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ditolabs.pwvault.i18n.LocalStrings
import com.ditolabs.pwvault.ui.components.IconKey
import com.ditolabs.pwvault.ui.components.VaultLogo

/**
 * First-launch welcome screen. Borrows the visual warmth of the reference
 * image (illustration, friendly copy, generous spacing) but NOT its mechanism
 * — no phone number, no OTP, no Google/Apple sign-in. PwVault has no accounts
 * and no server to sign into; the "Mulai" button leads straight to creating a
 * local vault, which is the only "login" this app has.
 */
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val s = LocalStrings.current
    Column(
        Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(140.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center,
        ) {
            VaultLogo(modifier = Modifier.size(64.dp))
        }
        Spacer(Modifier.height(28.dp))
        Text(s["onboarding_title"], style = MaterialTheme.typography.headlineSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(
            s["onboarding_desc"],
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = onGetStarted,
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            IconKey(modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(s["onboarding_cta"])
        }
    }
}
