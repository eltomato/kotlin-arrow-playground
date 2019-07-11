package it.eltomato.playground

import arrow.core.Try
import arrow.core.getOrElse
import arrow.effects.IO
import arrow.effects.liftIO
import it.eltomato.playground.GuessResult.*
import java.util.*

fun main(vararg args: String) {

    val upperLimit = 10
    val numberToGuess = Random().nextInt(upperLimit)

    writeIntroduction(upperLimit)
            .flatMap { keepAsking2(guessNumber(numberToGuess)) }
            .unsafeRunSync()
}

typealias X = (IO<GuessResult>) -> IO<GuessResult>
typealias F = Function1<X, X>

//        v-------------v--- let G reference G recursively
interface G : Function1<G, X>

//  v--- create a G from lazy blocking
fun G(block: (G) -> X) = object : G {
    //                          v--- delegate call `block(g)` like as `g(g)`
    override fun invoke(g: G) = block(g)
}

fun Y(f: F) = (fun(g: G) = g(g))(G { g -> f({ x -> g(g)(x) }) })

//val fact = Y { rec -> { n -> if (n == 0) 1 else n * rec(n - 1) } }

val keepAsking2 = Y { rec ->
    { guess ->
        guess.attempt()
                .flatMap { attempt ->
                    attempt.fold(
                            { writeException(it) },
                            { writeAnalysis(it) }
                    ).flatMap {
                        attempt.toOption()
                                .filter { it == GuessCorrect }
                                .map { it.liftIO() }
                                .getOrElse { rec(guess) }
                    }

                }
    }
}


fun keepAsking(guessNumber: IO<GuessResult>): IO<GuessResult> {
    return guessNumber.attempt()
            .flatMap { attempt ->
                attempt.fold(
                        { writeException(it) },
                        { writeAnalysis(it) }
                ).flatMap {
                    attempt.toOption()
                            .filter { it == GuessCorrect }
                            .map { it.liftIO() }
                            .getOrElse { keepAsking(guessNumber) }
                }

            }
}

fun writeException(it: Throwable): IO<Unit> = IO {
    println("Woops...${it::class.java.name} ${it.message}")
}

fun guessNumber(numberToGuess: Int): IO<GuessResult> =
        askForNumber()
                .flatMap { readAString() }
                .flatMap { parseNumber(it).toIO() }
                .map { analyzeGuess(it, numberToGuess) }

fun writeIntroduction(limit: Int): IO<Unit> = IO { println("You have to guess a number between 0 and $limit") }

fun writeAnalysis(guessResult: GuessResult): IO<Unit> = IO {
    when (guessResult) {
        is GuessIncorrect -> println("Wrong number!")
        is GuessCorrect -> println("That's the right number!")
    }
}

fun askForNumber(): IO<Unit> = IO { print("Take a guess:") }
fun readAString(): IO<String> = IO { readLine()!! }
fun parseNumber(string: String): Try<Int> = Try { string.toInt() }

fun analyzeGuess(number: Int, numberToGuess: Int): GuessResult = if (number == numberToGuess) GuessCorrect else GuessIncorrect

sealed class GuessResult {
    object GuessCorrect : GuessResult()
    object GuessIncorrect : GuessResult()
}

