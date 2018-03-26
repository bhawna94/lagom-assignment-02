package com.example.hello.impl

import akka.NotUsed
import com.example.hello.api
import com.example.hello.api._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementation of the HellolagomService.
  */
class HellolagomServiceImpl(persistentEntityRegistry: PersistentEntityRegistry,
externalService: ExternalService)(implicit ec: ExecutionContext)extends HellolagomService {


  val employeeList = new ListBuffer[Employee]

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the hello-lagom entity for the given ID.
    val ref = persistentEntityRegistry.refFor[HellolagomEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id))
  }

  override def getEmployees(id: Int): ServiceCall[NotUsed, ListBuffer[Employee]] = ServiceCall {
    request =>
      Future.successful(employeeList.filter(emp => emp.id == id))
  }

  override def postEmployee(): ServiceCall[Employee, ListBuffer[Employee]] = ServiceCall {
    request =>
      val add = Employee(request.id, request.name, request.age)
      employeeList += add
      Future.successful(employeeList)
  }

  override def deleteEmployee(id: Int): ServiceCall[NotUsed, ListBuffer[Employee]] = ServiceCall {
    request =>
      val deleteRecord = employeeList.filter(ele => ele.id == id)
      employeeList --= deleteRecord
      Future.successful(employeeList)
  }

  override def getName(name: String): ServiceCall[NotUsed, String] = ServiceCall{ request =>
    Future.successful(name)
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the hello-lagom entity for the given ID.
    val ref = persistentEntityRegistry.refFor[HellolagomEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message))
  }


  override def greetUser(name: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    Future.successful("Hi, " + name)
  }

  override def testUser(): ServiceCall[NotUsed, UserData] = ServiceCall { _ =>
    val result: Future[UserData] = externalService.getUser().invoke()
    result.map(response => response)
  }

  override def greetingsTopic(): Topic[api.GreetingMessageChanged] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(HellolagomEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(helloEvent: EventStreamElement[HellolagomEvent]): api.GreetingMessageChanged = {
    helloEvent.event match {
      case GreetingMessageChanged(msg) => api.GreetingMessageChanged(helloEvent.entityId, msg)
    }
  }
}
