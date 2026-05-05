package org.pixode.copywith

import io.kotest.matchers.shouldBe
import java.time.DayOfWeek
import kotlin.test.Test

class CollectionsTest {

    private val scalarsElement = Scalars(string = "x", integer = 1, nullable = null)
    private val original = Collections(
        list = listOf("a", "b", "c"),
        map = mapOf(DayOfWeek.MONDAY to 8),
        set = setOf(scalarsElement),
        nullable = listOf(1L, 2L),
    )

    @Test
    fun `copyWith with no changes returns an equal object`() {
        val result = original.copyWith {}
        result shouldBe original
    }

    // List

    @Test
    fun `copyWith adds an element to the list`() {
        val result = original.copyWith { list.add("d") }
        result.list shouldBe listOf("a", "b", "c", "d")
    }

    @Test
    fun `copyWith removes an element from the list`() {
        val result = original.copyWith { list.remove("b") }
        result.list shouldBe listOf("a", "c")
    }

    @Test
    fun `copyWith replaces an element in the list`() {
        val result = original.copyWith { list[0] = "z" }
        result.list shouldBe listOf("z", "b", "c")
    }

    @Test
    fun `copyWith replaces the entire list`() {
        val result = original.copyWith { list = mutableListOf("x", "y", "z") }
        result.list shouldBe listOf("x", "y", "z")
    }

    @Test
    fun `copyWith does not mutate the original list`() {
        original.copyWith { list.add("d") }
        original.list shouldBe listOf("a", "b", "c")
    }

    // Map

    @Test
    fun `copyWith adds an entry to the map`() {
        val result = original.copyWith { map[DayOfWeek.TUESDAY] = 9 }
        result.map shouldBe mapOf(DayOfWeek.MONDAY to 8, DayOfWeek.TUESDAY to 9)
    }

    @Test
    fun `copyWith modifies an existing entry in the map`() {
        val result = original.copyWith { map[DayOfWeek.MONDAY] = 10 }
        result.map shouldBe mapOf(DayOfWeek.MONDAY to 10)
    }

    @Test
    fun `copyWith replaces the entire map`() {
        val result = original.copyWith { map = mutableMapOf(DayOfWeek.FRIDAY to 5) }
        result.map shouldBe mapOf(DayOfWeek.FRIDAY to 5)
    }

    @Test
    fun `copyWith does not mutate the original map`() {
        original.copyWith { map[DayOfWeek.TUESDAY] = 9 }
        original.map shouldBe mapOf(DayOfWeek.MONDAY to 8)
    }

    // Set of @Alter elements

    @Test
    fun `copyWith adds an entry to the set`() {
        val result = original.copyWith { set.add(Scalars("added", 3, "set").toBuilder()) }
        result.set shouldBe setOf(scalarsElement, Scalars("added", 3, "set"))
    }

    @Test
    fun `copyWith modifies a field on an element in the set`() {
        val result = original.copyWith { set.first().string = "updated" }
        result.set shouldBe setOf(Scalars("updated", 1, null))
    }

    @Test
    fun `copyWith replaces the entire set`() {
        val result = original.copyWith { set = mutableSetOf(Scalars("replaced", 2, null).toBuilder()) }
        result.set shouldBe setOf(Scalars("replaced", 2, null))
    }

    @Test
    fun `copyWith does not mutate the original set`() {
        original.copyWith { set.first().string = "updated" }
        original.set shouldBe setOf(scalarsElement)
    }

    // Nullable collection

    @Test
    fun `copyWith replaces the nullable list`() {
        val result = original.copyWith { nullable = mutableListOf(4L, 5L, 6L) }
        result.nullable shouldBe listOf(4L, 5L, 6L)
    }

    @Test
    fun `copyWith sets the nullable list to null`() {
        val result = original.copyWith { nullable = null }
        result.nullable shouldBe null
    }

    @Test
    fun `copyWith sets the nullable list to a non-null value`() {
        val original = Collections(listOf("a", "b", "c"), mapOf(DayOfWeek.MONDAY to 8), setOf(scalarsElement), null)
        val result = original.copyWith { nullable = mutableListOf(4L, 5L, 6L) }
        result.nullable shouldBe listOf(4L, 5L, 6L)
    }

    @Test
    fun `copyWith adds an element to the nullable list`() {
        val result = original.copyWith { nullable?.add(3L) }
        result.nullable shouldBe listOf(1L, 2L, 3L)
    }

    @Test
    fun `copyWith does not mutate the original nullable list`() {
        original.copyWith { nullable = mutableListOf(4L, 5L, 6L) }
        original.nullable shouldBe listOf(1L, 2L)
    }
}
