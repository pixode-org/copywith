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

This generates a `copyWith()` extension function. Use `copyWith()` with a lambda to modify specific fields:

```kotlin
val original = User(name = "Alice", age = 30, email = "alice@example.com")

// Change one field
val renamed = original.copyWith { name = "Bob" }
// User(name="Bob", age=30, email="alice@example.com")

// Mutate fields based on their original values
val updated = original.copyWith { age++ }
// User(name="Alice", age=31, email="alice@example.com")

// Change multiple fields
val noEmail = original.copyWith {
    name = "Charlie"
    email = null
}
// User(name="Charlie", age=30, email=null)
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

```kotlin
@CopyWith
data class Track(val artist: String, val title: String)

@CopyWith
data class Playlist(val name: String, val tracks: List<Track>)
```

```kotlin
val original = Playlist(
    name = "Favorites",
    tracks = listOf(Track("Daft Punk", "Get Lucky"), Track("Radiohead", "Karma Police"))
)

// Remove a track
val trimmed = original.copyWith {
    tracks.removeAt(0)
}
// Playlist(name="Favorites", tracks=[Track("Radiohead", "Karma Police")])

// Modify a field on an existing track (deep mutation)
val retitled = original.copyWith {
    tracks[0].title = "Harder Better Faster Stronger"
}
// Playlist(name="Favorites", tracks=[Track("Daft Punk", "Harder Better Faster Stronger"), Track("Radiohead", "Karma Police")])

// Add a track
val extended = original.copyWith {
    tracks.add(Track("Tame Impala", "The Less I Know The Better"))
}
// Playlist(name="Favorites", tracks=[Track("Daft Punk", "Get Lucky"), Track("Radiohead", "Karma Police"), Track("Tame Impala", "The Less I Know The Better")])

// Replace the entire list
val replaced = original.copyWith {
    tracks = mutableListOf(Track("Portishead", "Glory Box").toBuilder())
}
// Playlist(name="Favorites", tracks=[Track("Portishead", "Glory Box")])
```

The same approach can be used with `Map`s and `Set`s.

## License

Copyright 2026 Flavien Charlon

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.

