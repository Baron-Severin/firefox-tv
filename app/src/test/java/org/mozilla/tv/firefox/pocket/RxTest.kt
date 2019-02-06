package org.mozilla.tv.firefox.pocket

import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class RxTest {

    private lateinit var testScheduler: TestScheduler
    private lateinit var rx: Rx

    private class FakeEndpoint : PocketEndpoint("", null) {
        var shouldSucceed = false
        var requestCount = 0
            private set

        override suspend fun getRecommendedVideos(): List<PocketViewModel.FeedItem.Video>? {
            requestCount++
            return when (shouldSucceed) {
                true -> PocketViewModel.noKeyPlaceholders
                false -> null
            }
        }
    }

    private val fakeEndpoint = FakeEndpoint()

    @Before
    fun setup() {
        testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }

        rx = Rx(fakeEndpoint)
    }









    @Test
    fun normalTimer() {
        val testNormal = rx.normalTimer().test()

        testScheduler.advanceTimeBy(40, TimeUnit.MINUTES)

        println(testNormal.events.first())

        testScheduler.advanceTimeBy(10, TimeUnit.MINUTES)

        println(testNormal.events.first())
    }


    @Test
    fun backoffTimer() {
        val testBackoff = rx.backoffTimer().test()

        println(testBackoff.events.first())

        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        println(testBackoff.events.first())

        testScheduler.advanceTimeBy(100, TimeUnit.SECONDS)

        println(testBackoff.events.first())

        testScheduler.advanceTimeTo(45, TimeUnit.MINUTES)

        println(testBackoff.events.first())
    }

    @Test
    fun normalCall() {
        val testNormal = rx.normalCall(fakeEndpoint).test()

        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        println(testNormal.events.first())

        testScheduler.advanceTimeTo(40, TimeUnit.MINUTES)

        println(testNormal.events.first())

        testScheduler.advanceTimeTo(45, TimeUnit.MINUTES)

        println(testNormal.events.first())
    }

    @Test
    fun backoffCall() {
        val testNormal = rx.backoffCall(fakeEndpoint).test()

        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        println(testNormal.events.first())

        testScheduler.advanceTimeTo(40, TimeUnit.MINUTES)

        println(testNormal.events.first())

        testScheduler.advanceTimeTo(45, TimeUnit.MINUTES)

        println(testNormal.events.first())
    }


    @Test
    fun merged() {
        runBlocking {
            val merged = rx.merged().test()

            println(merged.events.first())

            testScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS)

            println(merged.events.first())

            testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

            println(merged.events.first())

            testScheduler.advanceTimeBy(100, TimeUnit.SECONDS)

            println(merged.events.first())

            testScheduler.advanceTimeTo(45, TimeUnit.MINUTES)

            println(merged.events.first())
        }
    }

}