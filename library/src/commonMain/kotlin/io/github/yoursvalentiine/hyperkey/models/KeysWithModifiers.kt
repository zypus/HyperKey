package io.github.yoursvalentiine.hyperkey.models

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType

class KeysWithModifiers(
    val keys: Set<Key>,
    val modifiers: Set<KeyModifier>
) {
    /**
     * Continues a shortcut sequence by appending a chord containing a single key.
     *
     * This infix function finalizes the current chord (using [KeyEventType.KeyDown]),
     * wraps it as the first step of a sequence, and prepares to accept the next chord
     * which consists of the specified key with no modifiers initially.
     *
     * **Usage example:**
     * ```kotlin
     * // Simple two-step sequence: Ctrl+S then Q
     * Key.S with ctrl andThen Key.Q
     * ```
     *
     * **Result behavior:**
     * - The current chord (`keys` + `modifiers`) becomes the first step of the sequence
     * - The returned [PendingSequenceStep] starts with the specified `key` as the next pending chord
     * - The next chord can be further modified using `with` or extended with more `andThen` calls
     *
     * **Event type:** The current chord uses [KeyEventType.KeyDown] as its trigger.
     * Different event types can be specified when finalizing the entire sequence
     * (e.g., `.onKeyPressed()` at the end).
     *
     * **Chain example:**
     * ```kotlin
     * // Three-step sequence: Ctrl+X, then Ctrl+S, then Q
     * Key.X with ctrl andThen Key.S with ctrl andThen Key.Q press {}
     * ```
     *
     * @param key The key that will start the next chord in the sequence
     * @return A [PendingSequenceStep] representing the next pending chord,
     *         with the current chord already stored in `prevSteps`
     *
     * @see andThen KeyCombination
     * @see PendingSequenceStep.andThen
     * @see PendingSequenceStep.buildSequence
     */
    infix fun andThen(key: Key) = PendingSequenceStep(
        prevSteps = listOf(ShortcutTrigger.Chord(keys, modifiers, KeyEventType.KeyDown)),
        keys = setOf(key),
        modifiers = emptySet()
    )

    /**
     * Continues a shortcut sequence by appending a chord from a key combination.
     *
     * This infix function finalizes the current chord (using [KeyEventType.KeyDown]),
     * wraps it as the first step of a sequence, and prepares to accept the next chord
     * which uses the keys from the provided [KeyCombination].
     *
     * **Usage example:**
     * ```kotlin
     * // Two-step sequence: Ctrl+X then A+B (simultaneous)
     * Key.X with ctrl andThen (Key.A + Key.B)
     *
     * // Three-step sequence: F, then Ctrl+S, then Ctrl+Shift+T
     * Key.F andThen Key.S with ctrl andThen Key.T with ctrl + shift
     * ```
     *
     * **Important notes:**
     * - Only the `keys` from the [KeyCombination] are used for the next chord
     * - The `modifiers` from the combination are **ignored** in this overload
     * - Modifiers for the next chord should be added via subsequent [with] calls on the
     *   returned [PendingSequenceStep] if needed
     *
     * **Why are modifiers ignored?**
     * This design choice maintains consistency with the single-key overload and allows
     * modifiers to be added incrementally using the `with` DSL:
     * ```kotlin
     * // Instead of this (not supported):
     * (ctrl + Key.X) andThen (ctrl + Key.S)
     *
     * // Do this:
     * Key.X with ctrl andThen Key.S with ctrl
     * ```
     *
     * **Complete example with modifiers:**
     * ```kotlin
     * // Ctrl+X then Ctrl+S
     * Key.X with ctrl andThen Key.S with ctrl press {}
     *
     * // Alt+Shift+A then Ctrl+Alt+T
     * Key.A with alt + shift andThen Key.T with ctrl + alt press {}
     * ```
     *
     * @param combo The [KeyCombination] whose keys will form the next chord
     * @return A [PendingSequenceStep] representing the next pending chord,
     *         with the current chord already stored in `prevSteps`
     *
     * @see andThen Key
     * @see PendingSequenceStep.andThen
     * @see PendingSequenceStep.with
     */
    infix fun andThen(combo: KeyCombination) = PendingSequenceStep(
        prevSteps = listOf(ShortcutTrigger.Chord(keys, modifiers, KeyEventType.KeyDown)),
        keys = combo.keys,
        modifiers = emptySet()
    )
}