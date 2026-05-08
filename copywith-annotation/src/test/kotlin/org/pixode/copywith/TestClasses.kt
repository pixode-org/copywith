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
    val simpleList: List<String>,
    val copyableList: List<Nested>,
)

@CopyWith
data class MapFields(
    val simpleMap: Map<DayOfWeek, Int>,
    val copyableMap: Map<String, Nested>,
)

@CopyWith
data class SetFields(
    val simpleSet: Set<DayOfWeek>,
    val copyableSet: Set<Nested>,
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
