package io.github.yoursvalentiine.hyperkey.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import io.github.yoursvalentiine.hyperkey.scope.ShortcutScope
import io.github.yoursvalentiine.hyperkey.utils.ShortcutMatcher

@Composable
fun rememberShortcutModifier(shortcuts: ShortcutScope.() -> Unit): Modifier {
    val currentShortcuts by rememberUpdatedState(shortcuts)
    val matcher = remember { ShortcutMatcher() }

    return remember(matcher) {
        Modifier
            .onPreviewKeyEvent { event ->
                val scope = ShortcutScope().apply(currentShortcuts)
                matcher.handle(event, scope.shortcuts.filter { it.preview })
            }
            .onKeyEvent { event ->
                val scope = ShortcutScope().apply(currentShortcuts)
                matcher.handle(event, scope.shortcuts.filterNot { it.preview })
            }
    }
}

@Composable
fun ShortcutBox(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    shortcuts: ShortcutScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier.then(
            Modifier
                .focusRequester(focusRequester)
                .focusable()
                .onShortcut(rememberShortcutModifier(shortcuts))
        )
    ) {
        content()
    }
}