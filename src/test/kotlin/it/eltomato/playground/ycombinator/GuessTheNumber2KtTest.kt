package it.eltomato.playground.ycombinator

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.TextFromStandardInputStream
import kotlin.concurrent.thread

class GuessTheNumber2KtTest {
    @Rule
    @JvmField
    var systemInMock = TextFromStandardInputStream.emptyStandardInputStream()

    @Test
    fun something() {
        thread {
            while (true) {
                systemInMock.provideLines("1")
            }
        }

        it.eltomato.playground.ycombinator.main()

    }
}