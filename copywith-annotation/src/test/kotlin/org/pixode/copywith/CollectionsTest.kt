package org.pixode.copywith

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CollectionsTest {
    @Test
    fun `copyWith with no changes returns an equal object`() {
        val initial = ListFields(simpleList = listOf("a", "b", "c"), copyableList = listOf(Nested("aaa", 1)))
        val result = initial.copyWith { }
        result shouldBe ListFields(simpleList = listOf("a", "b", "c"), copyableList = listOf(Nested("aaa", 1)))
    }

    // List

    @Test
    fun `copyWith adds an element to the list`() {
        val initial = ListFields(
            simpleList = listOf("a", "b", "c"),
            copyableList = listOf(Nested("aaa", 1), Nested("bbb", 2))
        )
        val result = initial.copyWith {
            simpleList.add("d")
            copyableList.add(Nested("ccc", 3))
        }
        result shouldBe ListFields(
            simpleList = listOf("a", "b", "c", "d"),
            copyableList = listOf(Nested("aaa", 1), Nested("bbb", 2), Nested("ccc", 3))
        )
    }

    @Test
    fun `copyWith adds multiple elements to the list`() {
        val initial = ListFields(
            simpleList = listOf("a", "b", "c"),
            copyableList = listOf(Nested("aaa", 1), Nested("bbb", 2))
        )
        val result = initial.copyWith {
            simpleList.addAll(listOf("d", "e"))
            copyableList.addAll(listOf(Nested("ccc", 3), Nested("ddd", 4)))
        }
        result shouldBe ListFields(
            simpleList = listOf("a", "b", "c", "d", "e"),
            copyableList = listOf(Nested("aaa", 1), Nested("bbb", 2), Nested("ccc", 3), Nested("ddd", 4))
        )
    }

    @Test
    fun `copyWith removes an element from the list`() {
        val initial = ListFields(
            simpleList = listOf("a", "b", "c"),
            copyableList = listOf(Nested("aaa", 1), Nested("bbb", 2))
        )
        val result = initial.copyWith {
            simpleList.removeAt(1)
            copyableList.removeAt(0)
        }
        result shouldBe ListFields(
            simpleList = listOf("a", "c"),
            copyableList = listOf(Nested("bbb", 2))
        )
    }

    @Test
    fun `copyWith replaces an element in the list and does not mutate the original list`() {
        val initial = ListFields(
            simpleList = listOf("a", "b", "c"),
            copyableList = listOf(Nested("aaa", 1), Nested("bbb", 2))
        )
        val result = initial.copyWith {
            simpleList[1] = "d"
            copyableList[0] = Nested("ccc", 3)
        }
        result shouldBe ListFields(
            simpleList = listOf("a", "d", "c"),
            copyableList = listOf(Nested("ccc", 3), Nested("bbb", 2))
        )
        initial shouldBe ListFields(
            simpleList = listOf("a", "b", "c"),
            copyableList = listOf(Nested("aaa", 1), Nested("bbb", 2))
        )
    }

    @Test
    fun `copyWith modifies an element in the list and does not mutate the original list`() {
        val initial = ListFields(
            simpleList = listOf("a", "b", "c"),
            copyableList = listOf(Nested("aaa", 1), Nested("bbb", 2))
        )
        val result = initial.copyWith {
            copyableList[0].string = "ccc"
        }
        result shouldBe ListFields(
            simpleList = listOf("a", "b", "c"),
            copyableList = listOf(Nested("ccc", 1), Nested("bbb", 2))
        )
        initial shouldBe ListFields(
            simpleList = listOf("a", "b", "c"),
            copyableList = listOf(Nested("aaa", 1), Nested("bbb", 2))
        )
    }

    @Test
    fun `copyWith replaces the entire list`() {
        val initial = ListFields(
            simpleList = listOf("a", "b", "c"),
            copyableList = listOf(Nested("aaa", 1), Nested("bbb", 2))
        )
        val result = initial.copyWith {
            simpleList = listOf("d", "e").toMutableList()
            copyableList = listOf(Nested("ccc", 3).toBuilder()).toMutableList()
        }
        result shouldBe ListFields(
            simpleList = listOf("d", "e"),
            copyableList = listOf(Nested("ccc", 3))
        )
    }

    // Map


    // Set
}
