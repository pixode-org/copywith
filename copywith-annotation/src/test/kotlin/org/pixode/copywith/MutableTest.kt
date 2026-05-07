package org.pixode.copywith

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MutableTest {

    @Test
    fun `copyWith does not affect the initial value`() {
        val initial = Mutable(string = "initial", integer = 100)
        val result = initial.copyWith { string = "updated" }
        result shouldBe Mutable(string = "updated", integer = 100)
        initial shouldBe Mutable(string = "initial", integer = 100)
    }
}
