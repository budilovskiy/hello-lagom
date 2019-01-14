package com.example.settings.api

import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.transport.Method
import play.api.libs.json.{Format, Json}

object SettingsService {
  val TOPIC_NAME = "settings"
}

trait SettingsService extends Service {

  def updateSettings(): ServiceCall[UpdateSettingsRequest, Settings]

  def settingsTopic(): Topic[SettingsUpdated]

  override final def descriptor = {
    import Service._

    named("settings")
      .withCalls(
        restCall(Method.POST, "/api/settings", updateSettings _)
      )
      .withTopics(
        topic(SettingsService.TOPIC_NAME, settingsTopic)
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[SettingsUpdated](_.getClass.toString)
          )
      )
      .withAutoAcl(true)
  }

}

case class SettingsUpdated(value: Settings)

object SettingsUpdated {
  implicit val format: Format[SettingsUpdated] = Json.format[SettingsUpdated]
}

case class Settings(
                     parameter1: Option[Long],
                     parameter2: Option[String]
                   )

object Settings {
  implicit val format: Format[Settings] = Json.format[Settings]
}

case class UpdateSettingsRequest(
                                  parameter1: Option[Long],
                                  parameter2: Option[String]
                                )

object UpdateSettingsRequest {
  implicit val format: Format[UpdateSettingsRequest] = Json.format[UpdateSettingsRequest]
}
