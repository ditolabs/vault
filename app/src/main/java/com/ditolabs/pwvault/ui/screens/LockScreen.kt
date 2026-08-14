package com.ditolabs.pwvault.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ditolabs.pwvault.i18n.StringSet
import com.ditolabs.pwvault.ui.components.BrutalCard
import com.ditolabs.pwvault.ui.components.PwVaultIcons
import com.ditolabs.pwvault.ui.theme.LocalPwVaultColors
import com.ditolabs.pwvault.ui.theme.PwVaultTypography
import com.ditolabs.pwvault.ui.theme.SecretTextStyle

/**
 * Entry point to the app: master-key check. No "forgot password" affordance
 * exists on purpose (R-24 — no nav item should point at a flow that can't
 * exist for a local-only vault with no server-side reset).
 */
@Composable
fun LockScreen(
    strings: StringSet,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPwVaultColors.current
    var masterKey by remember { mutableStateOf("") }
    var revealed by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(strings.appName, style = PwVaultTypography.headlineLarge, color = colors.text)
        Spacer(modifier = Modifier.height(24.dp))

        BrutalCard(background = colors.raised, cornerRadius = 4.dp, borderWidth = 2.dp) {
            Text(
                strings.lockOfflineBadge,
                style = PwVaultTypography.labelSmall,
                color = colors.accent,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        BrutalCard(background = colors.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(PwVaultIcons.Lock, contentDescription = null, tint = colors.text, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.lockTitle, style = PwVaultTypography.titleLarge, color = colors.text)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(strings.masterKeyLabel, style = PwVaultTypography.labelLarge, color = colors.textDim)
                Spacer(modifier = Modifier.height(6.dp))

                BrutalCard(background = colors.raised, cornerRadius = 4.dp, borderWidth = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextField(
                            value = masterKey,
                            onValueChange = { masterKey = it },
                            singleLine = true,
                            textStyle = SecretTextStyle.copy(color = colors.text),
                            visualTransformation = if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = if (revealed) PwVaultIcons.VisibilityOff else PwVaultIcons.Visibility,
                            contentDescription = if (revealed) "Sembunyikan master key" else "Tampilkan master key",
                            tint = colors.textDim,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { revealed = !revealed },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                BrutalCard(background = colors.danger, cornerRadius = 4.dp, borderWidth = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(PwVaultIcons.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.lockWarning, style = PwVaultTypography.bodyMedium, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                BrutalCard(
                    background = colors.accent,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onUnlock,
                ) {
                    Text(
                        "${strings.unlockCta} →",
                        style = PwVaultTypography.titleMedium,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}
