/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tv.firefox.pocket

import android.support.annotation.VisibleForTesting
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.mozilla.tv.firefox.pocket.Rx.Internals.backoffRequests
import org.mozilla.tv.firefox.pocket.Rx.Internals.normalTimer
import org.mozilla.tv.firefox.pocket.Rx.Internals.singleRequest
import java.util.concurrent.TimeUnit
import kotlin.math.pow

private val CACHE_UPDATE_FREQUENCY_MINUTES = 45L
private val CACHE_UPDATE_FREQUENCY_SECONDS = TimeUnit.MINUTES.toSeconds(CACHE_UPDATE_FREQUENCY_MINUTES)

class Rx(val pocketEndpoint: PocketEndpoint) {

    /**
     * Makes one network request upon subscription. If this fails, more requests
     * will be made on an exponential backoff.
     *
     * Follow up requests will be made every 45 minutes, regardless of previous
     * success or failure.
     */
    fun merged(): Observable<List<PocketViewModel.FeedItem.Video>> = normalTimer()
        .flatMap {
            Observable.concat(singleRequest(pocketEndpoint).toObservable(), backoffRequests(pocketEndpoint))
                .filter { it is Internals.PocketResponse.Success }
                .map { (it as Internals.PocketResponse.Success).videos }
                .take(1)
        }

    /**
     * Contains data only available for testing.
     *
     * Before introducing this class, the constant [VisibleForTesting]
     * annotations made the file difficult to read.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    object Internals {

        /**
         * Generates an observable of [Long]s that together represent an
         * exponential backoff. Values summed will be smaller than
         * [CACHE_UPDATE_FREQUENCY_SECONDS]
         */
        fun backoffTimes(): Observable<Long> = Observable.range(1, Integer.MAX_VALUE)
            .map { 2.toDouble().pow(it).toLong() }
            .takeWhile { it < (CACHE_UPDATE_FREQUENCY_SECONDS / 2) }

        /**
         * Emits one value upon subscription, and another every 45 minutes
         */
        fun normalTimer(): Observable<Long> = Observable
            .interval(0, CACHE_UPDATE_FREQUENCY_SECONDS, TimeUnit.SECONDS)

        /**
         * Waits for the duration of each value in seconds, emits it, then
         * continues
         */
        fun backoffTimer(): Observable<Long> = backoffTimes()
            .concatMap {
                Observable.timer(it, TimeUnit.SECONDS)
            }

        /**
         * Makes a single network request and returns its contents
         */
        fun singleRequest(pocketEndpoint: PocketEndpoint): Single<PocketResponse> =
            pocketEndpoint.getRecommendedVideosAsObservable()

        /**
         * Makes a series of network requests, according to the schedule set by [backoffTimer]
         */
        fun backoffRequests(pocketEndpoint: PocketEndpoint): Observable<PocketResponse> = backoffTimer()
            .flatMapSingle { pocketEndpoint.getRecommendedVideosAsObservable() }

        /**
         * Classifies network responses as either [Success] or [Failure]
         *
         * See "Either" type from functional programming
         */
        sealed class PocketResponse {
            data class Success(val videos: List<PocketViewModel.FeedItem.Video>) : PocketResponse()
            object Failure : PocketResponse()
        }

        /**
         * Wraps a coroutine call so that values may be emitted as [Observable]s
         */
        private fun PocketEndpoint.getRecommendedVideosAsObservable(): Single<PocketResponse> {
            val endpoint = this
            fun wrapResponse(): PocketResponse {
                    val videos = runBlocking { endpoint.getRecommendedVideos() }
                    return when (videos) {
                        null -> PocketResponse.Failure
                        else -> PocketResponse.Success(videos)
                    }
            }
            return Single.fromCallable { wrapResponse() }
                .subscribeOn(Schedulers.io())
        }
    }
}
