package it.eltomato.playground

import arrow.core.Either
import arrow.core.Try
import arrow.effects.IO

fun <A> Try<A>.toIO(): IO<A> {
    val toEither = this.toEither()
    return when (toEither) {
        is Either.Left -> IO.raiseError(toEither.a)
        is Either.Right -> IO.just(toEither.b)
    }
}