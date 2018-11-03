/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tv.firefox.helpers

import android.net.Uri
import okhttp3.mockwebserver.MockWebServer
import org.mozilla.tv.firefox.ext.toUri

object TestAssetHelper {

    data class TestAsset(val url: Uri, val urlStr: String, val content: String)

    fun getBasicAssets(server: MockWebServer, quantity: Int = 3): List<TestAsset> {
        val paths = (1..quantity)
            .map { "pages/basic_nav$it.html" }
            .map { server.url(it) }
            .map { it.toString() }
            .map { it.toUri()!! to it }
        val contentList = (1..quantity)
            .map { "Page content: $it" }
        return paths.zip(contentList) { path, content ->
            TestAsset(path.first, path.second, content)
        }
    }
}
