package org.pixode.copywith

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ScalarsTest {

    private val original = Scalars(string = "initial", integer = 100, nullable = null)

    @Test
    fun `copyWith with no changes returns an equal object`() {
        val result = original.copyWith {}
        result shouldBe original
    }

    @Test
    fun `copyWith changes a scalar field with nullable staying null`() {
        val result = original.copyWith { string = "updated" }
        result shouldBe Scalars(string = "updated", integer = 100, nullable = null)
    }

    @Test
    fun `copyWith changes a scalar field with nullable staying non-null`() {
        val original = Scalars(string = "initial", integer = 100, nullable = "non-null")
        val result = original.copyWith { string = "updated" }
        result shouldBe Scalars(string = "updated", integer = 100, nullable = "non-null")
    }

    @Test
    fun `copyWith sets the nullable field to a non-null value`() {
        val result = original.copyWith { nullable = "non-null" }
        result shouldBe Scalars(string = "initial", integer = 100, nullable = "non-null")
    }

    @Test
    fun `copyWith sets the nullable field to null`() {
        val original = Scalars(string = "initial", integer = 100, nullable = "non-null")
        val result = original.copyWith { nullable = null }
        result shouldBe Scalars(string = "initial", integer = 100, nullable = null)
    }
}
