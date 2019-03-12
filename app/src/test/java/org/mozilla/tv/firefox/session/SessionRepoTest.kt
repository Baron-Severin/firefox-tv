package org.mozilla.tv.firefox.session

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.session.SessionUseCases
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.tv.firefox.utils.TurboMode
import org.robolectric.RobolectricTestRunner

//private const val YOU_TUBE_URL = "https://www.youtube.com/tv#"
private const val NON_YOU_TUBE_URL = "https://www.mozilla.org"

@RunWith(RobolectricTestRunner::class)
class SessionRepoTest {

    private lateinit var repo: SessionRepo

    @MockK private lateinit var sessionManager: SessionManager
    @MockK private lateinit var sessionUseCases: SessionUseCases
    @MockK private lateinit var turboMode: TurboMode
    @MockK private lateinit var session: Session
    @MockK private lateinit var goBackUseCase: SessionUseCases.GoBackUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { sessionManager.selectedSession } answers { session }
        every { sessionUseCases.goBack } answers { goBackUseCase }
        every { session.fullScreenMode } answers { false }

        repo = SessionRepo(sessionManager, sessionUseCases, turboMode)
    }

    @Test
    fun `minimal reproducible example`() {
        every { session.url } answers { NON_YOU_TUBE_URL }
        every { session.canGoBack } answers { true }

        repo.attemptBack(true)
    }
    /*
    java.lang.NullPointerException
        at mozilla.components.feature.session.SessionUseCases$GoBackUseCase.invoke$default(SessionUseCases.kt:118)
        at org.mozilla.tv.firefox.session.SessionRepo.attemptBack(SessionRepo.kt:115)
        at org.mozilla.tv.firefox.session.SessionRepoTest.t(SessionRepoTest.kt:45)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
        at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
        at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
        at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
        at org.robolectric.RobolectricTestRunner$HelperTestRunner$1.evaluate(RobolectricTestRunner.java:600)
        at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:26)
        at org.robolectric.internal.SandboxTestRunner$2.evaluate(SandboxTestRunner.java:260)
        at org.robolectric.internal.SandboxTestRunner.runChild(SandboxTestRunner.java:130)
        at org.robolectric.internal.SandboxTestRunner.runChild(SandboxTestRunner.java:42)
        at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
        at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
        at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
        at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
        at org.robolectric.internal.SandboxTestRunner$1.evaluate(SandboxTestRunner.java:84)
        at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
        at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
        at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
        at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)
        at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)
        at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
    */
}
