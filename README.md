# CopyWith

<a href="https://central.sonatype.com/artifact/org.pixode/copywith-annotation">![Maven Central Version](https://img.shields.io/maven-central/v/org.pixode/copywith-annotation)</a>

A Kotlin annotation processor that generates a fluent, type-safe `copyWith()` function for data classes. Unlike the built-in `.copy()` method, `copyWith()` supports deeply nested modifications and collection mutations without replacing the entire structure.

## Setup

CopyWith uses [KSP (Kotlin Symbol Processing)](https://github.com/google/ksp). Add the following to your `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "2.3.21"
    id("com.google.devtools.ksp") version "2.3.7"
}

dependencies {
    implementation("org.pixode:copywith-annotation:1.0.0")
    ksp("org.pixode:copywith-processor:1.0.0")
}
```

## Basic Usage

Annotate any data class with `@CopyWith`:

```kotlin
@CopyWith
data class User(
    val name: String,
    val age: Int,
    val email: String?
)
```

This generates a `copyWith()` extension function and a `toBuilder()` extension function. Use `copyWith()` with a lambda to modify specific fields:

```kotlin
val original = User(name = "Alice", age = 30, email = null)

// Change one field
val renamed = original.copyWith { name = "Bob" }
// User(name="Bob", age=30, email=null)

// Change multiple fields
val updated = original.copyWith {
    age++
    email = "bob@example.com"
}
// User(name="Bob", age=31, email="bob@example.com")

// Set a nullable field to null
val noEmail = original.copyWith { email = null }
// User(name="Alice", age=30, email=null)
```

## Nested Objects

When a data class contains fields that are themselves `@CopyWith`-annotated, you can modify nested fields directly without replacing the entire nested object:

```kotlin
@CopyWith
data class Address(val street: String, val city: String)

@CopyWith
data class Person(val name: String, val address: Address)
```

```kotlin
val original = Person(
    name = "Alice",
    address = Address(street = "123 Main St", city = "Springfield")
)

// Modify a nested field directly
val moved = original.copyWith {
    address.city = "Shelbyville"
}
// Person(name="Alice", address=Address(street="123 Main St", city="Shelbyville"))

// Replace the nested object entirely using toBuilder()
val newAddress = Address(street = "456 Elm St", city = "Capital City")
val relocated = original.copyWith {
    address = newAddress.toBuilder()
}
// Person(name="Alice", address=Address(street="456 Elm St", city="Capital City"))
```

## Collections

`copyWith()` exposes mutable versions of collection fields, so you can add, remove, or modify elements without replacing the entire collection. The original object is never mutated.

### Lists

```kotlin
@CopyWith
data class Playlist(val title: String, val tracks: List<String>)
```

```kotlin
val original = Playlist(title = "Favorites", tracks = listOf("Song A", "Song B", "Song C"))

// Add an element
val extended = original.copyWith { tracks.add("Song D") }
// Playlist(title="Favorites", tracks=["Song A", "Song B", "Song C", "Song D"])

// Remove an element
val trimmed = original.copyWith { tracks.remove("Song B") }
// Playlist(title="Favorites", tracks=["Song A", "Song C"])

// Replace the entire list
val replaced = original.copyWith { tracks = mutableListOf("New Song") }
// Playlist(title="Favorites", tracks=["New Song"])

// Original is unchanged
original.tracks // ["Song A", "Song B", "Song C"]
```

### Maps

```kotlin
@CopyWith
data class Config(val settings: Map<String, Int>)
```

```kotlin
val original = Config(settings = mapOf("timeout" to 30, "retries" to 3))

// Add or update an entry
val updated = original.copyWith { settings["timeout"] = 60 }
// Config(settings={"timeout": 60, "retries": 3})

// Remove an entry
val reduced = original.copyWith { settings.remove("retries") }
// Config(settings={"timeout": 30})
```

## Using `toBuilder()`

`toBuilder()` creates a builder pre-populated with an object's current values. This is useful for passing builders as nested field values or for constructing objects incrementally:

```kotlin
val base = User(name = "Alice", age = 30, email = null)
val builder = base.toBuilder()
builder.name = "Alice Updated"
builder.email = "alice@example.com"
val result = builder.build()
// User(name="Alice Updated", age=30, email="alice@example.com")
```

## License

Copyright 2026 Flavien Charlon

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.

