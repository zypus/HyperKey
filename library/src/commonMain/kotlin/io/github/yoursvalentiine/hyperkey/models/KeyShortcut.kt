package io.github.yoursvalentiine.hyperkey.models

data class KeyShortcut(
    val trigger: ShortcutTrigger,
    val preview: Boolean = false,
    val action: () -> Unit
)