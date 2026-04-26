package io.github.yoursvalentiine.hyperkey.scope

import androidx.compose.ui.input.key.Key
import io.github.yoursvalentiine.hyperkey.models.KeyCombination
import io.github.yoursvalentiine.hyperkey.models.PendingSequenceStep
import io.github.yoursvalentiine.hyperkey.models.ShortcutTrigger

class SequenceBuilder(
    internal val steps: List<ShortcutTrigger.Chord>
) {

    infix fun then(key: Key) = PendingSequenceStep(steps, key, emptySet())

    infix fun then(combo: KeyCombination) = PendingSequenceStep(steps, combo.keys, emptySet())
}