package it.eltomato.playground

import arrow.core.Either
import arrow.effects.IO
import java.util.*

fun main(vararg args: String) {

    val numberToGuess = Random().nextInt()

    val map = askForNumber()
            .map { analyzeGuess(it, numberToGuess) }
            .flatMap { writeAnalysis(it) }

    do {
        val unsafeRunSync = map.attempt().unsafeRunSync()
        val wrongResult = when (unsafeRunSync) {
            is Either.Left -> {
                println(unsafeRunSync.a::class.java.name + " " + unsafeRunSync.a.message)
                true
            }
            is Either.Right -> unsafeRunSync.b is GuessIncorrect
        }
    } while (wrongResult)
}

fun show(result: String): IO<Unit> {
    return IO {
        println(result)
    }
}

fun writeAnalysis(guessResult: GuessResult): IO<GuessResult> {
    return IO {
        when (guessResult) {
            is GuessIncorrect -> println("Wrong number!")
            is GuessCorrect -> println("That's the right number!")
        }
        guessResult
    }
}

fun askForNumber(): IO<Int> {
    return IO {
        print("Type a number to guess the mystery one:")
        readLine()!!.toInt()
    }
}

fun analyzeGuess(number: Int, numberToGuess: Int): GuessResult {
    return if (number == numberToGuess) {
        GuessCorrect
    } else {
        GuessIncorrect
    }
}

interface GuessResult
object GuessCorrect : GuessResult
object GuessIncorrect : GuessResult

data class Guess(val guessed: Int)
