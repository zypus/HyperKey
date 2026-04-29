# Changelog

## [0.2.0-beta02] - 28.04.2026

### Breaking changes

Complete change of API for working with ~~shortcut~~ hotkeys.

**What's new:**

- All shortcut classes have been removed. Hotkeys are now used instead.

**old:**

```kotlin
ShortcutBox(
    shortcuts = {
        Key.S with ctrl press { save() }
        Key.K with ctrl andThen Key.P press { openCommandPalette() }
        Key.Space with hyper press { spotlight() }
    }
) {
    MyApp()
}
```

**new:**

```kotlin
ShortcutBox(
    shortcuts = {
        Key.S + Ctrl press { save() }
        Key.K + Ctrl andThen Key.P press { openCommandPalette() }
        Key.Space + Hyper press { spotlight() }
    }
) {
    MyApp()
}
```

- Now the `press` and `up` functions pass the key event as a parameter.

```kotlin
ShortcutBox(
    shortcuts = {
        Key.B + Alt press { event -> println(event.toString()) }
    }
) {
    MyApp()
}
```

- The `Alt`, `Ctrl`, `Meta`, and `Hyper` modifiers are now full keys and can be combined without additional keys.

```kotlin
ShortcutBox(
    shortcuts = {
        Alt press { println("Single Alt") }
        Ctrl + Alt press { println("Pressed Ctrl+Alt") }
    }
) {
    MyApp()
}
```

- Removed unnecessary intermediate classes.

### Migration from `0.1.0`

The syntax from version 0.1.0 is deprecated because it was not very optimal to use.

1. Now, instead of using the with function to apply a modifier to a key, replace it with the "+" operator.

```kotlin
// old API v0.1.0
ShortcutBox(
    shortcuts = {
        Key.S with ctrl press { save() }
    }
) {
    // some code
}

// new API v0.2.0
HotKeyBox(
    hotkeys = {
        Key.S + Ctrl press { save() } // or Ctrl + Key.S
    }
) {
    // content
}
```

2. All modifiers and Compose functions use the `hotkey` prefix instead of `shortcut`.
    - ShortcutBox --> HotKeyBox
    - rememberShortcutModifier --> rememberHotKeyModifier
    - onShortcut --> onHotKey