name := """iot_server"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)
    .dependsOn(common, accounts, devices, events, services, sites)

lazy val common = (project in file("modules/common"))
    .enablePlugins(PlayJava, PlayEbean)

lazy val accounts = (project in file("modules/accounts"))
    .enablePlugins(PlayJava, PlayEbean)
    .dependsOn(common)

lazy val devices = (project in file("modules/devices"))
    .enablePlugins(PlayJava, PlayEbean)
    .dependsOn(common)

lazy val events = (project in file("modules/events"))
    .enablePlugins(PlayJava, PlayEbean)
    .dependsOn(common)

lazy val services = (project in file("modules/services"))
    .enablePlugins(PlayJava, PlayEbean)
    .dependsOn(common)

lazy val sites = (project in file("modules/sites"))
    .enablePlugins(PlayJava, PlayEbean)
    .dependsOn(common)
    
scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)
