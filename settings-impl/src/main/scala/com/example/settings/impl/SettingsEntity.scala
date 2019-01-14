package com.example.settings.impl

import java.util.UUID

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

class SettingsEntity extends PersistentEntity {

  override type Command = SettingsCommand[_]
  override type Event = SettingsEvent
  override type State = SettingsState

  override def initialState: SettingsState = SettingsState(Settings.initial)

  override def behavior: Behavior = {
    case SettingsState(settings) =>
      Actions()
        .onCommand[UpdateSettings, Settings] {
          case (UpdateSettings(newSettings), ctx, _) =>
            ctx.thenPersist(
              SettingsUpdated(newSettings)
            ) { _ =>
              ctx.reply(newSettings)
            }
        }
        .onReadOnlyCommand[GetSettings.type, Settings] {
          case (GetSettings, ctx, _) =>
            ctx.reply(settings)
        }
        .onEvent {
          case (SettingsUpdated(newSettings), _) =>
            SettingsState(newSettings)
        }
  }
}

object SettingsEntity {
  val entityId: String = UUID.nameUUIDFromBytes("Settings_entity_ID_v_2".getBytes).toString
}

case class SettingsState(value: Settings)

object SettingsState {
  implicit val format: Format[SettingsState] = Json.format
}

case class Settings(
                     parameter1: Option[Long],
                     parameter2: Option[String]
                   )

object Settings {
  val initial = Settings(Some(0L), Some("initial"))
  implicit val format: Format[Settings] = Json.format[Settings]
}

sealed trait SettingsEvent extends AggregateEvent[SettingsEvent] {
  def aggregateTag = SettingsEvent.Tag
}

object SettingsEvent {
  val Tag = AggregateEventTag[SettingsEvent]
}

case class SettingsUpdated(value: Settings) extends SettingsEvent

object SettingsUpdated {
  implicit val format: Format[SettingsUpdated] = Json.format
}

sealed trait SettingsCommand[R] extends ReplyType[R]

case class UpdateSettings(value: Settings) extends SettingsCommand[Settings]

object UpdateSettings {
  implicit val format: Format[UpdateSettings] = Json.format
}

case object GetSettings extends SettingsCommand[Settings] {
  implicit val format: Format[GetSettings.type] = JsonSerializer.emptySingletonFormat(GetSettings)
}

object SettingsSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[UpdateSettings],
    JsonSerializer[GetSettings.type],
    JsonSerializer[SettingsUpdated],
    JsonSerializer[SettingsState],
    JsonSerializer[Settings]
  )
}
