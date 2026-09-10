/*
 * Copyright 2023 DroidconKE
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
package com.android254.data.util

import com.android254.domain.sync.Synchronizer
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

/**
 * Attempts [block], returning a successful [Result] if it succeeds, otherwise a [Result.failure]
 * taking care not to break structured concurrency
 */
suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (exception: Exception) {
        Timber.tag("suspendRunCatching").i(exception, "Failed to evaluate a suspendRunCatchingBlock. Returning failure Result")
        Result.failure(exception)
    }

class SyncException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Utility function for syncing a repository with the network using reconciliation.
 * [remoteItemFetcher] Fetches the items from the remote source.
 * [localIdFetcher] Fetches the IDs of the items currently stored locally.
 * [localItemUpserter] Upserts the items into the local database.
 * [localItemDeleter] Deletes local items by their IDs.
 * [remoteToLocalIdSelector] Selects the ID from a remote item to match against local IDs.
 */
suspend fun <Remote, LocalId> Synchronizer.sync(
    remoteItemFetcher: suspend () -> List<Remote>,
    localIdFetcher: suspend () -> List<LocalId>,
    localItemUpserter: suspend (List<Remote>) -> Unit,
    localItemDeleter: suspend (List<LocalId>) -> Unit,
    remoteToLocalIdSelector: (Remote) -> LocalId,
): Result<Boolean> =
    suspendRunCatching {
        val remoteItems = remoteItemFetcher()
        val localIds = localIdFetcher()

        val remoteIds = remoteItems.map(remoteToLocalIdSelector).toSet()
        val orphanedIds = localIds.filterNot { it in remoteIds }

        if (orphanedIds.isNotEmpty()) {
            localItemDeleter(orphanedIds)
        }

        localItemUpserter(remoteItems)
        true
    }