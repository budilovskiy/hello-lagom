package com.example.settings.impl

import com.example.settings.api
import com.example.settings.api.{SettingsService, UpdateSettingsRequest}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

import scala.concurrent.ExecutionContext

class SettingsServiceImpl(persistentEntityRegistry: PersistentEntityRegistry)(implicit ec: ExecutionContext) extends SettingsService {

  def updateSettings(): ServiceCall[api.UpdateSettingsRequest, api.Settings] =
    ServiceCall { request =>
      val ref = persistentEntityRegistry.refFor[SettingsEntity](SettingsEntity.entityId)

      // Tell the entity to use the greeting message specified.
      ref
        .ask(UpdateSettings(Settings(request.parameter1, request.parameter2)))
        .map((settings: Settings) => api.Settings(settings.parameter1, settings.parameter2))
    }

  override def settingsTopic(): Topic[api.SettingsUpdated] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(SettingsEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(helloEvent: EventStreamElement[SettingsEvent]): api.SettingsUpdated = {
    helloEvent.event match {
      case SettingsUpdated(value) => api.SettingsUpdated(api.Settings(value.parameter1, value.parameter2))
    }
  }
}
