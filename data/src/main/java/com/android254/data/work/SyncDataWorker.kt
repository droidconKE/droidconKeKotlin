package com.android254.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android254.data.di.IoDispatcher
import com.android254.data.repos.local.LocalSessionsDataSource
import com.android254.data.repos.local.LocalSpeakersDataSource
import com.android254.data.repos.local.LocalSponsorsDataSource
import com.android254.data.repos.remote.RemoteSessionsDataSource
import com.android254.data.repos.remote.RemoteSpeakersDataSource
import com.android254.data.repos.remote.RemoteSponsorsDataSource
import com.android254.domain.models.ResourceResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext


@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted val workerParameters: WorkerParameters,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,

    private val remoteSessionsDataSource: RemoteSessionsDataSource,
    private val remoteSponsorsDataSource: RemoteSponsorsDataSource,
    private val remoteSpeakersDataSource: RemoteSpeakersDataSource,
    private val localSessionsDataSource: LocalSessionsDataSource,
    private val localSpeakersDataSource: LocalSpeakersDataSource,
    private val localSponsorsDataSource: LocalSponsorsDataSource,

    ) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        withContext(ioDispatcher){
            val sessionSyncDefferred = async { syncSessions() }
            val speakersDeffered = async { syncSpeakers() }
            val sponsorsDeffered = async{ syncSponsors() }

            sessionSyncDefferred.await()
            speakersDeffered.await()
            sponsorsDeffered.await()

        }


        return Result.success()
    }


    private suspend fun syncSessions() {
        withContext(ioDispatcher) {
            val response = remoteSessionsDataSource.getAllSessionsRemote()
            when (response) {
                is ResourceResult.Success -> {
                    localSessionsDataSource.deleteCachedSessions()
                    localSessionsDataSource.saveCachedSessions(
                        sessions = response.data ?: emptyList()
                    )
                    println("Sync sessions successful")

                }

                is ResourceResult.Error -> {
                    println("Sync sessions failed ${response.message}")

                }

                else -> {}
            }
        }

    }

    private suspend fun syncSponsors() {
        withContext(ioDispatcher) {
            val response = remoteSponsorsDataSource.getAllSponsorsRemote()
            when (response) {
                is ResourceResult.Success -> {
                    localSponsorsDataSource.deleteCachedSponsors()
                    localSponsorsDataSource.saveCachedSponsors(
                        sponsors = response.data ?: emptyList()
                    )
                    println("Sync sponsors successful")

                }

                is ResourceResult.Error -> {
                    println("Sync sponsors failed ${response.message}")

                }

                else -> {}
            }

        }
    }

    private suspend fun syncSpeakers() {
        withContext(ioDispatcher) {
            val response = remoteSpeakersDataSource.getAllSpeakersRemote()
            when (response) {
                is ResourceResult.Success -> {
                    localSpeakersDataSource.deleteAllCachedSpeakers()
                    localSpeakersDataSource.saveCachedSpeakers(
                        speakers = response.data ?: emptyList()
                    )

                    println("Sync speakers successful")

                }

                is ResourceResult.Error -> {
                    println("Sync speakers failed ${response.message}")

                }

                else -> {}
            }
        }
    }
}