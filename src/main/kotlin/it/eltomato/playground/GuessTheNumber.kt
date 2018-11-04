package it.eltomato.playground

import arrow.core.Try
import arrow.effects.IO
import it.eltomato.playground.GuessResult.GuessCorrect
import it.eltomato.playground.GuessResult.GuessIncorrect
import java.util.*

fun main(vararg args: String) {

    val upperLimit = 10
    val numberToGuess = Random().nextInt(upperLimit)

    writeIntroduction(upperLimit)
        .flatMap { keepAsking(guessNumber(numberToGuess)) }
        .unsafeRunSync()
}

fun keepAsking(guessNumber: IO<GuessResult>): IO<GuessResult> {
    return guessNumber.attempt().flatMap {
        it.fold(
            { writeException(it).flatMap { keepAsking(guessNumber) } },
            { guessResult ->
                when (guessResult) {
                    is GuessIncorrect -> keepAsking(guessNumber)
                    else -> {
                        writeNailed().map { guessResult }
                    }
                }
            })
    }
}

fun writeException(it: Throwable): IO<Unit> = IO {
    println("Woops...${it::class.java.name} ${it.message}")
}

fun writeNailed(): IO<Unit> = IO {
    println("You nailed it!")
}

fun guessNumber(numberToGuess: Int): IO<GuessResult> =
    askForNumber()
        .flatMap { readAString() }
        .flatMap { parseNumber(it).toIO() }
        .map { analyzeGuess(it, numberToGuess) }
        .flatMap { writeAnalysis(it) }

fun writeIntroduction(limit: Int): IO<Unit> = IO { println("You have to guess a number between 0 and $limit") }

fun writeAnalysis(guessResult: GuessResult): IO<GuessResult> = IO {
    when (guessResult) {
        is GuessIncorrect -> println("Wrong number!")
        is GuessCorrect -> println("That's the right number!")
    }
    guessResult
}

fun askForNumber(): IO<Unit> = IO { print("Take a guess:") }
fun readAString(): IO<String> = IO { readLine()!! }
fun parseNumber(string: String): Try<Int> = Try { string.toInt() }

fun analyzeGuess(number: Int, numberToGuess: Int): GuessResult =
    if (number == numberToGuess) {
        GuessCorrect
    } else {
        GuessIncorrect
    }

sealed class GuessResult {
    object GuessCorrect : GuessResult()
    object GuessIncorrect : GuessResult()
}

