package com.example.hello.api

import akka.{Done, NotUsed}

import scala.collection.mutable.ListBuffer
//import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

object HellolagomService  {
  val TOPIC_NAME = "greetings"
}

/**
  * The hello-lagom service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the HellolagomService.
  */

case class Employee(id: Int, name: String, age: Int)

object Employee {
  implicit val format: Format[Employee] = Json.format[Employee]
}

trait HellolagomService extends Service {

  /**
    * Example: curl http://localhost:9000/api/hello/Alice
    */


//  def updateEmployee(id: Int): ServiceCall[]

  def deleteEmployee(id: Int): ServiceCall[NotUsed, ListBuffer[Employee]]

  def postEmployee(): ServiceCall[Employee, ListBuffer[Employee]]

  def getEmployees(id: Int): ServiceCall[NotUsed, ListBuffer[Employee]]

//  def postEmployee(employee: Employee): ServiceCall[String, Done]

  def hello(id: String): ServiceCall[NotUsed, String]


  def getName(name: String): ServiceCall[NotUsed, String]
  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"message":
    * "Hi"}' http://localhost:9000/api/hello/Alice
    */
  def useGreeting(id: String): ServiceCall[GreetingMessage, Done]


  /**
    * This gets published to Kafka.
    */
  def greetingsTopic(): Topic[GreetingMessageChanged]


  def greetUser(name: String): ServiceCall[NotUsed, String]

  def testUser(): ServiceCall[NotUsed, UserData]

  override final def descriptor = {
    import Service._
    // @formatter:off


    named("hello-lagom")
      .withCalls(
        pathCall("/api/hello/:id", hello _),
        pathCall("/api/hello/:id", useGreeting _),
        pathCall("/api/myName/:name", getName _),
        restCall(Method.GET,"/api/employeeDetails/:id", getEmployees _),
        restCall(Method.GET,"/api/external", testUser _),
        restCall(Method.POST,"/api/addEmployee", postEmployee _),
        restCall(Method.GET, "/api/deleteEmployee/:id", deleteEmployee _)
//        restCall(Method.PUT, "/api/updateEmployee/:id", updateEmployee _)
      )
      .withTopics(
        topic(HellolagomService.TOPIC_NAME, greetingsTopic)
          // Kafka partitions messages, messages within the same partition will
          // be delivered in order, to ensure that all messages for the same user
          // go to the same partition (and hence are delivered in order with respect
          // to that user), we configure a partition key strategy that extracts the
          // name as the partition key.
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[GreetingMessageChanged](_.name)
          )
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

/**
  * The greeting message class.
  */
case class GreetingMessage(message: String)

object GreetingMessage {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[GreetingMessage] = Json.format[GreetingMessage]
}



/**
  * The greeting message class used by the topic stream.
  * Different than [[GreetingMessage]], this message includes the name (id).
  */
case class GreetingMessageChanged(name: String, message: String)

object GreetingMessageChanged {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[GreetingMessageChanged] = Json.format[GreetingMessageChanged]
}
