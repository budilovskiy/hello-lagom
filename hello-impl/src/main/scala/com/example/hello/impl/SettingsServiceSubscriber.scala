package com.example.hello.impl

import akka.Done
import akka.stream.scaladsl.Flow
import com.example.settings.api.{SettingsService, SettingsUpdated}

class SettingsServiceSubscriber(settingsService: SettingsService) {
  settingsService
    .settingsTopic()
    .subscribe
    .atLeastOnce(
      Flow[SettingsUpdated].map {
        case SettingsUpdated(settings) =>
          println(settings)
          Done
        case _ =>
          Done
      }
    )
}
