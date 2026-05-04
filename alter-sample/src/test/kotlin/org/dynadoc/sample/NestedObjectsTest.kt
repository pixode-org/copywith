package org.dynadoc.sample

import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals

class NestedObjectsTest {

    private val uri = URI("http://example.com")
    private val scalars = Scalars(string = "hello", integer = 42, nullable = null)
    private val original = NestedObjects(alterable = scalars, nonAlterable = uri)

    @Test
    fun `alter with no changes returns an equal object`() {
        assertEquals(original, original.alter {})
    }

    @Test
    fun `alter modifies a field on the alterable nested object`() {
        val result = original.alter { alterable.string = "world" }
        assertEquals(Scalars("world", 42, null), result.alterable)
    }

    @Test
    fun `alter replaces the alterable nested object`() {
        val replacement = Scalars("new", 0, "tag")
        val result = original.alter { alterable = replacement.toBuilder() }
        assertEquals(replacement, result.alterable)
    }

    @Test
    fun `alter sets the non-alterable field`() {
        val newUri = URI("http://other.com")
        val result = original.alter { nonAlterable = newUri }
        assertEquals(newUri, result.nonAlterable)
    }

    @Test
    fun `alter does not change unmodified fields`() {
        val result = original.alter { alterable.string = "world" }
        assertEquals(original.nonAlterable, result.nonAlterable)
    }

    @Test
    fun `alter does not mutate the original`() {
        original.alter { alterable.string = "world" }
        assertEquals(scalars, original.alterable)
    }

    @Test
    fun `toBuilder round-trips the object unchanged`() {
        assertEquals(original, original.toBuilder().build())
    }
}