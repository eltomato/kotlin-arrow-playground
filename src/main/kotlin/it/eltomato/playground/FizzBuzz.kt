package it.eltomato.playground

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.instances.option.monoidK.monoidK
import arrow.instances.option.semigroup.semigroup
import arrow.instances.semigroup
import java.util.stream.IntStream.range


typealias Rule = (Int) -> Option<String>

fun main() {

    fun moduleRule(n: Int, s: String): Rule = {
        if (it % n == 0) Some(s) else None
    }

    fun containsRule(n: Int, s: String): Rule = {
        if (it.toString().contains(n.toString())) Some(s) else None
    }

    fun or(r1: Rule, r2: Rule): Rule {
        return {
            Option.monoidK().run {
                r1(it).combineK(r2(it))
            }
        }
    }

    val fizz = or(moduleRule(3, "Fizz"), containsRule(3, "Fizz"))

    val buzz = or(moduleRule(5, "Buzz"), containsRule(5, "Buzz"))

    val fizzbuzz: Rule = {
        Option.semigroup(String.semigroup()).run {
            fizz(it).combine(buzz(it))
        }
    }

    range(1, 36).mapToObj {
        fizzbuzz(it).getOrElse { it.toString() }
    }.forEach { println(it) }
}

