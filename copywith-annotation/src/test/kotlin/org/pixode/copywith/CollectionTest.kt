package org.pixode.copywith

import io.kotest.matchers.shouldBe
import java.time.DayOfWeek
import kotlin.test.Test

class CollectionTest {

    @Test
    fun `copyWith with no changes returns an equal object`() {
        val initial = ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
        val result = initial.copyWith {}
        result shouldBe ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
    }

    // List

    @Test
    fun `copyWith adds an element to the list`() {
        val initial = ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
        val result = initial.copyWith { list.add("d") }
        result shouldBe ListCollection(list = listOf("a", "b", "c", "d"), nullable = listOf(1L, 2L))
    }

    @Test
    fun `copyWith removes an element from the list`() {
        val initial = ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
        val result = initial.copyWith { list.remove("b") }
        result shouldBe ListCollection(list = listOf("a", "c"), nullable = listOf(1L, 2L))
    }

    @Test
    fun `copyWith replaces an element in the list`() {
        val initial = ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
        val result = initial.copyWith { list[0] = "z" }
        result shouldBe ListCollection(list = listOf("z", "b", "c"), nullable = listOf(1L, 2L))
    }

    @Test
    fun `copyWith replaces the entire list`() {
        val initial = ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
        val result = initial.copyWith { list = mutableListOf("x", "y", "z") }
        result shouldBe ListCollection(list = listOf("x", "y", "z"), nullable = listOf(1L, 2L))
    }

    @Test
    fun `copyWith does not mutate the original list`() {
        val initial = ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
        initial.copyWith { list.add("d") }
        initial shouldBe ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
    }

    // Map

    @Test
    fun `copyWith adds an entry to the map`() {
        val initial = MapCollection(map = mapOf(DayOfWeek.MONDAY to 8))
        val result = initial.copyWith { map[DayOfWeek.TUESDAY] = 9 }
        result shouldBe MapCollection(map = mapOf(DayOfWeek.MONDAY to 8, DayOfWeek.TUESDAY to 9))
    }

    @Test
    fun `copyWith modifies an existing entry in the map`() {
        val initial = MapCollection(map = mapOf(DayOfWeek.MONDAY to 8))
        val result = initial.copyWith { map[DayOfWeek.MONDAY] = 10 }
        result shouldBe MapCollection(map = mapOf(DayOfWeek.MONDAY to 10))
    }

    @Test
    fun `copyWith replaces the entire map`() {
        val initial = MapCollection(map = mapOf(DayOfWeek.MONDAY to 8))
        val result = initial.copyWith { map = mutableMapOf(DayOfWeek.FRIDAY to 5) }
        result shouldBe MapCollection(map = mapOf(DayOfWeek.FRIDAY to 5))
    }

    @Test
    fun `copyWith does not mutate the original map`() {
        val initial = MapCollection(map = mapOf(DayOfWeek.MONDAY to 8))
        initial.copyWith { map[DayOfWeek.TUESDAY] = 9 }
        initial shouldBe MapCollection(map = mapOf(DayOfWeek.MONDAY to 8))
    }

    // Set of @CopyWith elements

    @Test
    fun `copyWith adds an entry to the set`() {
        val initial = SetCollection(set = setOf(Scalar(string = "x", integer = 1, nullable = null)))
        val result = initial.copyWith { set.add(Scalar("added", 3, "set").toBuilder()) }
        result shouldBe SetCollection(
            set = setOf(
                Scalar(string = "x", integer = 1, nullable = null),
                Scalar(string = "added", integer = 3, nullable = "set")
            )
        )
    }

    @Test
    fun `copyWith modifies a field on an element in the set`() {
        val initial = SetCollection(set = setOf(Scalar(string = "x", integer = 1, nullable = null)))
        val result = initial.copyWith { set.first().string = "updated" }
        result shouldBe SetCollection(
            set = setOf(Scalar(string = "updated", integer = 1, nullable = null))
        )
    }

    @Test
    fun `copyWith replaces the entire set`() {
        val initial = SetCollection(set = setOf(Scalar(string = "x", integer = 1, nullable = null)))
        val result = initial.copyWith { set = mutableSetOf(Scalar("replaced", 2, null).toBuilder()) }
        result shouldBe SetCollection(
            set = setOf(Scalar(string = "replaced", integer = 2, nullable = null))
        )
    }

    @Test
    fun `copyWith does not mutate the original set`() {
        val initial = SetCollection(set = setOf(Scalar(string = "x", integer = 1, nullable = null)))
        initial.copyWith { set.first().string = "updated" }
        initial shouldBe SetCollection(
            set = setOf(Scalar(string = "x", integer = 1, nullable = null))
        )
    }

    // Nullable collection

    @Test
    fun `copyWith replaces the nullable list`() {
        val initial = ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
        val result = initial.copyWith { nullable = mutableListOf(4L, 5L, 6L) }
        result shouldBe ListCollection(list = listOf("a", "b", "c"), nullable = listOf(4L, 5L, 6L))
    }

    @Test
    fun `copyWith sets the nullable list to null`() {
        val initial = ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
        val result = initial.copyWith { nullable = null }
        result shouldBe ListCollection(list = listOf("a", "b", "c"), nullable = null)
    }

    @Test
    fun `copyWith sets the nullable list to a non-null value`() {
        val initial = ListCollection(list = listOf("a", "b", "c"), nullable = null)
        val result = initial.copyWith { nullable = mutableListOf(4L, 5L, 6L) }
        result shouldBe ListCollection(list = listOf("a", "b", "c"), nullable = listOf(4L, 5L, 6L))
    }

    @Test
    fun `copyWith adds an element to the nullable list`() {
        val initial = ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
        val result = initial.copyWith { nullable?.add(3L) }
        result shouldBe ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L, 3L))
    }

    @Test
    fun `copyWith does not mutate the original nullable list`() {
        val initial = ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
        initial.copyWith { nullable = mutableListOf(4L, 5L, 6L) }
        initial shouldBe ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
    }
}
