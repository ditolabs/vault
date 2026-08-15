package com.ditolabs.pwvault.autofill

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillContext
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.ditolabs.pwvault.MainActivity
import com.ditolabs.pwvault.data.Entry
import com.ditolabs.pwvault.data.VaultSession

/**
 * Real Android Autofill Framework integration (API 26+), not an Accessibility
 * Service hack. Android calls onFillRequest() when the user focuses a
 * username/password field in ANY other app; this class never draws its own UI —
 * the suggestion dropdown itself is rendered by the system.
 *
 * KNOWN LIMITATION: matching a vault entry to the app/site being filled uses a
 * simple substring match against the requesting package name (no public-suffix
 * / web-domain verification like Chrome does). Good enough to be useful, not a
 * guarantee against a lookalike package name.
 */
@RequiresApi(Build.VERSION_CODES.O)
class PwVaultAutofillService : AutofillService() {

    override fun onFillRequest(request: FillRequest, cancellationSignal: CancellationSignal, callback: FillCallback) {
        val structure = request.fillContexts.lastOrNull()?.structure
        if (structure == null) { callback.onSuccess(null); return }

        val fields = findAutofillFields(structure)
        if (fields.usernameId == null && fields.passwordId == null) { callback.onSuccess(null); return }

        val responseBuilder = FillResponse.Builder()

        if (!VaultSession.isUnlocked) {
            // Vault is locked: offer one dataset that just launches PwVault to unlock,
            // then Android re-runs the fill request once the user comes back.
            val authIntent = Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_AUTOFILL_UNLOCK, true)
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0, authIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
                setTextViewText(android.R.id.text1, "Buka PwVault untuk isi otomatis")
            }
            val datasetBuilder = Dataset.Builder()
            fields.usernameId?.let { datasetBuilder.setValue(it, null, presentation) }
            fields.passwordId?.let { datasetBuilder.setValue(it, null, presentation) }
            datasetBuilder.setAuthentication(pendingIntent.intentSender)
            responseBuilder.addDataset(datasetBuilder.build())
            callback.onSuccess(responseBuilder.build())
            return
        }

        val packageName = structure.activityComponent?.packageName ?: request.fillContexts.lastOrNull()?.structure?.activityComponent?.packageName ?: ""
        val candidates = VaultSession.currentEntries().filter { entry ->
            matchesPackage(entry, packageName)
        }.ifEmpty { VaultSession.currentEntries() } // fall back to showing everything rather than nothing

        if (candidates.isEmpty()) { callback.onSuccess(null); return }

        for (entry in candidates.take(5)) {
            val presentation = RemoteViews(this.packageName, android.R.layout.simple_list_item_2).apply {
                setTextViewText(android.R.id.text1, entry.title)
                setTextViewText(android.R.id.text2, entry.username)
            }
            val datasetBuilder = Dataset.Builder()
            fields.usernameId?.let { datasetBuilder.setValue(it, AutofillValue.forText(entry.username), presentation) }
            fields.passwordId?.let { datasetBuilder.setValue(it, AutofillValue.forText(entry.password), presentation) }
            responseBuilder.addDataset(datasetBuilder.build())
        }

        callback.onSuccess(responseBuilder.build())
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        // Deliberately not implemented yet: auto-capturing new credentials from other
        // apps needs its own confirmation UI so we don't silently save typos/junk.
        // Adding entries stays a manual, explicit action inside PwVault for now.
        callback.onFailure("Simpan otomatis belum didukung — tambahkan entry langsung di PwVault")
    }

    private fun matchesPackage(entry: Entry, packageName: String): Boolean {
        if (packageName.isBlank()) return false
        val needle = packageName.substringAfterLast(".").lowercase()
        return entry.title.lowercase().contains(needle) ||
            entry.url.lowercase().contains(needle) ||
            packageName.lowercase().contains(entry.title.lowercase())
    }

    private data class AutofillFields(val usernameId: AutofillId?, val passwordId: AutofillId?)

    private fun findAutofillFields(structure: AssistStructure): AutofillFields {
        var usernameId: AutofillId? = null
        var passwordId: AutofillId? = null

        fun visit(node: AssistStructure.ViewNode) {
            val hints = node.autofillHints?.toList().orEmpty()
            val isPasswordByHint = hints.any { it == android.view.View.AUTOFILL_HINT_PASSWORD }
            val isUsernameByHint = hints.any {
                it == android.view.View.AUTOFILL_HINT_USERNAME || it == android.view.View.AUTOFILL_HINT_EMAIL_ADDRESS
            }
            val inputType = node.inputType
            val isPasswordByType = (inputType and android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0 ||
                (inputType and android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD) != 0 ||
                (inputType and android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) != 0

            if ((isPasswordByHint || isPasswordByType) && passwordId == null) passwordId = node.autofillId
            else if (isUsernameByHint && usernameId == null) usernameId = node.autofillId
            else if (usernameId == null && node.className?.contains("EditText") == true && !isPasswordByType) {
                // Loose fallback: first non-password text field on the screen.
                usernameId = node.autofillId
            }

            for (i in 0 until node.childCount) visit(node.getChildAt(i))
        }

        for (i in 0 until structure.windowNodeCount) {
            visit(structure.getWindowNodeAt(i).rootViewNode)
        }
        return AutofillFields(usernameId, passwordId)
    }
}
