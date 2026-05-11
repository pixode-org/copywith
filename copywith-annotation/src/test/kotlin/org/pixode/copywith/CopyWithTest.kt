package org.pixode.copywith

import io.kotest.matchers.shouldBe
import java.time.DayOfWeek.MONDAY
import kotlin.test.Test

class CopyWithTest {

    @Test
    fun `copyWith with no changes returns an equal object`() {
        val initial = MultipleFields(
            string = "initial",
            enum = MONDAY,
            copyable = Nested("aaa", 1),
        )
        val result = initial.copyWith { }
        result shouldBe MultipleFields(
            string = "initial",
            enum = MONDAY,
            copyable = Nested("aaa", 1),
        )
    }

    @Test
    fun `copyWith modifies one field`() {
        val initial = MultipleFields(
            string = "initial",
            enum = MONDAY,
            copyable = Nested("aaa", 1),
        )
        val result = initial.copyWith { string = "updated" }
        result shouldBe MultipleFields(
            string = "updated",
            enum = MONDAY,
            copyable = Nested("aaa", 1),
        )
    }

    @Test
    fun `copyWith modifies nested field`() {
        val initial = MultipleFields(
            string = "initial",
            enum = MONDAY,
            copyable = Nested("aaa", 1),
        )
        val result = initial.copyWith { copyable.string = "bbb" }
        result shouldBe MultipleFields(
            string = "initial",
            enum = MONDAY,
            copyable = Nested("bbb", 1),
        )
    }

    @Test
    fun `copyWith sets nested field`() {
        val initial = MultipleFields(
            string = "initial",
            enum = MONDAY,
            copyable = Nested("aaa", 1),
        )
        val result = initial.copyWith { copyable = Nested("bbb", 2).toBuilder() }
        result shouldBe MultipleFields(
            string = "initial",
            enum = MONDAY,
            copyable = Nested("bbb", 2),
        )
    }

    @Test
    fun `copyWith modifies multiple fields`() {
        val initial = MultipleFields(
            string = "initial",
            enum = MONDAY,
            copyable = Nested("aaa", 1),
        )
        val result = initial.copyWith {
            string = copyable.string
            copyable.string = enum.name
        }
        result shouldBe MultipleFields(
            string = "aaa",
            enum = MONDAY,
            copyable = Nested("MONDAY", 1),
        )
    }

    @Test
    fun `copyWith does not affect the initial value`() {
        val initial = MutableClass(string = "initial", integer = 100)
        val result = initial.copyWith { string = "updated" }
        result shouldBe MutableClass(string = "updated", integer = 100)
        initial shouldBe MutableClass(string = "initial", integer = 100)
    }

    @Test
    fun `copyWith generates fields with reserved names`() {
        val initial = ReservedKeywordsFields(
            `in` = "in",
            `return` = "return",
            `val` = "val",
            `true` = "true",
            `name with spaces` = "name with spaces",
        )
        val result = initial.copyWith {
            `name with spaces` = "updated"
        }
        result shouldBe ReservedKeywordsFields(
            `in` = "in",
            `return` = "return",
            `val` = "val",
            `true` = "true",
            `name with spaces` = "updated",
        )
    }
}
