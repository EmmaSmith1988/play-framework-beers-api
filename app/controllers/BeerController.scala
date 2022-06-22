package controllers

import models.{BeerItem, NewBeerItem}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.collection.mutable

@Singleton
class BeerController @Inject() (val controllerComponents: ControllerComponents) extends BaseController{
  private val beerList = new mutable.ListBuffer[BeerItem]()
  beerList += BeerItem(1, "Corona", 4.5)
  beerList += BeerItem(2, "Budweiser", 6.0)
  beerList += BeerItem(3, "Carlsberg", 3.4)

  implicit val beerListJson = Json.format[BeerItem]
  implicit val newBeerListJson = Json.format[NewBeerItem]

  def getAll(): Action[AnyContent] = Action {
    if (beerList.isEmpty) {
      NoContent
    } else {
      Ok(Json.toJson(beerList))
    }
  }

  def getById(itemId: Long) = Action {
    val foundItem = beerList.find(_.id == itemId)
    foundItem match {
      case Some(item) => Ok(Json.toJson(item))
      case None => NotFound
    }
  }

  def deleteById(itemId: Long) = Action {
    beerList.filterInPlace(_.id != itemId )
    Accepted(Json.toJson(beerList))
  }

  def addNewBeer() = Action { implicit request =>
    val content = request.body
    val jsonObject = content.asJson
    val todoListItem: Option[NewBeerItem] =
      jsonObject.flatMap(
        Json.fromJson[NewBeerItem](_).asOpt
      )
    todoListItem match {
      case Some(newItem) =>
        val nextId = beerList.map(_.id).max + 1
        val toBeAdded = BeerItem(nextId, newItem.name, newItem.ABV)
        beerList += toBeAdded
        Created(Json.toJson(toBeAdded))
      case None =>
        BadRequest
    }
  }

}
