package org.pixode.copywith

import io.kotest.matchers.shouldBe
import java.net.URI
import kotlin.test.Test

class NestedObjectsTest {

    private val uri = URI("http://example.com")
    private val scalars = Scalars(string = "hello", integer = 42, nullable = null)
    private val original = NestedObjects(alterable = scalars, nonAlterable = uri, nullable = null)

    @Test
    fun `copyWith with no changes returns an equal object`() {
        val result = original.copyWith {}
        result shouldBe original
    }

    @Test
    fun `copyWith modifies a field on the alterable nested object`() {
        val result = original.copyWith { alterable.string = "world" }
        result.alterable shouldBe Scalars("world", 42, null)
    }

    @Test
    fun `copyWith replaces the alterable nested object`() {
        val replacement = Scalars("new", 0, "tag")
        val result = original.copyWith { alterable = replacement.toBuilder() }
        result.alterable shouldBe replacement
    }

    @Test
    fun `copyWith sets the non-alterable field`() {
        val newUri = URI("http://other.com")
        val result = original.copyWith { nonAlterable = newUri }
        result.nonAlterable shouldBe newUri
    }

    @Test
    fun `copyWith does not change unmodified fields`() {
        val result = original.copyWith { alterable.string = "world" }
        result.nonAlterable shouldBe original.nonAlterable
    }

    // Nullable nested @Alter field

    @Test
    fun `copyWith sets a null nullable field to a new builder`() {
        val result = original.copyWith { nullable = Scalars("x", 1, null).toBuilder() }
        result.nullable shouldBe Scalars("x", 1, null)
    }

    @Test
    fun `copyWith modifies a field on a non-null nullable nested object`() {
        val withNullable = original.copy(nullable = Scalars("x", 1, null))
        val result = withNullable.copyWith { nullable?.string = "updated" }
        result.nullable shouldBe Scalars("updated", 1, null)
    }

    @Test
    fun `copyWith sets a non-null nullable field to null`() {
        val withNullable = original.copy(nullable = Scalars("x", 1, null))
        val result = withNullable.copyWith { nullable = null }
        result.nullable shouldBe null
    }

    @Test
    fun `copyWith leaves a null nullable field as null when unset`() {
        val result = original.copyWith { alterable.string = "world" }
        result.nullable shouldBe null
    }
}
