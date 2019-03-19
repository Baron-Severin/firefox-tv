/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tv.firefox.webrender

import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.mozilla.tv.firefox.R
import org.mozilla.tv.firefox.hint.Hint
import org.mozilla.tv.firefox.hint.HintViewModel
import org.mozilla.tv.firefox.navigationoverlay.OverlayHintViewModel
import org.mozilla.tv.firefox.session.SessionRepo
import org.mozilla.tv.firefox.utils.Direction
import org.mozilla.tv.firefox.utils.URLs
import org.mozilla.tv.firefox.webrender.cursor.CursorEventRepo

private val OPEN_MENU_HINT =  Hint(
        R.string.hint_press_menu_to_open_overlay,
        R.string.hardware_button_a11y_menu,
        R.drawable.hardware_remote_menu
)

/**
 * Contains business logic for, and exposes data to the hint bar.
 *
 * See comment on [OverlayHintViewModel] for why this is split into two classes.
 */
class WebRenderHintViewModel(
        sessionRepo: SessionRepo,
        cursorEventRepo: CursorEventRepo
) : ViewModel(), HintViewModel {

    override val isDisplayed: Observable<Boolean> by lazy {
        Observable.merge(cursorEvents, loadCompleteEvents)
                .observeOn(AndroidSchedulers.mainThread())
                .startWith(false)
                .replay(1)
                .autoConnect(0)
    }
    override val hints: Observable<List<Hint>> = Observable.just(listOf(OPEN_MENU_HINT))

    private val loadCompleteEvents = sessionRepo.state
            .filter { it.currentUrl != URLs.APP_URL_HOME }
            .map { it.loading }
            .distinctUntilChanged()
            .filter { loading -> !loading }
            .doOnNext { println("SEVTEST: loadComplete event") }
            .map { true } // TODO this is too complex. find a better way to do it

    private val cursorEvents: Observable<Boolean> = cursorEventRepo.scrollEvents
            .flatMap {
                fun handleEdge(scrolledToEdge: CursorEventRepo.CursorEvent.ScrolledToEdge) =
                        when (scrolledToEdge.edge) {
                            Direction.UP -> true
                            Direction.DOWN -> true
                            else -> null
                        }
                fun handleMovement(move: CursorEventRepo.CursorEvent.CursorMoved) =
                        when (move.direction) {
                            Direction.UP -> false
                            Direction.DOWN -> false
                            else -> null
                        }

                val shouldDisplay = when (it) {
                    is CursorEventRepo.CursorEvent.ScrolledToEdge -> handleEdge(it)
                    is CursorEventRepo.CursorEvent.CursorMoved -> handleMovement(it)
                }

                if (shouldDisplay != null) Observable.just(shouldDisplay)
                else Observable.empty()
            }
}
