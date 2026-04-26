package io.github.yoursvalentiine.hyperkey.models

import androidx.compose.ui.input.key.Key

class KeyCombination internal constructor(
    val keys: Set<Key>
) {
    operator fun plus(other: Key) =
        KeyCombination(keys + other)
}

operator fun Key.plus(other: Key) =
    KeyCombination(setOf(this, other))