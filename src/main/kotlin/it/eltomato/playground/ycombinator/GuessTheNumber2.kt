package it.eltomato.playground.ycombinator

import it.eltomato.playground.ycombinator.GuessResult.GuessCorrect
import it.eltomato.playground.ycombinator.GuessResult.GuessIncorrect
import java.util.*

fun main(vararg args: String) {

    val upperLimit = 10
    val numberToGuess = Random().nextInt(upperLimit)

    writeIntroduction(upperLimit)
    keepAsking2(guessNumber(numberToGuess))
}

typealias X = (() -> GuessResult) -> GuessResult
typealias F = Function1<X, X>

//        v-------------v--- let G reference G recursively
interface G : Function1<G, X>

//  v--- create a G from lazy blocking
fun G(block: (G) -> X) = object : G {
    //                          v--- delegate call `block(g)` like as `g(g)`
    override fun invoke(g: G) = block(g)
}

fun Y(f: F) = (fun(g: G) = g(g))(G { g -> f({ x -> g(g)(x) }) })

val keepAsking2 = Y { rec ->
    { guess ->
        try {
        val guessResult = guess()
            when (guessResult) {
                is GuessCorrect -> {
                    println("That's the right number!")
                    guessResult
                }
                is GuessIncorrect -> {
                    println("Wrong number!")
                    rec(guess)
                }
            }
        } catch (e: Exception) {
            rec(guess)
        }
//        guess.attempt()
//                .flatMap { attempt ->
//                    attempt.fold(
//                            { writeException(it) },
//                            { writeAnalysis(it) }
//                    ).flatMap {
//                        attempt.toOption()
//                                .filter { it == GuessCorrect }
//                                .map { it.liftIO() }
//                                .getOrElse { rec(guess) }
//                    }
//
//                }
    }
}

fun writeException(it: Throwable) = {
    println("Woops...${it::class.java.name} ${it.message}")
}

fun guessNumber(numberToGuess: Int): () -> GuessResult = {
    askForNumber()
    analyzeGuess(parseNumber(readAString()), numberToGuess)
}
//                .flatMap { readAString() }
//                .flatMap { parseNumber(it).toIO() }
//                .map { analyzeGuess(it, numberToGuess) }

fun writeIntroduction(limit: Int) = println("You have to guess a number between 0 and $limit")

fun writeAnalysis(guessResult: GuessResult) = {
    when (guessResult) {
        is GuessIncorrect -> println("Wrong number!")
        is GuessCorrect -> println("That's the right number!")
    }
}

fun askForNumber() = print("Take a guess:")
fun readAString() = readLine()!!
fun parseNumber(string: String): Int = string.toInt()

fun analyzeGuess(number: Int, numberToGuess: Int): GuessResult = if (number == numberToGuess) GuessCorrect else GuessIncorrect

sealed class GuessResult {
    object GuessCorrect : GuessResult()
    object GuessIncorrect : GuessResult()
}

