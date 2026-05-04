package org.dynadoc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScalarsTest {

    private val original = Scalars(string = "hello", integer = 42, nullable = null)

    @Test
    fun `alter with no changes returns an equal object`() {
        assertEquals(original, original.alter {})
    }

    @Test
    fun `alter changes the string field`() {
        assertEquals("world", original.alter { string = "world" }.string)
    }

    @Test
    fun `alter changes the integer field`() {
        assertEquals(99, original.alter { integer = 99 }.integer)
    }

    @Test
    fun `alter sets the nullable field`() {
        assertEquals("tag", original.alter { nullable = "tag" }.nullable)
    }

    @Test
    fun `alter leaves the nullable field as null when unset`() {
        assertNull(original.alter { string = "world" }.nullable)
    }

    @Test
    fun `alter does not change unmodified fields`() {
        val result = original.alter { string = "world" }
        assertEquals(original.integer, result.integer)
        assertEquals(original.nullable, result.nullable)
    }

    @Test
    fun `alter does not mutate the original`() {
        original.alter { string = "world"; integer = 99 }
        assertEquals("hello", original.string)
        assertEquals(42, original.integer)
    }

    @Test
    fun `toBuilder round-trips the object unchanged`() {
        assertEquals(original, original.toBuilder().build())
    }

    @Test
    fun `toBuilder allows modification before build`() {
        val result = original.toBuilder().apply { integer = 0 }.build()
        assertEquals(0, result.integer)
        assertEquals(original.string, result.string)
    }
}