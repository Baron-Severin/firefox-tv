package org.mozilla.tv.firefox.pocket

import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import java.util.concurrent.TimeUnit

class RxTest {

    private lateinit var testScheduler: TestScheduler
    private lateinit var rx: Rx
    private val fakeEndpoint = object : PocketEndpoint("", null) {
        override suspend fun getRecommendedVideos() = null
    }

    @Before
    fun setup() {
        testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

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