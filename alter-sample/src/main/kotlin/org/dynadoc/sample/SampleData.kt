package org.dynadoc.sample

import org.dynadoc.Alter

@Alter
data class Person(
    val name: String,
    val age: Int,
    val email: String?,
    val friends: List<String>,
    val relatives: Map<String, String>,
    val address: Address,
    val secondaryAddresses: List<Address>,
)

@Alter
data class Config(
    val host: String,
    val port: Int,
    val tags: Set<String>,
    val properties: Map<String, String>,
)

@Alter
data class Address(
    val street: String,
    val house: Int
)

fun main() {
    val alice = Person(
        name = "Alice",
        age = 30,
        email = null,
        friends = listOf("Bob", "Carol"),
        relatives = mapOf("mother" to "Therese", "father" to "Bob"),
        address = Address("Baker st", 15),
        secondaryAddresses = listOf(
            Address("Downing st", 11)
        )
    )
    val olderAlice = alice.alter { age = 31 }
    val aliceWithEmail = alice.alter {
        email = "alice@example.com"
        secondaryAddresses[0].house = 22
        relatives["mother"] = "Claire"
    }
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