package org.pixode.copywith

import io.kotest.matchers.shouldBe
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.TUESDAY
import kotlin.test.Test

class NullableFieldsTest {

    @Test
    fun `copyWith preserves null field`() {
        val initial = NullableFields(
            nonNullable = MONDAY,
            string = null,
            collection = null,
            copyable = null,
        )
        val result = initial.copyWith { nonNullable = TUESDAY }
        result shouldBe NullableFields(
            nonNullable = TUESDAY,
            string = null,
            collection = null,
            copyable = null,
        )
    }

    @Test
    fun `copyWith preserves non-null field`() {
        val initial = NullableFields(
            nonNullable = MONDAY,
            string = "initial",
            collection = listOf(1, 2, 3),
            copyable = Nested("aaa", 1),
        )
        val result = initial.copyWith { nonNullable = TUESDAY }
        result shouldBe NullableFields(
            nonNullable = TUESDAY,
            string = "initial",
            collection = listOf(1, 2, 3),
            copyable = Nested("aaa", 1),
        )
    }

    @Test
    fun `copyWith sets nullable field to a non-null value`() {
        val initial = NullableFields(
            nonNullable = MONDAY,
            string = null,
            collection = null,
            copyable = null,
        )
        val result = initial.copyWith {
            string = "updated"
            collection = mutableListOf(1, 2, 3)
            copyable = Nested("aaa", 1).toBuilder()
        }
        result shouldBe NullableFields(
            nonNullable = MONDAY,
            string = "updated",
            collection = listOf(1, 2, 3),
            copyable = Nested("aaa", 1),
        )
    }

    @Test
    fun `copyWith sets nullable field to null`() {
        val initial = NullableFields(
            nonNullable = MONDAY,
            string = "initial",
            collection = listOf(1, 2, 3),
            copyable = Nested("aaa", 1),
        )
        val result = initial.copyWith {
            string = null
            collection = null
            copyable = null
        }
        result shouldBe NullableFields(
            nonNullable = MONDAY,
            string = null,
            collection = null,
            copyable = null,
        )
    }

    @Test
    fun `copyWith modified nested nullable field`() {
        val initial = NullableFields(
            nonNullable = MONDAY,
            string = "initial",
            collection = listOf(1, 2, 3),
            copyable = Nested("aaa", 1),
        )
        val result = initial.copyWith {
            copyable?.string = "bbb"
        }
        result shouldBe NullableFields(
            nonNullable = MONDAY,
            string = "initial",
            collection = listOf(1, 2, 3),
            copyable = Nested("bbb", 1),
        )
    }

    @Test
    fun `copyWith modified nested collection`() {
        val initial = NullableFields(
            nonNullable = MONDAY,
            string = "initial",
            collection = listOf(1, 2, 3),
            copyable = Nested("aaa", 1),
        )
        val result = initial.copyWith {
            collection?.add(4)
            collection?.remove(2)
        }
        result shouldBe NullableFields(
            nonNullable = MONDAY,
            string = "initial",
            collection = listOf(1, 3, 4),
            copyable = Nested("aaa", 1),
        )
    }
}
