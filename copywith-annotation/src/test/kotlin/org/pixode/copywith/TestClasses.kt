package org.pixode.copywith

import java.net.URI
import java.time.DayOfWeek

@CopyWith
data class Scalar(
    val string: String,
    val integer: Int,
    val nullable: String?,
)

@CopyWith
data class ListCollection(
    val list: List<String>,
    val nullable: List<Long>?,
)

@CopyWith
data class MapCollection(
    val map: Map<DayOfWeek, Int>,
)
@CopyWith
data class SetCollection(
    val set: Set<Scalar>,
)

@CopyWith
data class NestedObject(
    val alterable: Scalar,
    val nonAlterable: URI,
    val nullable: Scalar?,
)

@CopyWith
data class ObjectCollection(
    val list: List<Scalar>,
)

open class Base(val integer: Int) {
    override fun equals(other: Any?) = other is Base && integer == other.integer
    override fun hashCode() = integer
}

@CopyWith
data class Generics<out T : Base, U>(
    val valueT: T,
    val valueU: U?,
)

@CopyWith
data class Mutable(
    var string: String,
    val integer: Int,
)