ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "dinges"
ThisBuild / scalaVersion := "3.7.2"

lazy val scalafxV  = "20.0.0-R31"   // see ScalaFX docs — replace with newest if available. :contentReference[oaicite:7]{index=7}
lazy val catsV     = "3.6.3"        // example Cats-Effect 3.x (optional). :contentReference[oaicite:8]{index=8}
lazy val agronaV   = "2.2.4"        // Agrona recent release (example). :contentReference[oaicite:9]{index=9}
lazy val fastutilV = "8.5.16"       // example; check Maven Central for latest. :contentReference[oaicite:10]{index=10}
lazy val roaringV  = "1.3.0"        // example; check Maven Central. :contentReference[oaicite:11]{index=11}

libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % scalafxV,
  // optional helpers:
  "org.typelevel" %% "cats-effect" % catsV,        // optional concurrency
  "org.agrona" % "agrona" % agronaV,               // optional high-perf buffers
  "it.unimi.dsi" % "fastutil" % fastutilV,         // optional primitive collections
  "org.roaringbitmap" % "RoaringBitmap" % roaringV // optional compressed bitmaps
)

lazy val root = (project in file("."))
  .settings(
    name := "DingesAutomata",
    idePackagePrefix := Some("dinges.automata")
  )

Compile / mainClass := Some("dinges.automata.core.App")
Compile / run / fork := true