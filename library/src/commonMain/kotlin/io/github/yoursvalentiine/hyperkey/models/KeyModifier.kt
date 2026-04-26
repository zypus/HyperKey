package io.github.yoursvalentiine.hyperkey.models

import io.github.yoursvalentiine.hyperkey.scope.BaseKeyScope

/**
 * Represents keyboard modifier keys that can be combined with regular keys to form shortcuts.
 *
 * Modifiers can be combined (e.g., `CTRL + ALT`) and then with a regular key
 * (e.g., `CTRL + ALT + Key.A`) to create a complete key combination.
 *
 * @property HYPER A special modifier that expands to `CTRL + ALT + SHIFT + META` for convenience,
 *                 useful for Emacs-style shortcuts or complex multi-modifier combinations.
 *
 * @see models.KeyCombination
 * @see BaseKeyScope
 */
enum class KeyModifier {
    /** Control key (Cmd on macOS, Ctrl on other platforms) */
    CTRL,

    /** Alt key (Option on macOS) */
    ALT,

    /** Shift key */
    SHIFT,

    /** Meta key (Windows key on Windows, Cmd on macOS when CTRL is used as Control) */
    META,

    /**
     * Hyper key - expands to `CTRL + ALT + SHIFT + META` combination.
     * Useful for creating complex shortcuts without pressing all modifiers manually.
     */
    HYPER
}

val ctrl = KeyModifier.CTRL
val alt = KeyModifier.ALT
val shift = KeyModifier.SHIFT
val meta = KeyModifier.META
val hyper = KeyModifier.HYPER

operator fun KeyModifier.plus(other: KeyModifier): Set<KeyModifier> =
    setOf(this, other)

operator fun Set<KeyModifier>.plus(mod: KeyModifier): Set<KeyModifier> =
    this + mod

internal val KeyModifier.expanded: Set<KeyModifier>
    get() = when (this) {
        KeyModifier.HYPER -> setOf(KeyModifier.CTRL, KeyModifier.ALT, KeyModifier.SHIFT, KeyModifier.META)
        else -> setOf(this)
    }

internal fun Set<KeyModifier>.expand(): Set<KeyModifier> =
    flatMap { it.expanded }.toSet()