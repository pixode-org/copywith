package org.pixode.copywith

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class GenericsTest {

    @Test
    fun `copyWith with no changes returns an equal object`() {
        val initial = Generics(valueT = Base(integer = 1), valueU = "aaa")
        val result = initial.copyWith {}
        result shouldBe initial
    }

    @Test
    fun `copyWith changes the value field`() {
        val initial = Generics(valueT = Base(integer = 1), valueU = "aaa")
        val result = initial.copyWith { valueT = Base(integer = 2) }
        result shouldBe Generics(valueT = Base(integer = 2), valueU = "aaa")
    }

    @Test
    fun `copyWith sets nullable field to null`() {
        val initial = Generics(valueT = Base(integer = 1), valueU = "aaa")
        val result = initial.copyWith { valueU = null }
        result shouldBe Generics(valueT = Base(integer = 1), valueU = null)
    }
}
