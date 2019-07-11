package it.eltomato.playground

import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.TextFromStandardInputStream.emptyStandardInputStream
import kotlin.concurrent.thread


class GuessTheNumberKtTest {

    @Rule
    @JvmField
    var systemInMock = emptyStandardInputStream()

    @Test
    fun something() {
        thread {
            while (true) {
                systemInMock.provideLines("1")
            }
        }

//        it.eltomato.playground.main()

    }
}