/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.session

import android.net.Uri
import android.support.test.espresso.IdlingRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.focus.SkipOnboardingMainActivityTestRule
import org.mozilla.focus.helpers.MockServerHelper
import org.mozilla.focus.helpers.SessionLoadedIdlingResource
import org.mozilla.focus.robots.home

@RunWith(AndroidJUnit4::class)
class BackForwardTest {

    @Rule
    @JvmField
    val activityTestRule = SkipOnboardingMainActivityTestRule()

    private lateinit var loadingIdlingResource: SessionLoadedIdlingResource
    private lateinit var endpoints: List<Uri>

    @Before
    fun setup() {
        loadingIdlingResource = SessionLoadedIdlingResource()
        IdlingRegistry.getInstance().register(loadingIdlingResource)
        endpoints = MockServerHelper
                .initMockServerAndReturnEndpoints("This is Google", "This is YouTube")
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(loadingIdlingResource)
        activityTestRule.activity.finishAndRemoveTask()
    }

    @Test
    fun WHEN_user_navigates_by_typing_urls_THEN_back_and_forward_enable_and_disable_as_expected() {
        home {
            assertCannotGoBack()

            assertCannotGoForward()

            navigateToPage(endpoints[0])

            openMenu()

            assertCanGoBack()

            assertCannotGoForward()

            navigateToPage(endpoints[1])

            openMenu()

            assertCanGoBack()

            assertCannotGoForward()

            goBack()

            assertCanGoBack()

            assertCanGoForward()

            goBack()

            assertCannotGoBack()

            assertCanGoForward()
        }
    }

    // TODO doesn't pass because opening youtube adds tons of sites to the back history
    // Update this to substitute in fake data
    // See https://android.jlelse.eu/espresso-tests-from-0-to-1-e5c32c8a595
    @Test
    fun WHEN_user_navigates_using_pinned_tiles_THEN_back_and_forward_enable_and_disable_as_expected() {
        home {
            assertCannotGoBack()

            assertCannotGoForward()

            openTile(0)

            openMenu()

            assertCanGoBack()

            assertCannotGoForward()

            openTile(1)

            openMenu()

            assertCanGoBack()

            assertCannotGoForward()

            goBack()

            assertCanGoBack()

            assertCanGoForward()

            goBack()

            assertCannotGoBack()

            assertCanGoForward()
        }
    }
}
