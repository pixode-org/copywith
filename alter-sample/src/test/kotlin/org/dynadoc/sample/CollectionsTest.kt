package org.dynadoc.sample

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollectionsTest {

    private val scalarsElement = Scalars(string = "x", integer = 1, nullable = null)
    private val original = Collections(
        list = listOf("a", "b", "c"),
        map = emptyMap(),
        set = setOf(scalarsElement),
        nullable = listOf(1L, 2L),
    )

    @Test
    fun `alter with no changes returns an equal object`() {
        assertEquals(original, original.alter {})
    }

    // List

    @Test
    fun `alter adds an element to the list`() {
        assertEquals(listOf("a", "b", "c", "d"), original.alter { list.add("d") }.list)
    }

    @Test
    fun `alter removes an element from the list`() {
        assertEquals(listOf("a", "c"), original.alter { list.remove("b") }.list)
    }

    @Test
    fun `alter replaces an element in the list`() {
        assertEquals(listOf("z", "b", "c"), original.alter { list[0] = "z" }.list)
    }

    @Test
    fun `alter does not mutate the original list`() {
        original.alter { list.add("d") }
        assertEquals(listOf("a", "b", "c"), original.list)
    }

    // Set of @Alter elements

    @Test
    fun `alter modifies a field on an element in the set`() {
        val result = original.alter { set.first().string = "updated" }
        assertEquals(setOf(Scalars("updated", 1, null)), result.set)
    }

    @Test
    fun `alter does not mutate the original set`() {
        original.alter { set.first().string = "updated" }
        assertEquals(setOf(scalarsElement), original.set)
    }

    // Nullable list (backed by Optional — assigned as a whole)

    @Test
    fun `alter replaces the nullable list`() {
        assertEquals(listOf(1L, 2L, 3L), original.alter { nullable = listOf(1L, 2L, 3L) }.nullable)
    }

    @Test
    fun `alter does not mutate the original nullable list`() {
        original.alter { nullable = listOf(9L) }
        assertEquals(listOf(1L, 2L), original.nullable)
    }

    @Test
    fun `toBuilder round-trips the object unchanged`() {
        assertEquals(original, original.toBuilder().build())
    }
}