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
typealias RuleFor = (String) -> Rule

fun main() {

    fun divisibleBy(n: Int): RuleFor = { s ->
        {
            if (it % n == 0) Some(s) else None
        }
    }

    fun contains(str: String): RuleFor = { s ->
        {
            if (it.toString().contains(str)) Some(s) else None
        }
    }

    infix fun RuleFor.or(r: RuleFor): RuleFor {
        val ruleFor = this
        return { s ->
            {
                Option.monoidK().run {
                    ruleFor(s)(it).combineK(r(s)(it))
                }
            }
        }
    }

    infix fun RuleFor.then(s: String): Rule = this(s)

    val fizz = divisibleBy(3) or contains("3") then "Fizz"

    val buzz = divisibleBy(5) or contains("5") then "Buzz"

    val fizzbuzz: Rule = {
        Option.semigroup(String.semigroup()).run {
            fizz(it).combine(buzz(it))
        }
    }

    range(1, 100)
            .mapToObj { fizzbuzz(it).getOrElse { it.toString() } }
            .forEach { println(it) }

}