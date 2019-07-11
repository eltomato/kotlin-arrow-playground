package it.eltomato.playground

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.data.ListK
import arrow.data.k
import arrow.instances.listk.semigroup.semigroup
import arrow.instances.option.semigroup.semigroup
import arrow.instances.option.semigroupK.semigroupK
import arrow.instances.semigroup

fun main() {
    println(semigroup(Some(1), Some(2)))
    println(semigroup(None, Some(2)))
    println("---")
    println(semigroupk(Some(1), Some(2)))
    println(semigroupk(None, Some(2)))
    println("---")
    println(optionSemigroupList())
    println("---")
    println(some())
}

val semigroup: (Option<Int>, Option<Int>) -> Option<Int> = { first, second ->
    Option.semigroup(Int.semigroup()).run {
        first.combine(second)
    }
}

val semigroupk: (Option<Int>, Option<Int>) -> Option<Int> = { first, second ->
    Option.semigroupK().run {
        first.combineK(second)
    }
}

val optionSemigroupList = {
    Option.semigroup(ListK.semigroup<String>()).run {
        Some(listOf("1", "2").k()).combine(Some(listOf("3", "4").k()))
    }
}

val some = {
    ListK.semigroup<Int>().run {
        listOf(1,2).k().combine(listOf(3,4).k())
    }
}