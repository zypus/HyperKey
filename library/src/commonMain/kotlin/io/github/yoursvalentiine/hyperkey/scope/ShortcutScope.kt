package io.github.yoursvalentiine.hyperkey.scope

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import io.github.yoursvalentiine.hyperkey.annotations.ShortcutDsl
import io.github.yoursvalentiine.hyperkey.models.KeyCombination
import io.github.yoursvalentiine.hyperkey.models.KeysWithModifiers
import io.github.yoursvalentiine.hyperkey.models.PendingSequenceStep
import io.github.yoursvalentiine.hyperkey.models.ShortcutTrigger

@ShortcutDsl
class ShortcutScope : BaseKeyScope(mutableListOf()) {

    fun preview(block: PreviewScope.() -> Unit): Unit {
        PreviewScope().block()
    }

    inner class PreviewScope : BaseKeyScope(this@ShortcutScope.shortcuts) {
        override infix fun Key.press(action: () -> Unit) {
            register(
                trigger = ShortcutTrigger.Chord(setOf(this), emptySet(), KeyEventType.KeyDown),
                preview = true,
                action = action
            )
        }

        override infix fun Key.up(action: () -> Unit) {
            register(
                trigger = ShortcutTrigger.Chord(setOf(this), emptySet(), KeyEventType.KeyUp),
                preview = true,
                action = action
            )
        }

        override infix fun KeyCombination.press(action: () -> Unit) {
            register(
                trigger = ShortcutTrigger.Chord(keys, emptySet(), KeyEventType.KeyDown),
                preview = true,
                action = action
            )
        }

        override infix fun KeyCombination.up(action: () -> Unit) {
            register(
                trigger = ShortcutTrigger.Chord(keys, emptySet(), KeyEventType.KeyUp),
                preview = true,
                action = action
            )
        }

        override infix fun KeysWithModifiers.press(action: () -> Unit) {
            register(
                trigger = ShortcutTrigger.Chord(keys, modifiers, KeyEventType.KeyDown),
                preview = true,
                action = action
            )
        }

        override infix fun KeysWithModifiers.up(action: () -> Unit) {
            register(
                trigger = ShortcutTrigger.Chord(keys, modifiers, KeyEventType.KeyUp),
                preview = true,
                action = action
            )
        }

        override infix fun PendingSequenceStep.press(action: () -> Unit) =
            register(
                trigger = buildSequence(KeyEventType.KeyDown),
                preview = true,
                action = action
            )

        override infix fun PendingSequenceStep.up(action: () -> Unit) =
            register(
                trigger = buildSequence(KeyEventType.KeyUp),
                preview = true,
                action = action
            )
    }
}