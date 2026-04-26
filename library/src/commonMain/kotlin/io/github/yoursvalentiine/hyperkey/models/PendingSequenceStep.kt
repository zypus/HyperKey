package io.github.yoursvalentiine.hyperkey.models

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import kotlin.collections.plus

class PendingSequenceStep(
    private val prevSteps: List<ShortcutTrigger.Chord>,
    private val keys: Set<Key>,
    private val modifiers: Set<KeyModifier>
) {
    constructor(prevSteps: List<ShortcutTrigger.Chord>, key: Key, modifiers: Set<KeyModifier>)
            : this(prevSteps, setOf(key), modifiers)

    infix fun with(modifier: KeyModifier) =
        PendingSequenceStep(prevSteps, keys, setOf(modifier))

    infix fun with(modifiers: Set<KeyModifier>) =
        PendingSequenceStep(prevSteps, keys, modifiers)

    infix fun andThen(key: Key) = PendingSequenceStep(
        prevSteps = prevSteps + ShortcutTrigger.Chord(
            keys = keys,
            modifiers = modifiers,
            eventType = KeyEventType.Companion.KeyDown
        ),
        keys = setOf(key),
        modifiers = emptySet()
    )

    infix fun andThen(combo: KeyCombination) = PendingSequenceStep(
        prevSteps = prevSteps + ShortcutTrigger.Chord(
            keys = keys,
            modifiers = modifiers,
            eventType = KeyEventType.Companion.KeyDown
        ),
        keys = combo.keys,
        modifiers = emptySet()
    )

    internal fun buildSequence(eventType: KeyEventType): ShortcutTrigger.Sequence {
        val lastStep = ShortcutTrigger.Chord(keys, modifiers, eventType)
        return ShortcutTrigger.Sequence(prevSteps + lastStep)
    }
}