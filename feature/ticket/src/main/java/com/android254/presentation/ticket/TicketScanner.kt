/*
 * Copyright 2026 DroidconKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android254.presentation.ticket

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

fun interface TicketScanner {
    suspend fun scan(): ScanResult
}

/** Play services hosts the camera UI, so the app needs no camera permission. */
class PlayServicesTicketScanner
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : TicketScanner {
        override suspend fun scan(): ScanResult =
            suspendCancellableCoroutine { continuation ->
                val options =
                    GmsBarcodeScannerOptions
                        .Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .enableAutoZoom()
                        .build()
                GmsBarcodeScanning
                    .getClient(context, options)
                    .startScan()
                    .addOnSuccessListener { barcode -> continuation.resume(ScanResult.Code(barcode.rawValue.orEmpty())) }
                    .addOnCanceledListener { continuation.resume(ScanResult.Cancelled) }
                    .addOnFailureListener { continuation.resume(ScanResult.Unavailable) }
            }
    }

@Module
@InstallIn(SingletonComponent::class)
abstract class TicketScannerModule {
    @Binds
    abstract fun bindTicketScanner(scanner: PlayServicesTicketScanner): TicketScanner
}