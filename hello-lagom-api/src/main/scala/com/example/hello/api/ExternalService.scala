package com.example.hello.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import com.lightbend.lagom.scaladsl.api.Service._

trait ExternalService extends Service{

  def getUser():ServiceCall[NotUsed, UserData]

  override final def descriptor: Descriptor = {
    // @formatter:off
    named("external-service")
      .withCalls(
        pathCall("/posts/1", getUser _)
      ).withAutoAcl(true)
    // @formatter:on
  }

}
