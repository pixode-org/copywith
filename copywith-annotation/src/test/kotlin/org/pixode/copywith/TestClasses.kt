package org.pixode.copywith

import java.net.URI
import java.time.DayOfWeek

@Alter
data class Scalars(
    val string: String,
    val integer: Int,
    val nullable: String?
)

@Alter
data class Collections(
    val list: List<String>,
    val map: Map<DayOfWeek, Int>,
    val set: Set<Scalars>,
    val nullable: List<Long>?,
)

@Alter
data class NestedObjects(
    val alterable: Scalars,
    val nonAlterable: URI,
    val nullable: Scalars?,
)
