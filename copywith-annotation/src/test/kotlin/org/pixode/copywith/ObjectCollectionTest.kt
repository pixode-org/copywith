package org.pixode.copywith

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ObjectCollectionTest {

    val scalar = Scalar(string = "x", integer = 1, nullable = null)

    @Test
    fun `copyWith with no changes returns an equal object`() {
        val initial = ObjectCollection(list = listOf(scalar))
        val result = initial.copyWith {}
        result shouldBe ObjectCollection(list = listOf(scalar))
    }

    @Test
    fun `copyWith adds an element to the list`() {
        val initial = ObjectCollection(list = listOf(scalar))
        val result = initial.copyWith { list.add(Scalar("added", 2, null).toBuilder()) }
        result shouldBe ObjectCollection(list = listOf(scalar, Scalar(string = "added", integer = 2, nullable = null)))
    }

    @Test
    fun `copyWith removes an element from the list`() {
        val initial = ObjectCollection(list = listOf(scalar, Scalar(string = "y", integer = 2, nullable = null)))
        val result = initial.copyWith { list.removeAt(0) }
        result shouldBe ObjectCollection(list = listOf(Scalar(string = "y", integer = 2, nullable = null)))
    }

    @Test
    fun `copyWith replaces an element in the list`() {
        val initial = ObjectCollection(list = listOf(scalar))
        val result = initial.copyWith { list[0] = Scalar("replaced", 9, null).toBuilder() }
        result shouldBe ObjectCollection(list = listOf(Scalar(string = "replaced", integer = 9, nullable = null)))
    }

    @Test
    fun `copyWith modifies a field on an element in the list`() {
        val initial = ObjectCollection(list = listOf(scalar))
        val result = initial.copyWith { list[0].string = "updated" }
        result shouldBe ObjectCollection(list = listOf(Scalar(string = "updated", integer = 1, nullable = null)))
    }

    @Test
    fun `copyWith replaces the entire list`() {
        val initial = ObjectCollection(list = listOf(scalar))
        val result = initial.copyWith { list = mutableListOf(Scalar("new", 0, "tag").toBuilder()) }
        result shouldBe ObjectCollection(list = listOf(Scalar(string = "new", integer = 0, nullable = "tag")))
    }

    @Test
    fun `copyWith does not mutate the original list`() {
        val initial = ObjectCollection(list = listOf(scalar))
        initial.copyWith { list.add(Scalar("added", 2, null).toBuilder()) }
        initial shouldBe ObjectCollection(list = listOf(scalar))
    }

    @Test
    fun `copyWith does not mutate the original element`() {
        val initial = ObjectCollection(list = listOf(scalar))
        initial.copyWith { list[0].string = "updated" }
        initial shouldBe ObjectCollection(list = listOf(scalar))
    }
}
