package org.pixode.copywith

import io.kotest.matchers.shouldBe
import java.net.URI
import kotlin.test.Test

class NestedObjectTest {
    val scalar = Scalar(string = "hello", integer = 42, nullable = null)
    val uri = URI("http://example.com")

    // Non-nullable nested @CopyWith field

    @Test
    fun `copyWith with no changes returns an equal object`() {
        val initial = NestedObject(alterable = scalar, nonAlterable = uri, nullable = null)
        val result = initial.copyWith {}
        result shouldBe NestedObject(
            alterable = scalar,
            nonAlterable = uri,
            nullable = null
        )
    }

    @Test
    fun `copyWith modifies a field on the alterable nested object`() {
        val initial = NestedObject(alterable = scalar, nonAlterable = uri, nullable = null)
        val result = initial.copyWith { alterable.string = "world" }
        result shouldBe NestedObject(
            alterable = Scalar(string = "world", integer = 42, nullable = null),
            nonAlterable = uri,
            nullable = null
        )
    }

    @Test
    fun `copyWith replaces the alterable nested object with nullable field staying null`() {
        val initial = NestedObject(alterable = scalar, nonAlterable = uri, nullable = null)
        val result = initial.copyWith { alterable = Scalar("new", 0, "tag").toBuilder() }
        result shouldBe NestedObject(
            alterable = Scalar(string = "new", integer = 0, nullable = "tag"),
            nonAlterable = uri,
            nullable = null
        )
    }

    @Test
    fun `copyWith replaces the alterable nested object with nullable field staying non-null`() {
        val initial = NestedObject(alterable = scalar, nonAlterable = uri, nullable = scalar)
        val result = initial.copyWith { alterable = Scalar("new", 0, "tag").toBuilder() }
        result shouldBe NestedObject(
            alterable = Scalar(string = "new", integer = 0, nullable = "tag"),
            nonAlterable = uri,
            nullable = scalar
        )
    }

    // Nested non-@CopyWith field

    @Test
    fun `copyWith sets the non-alterable field`() {
        val initial = NestedObject(alterable = scalar, nonAlterable = uri, nullable = null)
        val result = initial.copyWith { nonAlterable = URI("http://other.com") }
        result shouldBe NestedObject(
            alterable = scalar,
            nonAlterable = URI("http://other.com"),
            nullable = null
        )
    }

    // Nullable nested @CopyWith field

    @Test
    fun `copyWith sets a null nullable field to a new builder`() {
        val initial = NestedObject(alterable = scalar, nonAlterable = uri, nullable = null)
        val result = initial.copyWith { nullable = Scalar("x", 1, null).toBuilder() }
        result shouldBe NestedObject(
            alterable = scalar,
            nonAlterable = uri,
            nullable = Scalar(string = "x", integer = 1, nullable = null)
        )
    }

    @Test
    fun `copyWith modifies a field on a non-null nullable nested object`() {
        val initial = NestedObject(alterable = scalar, nonAlterable = uri, nullable = scalar)
        val result = initial.copyWith { nullable?.string = "updated" }
        result shouldBe NestedObject(
            alterable = scalar,
            nonAlterable = uri,
            nullable = Scalar(string = "updated", integer = 1, nullable = null)
        )
    }

    @Test
    fun `copyWith sets a non-null nullable field to null`() {
        val initial = NestedObject(alterable = scalar, nonAlterable = uri, nullable = scalar)
        val result = initial.copyWith { nullable = null }
        result shouldBe NestedObject(
            alterable = scalar,
            nonAlterable = uri,
            nullable = null
        )
    }
}
