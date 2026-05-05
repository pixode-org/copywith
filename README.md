# CopyWith

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

Annotate any data class with `@Alter`:

```kotlin
@Alter
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
    name = "Charlie"
    age = 25
    email = "charlie@example.com"
}
// User(name="Charlie", age=25, email="charlie@example.com")

// Set a nullable field to null
val noEmail = original.copyWith { email = null }
// User(name="Alice", age=30, email=null)
```

## Nested Objects

When a data class contains fields that are themselves `@Alter`-annotated, you can modify nested fields directly without replacing the entire nested object:

```kotlin
@Alter
data class Address(val street: String, val city: String)

@Alter
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
@Alter
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
@Alter
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

### Sets of @Alter Objects

When a collection contains `@Alter`-annotated elements, the builder exposes a mutable collection of builders, allowing deep modifications:

```kotlin
@Alter
data class Tag(val name: String, val color: String)

@Alter
data class Article(val title: String, val tags: Set<Tag>)
```

```kotlin
val original = Article(
    title = "Hello World",
    tags = setOf(Tag("kotlin", "orange"), Tag("jvm", "purple"))
)

// Add a new tag
val tagged = original.copyWith {
    tags.add(Tag("oss", "green").toBuilder())
}

// Modify an existing tag's color
val recolored = original.copyWith {
    tags.first { it.name == "kotlin" }.color = "blue"
}
```

## Nullable Collections

Nullable collection fields can be set, cleared, or modified when present:

```kotlin
@Alter
data class Report(val title: String, val notes: List<String>?)
```

```kotlin
val original = Report(title = "Q1", notes = null)

// Set a null collection to a value
val withNotes = original.copyWith { notes = mutableListOf("Note 1", "Note 2") }
// Report(title="Q1", notes=["Note 1", "Note 2"])

// Clear a non-null collection
val cleared = withNotes.copyWith { notes = null }
// Report(title="Q1", notes=null)

// Modify a non-null collection in place
val extended = withNotes.copyWith { notes?.add("Note 3") }
// Report(title="Q1", notes=["Note 1", "Note 2", "Note 3"])
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

Apache License 2.0 — Copyright 2026 Flavien Charlon
