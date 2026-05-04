package org.dynadoc

import io.kotest.matchers.shouldBe
import kotlin.test.Test

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
        val result = original.alter {}
        result shouldBe original
    }

    // List

    @Test
    fun `alter adds an element to the list`() {
        val result = original.alter { list.add("d") }
        result.list shouldBe listOf("a", "b", "c", "d")
    }

    @Test
    fun `alter removes an element from the list`() {
        val result = original.alter { list.remove("b") }
        result.list shouldBe listOf("a", "c")
    }

    @Test
    fun `alter replaces an element in the list`() {
        val result = original.alter { list[0] = "z" }
        result.list shouldBe listOf("z", "b", "c")
    }

    @Test
    fun `alter does not mutate the original list`() {
        original.alter { list.add("d") }
        original.list shouldBe listOf("a", "b", "c")
    }

    // Set of @Alter elements

    @Test
    fun `alter modifies a field on an element in the set`() {
        val result = original.alter { set.first().string = "updated" }
        result.set shouldBe setOf(Scalars("updated", 1, null))
    }

    @Test
    fun `alter does not mutate the original set`() {
        original.alter { set.first().string = "updated" }
        original.set shouldBe setOf(scalarsElement)
    }

    // Nullable list (backed by Optional — assigned as a whole)

    @Test
    fun `alter replaces the nullable list`() {
        val result = original.alter { nullable = listOf(1L, 2L, 3L) }
        result.nullable shouldBe listOf(1L, 2L, 3L)
    }

    @Test
    fun `alter does not mutate the original nullable list`() {
        original.alter { nullable = listOf(9L) }
        original.nullable shouldBe listOf(1L, 2L)
    }

    @Test
    fun `toBuilder round-trips the object unchanged`() {
        val result = original.toBuilder().build()
        result shouldBe original
    }
}
