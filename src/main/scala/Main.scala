package com.thenewmotion.streams

import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl._
import akka.actor.{PoisonPill, Props, ActorSystem}
import akka.stream.{FlattenStrategy, OverflowStrategy, FlowMaterializer}
import scala.language.postfixOps
import scala.concurrent.duration._

case class Tick()

class TickActor extends ActorPublisher[Int] {

  implicit val ec = context.dispatcher

  val tick = context.system.scheduler.schedule(0 second, 0.1 second, self, Tick())

  var cnt = 0
  var buffer = Vector.empty[Int]

  override def receive: Receive = {
    case Tick() => {
      cnt = cnt + 1
      if (buffer.isEmpty && totalDemand > 0) {
        onNext(cnt)
      }
      else {
        buffer :+= cnt
        if (totalDemand > 0) {
          val (use,keep) = buffer.splitAt(totalDemand.toInt)
          buffer = keep
          use foreach onNext
        }
      }
    }
  }

  override def postStop() = tick.cancel()

}

object Main extends App {

  implicit val system = ActorSystem("StreamTest")
  implicit val executionContext =  system.dispatcher

  val tickActor = system.actorOf(Props[TickActor])

  implicit val materializer = FlowMaterializer()

  val source:Source[Int] = Source(ActorPublisher[Int](tickActor))
  //val source:Source[Int] = Source(Stream.from(1))

  val sink:Sink[Int] = ForeachSink[Int](println)

  val materialized = source
    .filter(_ % 2 == 0)                         // Source[Int]      keep only even numbers  |
    .map(_ / 2)                                 // Source[Int]      divide by two           | slows down the stream by a factor 2
    .buffer(10, OverflowStrategy.backpressure)  // Source[Int]      buffer 10 items, and apply backpressure if more
    .groupedWithin(5, 1 second)                 // Source[Seq[Int]] group by 5 items or when the duration expires
    .map(xs => xs.foldLeft(0)(_+_))             // Source[Int]      sum up the 5 items
    .to(sink)                                   // RunnableFlow     Attach the Sink
    .run()                                      // MaterializedMap  run the flow

}

