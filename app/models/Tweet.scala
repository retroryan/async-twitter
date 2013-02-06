package models

import scala.collection.mutable.{SynchronizedMap, HashMap}

import org.codehaus.jackson.annotate.JsonProperty
import play.api.libs.json._

import play.api.Play.current

/**
 * The Class that is stored in the database can not be a case class.  The problem is case classes are immutable
 * and so the id doesn't get properly updated with the mongo db id.  Instead it just adds another field for the id.
 *
 * @param twitterId
 * @param status
 * @param user
 */
case class Tweet(@JsonProperty("twitterId") twitterId: String,
                 @JsonProperty("status") status: String,
                 @JsonProperty("user") user: String,
                 @JsonProperty("owner") owner: String) {

}

object Tweet {
  private val tweetDB = new HashMap[String, Tweet] with SynchronizedMap[String, Tweet]

  def create(status: Tweet): Unit = {
    tweetDB(status.twitterId) = status
  }

  def findAll() = tweetDB.values

  def findByOwner(owner: String) = tweetDB.values.filter{
      _.owner == owner
  }

  def findByTwitterId(twitterId: String) = tweetDB.get(twitterId)

  def tweetToMap(tweet: Tweet) = (
    Map(
      "status" -> tweet.status,
      "user" -> tweet.user,
      "twitterId" -> tweet.twitterId,
      "owner" -> tweet.owner
    )
    )

  def createTweetFromTwitterJson(json: JsValue, owner: String) = {
    new Tweet(
      (json \ "id_str").as[String],
      (json \ "text").as[String],
      (json \ "user" \ "name").as[String],
      owner
    )
  }

  implicit object TweetFormat extends Format[Tweet] {

    def reads(json: JsValue): JsResult[Tweet] = JsSuccess(
      Tweet(
        (json \ "twitterId").as[String],
        (json \ "status").as[String],
        (json \ "user").as[String],
        "")
    )

    def writes(tweet: Tweet): JsValue =
      Json.obj(
        "twitterId" -> tweet.twitterId,
        "status" -> tweet.status,
        "user" -> tweet.user
      )
  }

  implicit object TweetListWrites extends Writes[List[Tweet]] {
    def writes(tweetList: List[Tweet]) = Json.toJson(
      tweetList.map(tweetToMap(_))
    )
  }

}
