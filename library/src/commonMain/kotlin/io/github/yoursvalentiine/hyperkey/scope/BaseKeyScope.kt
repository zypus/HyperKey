package io.github.yoursvalentiine.hyperkey.scope

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import io.github.yoursvalentiine.hyperkey.annotations.ShortcutDsl
import io.github.yoursvalentiine.hyperkey.models.*

@ShortcutDsl
abstract class BaseKeyScope(
    val shortcuts: MutableList<KeyShortcut>
) {
    protected fun register(trigger: ShortcutTrigger, preview: Boolean = false, action: () -> Unit) {
        shortcuts += KeyShortcut(trigger, preview, action)
    }

    open infix fun Key.with(modifier: KeyModifier): KeysWithModifiers =
        KeysWithModifiers(keys = setOf(this), modifiers = setOf(modifier))

    open infix fun Key.with(modifiers: Set<KeyModifier>): KeysWithModifiers =
        KeysWithModifiers(keys = setOf(this), modifiers = modifiers)

    open infix fun KeyCombination.with(modifier: KeyModifier) =
        KeysWithModifiers(keys = keys, modifiers = setOf(modifier))

    open infix fun KeyCombination.with(modifiers: Set<KeyModifier>) =
        KeysWithModifiers(keys = keys, modifiers = modifiers)

    open infix fun Key.andThen(other: Key) =
        PendingSequenceStep(
            prevSteps = listOf(ShortcutTrigger.Chord(setOf(this), emptySet(), KeyEventType.KeyDown)),
            keys = setOf(other),
            modifiers = emptySet()
        )

    protected open fun Key.toChord(eventType: KeyEventType) =
        ShortcutTrigger.Chord(setOf(this), emptySet(), eventType)

    protected open fun KeyCombination.toChord(eventType: KeyEventType) =
        ShortcutTrigger.Chord(keys, emptySet(), eventType)

    protected open fun KeysWithModifiers.toChord(eventType: KeyEventType) =
        ShortcutTrigger.Chord(keys, modifiers, eventType)

    open infix fun Key.press(action: () -> Unit) {
        register(
            trigger = ShortcutTrigger.Chord(
                keys = setOf(this),
                eventType = KeyEventType.KeyDown,
                modifiers = emptySet(),
            ),
            action = action
        )
    }

    open infix fun Key.up(action: () -> Unit) {
        register(
            trigger = ShortcutTrigger.Chord(
                keys = setOf(this),
                eventType = KeyEventType.KeyUp,
                modifiers = emptySet(),
            ),
            action = action
        )
    }

    open infix fun KeyCombination.press(action: () -> Unit) {
        register(
            trigger = ShortcutTrigger.Chord(
                keys = keys,
                eventType = KeyEventType.KeyDown,
                modifiers = emptySet(),
            ),
            action = action
        )
    }

    open infix fun KeyCombination.up(action: () -> Unit) {
        register(
            trigger = ShortcutTrigger.Chord(
                keys = keys,
                eventType = KeyEventType.KeyUp,
                modifiers = emptySet(),
            ),
            action = action
        )
    }

    open infix fun KeysWithModifiers.press(action: () -> Unit) {
        register(
            trigger = ShortcutTrigger.Chord(
                keys = keys,
                eventType = KeyEventType.KeyDown,
                modifiers = modifiers,
            ),
            action = action
        )
    }

    open infix fun KeysWithModifiers.up(action: () -> Unit) {
        register(
            trigger = ShortcutTrigger.Chord(
                keys = keys,
                eventType = KeyEventType.KeyUp,
                modifiers = modifiers,
            ),
            action = action
        )
    }

    open infix fun PendingSequenceStep.press(action: () -> Unit) {
        register(
            trigger = buildSequence(eventType = KeyEventType.KeyDown),
            action = action
        )
    }

    open infix fun PendingSequenceStep.up(action: () -> Unit) {
        register(
            trigger = buildSequence(eventType = KeyEventType.KeyUp),
            action = action
        )
    }
}