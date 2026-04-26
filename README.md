# HyperKey

A Kotlin DSL for declarative keyboard shortcut handling in Compose Multiplatform (Android, Desktop).

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

---

## Installation

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.yoursvalentiine:hyperkey:0.1.0-beta03")
}
```

---

## Core concepts

The library has three layers:

| Layer         | API                                       | Use case                        |
|---------------|-------------------------------------------|---------------------------------|
| Container     | `ShortcutBox`                             | Shortcuts for an entire area    |
| Modifier      | `rememberShortcutModifier` + `onShortcut` | Shortcuts on a single component |
| Trigger types | Chord, Sequence                           | Simultaneous vs sequential keys |

---

## ShortcutBox

Wraps any content and intercepts keyboard events within it. Takes a `ShortcutScope` lambda where all shortcuts are
declared.

```kotlin
ShortcutBox(
    modifier = Modifier.fillMaxSize(),
    shortcuts = {
        Key.S with ctrl press { save() }
        Key.Z with ctrl press { undo() }
        Key.Z with ctrl + shift press { redo() }
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
    val shortcuts = rememberShortcutModifier {
        Key.Enter press { submit() }
        Key.Escape press { cancel() }
        Key.Tab press { focusNext() }
    }

    // Step 2 — apply in modifier chain
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .onShortcut(shortcuts)
            .padding(16.dp)
    )
}
```

Multiple independent shortcut groups on one component:

```kotlin
val navigationShortcuts = rememberShortcutModifier {
    Key.K with ctrl andThen Key.S press { saveAll() }
    Key.K with ctrl andThen Key.B press { toggleSidebar() }
}

val editingShortcuts = rememberShortcutModifier {
    Key.S with ctrl press { save() }
    Key.Z with ctrl press { undo() }
}

Box(
    modifier = Modifier
        .onShortcut(navigationShortcuts)  // own sequence progress tracker
        .onShortcut(editingShortcuts)     // own sequence progress tracker
)
```

---

## ShortcutScope DSL

### Modifiers

Modifiers are top-level objects that combine with `+`:

```kotlin
ctrl
alt
shift
meta
hyper  // expands to ctrl + alt + shift + meta
```

```kotlin
Key.S with ctrl press { }
Key.S with ctrl + shift press { }
Key.S with hyper press { }         // ctrl + alt + shift + meta + S
```

### Chord — simultaneous keys

A chord fires when all specified keys and modifiers are held at the same time.

```kotlin
// Single key
Key.Escape press { }

// Key + modifier
Key.S with ctrl press { }
Key.Z with ctrl + shift press { }

// Multiple keys + modifier
Key.A + Key.B with ctrl press { }
Key.A + Key.B + Key.C with alt press { }
```

### Sequence — sequential keys

A sequence fires when keys are pressed one after another, in order. The timeout between steps is 2 seconds by default.

```kotlin
// Two steps
Key.K with ctrl andThen Key.S press { saveAll() }
Key.K with ctrl andThen Key.B press { toggleSidebar() }

// Three steps
Key.K with ctrl andThen Key.G andThen Key.G press { goToLine() }

// Mixed modifiers per step
Key.K with ctrl andThen Key.P with shift press { openPalette() }
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

Key.S with ctrl press { }
Key.S with ctrl up { }

Key.K with ctrl andThen Key.S press { }
Key.K with ctrl andThen Key.S up { }
```

### preview — parent intercepts before child

By default, keyboard events bubble upward — the innermost focused component handles them first. Events inside
`preview { }` are dispatched top-down, so a parent `ShortcutBox` intercepts them before any child.

```kotlin
ShortcutBox(
    shortcuts = {
        Key.S with ctrl press { save() }     // child handles first (normal)

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
ShortcutBox(
    shortcuts = {
        Key.S with ctrl press { globalSave() }  // handles Ctrl+S everywhere
    }
) {
    ShortcutBox(
        shortcuts = {
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
Key.A with ctrl press { }
Key.A with ctrl + alt press { }
Key.A with ctrl + alt + shift press { }
Key.A with hyper press { }              // ctrl + alt + shift + meta + A
Key.A + Key.B press { }                 // A and B simultaneously
Key.A + Key.B with ctrl press { }

// ── Sequences ───────────────────────────────────────────────────
Key.A andThen Key.B press { }
Key.A with ctrl andThen Key.B press { }
Key.A with ctrl andThen Key.B with shift press { }
Key.A andThen Key.B andThen Key.C press { }

// ── KeyUp ───────────────────────────────────────────────────────
Key.A up { }
Key.A with ctrl up { }
Key.A andThen Key.B up { }
```

---

## How it works

### ShortcutMatcher

Each `rememberShortcutModifier` and `ShortcutBox` owns a `ShortcutMatcher` instance that lives for the lifetime of the
composable (survives recomposition via `remember`).

The matcher tracks:

- `pressedKeys: Set<Key>` — currently held keys, for multi-key chords
- `sequenceProgress: Map<KeyShortcut, Int>` — current step index per sequence shortcut
- `lastEventTime` — for sequence timeout detection

### Hyper key expansion

`hyper` is a virtual modifier that expands to `ctrl + alt + shift + meta` at match time:

```kotlin
// These are equivalent:
Key.A with hyper press { }
Key.A with ctrl + alt + shift + meta press { }
```

---

## License

[BSD 3-Clause](LICENSE)

## AI assistance disclosure

Parts of this code were generated with the assistance of [Claude](https://anthropic.com/claude) (Anthropic).