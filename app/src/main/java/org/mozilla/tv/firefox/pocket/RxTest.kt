/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tv.firefox.pocket

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.math.pow

private val CACHE_UPDATE_FREQUENCY_MINUTES = 45L
private val CACHE_UPDATE_FREQUENCY_SECONDS = TimeUnit.MINUTES.toSeconds(CACHE_UPDATE_FREQUENCY_MINUTES)
private val CACHE_UPDATE_FREQUENCY_MILLIS = TimeUnit.MINUTES.toMillis(CACHE_UPDATE_FREQUENCY_MINUTES)

class RxTest {

    val backoffTimes = Observable.range(1, Integer.MAX_VALUE)
        .map { 2.toDouble().pow(it).toLong() }
        .takeUntil { it > CACHE_UPDATE_FREQUENCY_SECONDS }

    fun normalTimer() = Observable.interval(0, CACHE_UPDATE_FREQUENCY_SECONDS, TimeUnit.SECONDS)
    fun backoffTimer() = backoffTimes
        .concatMap { wait -> Observable.timer(wait, TimeUnit.SECONDS).map { wait } }


}
