# Docs

> Note: This library is under active development, so the API may change.

## Installation

All library packages are publicly available on Maven Central.

1. Create a project on Compose Multiplatform or Jetpack Compose.

   **Please note:** The library currently officially supports Desktop and Android.

2. Check that Maven Central is enabled in `settings.gradle.kts`.

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        // other...
    }
}
```

3. Add the dependency to your project. (If you use Compose Multiplatform, add it to `commonMain`)

```toml
[versions]
hyperkey = "0.2.0-beta02" # or latest version

[libraries]
hyperkey = { module = "io.github.yours-valentiine:hyperkey", version.ref = "hyperkey" }
```

```kotlin
// build.gradle.kts

sourceSets {
    commonMain.dependencies {
        // other
        implementation(libs.hyperkey)
        // or implementation("io.github.yours-valentiine:hyperkey:0.2.0-beta02")
    }
}
```

## DSL

In fact, the rules for creating hotkeys are quite simple:

`[key] [action] [function]`

OR

`[sequence] [action for the last sequence] [function]`

The following classes can act as keys:

| Class         | Package                                     | Example                                 | Description                                                                             |
|---------------|---------------------------------------------|-----------------------------------------|-----------------------------------------------------------------------------------------|
| `Key`         | `androidx.compose.ui.input.key`             | `Key.Escape`, `Key.Tab`, `Key.A`        | Represents a value class for a keyboard key. It has ready-made variables for most keys. |
| `KeyModifier` | `io.github.yoursvalentiine.hyperkey.models` | `Ctrl`, `Alt`, `Meta`, `Shift`, `Hyper` | It is a sealed interface for modifier keys.                                             |

There is also a class for creating dynamic hotkey bindings (this API is currently unstable, but a more convenient method
will appear in the future)

| Class      | Parameters                                      | Description                                                                                                                                     |
|------------|-------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| `RawChord` | `keys: Set<Key>`, `modifiers: Set<KeyModifier>` | Represents a regular keyboard shortcut. Any of the parameters can be empty.<br/> (e.g., `RawChord(keys = emptySet(), modifiers = setOf(Ctrl))`) |

Currently, two types of actions are supported: pressing and releasing a key. These two functions pass the KeyEvent class
as a parameter.

| Action  | Description                                             |
|---------|---------------------------------------------------------|
| `press` | Triggered when a key combination is pressed.            |
| `up`    | Key release trigger. Currently only works with one key. |

### andThen

The library also allows you to create key combinations using the andThen keyword. It works as follows:

1. the user presses the first combination
2. releases the keys
3. the user presses the second combination
4. and so on

### Examples

```kotlin
// Classic combination
Ctrl + Key.A press {}
Key.B up {}
Ctrl press {}

// Sequence
Ctrl + Key.K andThen Alt + Key.B press {}
Key.A andThen Key.S press {}
Ctrl + Alt + Key.B andThen Shift + Key.Y andThen Key.L press {}

// Dynamic combo
RawChord(
   keys = setOf(Key.A),
   modifiers = setOf(Ctrl)
) press {
   println("RAW")
}
```

## Creating hotkeys scope

There are two ways to add hotkeys to an application using this library. Let's look at them in turn.

### The first method

This method is great for Compose functions that don't have focus by default (Box, Column...)

To create hotkeys, wrap your component in the Compose HotKeyBox function and pass the desired combinations as the
hotkeys parameter.

```kotlin
@Composable
fun SomeContent(
    modifier: Modifier = Modifier,
    onBackRequest: () -> Unit,
    onEditRequest: () -> Unit
) {
    HotKeyBox(
        modifier = modifier,
        hotkeys = {
            Key.Escape press { onBackRequest() }
            Ctrl + Key.E press { onEditRequest() }
        }
    ) {
        Column {
            // content
        }
    }
}
```

In this case, HotKeyBox will automatically create a focusRequester and request focus.

### The second method

This method is convenient to apply to components that already have the ability to receive focus (for example, TextField
or custom Box)

In this case, you need to define hot keys inside the `rememberHotKeyModifier` function and simply pass the variable as a
parameter to the `onHotKey` modifier

```kotlin
@Composable
fun SomeFocusableContent(
    modifier: Modifier = Modifier,
    onEnterRequest: () -> Unit,
    onEscapeRequest: () -> Unit
) {
    val hotkeys = rememberHotKeyModifier {
        Key.Enter press { onEnterRequest() }
        Key.Escape press { onEscapeRequest() }
    }

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .onHotKey(shortcuts)
            .padding(16.dp)
    )
}
```
