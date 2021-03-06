package battleship.game

/**
  * Created by jordidevos on 26/07/2017.
  */

object BoardState {
  def empty: BoardState = BoardState(Set.empty, Seq.empty)
}

case class BoardState(boats: Set[(Boat, BoatLocation)], history: Seq[Shot]) {

  def placeBoat(boat: Boat, boatLocation: BoatLocation): BoardState = {
    val allUsedCoordinates = boats.flatMap{case (placedBoat, placement) => placedBoat.coordinates(placement)}
    val boatLocationFree = boat.coordinates(boatLocation).toSet.intersect(allUsedCoordinates).isEmpty
    require(boatLocationFree, s"Cannot place $boat on $boatLocation: ${boat.coordinates(boatLocation)} overlaps with $allUsedCoordinates")

    val location = (boat, boatLocation)
    BoardState(boats + location, history)
  }

  def shoot(coordinate: Coordinate): BoardState = {
    require(!history.exists(_._1 == coordinate), "Can't shoot at the same coordinate twice")

    val shotResult = calculateShotResult(coordinate)
    BoardState(boats, (coordinate, shotResult) +: history)
  }

  def gameOver: Boolean = {
    boats.size == history.count(_._2 match {
      case Sink(_) => true
      case _ => false
    })
  }

  def shotsOnBoatSoFar(boat: Boat, location: BoatLocation): Int = {
    val  boatCoordinates = boat.coordinates(location)
    history.count { case ((x, y), _) => boatCoordinates.contains((x, y)) }
  }

  def calculateShotResult(coordinate: Coordinate): ShotResult = {

    val boatShotAt = boats.find { case (boat, location) =>
      boat.liesOn(location, coordinate._1, coordinate._2)
    }

    boatShotAt match { // TODO Fix (check duplicate shots / history)
      case None => Miss
      case Some((boatBeingHit, location)) =>
        if (shotsOnBoatSoFar(boatBeingHit, location) < boatBeingHit.size - 1) Hit
        else Sink(boatBeingHit)
    }
  }
}
