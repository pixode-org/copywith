package org.dynadoc.sample

import org.dynadoc.Alter

@Alter
data class Person(
    val name: String,
    val age: Int,
    val email: String?,
    val friends: List<String>,
)

@Alter
data class Config(
    val host: String,
    val port: Int,
    val tags: Set<String>,
    val properties: Map<String, String>,
)

fun main() {
    val alice = Person(name = "Alice", age = 30, email = null, friends = listOf("Bob", "Carol"))
    val olderAlice = alice.alter { age = 31 }
    val aliceWithEmail = alice.alter { email = "alice@example.com" }
    val aliceNewFriends = alice.alter { friends.add("Dave") }

    println(alice)
    println(olderAlice)
    println(aliceWithEmail)
    println(aliceNewFriends)

    val config = Config(host = "localhost", port = 8080, tags = setOf("prod"), properties = mapOf("k" to "v"))
    val remoteConfig = config.alter {
        host = "example.com"
        tags.add("remote")
        properties["k"] = "v2"
    }
    println(config)
    println(remoteConfig)
}