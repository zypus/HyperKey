# HyperKey

A Kotlin DSL for declarative keyboard shortcut handling in Compose Multiplatform (Android, Desktop).

```kotlin
HotKeyBox(
    hotkeys = {
        Key.S + Ctrl press { save() }
        Key.K + Ctrl andThen Key.P press { openCommandPalette() }
        Key.Space + Hyper press { spotlight() }
    }
) {
    MyApp()
}
```

---

## Installation

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.yoursvalentiine:hyperkey:0.2.0-beta02")
}
```

---

## Core concepts

The library has three layers:

| Layer         | API                                   | Use case                        |
|---------------|---------------------------------------|---------------------------------|
| Container     | `HotKeyBox`                           | Shortcuts for an entire area    |
| Modifier      | `rememberHotKeyModifier` + `onHotKey` | Shortcuts on a single component |
| Trigger types | `Chord`, `Sequence`                   | Simultaneous vs sequential keys |

---

## HotKeyBox

Wraps any content and intercepts keyboard events within it. Takes a `ShortcutScope` lambda where all shortcuts are
declared.

```kotlin
HotKeyBox(
    modifier = Modifier.fillMaxSize(),
    hotkeys = {
        Key.S + Ctrl press { save() }
        Key.Z + Ctrl press { undo() }
        Key.Z + Ctrl + Shift press { redo() }
        Key.Escape press { closeDialog() }
    }
) {
    // Normal Box content
    EditorContent()
}
```

---

## Modifier.onShortcut

For attaching shortcuts to a single component. Since `onShortcut` requires `remember` internally, use the two-step
pattern:

```kotlin
@Composable
fun MyTextField() {
    // Step 1 — create at composable scope
    val hotkeys = rememberHotKeyModifier {
        Key.Enter press { submit() }
        Key.Escape press { cancel() }
        Key.Tab press { focusNext() }
    }

    // Step 2 — apply in modifier chain
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .onHotKey(shortcuts)
            .padding(16.dp)
    )
}
```

Multiple independent shortcut groups on one component:

```kotlin
val navigationHotkeys = rememberHotKeyModifier {
    Key.K + Ctrl andThen Key.S press { saveAll() }
    Key.K + Ctrl andThen Key.B press { toggleSidebar() }
}

val editingHotkeys = rememberShortcutModifier {
    Key.S + Ctrl press { save() }
    Key.Z + Ctrl press { undo() }
}

Box(
    modifier = Modifier
        .onHotKey(navigationShortcuts)  // own sequence progress tracker
        .onHotKey(editingShortcuts)     // own sequence progress tracker
)
```

---

## ShortcutScope DSL

### Modifiers

Modifiers are top-level objects that combine with `+`:

```kotlin
Ctrl
Alt
Shift
Meta
Hyper  // expands to ctrl + alt + shift + meta
```

```kotlin
Key.S + Ctrl press { }
Key.S + Ctrl + Shift press { }
Key.S + Hyper press { }         // ctrl + alt + shift + meta + S
```

### Chord — simultaneous keys

A chord fires when all specified keys and modifiers are held at the same time.

```kotlin
// Single key
Key.Escape press { }

// Single modifier
Ctrl press { }
Alt + Ctrl press { }

// Key + modifier
Key.S + Ctrl press { }
Key.Z + Ctrl + shift press { }

// Multiple keys + modifier
Key.A + Key.B + Ctrl press { }
Key.A + Key.B + Key.C + Alt press { }
```

### Sequence — sequential keys

A sequence fires when keys are pressed one after another, in order. The timeout between steps is 2 seconds by default.

```kotlin
// Two steps
Key.K + Ctrl andThen Key.S press { saveAll() }
Key.K + Ctrl andThen Key.B press { toggleSidebar() }

// Three steps
Key.K + Ctrl andThen Key.G andThen Key.G press { goToLine() }

// Mixed modifiers per step
Key.K + Ctrl andThen Key.P + Shift press { openPalette() }
```

Sequence matching rules:

- `KeyUp` events between steps are ignored — releasing a key does not reset progress
- Autorepeat `KeyDown` events (holding a key) are ignored
- Progress resets if a wrong key is pressed at any step
- Progress resets after 2 seconds of inactivity

### press vs up

```kotlin
Key.Enter press { }   // fires on KeyDown
Key.Enter up { }      // fires on KeyUp

Key.S + Ctrl press { }
Key.S + Ctrl up { }

Key.K + Ctrl andThen Key.S press { }
Key.K + Ctrl andThen Key.S up { }
```

### preview — parent intercepts before child

By default, keyboard events bubble upward — the innermost focused component handles them first. Events inside
`preview { }` are dispatched top-down, so a parent `ShortcutBox` intercepts them before any child.

```kotlin
HotKeyBox(
    hotkeys = {
        Key.S + Ctrl press { save() }     // child handles first (normal)

        preview {
            Key.Escape press { closeModal() } // parent intercepts before child
            Key.F10 press { openMenu() }
        }
    }
) {
    ChildContent()
}
```

---

## Nested ShortcutBox

Shortcut events propagate upward — the innermost `ShortcutBox` handles matching shortcuts first. If it returns `true` (
handled), the event stops. Unmatched events bubble to the parent.

```kotlin
HotKeyBox(
    hotkeys = {
        Key.S + Ctrl press { globalSave() }  // handles Ctrl+S everywhere
    }
) {
    HotKeyBox(
        hotkeys = {
            Key.Escape press { closeModal() }   // intercepts Escape here
            // Ctrl+S bubbles to parent
        }
    ) {
        ModalContent()
    }
}
```

---

## Key combinations reference

```kotlin
// ── Chords ──────────────────────────────────────────────────────
Key.A press { }
Key.A + Ctrl press { }
Key.A + Ctrl + Alt press { }
Key.A + Ctrl + Alt + Shift press { }
Key.A + Hyper press { }              // ctrl + alt + shift + meta + A
Key.A + Key.B press { }                 // A and B simultaneously
Key.A + Key.B with Ctrl press { }
Alt + Ctrl { }

// ── Sequences ───────────────────────────────────────────────────
Key.A andThen Key.B press { }
Key.A + Ctrl andThen Key.B press { }
Key.A + Ctrl andThen Key.B + Shift press { }
Key.A andThen Key.B andThen Key.C press { }

// ── KeyUp ───────────────────────────────────────────────────────
Key.A up { }
Key.A + Ctrl up { }
Key.A andThen Key.B up { }
```

---

## How it works

### ShortcutMatcher

Each `rememberHotKeyModifier` and `HotKeyBox` owns a `HotKeyMatcher` instance that lives for the lifetime of the
composable (survives recomposition via `remember`).

The matcher tracks:

- `pressed: Set<Key>` — currently held keys, for multi-key chords
- `sequenceProgress: Map<KeyShortcut, Int>` — current step index per sequence shortcut
- `lastEventTime` — for sequence timeout detection

### Hyper key expansion

`Hyper` is a virtual modifier that expands to `ctrl + alt + shift + meta` at match time:

```kotlin
// These are equivalent:
Key.A + Hyper press { }
Key.A + Ctrl + Alt + Shift + Meta press { }
```

---

## License

[BSD 3-Clause](LICENSE)

## AI assistance disclosure

Parts of this code were generated with the assistance of [Claude](https://anthropic.com/claude) (Anthropic).