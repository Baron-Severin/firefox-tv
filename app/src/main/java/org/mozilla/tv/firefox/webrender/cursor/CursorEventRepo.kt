/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tv.firefox.webrender.cursor

import io.reactivex.subjects.PublishSubject
import org.mozilla.tv.firefox.utils.Direction

/**
 * This class exposes low level cursor movement events.
 *
 * This is usually not the class you want to use. These are unprocessed, and for
 * most use cases you will want to use a more abstract class.
 */
class CursorEventRepo {

    sealed class CursorEvent {
        data class ScrolledToEdge(val edge: Direction) : CursorEvent()
        data class CursorMoved(val direction: Direction) : CursorEvent()
    }

    /**
     * Use this with care. This is a global bus. There is usually a better solution.
     */
    val scrollEvents = PublishSubject.create<CursorEvent>()
}
