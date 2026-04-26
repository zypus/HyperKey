package io.github.yoursvalentiine.hyperkey.utils

import androidx.compose.ui.input.key.*
import io.github.yoursvalentiine.hyperkey.models.KeyModifier
import io.github.yoursvalentiine.hyperkey.models.KeyShortcut
import io.github.yoursvalentiine.hyperkey.models.ShortcutTrigger
import io.github.yoursvalentiine.hyperkey.models.expand
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class ShortcutMatcher {
    val pressedKeys = mutableSetOf<Key>()

    private val sequenceProgress = mutableMapOf<KeyShortcut, Int>()
    private val timeSource = TimeSource.Monotonic

    private var lastEventTime = timeSource.markNow()
    private val sequenceTimeoutMs = 2000L

    fun handle(event: KeyEvent, shortcuts: List<KeyShortcut>): Boolean {
        val now = timeSource.markNow()

        if (now - lastEventTime > sequenceTimeoutMs.milliseconds) {
            sequenceProgress.clear()
        }
        lastEventTime = now

        val isAutoRepeat = event.type == KeyEventType.KeyDown && event.key in pressedKeys

        when (event.type) {
            KeyEventType.KeyUp -> pressedKeys -= event.key
            KeyEventType.KeyDown -> pressedKeys += event.key
            else -> return false
        }

        if (isAutoRepeat) return false

        shortcuts.forEach { shortcut ->
            val matched = when (val trigger = shortcut.trigger) {
                is ShortcutTrigger.Chord -> matchChord(trigger, event)
                is ShortcutTrigger.Sequence -> matchSequence(shortcut, trigger, event)
            }

            if (matched) {
                sequenceProgress.remove(shortcut)
                shortcut.action()
                return true
            }
        }
        return false
    }

    private fun matchChord(
        chord: ShortcutTrigger.Chord,
        event: KeyEvent
    ): Boolean {
        if (chord.eventType != event.type) return false

        val effectiveModifiers = chord.modifiers.expand()
        val modifiersMatch =
            event.isCtrlPressed == (KeyModifier.CTRL in effectiveModifiers) &&
                    event.isAltPressed == (KeyModifier.ALT in effectiveModifiers) &&
                    event.isShiftPressed == (KeyModifier.SHIFT in effectiveModifiers) &&
                    event.isMetaPressed == (KeyModifier.META in effectiveModifiers)

        if (!modifiersMatch) return false

        return if (chord.keys.size == 1) {
            event.key == chord.keys.first()
        } else {
            event.key in chord.keys && chord.keys.all { it in pressedKeys }
        }
    }

    private fun matchSequence(
        shortcut: KeyShortcut,
        sequence: ShortcutTrigger.Sequence,
        event: KeyEvent
    ): Boolean {
        val currentStep = sequenceProgress[shortcut] ?: 0
        val expectedChord = sequence.steps[currentStep]

        if (expectedChord.eventType != event.type) return false

        if (!matchChord(expectedChord, event)) {
            sequenceProgress.remove(shortcut)

            val firstChord = sequence.steps[0]

            if (matchChord(firstChord, event)) {
                sequenceProgress[shortcut] = 1
            }
            return false
        }

        val nextStep = currentStep + 1
        return if (nextStep == sequence.steps.size) {
            true
        } else {
            sequenceProgress[shortcut] = nextStep
            false
        }

        /*return if (matchChord(expectedChord, event)) {
            val nextStep = currentStep + 1
            if (nextStep == sequence.steps.size) {
                true
            } else {
                sequenceProgress[shortcut] = nextStep
                false
            }
        } else {
            sequenceProgress.remove(shortcut)

            val firstChord = sequence.steps[0]
            if (firstChord.eventType == event.type && matchChord(firstChord, event)) {
                sequenceProgress[shortcut] = 1
            }

            false
        }*/
    }
}