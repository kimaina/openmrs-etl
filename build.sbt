
lazy val kafkaVersion = "0.8.2.1"
lazy val sparkVersion = "1.6.0"

lazy val commonSettings = Seq(
  name := "openmrs-data-stream",
  version := "1.0",
  organization := "http://org.github.io/",
  scalaVersion := "2.11.7"
)

lazy val customScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import"
)

lazy val customLibraryDependencies = Seq(
  "org.apache.kafka" %% "kafka" % kafkaVersion,
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,

  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka" % sparkVersion,

  "com.twitter" %% "bijection-avro" % "0.8.1",
  "com.twitter" %% "chill-avro" % "0.7.2",

  "com.typesafe" % "config" % "1.2.1",
  "net.ceedubs" %% "ficus" % "1.1.1",

  "ch.qos.logback" % "logback-classic" % "0.9.24",

  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

lazy val commonExcludeDependencies = Seq(
  "org.slf4j" % "slf4j-log4j12"
)

lazy val customJavaOptions = Seq(
  "-Xmx1024m",
  "-XX:-MaxFDLimit"
)

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
lazy val testScalastyle = taskKey[Unit]("testScalastyle")

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(scalacOptions ++= customScalacOptions)
  .settings(libraryDependencies ++= customLibraryDependencies)
  .settings(excludeDependencies ++= commonExcludeDependencies)
  .settings(fork in run := true)
  .settings(connectInput in run := true)
  .settings(javaOptions in run ++= customJavaOptions)
  .settings(
    scalastyleFailOnError := false,
    compileScalastyle := scalastyle.in(Compile).toTask("").value,
    (compile in Compile) := ((compile in Compile) dependsOn compileScalastyle).value,
    testScalastyle := scalastyle.in(Test).toTask("").value,
    (test in Test) := ((test in Test) dependsOn testScalastyle).value)