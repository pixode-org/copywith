package org.pixode.copywith

import io.kotest.matchers.shouldBe
import java.time.DayOfWeek
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

    @Test
    fun `copyWith adds an entry to the map`() {
        val initial = MapFields(
            simpleMap = mapOf(DayOfWeek.MONDAY to 1, DayOfWeek.TUESDAY to 2),
            copyableMap = mapOf("A" to Nested("aaa", 1), "B" to Nested("bbb", 2))
        )
        val result = initial.copyWith {
            simpleMap.put(DayOfWeek.WEDNESDAY, 3)
            copyableMap.put("C", Nested("ccc", 3))
        }
        result shouldBe MapFields(
            simpleMap = mapOf(DayOfWeek.MONDAY to 1, DayOfWeek.TUESDAY to 2, DayOfWeek.WEDNESDAY to 3),
            copyableMap = mapOf("A" to Nested("aaa", 1), "B" to Nested("bbb", 2), "C" to Nested("ccc", 3))
        )
    }

    @Test
    fun `copyWith adds multiple entries to the map`() {
        val initial = MapFields(
            simpleMap = mapOf(DayOfWeek.MONDAY to 1, DayOfWeek.TUESDAY to 2),
            copyableMap = mapOf("A" to Nested("aaa", 1), "B" to Nested("bbb", 2))
        )
        val result = initial.copyWith {
            simpleMap.putAll(mapOf(DayOfWeek.WEDNESDAY to 3, DayOfWeek.THURSDAY to 4))
            copyableMap.putAll(mapOf("C" to Nested("ccc", 3), "D" to Nested("ddd", 4)))
        }
        result shouldBe MapFields(
            simpleMap = mapOf(DayOfWeek.MONDAY to 1, DayOfWeek.TUESDAY to 2, DayOfWeek.WEDNESDAY to 3, DayOfWeek.THURSDAY to 4),
            copyableMap = mapOf("A" to Nested("aaa", 1), "B" to Nested("bbb", 2), "C" to Nested("ccc", 3), "D" to Nested("ddd", 4))
        )
    }

    @Test
    fun `copyWith removes an entry from the map`() {
        val initial = MapFields(
            simpleMap = mapOf(DayOfWeek.MONDAY to 1, DayOfWeek.TUESDAY to 2),
            copyableMap = mapOf("A" to Nested("aaa", 1), "B" to Nested("bbb", 2))
        )
        val result = initial.copyWith {
            simpleMap.remove(DayOfWeek.TUESDAY)
            copyableMap.remove("A")
        }
        result shouldBe MapFields(
            simpleMap = mapOf(DayOfWeek.MONDAY to 1),
            copyableMap = mapOf("B" to Nested("bbb", 2))
        )
    }

    @Test
    fun `copyWith replaces an entry in the map and does not mutate the original map`() {
        val initial = MapFields(
            simpleMap = mapOf(DayOfWeek.MONDAY to 1, DayOfWeek.TUESDAY to 2),
            copyableMap = mapOf("A" to Nested("aaa", 1), "B" to Nested("bbb", 2))
        )
        val result = initial.copyWith {
            simpleMap[DayOfWeek.TUESDAY] = 99
            copyableMap["A"] = Nested("ccc", 3)
        }
        result shouldBe MapFields(
            simpleMap = mapOf(DayOfWeek.MONDAY to 1, DayOfWeek.TUESDAY to 99),
            copyableMap = mapOf("A" to Nested("ccc", 3), "B" to Nested("bbb", 2))
        )
        initial shouldBe MapFields(
            simpleMap = mapOf(DayOfWeek.MONDAY to 1, DayOfWeek.TUESDAY to 2),
            copyableMap = mapOf("A" to Nested("aaa", 1), "B" to Nested("bbb", 2))
        )
    }

    @Test
    fun `copyWith modifies an entry in the map and does not mutate the original map`() {
        val initial = MapFields(
            simpleMap = mapOf(DayOfWeek.MONDAY to 1, DayOfWeek.TUESDAY to 2),
            copyableMap = mapOf("A" to Nested("aaa", 1), "B" to Nested("bbb", 2))
        )
        val result = initial.copyWith {
            copyableMap["A"]?.string = "ccc"
        }
        result shouldBe MapFields(
            simpleMap = mapOf(DayOfWeek.MONDAY to 1, DayOfWeek.TUESDAY to 2),
            copyableMap = mapOf("A" to Nested("ccc", 1), "B" to Nested("bbb", 2))
        )
        initial shouldBe MapFields(
            simpleMap = mapOf(DayOfWeek.MONDAY to 1, DayOfWeek.TUESDAY to 2),
            copyableMap = mapOf("A" to Nested("aaa", 1), "B" to Nested("bbb", 2))
        )
    }

    @Test
    fun `copyWith replaces the entire map`() {
        val initial = MapFields(
            simpleMap = mapOf(DayOfWeek.MONDAY to 1, DayOfWeek.TUESDAY to 2),
            copyableMap = mapOf("A" to Nested("aaa", 1), "B" to Nested("bbb", 2))
        )
        val result = initial.copyWith {
            simpleMap = mutableMapOf(DayOfWeek.WEDNESDAY to 3)
            copyableMap = mutableMapOf("C" to Nested("ccc", 3).toBuilder())
        }
        result shouldBe MapFields(
            simpleMap = mapOf(DayOfWeek.WEDNESDAY to 3),
            copyableMap = mapOf("C" to Nested("ccc", 3))
        )
    }

    // Set

    @Test
    fun `copyWith adds an element to the set`() {
        val initial = SetFields(
            simpleSet = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
            copyableSet = setOf(Nested("aaa", 1), Nested("bbb", 2))
        )
        val result = initial.copyWith {
            simpleSet.add(DayOfWeek.WEDNESDAY)
            copyableSet.add(Nested("ccc", 3))
        }
        result shouldBe SetFields(
            simpleSet = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY),
            copyableSet = setOf(Nested("aaa", 1), Nested("bbb", 2), Nested("ccc", 3))
        )
    }

    @Test
    fun `copyWith adds multiple elements to the set`() {
        val initial = SetFields(
            simpleSet = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
            copyableSet = setOf(Nested("aaa", 1), Nested("bbb", 2))
        )
        val result = initial.copyWith {
            simpleSet.addAll(setOf(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY))
            copyableSet.addAll(setOf(Nested("ccc", 3), Nested("ddd", 4)))
        }
        result shouldBe SetFields(
            simpleSet = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY),
            copyableSet = setOf(Nested("aaa", 1), Nested("bbb", 2), Nested("ccc", 3), Nested("ddd", 4))
        )
    }

    @Test
    fun `copyWith modifies an element in the set and does not mutate the original set`() {
        val initial = SetFields(
            simpleSet = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
            copyableSet = setOf(Nested("aaa", 1), Nested("bbb", 2))
        )
        val result = initial.copyWith {
            copyableSet.first { it.string == "aaa" }.string = "ccc"
        }
        result shouldBe SetFields(
            simpleSet = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
            copyableSet = setOf(Nested("ccc", 1), Nested("bbb", 2))
        )
        initial shouldBe SetFields(
            simpleSet = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
            copyableSet = setOf(Nested("aaa", 1), Nested("bbb", 2))
        )
    }

    @Test
    fun `copyWith replaces the entire set`() {
        val initial = SetFields(
            simpleSet = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
            copyableSet = setOf(Nested("aaa", 1), Nested("bbb", 2))
        )
        val result = initial.copyWith {
            simpleSet = mutableSetOf(DayOfWeek.FRIDAY)
            copyableSet = mutableSetOf(Nested("ccc", 3).toBuilder())
        }
        result shouldBe SetFields(
            simpleSet = setOf(DayOfWeek.FRIDAY),
            copyableSet = setOf(Nested("ccc", 3))
        )
    }
}
