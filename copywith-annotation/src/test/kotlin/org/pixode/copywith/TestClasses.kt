package org.pixode.copywith

import java.time.DayOfWeek

@CopyWith
data class MultipleFields(
    val string: String,
    val enum: DayOfWeek,
    val copyable: Nested,
)

@CopyWith
data class NullableFields(
    val nonNullable: DayOfWeek,
    val string: String?,
    val collection: List<Int>?,
    val copyable: Nested?,
)

@CopyWith
data class ListFields(
    val simpleList: List<String> = emptyList(),
    val copyableList: List<Nested> = emptyList(),
)

@CopyWith
data class MapFields(
    val simpleList: Map<DayOfWeek, Int> = emptyMap(),
    val copyableList: Map<String, Nested> = emptyMap(),
)

@CopyWith
data class SetFields(
    val simpleSet: Set<DayOfWeek> = emptySet(),
    val copyableSet: Set<Nested> = emptySet(),
)

open class Base(val integer: Int) {
    override fun equals(other: Any?) = other is Base && integer == other.integer
    override fun hashCode() = integer
}

@CopyWith
data class GenericClass<out T : Base, U>(
    val valueT: T,
    val valueU: U?,
)

@CopyWith
data class MutableClass(
    var string: String,
    val integer: Int,
)

@CopyWith
data class Nested(
    val string: String,
    val integer: Int,
)
