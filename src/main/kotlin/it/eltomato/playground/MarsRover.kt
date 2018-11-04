package it.eltomato.playground

import arrow.core.Either
import arrow.core.Failure
import arrow.core.Success
import arrow.core.Try
import arrow.effects.IO
import it.eltomato.playground.Command.MoveCommand.Backward
import it.eltomato.playground.Command.MoveCommand.Forward
import it.eltomato.playground.Direction.*

fun main(args: Array<String>) {
    val inititialPlanet = Planet(Dimension(3, 3), Rover(Position(0, 0), North), setOf(Obstacle(Position(0, 2))))

    val resultedRover = readCommands()
        .flatMap { commands -> handleCommands(commands, inititialPlanet).toIO() }
        .map { it.rover }
        .attempt().unsafeRunSync()

    when (resultedRover) {
        is Either.Right -> print("The rover ended up in ${resultedRover.b.position} and is facing ${resultedRover.b.direction}")
        is Either.Left -> println("Okay, Houston, we've had a problem here: ${resultedRover.a.message}")
    }
}

sealed class Command {
    object UnkownCommand : Command()

    sealed class MoveCommand : Command() {
        object Forward : MoveCommand() {
            override fun toString(): String {
                return "Forward${super.toString()}"
            }
        }

        object Backward : MoveCommand() {
            override fun toString(): String {
                return "Backward${super.toString()}"
            }
        }

        override fun toString(): String {
            return "MoveCommand"
        }
    }

    sealed class TurnCommand : Command() {
        object Left : TurnCommand() {
            override fun toString(): String {
                return "Left${super.toString()}"
            }
        }

        object Right : TurnCommand() {
            override fun toString(): String {
                return "Right${super.toString()}"
            }
        }

        override fun toString(): String {
            return "TurnCommand"
        }
    }
}

enum class Direction {
    North,
    East,
    South,
    West
}

data class Position(val x: Int, val y: Int)

data class Rover(val position: Position, val direction: Direction)

data class Obstacle(val position: Position)

data class Dimension(val x: Int, val y: Int)
data class Planet(val dimension: Dimension, val rover: Rover, val obstacles: Set<Obstacle>)

class ObstacleFoundException(rover: Rover, command: Command.MoveCommand) : Exception("Obstacle found for $rover trying to execute $command")

private fun handleCommands(commands: List<Command>, inititialPlanet: Planet): Try<Planet> =
    commands.fold(Try.just(inititialPlanet), ::handleCommand)

private fun handleCommand(planet: Try<Planet>, command: Command): Try<Planet> {
    return planet.flatMap { planet ->
        when (command) {
            is Command.MoveCommand -> move(command, planet.rover, planet).map { planet.copy(rover = it) }
            is Command.TurnCommand -> Success(planet.copy(rover = turn(command, planet.rover)))
            is Command.UnkownCommand -> Success(planet)
        }
    }
}

fun turn(command: Command.TurnCommand, rover: Rover): Rover {
    return rover.copy(direction = when (command) {
        is Command.TurnCommand.Left -> left(rover.direction)
        is Command.TurnCommand.Right -> right(rover.direction)
    })
}

fun left(direction: Direction): Direction =
    when (direction) {
        North -> West
        West -> South
        South -> East
        East -> North
    }

fun right(direction: Direction): Direction = left(left(left(direction)))

fun move(command: Command.MoveCommand, rover: Rover, planet: Planet): Try<Rover> {
    val nextPosition = nextCandidatePosition(command, rover, planet)
    if (planet.obstacles.any { it.position == nextPosition }) {
        return Failure(ObstacleFoundException(rover, command))
    }
    return Success(rover.copy(position = nextPosition))
}

fun nextCandidatePosition(command: Command.MoveCommand, rover: Rover, planet: Planet): Position {
    return when (command) {
        Forward -> positionMovingForward(rover, planet)
        Backward -> positionMovingBackward(rover, planet)
    }
}


fun positionMovingForward(rover: Rover, planet: Planet): Position {
    return when (rover.direction) {
        North -> rover.position.copy(y = (rover.position.y - 1 + planet.dimension.y) % planet.dimension.y)
        East -> rover.position.copy(x = (rover.position.x + 1) % planet.dimension.x)
        South -> rover.position.copy(y = (rover.position.y + 1) % planet.dimension.y)
        West -> rover.position.copy(x = (rover.position.x - 1 + planet.dimension.x) % planet.dimension.x)
    }
}

fun positionMovingBackward(rover: Rover, planet: Planet): Position = positionMovingForward(rover.copy(direction = left(left(rover.direction))), planet)

fun readCommands(): IO<List<Command>> = IO {
    listOf(Forward)
}
