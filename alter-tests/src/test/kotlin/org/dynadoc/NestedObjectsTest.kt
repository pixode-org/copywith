package org.dynadoc

import io.kotest.matchers.shouldBe
import java.net.URI
import kotlin.test.Test

class NestedObjectsTest {

    private val uri = URI("http://example.com")
    private val scalars = Scalars(string = "hello", integer = 42, nullable = null)
    private val original = NestedObjects(alterable = scalars, nonAlterable = uri)

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
    fun `alter does not mutate the original`() {
        original.alter { alterable.string = "world" }
        original.alterable shouldBe scalars
    }

    @Test
    fun `toBuilder round-trips the object unchanged`() {
        val result = original.toBuilder().build()
        result shouldBe original
    }
}
