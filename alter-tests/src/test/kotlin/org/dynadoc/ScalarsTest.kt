package org.dynadoc

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ScalarsTest {

    private val original = Scalars(string = "hello", integer = 42, nullable = null)

    @Test
    fun `alter with no changes returns an equal object`() {
        val result = original.alter {}
        result shouldBe original
    }

    @Test
    fun `alter changes the string field`() {
        val result = original.alter { string = "world" }
        result.string shouldBe "world"
    }

    @Test
    fun `alter changes the integer field`() {
        val result = original.alter { integer = 99 }
        result.integer shouldBe 99
    }

    @Test
    fun `alter sets the nullable field`() {
        val result = original.alter { nullable = "tag" }
        result.nullable shouldBe "tag"
    }

    @Test
    fun `alter leaves the nullable field as null when unset`() {
        val result = original.alter { string = "world" }
        result.nullable.shouldBeNull()
    }

    @Test
    fun `alter does not change unmodified fields`() {
        val result = original.alter { string = "world" }
        result.integer shouldBe original.integer
        result.nullable shouldBe original.nullable
    }

    @Test
    fun `alter does not mutate the original`() {
        original.alter { string = "world"; integer = 99 }
        original.string shouldBe "hello"
        original.integer shouldBe 42
    }

    @Test
    fun `toBuilder round-trips the object unchanged`() {
        val result = original.toBuilder().build()
        result shouldBe original
    }

    @Test
    fun `toBuilder allows modification before build`() {
        val result = original.toBuilder().apply { integer = 0 }.build()
        result.integer shouldBe 0
        result.string shouldBe original.string
    }
}
