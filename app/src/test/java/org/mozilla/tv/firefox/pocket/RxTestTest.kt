package org.mozilla.tv.firefox.pocket

import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class RxTestTest {

    private lateinit var testScheduler: TestScheduler
    private val rxTest = RxTest()

    @Before
    fun setup() {
        testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
    }


    @Test
    fun t() {
        val testNormal = rxTest.normalTimer().test()

        testScheduler.advanceTimeBy(40, TimeUnit.MINUTES)

        println(testNormal.events.first())

        testScheduler.advanceTimeBy(10, TimeUnit.MINUTES)

        println(testNormal.events.first())

    }

    @Test
    fun tt() {
        val testBackoff = rxTest.backoffTimer().test()

        println(testBackoff.events.first())

        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        println(testBackoff.events.first())

        testScheduler.advanceTimeBy(100, TimeUnit.SECONDS)

        println(testBackoff.events.first())

        testScheduler.advanceTimeTo(45, TimeUnit.MINUTES)

        println(testBackoff.events.first())
    }


}