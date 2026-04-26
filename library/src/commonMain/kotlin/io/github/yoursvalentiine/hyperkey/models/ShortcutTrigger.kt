package io.github.yoursvalentiine.hyperkey.models

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType

sealed interface ShortcutTrigger {

    data class Chord(
        val keys: Set<Key>,
        val modifiers: Set<KeyModifier>,
        val eventType: KeyEventType
    ) : ShortcutTrigger

    data class Sequence(
        val steps: List<Chord>
    ) : ShortcutTrigger
}