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
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

fun interface TicketScanner {
    suspend fun scan(): ScanResult
}

/** Play services hosts the camera UI, so the app needs no camera permission; the UI is a module it may still have to fetch. */
class PlayServicesTicketScanner
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : TicketScanner {
        private val options =
            GmsBarcodeScannerOptions
                .Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .enableAutoZoom()
                .build()

        override suspend fun scan(): ScanResult {
            val scanner = GmsBarcodeScanning.getClient(context, options)
            return try {
                ScanResult.Code(
                    scanner
                        .startScan()
                        .await()
                        .rawValue
                        .orEmpty(),
                )
            } catch (e: CancellationException) {
                if (currentCoroutineContext().isActive) ScanResult.Cancelled else throw e
            } catch (e: MlKitException) {
                Timber.d("Code scanner failed with %d: %s", e.errorCode, e.message)
                val installer = ModuleInstall.getClient(context)
                if (installer.areModulesAvailable(scanner).await().areModulesAvailable()) {
                    ScanResult.Unavailable
                } else {
                    installer.installModules(ModuleInstallRequest.newBuilder().addApi(scanner).build())
                    ScanResult.Installing
                }
            }
        }
    }

@Module
@InstallIn(SingletonComponent::class)
abstract class TicketScannerModule {
    @Binds
    abstract fun bindTicketScanner(scanner: PlayServicesTicketScanner): TicketScanner
}