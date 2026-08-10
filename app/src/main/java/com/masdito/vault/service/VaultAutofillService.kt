package com.masdito.vault.service

import android.app.assist.AssistStructure
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest

class VaultAutofillService : AutofillService() {

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        // TODO: Traversing AssistStructure untuk mencari field 'username' & 'password'
        // Jika ditemukan, query ke AppDatabase berdasarkan nama package target, 
        // lalu kembalikan FillResponse.
        
        // Return kosong untuk prototype (mencegah crash)
        callback.onSuccess(FillResponse.Builder().build())
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        // TODO: Simpan kredensial baru saat OS mendeteksi login berhasil di aplikasi target
        callback.onSuccess()
    }
}
