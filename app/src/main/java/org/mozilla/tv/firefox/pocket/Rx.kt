/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tv.firefox.pocket

import io.reactivex.Observable
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import kotlin.math.pow

private val CACHE_UPDATE_FREQUENCY_MINUTES = 45L
private val CACHE_UPDATE_FREQUENCY_SECONDS = TimeUnit.MINUTES.toSeconds(CACHE_UPDATE_FREQUENCY_MINUTES)

class Rx(val pocketEndpoint: PocketEndpoint) {

    val backoffTimes = Observable.range(1, Integer.MAX_VALUE)
        .map { 2.toDouble().pow(it).toLong() }
        .takeWhile { it < (CACHE_UPDATE_FREQUENCY_SECONDS / 2) }

    fun normalTimer() = Observable.interval(0, CACHE_UPDATE_FREQUENCY_SECONDS, TimeUnit.SECONDS)
        .map { "NORMAL: $it" }
    fun backoffTimer() = backoffTimes
        .concatMap {
            Observable.timer(it, TimeUnit.SECONDS)
                .map { "BACKOFF: $it" }
        }

    fun normalCall(pocketEndpoint: PocketEndpoint) = pocketEndpoint.getVidsObs()
    fun backoffCall(pocketEndpoint: PocketEndpoint) = backoffTimer().flatMap { pocketEndpoint.getVidsObs() }

    fun merged() = normalTimer()
        .flatMap {
            Observable.concat(normalCall(pocketEndpoint), backoffCall(pocketEndpoint))
                .filter { it is PocketRequest.Success }
                .map { (it as PocketRequest.Success).videos }
                .take(1)
        }

    sealed class PocketRequest {
        data class Success(val videos: List<PocketViewModel.FeedItem.Video>) : PocketRequest()
        object Failure : PocketRequest() {
            override fun toString() = "Failed Request"
        }
    }

    private fun PocketEndpoint.getVidsObs(): Observable<PocketRequest> {
        val endpoint = this
        return Observable.create { observableEmitter ->
            runBlocking {
                val videos = async { endpoint.getRecommendedVideos() }.await()
                when (videos) {
                    null -> {
                        observableEmitter.onNext(PocketRequest.Failure)
                        observableEmitter.onComplete()
                    }
                    else -> {
                        observableEmitter.onNext(PocketRequest.Success(videos))
                        observableEmitter.onComplete()
                    }
                }
            }
        }
    }
}
