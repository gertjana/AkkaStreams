organization := "com.thenewmotion"

name := "streams"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.4"

resolvers ++= Seq("TnmGeneral" at "http://nexus.thenewmotion.com/content/groups/general")

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature", "-Xlint")

javaOptions in(Test, run) += "-XX:MaxPermSize=128M"

testOptions in Test += Tests.Argument("-oI")

//mainClass in AssemblyKeys.assembly := Some("com.thenewmotion.streams.Main")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0-M1"
)
