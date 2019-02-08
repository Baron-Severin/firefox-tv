package org.mozilla.tv.firefox.pocket

import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class RxTest {

    private lateinit var testScheduler: TestScheduler
    private lateinit var rx: Rx
    private lateinit var fakeEndpoint: FakeEndpoint

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

    @Before
    fun setup() {
        testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }

        fakeEndpoint = FakeEndpoint()
        rx = Rx(fakeEndpoint)
    }

    @Test
    fun `WHEN backoffTimes emits it SHOULD include all powers of 2 that add up to be smaller than 45 * 60`() {
        // 45 minutes * 60 seconds == 2700
        // 2 + 4 + 8 + 16 + 32 + 64 + 128 + 256 + 512 + 1024 == 2046
        val expected = listOf<Long>(2, 4, 8, 16, 32, 64, 128, 256, 512, 1024)
        val actual = Rx.Internals.backoffTimes().toList().blockingGet().toList()

        assertEquals(expected, actual)
    }

    @Test
    fun `WHEN normal timer is started THEN it should immediately emit`() {
        val testNormal = Rx.Internals.normalTimer().test()

        testScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS)
        assertEquals(1, testNormal.events.first().size)
    }

    @Test
    fun `GIVEN normal timer has been started WHEN 45 minutes pass THEN it should emit again`() {
        val testNormal = Rx.Internals.normalTimer().test()

        testScheduler.advanceTimeTo(44, TimeUnit.MINUTES)
        assertEquals(1, testNormal.events.first().size)

        testScheduler.advanceTimeTo(45, TimeUnit.MINUTES)
        assertEquals(2, testNormal.events.first().size)

        testScheduler.advanceTimeTo(89, TimeUnit.MINUTES)
        assertEquals(2, testNormal.events.first().size)

        testScheduler.advanceTimeTo(90, TimeUnit.MINUTES)
        assertEquals(3, testNormal.events.first().size)
    }

    @Test
    fun `WHEN backoff timer has started THEN it should emit after pauses set by backoffTimes`() {
        val testBackoff = Rx.Internals.backoffTimer().test()
        val backoffTimes = Rx.Internals.backoffTimes().toList().blockingGet().toList()

        var expectedEmissions = 0
        backoffTimes.forEach { wait ->
            testScheduler.advanceTimeBy(wait, TimeUnit.SECONDS)
            assertEquals(++expectedEmissions, testBackoff.events.first().size)
        }
    }

    @Test
    fun `GIVEN requests fail WHEN merged has started THEN requests should be made on both normal and backoff timers`() {
        rx.merged().subscribe()

        var expectedCalls = 1
        testScheduler.advanceTimeTo(1, TimeUnit.MILLISECONDS)
        assertEquals(expectedCalls, fakeEndpoint.requestCount)

        val expected = listOf<Long>(2, 4, 8, 16, 32, 64, 128, 256, 512, 1024)
        expected.forEach { wait ->
            testScheduler.advanceTimeBy(wait, TimeUnit.SECONDS)
            assertEquals(++expectedCalls, fakeEndpoint.requestCount)
        }

        testScheduler.advanceTimeTo(45, TimeUnit.MINUTES)
        assertEquals(++expectedCalls, fakeEndpoint.requestCount)

        expected.forEach { wait ->
            testScheduler.advanceTimeBy(wait, TimeUnit.SECONDS)
            assertEquals(++expectedCalls, fakeEndpoint.requestCount)
        }
    }

    @Test
    fun `GIVEN requests succeed WHEN merged has started THEN only one request should be made every 45 minutes`() {
        rx.merged().subscribe()
        fakeEndpoint.shouldSucceed = true

        testScheduler.advanceTimeTo(1, TimeUnit.MILLISECONDS)
        assertEquals(1, fakeEndpoint.requestCount)

        testScheduler.advanceTimeTo(10, TimeUnit.MINUTES)
        assertEquals(1, fakeEndpoint.requestCount)

        testScheduler.advanceTimeTo(45, TimeUnit.MINUTES)
        assertEquals(2, fakeEndpoint.requestCount)

        testScheduler.advanceTimeTo(50, TimeUnit.MINUTES)
        assertEquals(2, fakeEndpoint.requestCount)

        testScheduler.advanceTimeTo(90, TimeUnit.MINUTES)
        assertEquals(3, fakeEndpoint.requestCount)
    }

    @Test
    fun `GIVEN requests have been failing AND merged has started WHEN requests succeed THEN backoff requests should stop`() {
        rx.merged().subscribe()
        fakeEndpoint.shouldSucceed = false

        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS)
        assertEquals(3, fakeEndpoint.requestCount)

        fakeEndpoint.shouldSucceed = true

        testScheduler.advanceTimeBy(8, TimeUnit.SECONDS)
        assertEquals(4, fakeEndpoint.requestCount)

        testScheduler.advanceTimeBy(16, TimeUnit.SECONDS)
        assertEquals(4, fakeEndpoint.requestCount)

        testScheduler.advanceTimeTo(44, TimeUnit.SECONDS)
        assertEquals(4, fakeEndpoint.requestCount)
    }
}