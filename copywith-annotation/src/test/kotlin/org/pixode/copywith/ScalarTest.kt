package org.pixode.copywith

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ScalarTest {

    private val original = Scalar(string = "initial", integer = 100, nullable = null)

    @Test
    fun `copyWith with no changes returns an equal object`() {
        val result = original.copyWith {}
        result shouldBe original
    }

    @Test
    fun `copyWith changes a scalar field with nullable staying null`() {
        val result = original.copyWith { string = "updated" }
        result shouldBe Scalar(string = "updated", integer = 100, nullable = null)
    }

    @Test
    fun `copyWith changes a scalar field with nullable staying non-null`() {
        val original = Scalar(string = "initial", integer = 100, nullable = "non-null")
        val result = original.copyWith { string = "updated" }
        result shouldBe Scalar(string = "updated", integer = 100, nullable = "non-null")
    }

    @Test
    fun `copyWith sets the nullable field to a non-null value`() {
        val result = original.copyWith { nullable = "non-null" }
        result shouldBe Scalar(string = "initial", integer = 100, nullable = "non-null")
    }

    @Test
    fun `copyWith sets the nullable field to null`() {
        val original = Scalar(string = "initial", integer = 100, nullable = "non-null")
        val result = original.copyWith { nullable = null }
        result shouldBe Scalar(string = "initial", integer = 100, nullable = null)
    }

    @Test
    fun `copyWith accesses the original values`() {
        val result = original.copyWith {
            string = "updated_$string"
            integer += 10
            nullable = string
        }
        result shouldBe Scalar(string = "updated_initial", integer = 110, nullable = "updated_initial")
    }
}
