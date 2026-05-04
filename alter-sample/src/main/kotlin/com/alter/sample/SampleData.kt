package com.alter.sample

import com.alter.Alter

@Alter
data class Person(
    val name: String,
    val age: Int,
    val email: String? = null,
)

@Alter
data class Box<T>(
    val value: T,
    val label: String,
)

fun main() {
    val alice = Person(name = "Alice", age = 30)
    val olderAlice = alice.alter(age = 31)
    val aliceWithEmail = alice.alter(email = "alice@example.com")

    println(alice)
    println(olderAlice)
    println(aliceWithEmail)

    val box = Box(value = 42, label = "answer")
    val renamedBox = box.alter(label = "the answer")
    println(box)
    println(renamedBox)
}
