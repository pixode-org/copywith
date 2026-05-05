package org.dynadoc

import io.kotest.matchers.shouldBe
import java.net.URI
import kotlin.test.Test

class NestedObjectsTest {

    private val uri = URI("http://example.com")
    private val scalars = Scalars(string = "hello", integer = 42, nullable = null)
    private val original = NestedObjects(alterable = scalars, nonAlterable = uri, nullable = null)

    @Test
    fun `alter with no changes returns an equal object`() {
        val result = original.alter {}
        result shouldBe original
    }

    @Test
    fun `alter modifies a field on the alterable nested object`() {
        val result = original.alter { alterable.string = "world" }
        result.alterable shouldBe Scalars("world", 42, null)
    }

    @Test
    fun `alter replaces the alterable nested object`() {
        val replacement = Scalars("new", 0, "tag")
        val result = original.alter { alterable = replacement.toBuilder() }
        result.alterable shouldBe replacement
    }

    @Test
    fun `alter sets the non-alterable field`() {
        val newUri = URI("http://other.com")
        val result = original.alter { nonAlterable = newUri }
        result.nonAlterable shouldBe newUri
    }

    @Test
    fun `alter does not change unmodified fields`() {
        val result = original.alter { alterable.string = "world" }
        result.nonAlterable shouldBe original.nonAlterable
    }

    @Test
    fun `toBuilder round-trips the object unchanged`() {
        val result = original.toBuilder().build()
        result shouldBe original
    }

    // Nullable nested @Alter field

    @Test
    fun `alter sets a null nullable field to a new builder`() {
        val result = original.alter { nullable = Scalars("x", 1, null).toBuilder() }
        result.nullable shouldBe Scalars("x", 1, null)
    }

    @Test
    fun `alter modifies a field on a non-null nullable nested object`() {
        val withNullable = original.copy(nullable = Scalars("x", 1, null))
        val result = withNullable.alter { nullable?.string = "updated" }
        result.nullable shouldBe Scalars("updated", 1, null)
    }

    @Test
    fun `alter sets a non-null nullable field to null`() {
        val withNullable = original.copy(nullable = Scalars("x", 1, null))
        val result = withNullable.alter { nullable = null }
        result.nullable shouldBe null
    }

    @Test
    fun `alter leaves a null nullable field as null when unset`() {
        val result = original.alter { alterable.string = "world" }
        result.nullable shouldBe null
    }
}
