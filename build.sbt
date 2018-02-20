name := """openmrs-etl"""

version := "1.0"

scalaVersion := "2.12.4"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0-SNAP10" % "test"
libraryDependencies += "org.apache.kafka" % "kafka_2.11" % "1.0.0"

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"

