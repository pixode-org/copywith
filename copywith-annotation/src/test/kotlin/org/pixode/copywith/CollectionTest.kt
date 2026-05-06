package org.pixode.copywith

import io.kotest.matchers.shouldBe
import java.time.DayOfWeek
import kotlin.test.Test

class CollectionTest {

    private val scalarElement = Scalar(string = "x", integer = 1, nullable = null)
    private val listOriginal = ListCollection(list = listOf("a", "b", "c"), nullable = listOf(1L, 2L))
    private val mapOriginal = MapCollection(map = mapOf(DayOfWeek.MONDAY to 8))
    private val setOriginal = SetCollection(set = setOf(scalarElement))

    @Test
    fun `copyWith with no changes returns an equal object`() {
        val result = listOriginal.copyWith {}
        result shouldBe listOriginal
    }

    // List

    @Test
    fun `copyWith adds an element to the list`() {
        val result = listOriginal.copyWith { list.add("d") }
        result.list shouldBe listOf("a", "b", "c", "d")
    }

    @Test
    fun `copyWith removes an element from the list`() {
        val result = listOriginal.copyWith { list.remove("b") }
        result.list shouldBe listOf("a", "c")
    }

    @Test
    fun `copyWith replaces an element in the list`() {
        val result = listOriginal.copyWith { list[0] = "z" }
        result.list shouldBe listOf("z", "b", "c")
    }

    @Test
    fun `copyWith replaces the entire list`() {
        val result = listOriginal.copyWith { list = mutableListOf("x", "y", "z") }
        result.list shouldBe listOf("x", "y", "z")
    }

    @Test
    fun `copyWith does not mutate the original list`() {
        listOriginal.copyWith { list.add("d") }
        listOriginal.list shouldBe listOf("a", "b", "c")
    }

    // Map

    @Test
    fun `copyWith adds an entry to the map`() {
        val result = mapOriginal.copyWith { map[DayOfWeek.TUESDAY] = 9 }
        result.map shouldBe mapOf(DayOfWeek.MONDAY to 8, DayOfWeek.TUESDAY to 9)
    }

    @Test
    fun `copyWith modifies an existing entry in the map`() {
        val result = mapOriginal.copyWith { map[DayOfWeek.MONDAY] = 10 }
        result.map shouldBe mapOf(DayOfWeek.MONDAY to 10)
    }

    @Test
    fun `copyWith replaces the entire map`() {
        val result = mapOriginal.copyWith { map = mutableMapOf(DayOfWeek.FRIDAY to 5) }
        result.map shouldBe mapOf(DayOfWeek.FRIDAY to 5)
    }

    @Test
    fun `copyWith does not mutate the original map`() {
        mapOriginal.copyWith { map[DayOfWeek.TUESDAY] = 9 }
        mapOriginal.map shouldBe mapOf(DayOfWeek.MONDAY to 8)
    }

    // Set of @CopyWith elements

    @Test
    fun `copyWith adds an entry to the set`() {
        val result = setOriginal.copyWith { set.add(Scalar("added", 3, "set").toBuilder()) }
        result.set shouldBe setOf(scalarElement, Scalar("added", 3, "set"))
    }

    @Test
    fun `copyWith modifies a field on an element in the set`() {
        val result = setOriginal.copyWith { set.first().string = "updated" }
        result.set shouldBe setOf(Scalar("updated", 1, null))
    }

    @Test
    fun `copyWith replaces the entire set`() {
        val result = setOriginal.copyWith { set = mutableSetOf(Scalar("replaced", 2, null).toBuilder()) }
        result.set shouldBe setOf(Scalar("replaced", 2, null))
    }

    @Test
    fun `copyWith does not mutate the original set`() {
        setOriginal.copyWith { set.first().string = "updated" }
        setOriginal.set shouldBe setOf(scalarElement)
    }

    // Nullable collection

    @Test
    fun `copyWith replaces the nullable list`() {
        val result = listOriginal.copyWith { nullable = mutableListOf(4L, 5L, 6L) }
        result.nullable shouldBe listOf(4L, 5L, 6L)
    }

    @Test
    fun `copyWith sets the nullable list to null`() {
        val result = listOriginal.copyWith { nullable = null }
        result.nullable shouldBe null
    }

    @Test
    fun `copyWith sets the nullable list to a non-null value`() {
        val original = ListCollection(listOf("a", "b", "c"), null)
        val result = original.copyWith { nullable = mutableListOf(4L, 5L, 6L) }
        result.nullable shouldBe listOf(4L, 5L, 6L)
    }

    @Test
    fun `copyWith adds an element to the nullable list`() {
        val result = listOriginal.copyWith { nullable?.add(3L) }
        result.nullable shouldBe listOf(1L, 2L, 3L)
    }

    @Test
    fun `copyWith does not mutate the original nullable list`() {
        listOriginal.copyWith { nullable = mutableListOf(4L, 5L, 6L) }
        listOriginal.nullable shouldBe listOf(1L, 2L)
    }
}
