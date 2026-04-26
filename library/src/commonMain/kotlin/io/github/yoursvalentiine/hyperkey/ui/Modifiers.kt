package io.github.yoursvalentiine.hyperkey.ui

import androidx.compose.ui.Modifier

fun Modifier.onShortcut(shortcutModifier: Modifier): Modifier =
    this.then(shortcutModifier)