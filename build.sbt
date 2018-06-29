name := "flowdb"
organization in ThisBuild := "com.jjunqueira"
scalaVersion in ThisBuild := "2.12.6"

// PROJECTS

lazy val global = project
  .in(file("."))
  .settings(settings)
  .aggregate(
    common,
    node,
    api
  )

lazy val common = project
  .settings(
    name := "common",
    settings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.ip4s
    )
  )

lazy val node = project
  .settings(
    name := "node",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.pureconfig,
      dependencies.akkaActor,
      dependencies.akkaCluster,
      dependencies.rocksDb,
      dependencies.ip4s
    )
  )
  .dependsOn(
    common
  )

lazy val api = project
  .settings(
    name := "api",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.pureconfig,
      dependencies.akkaActor,
      dependencies.akkaCluster
    )
  )
  .dependsOn(
    common
  )

// DEPENDENCIES

lazy val dependencies =
  new {
    val logbackV = "1.2.3"
    val pureconfigV = "0.9.1"
    val akkaV = "2.5.13"
    val scalatestV = "3.0.5"
    val scalacheckV = "1.14.0"
    val typesafeConfigV = "1.3.1"
    val catsV = "1.1.0"
    val rocksDbV = "5.13.4"
    val ip4sV = "1.0.1"

    val logback = "ch.qos.logback" % "logback-classic" % logbackV
    val akkaActor = "com.typesafe.akka" %% "akka-actor-typed" % akkaV
    val akkaCluster = "com.typesafe.akka" %% "akka-cluster-typed" % akkaV
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaV
    val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaV
    val typesafeConfig = "com.typesafe" % "config" % typesafeConfigV
    val pureconfig = "com.github.pureconfig" %% "pureconfig" % pureconfigV
    val scalatest = "org.scalatest" %% "scalatest" % scalatestV
    val scalacheck = "org.scalacheck" %% "scalacheck" % scalacheckV
    val catsCore = "org.typelevel" %% "cats-core" % catsV
    val rocksDb = "org.rocksdb" % "rocksdbjni" % rocksDbV
    val ip4s = "com.comcast" %% "ip4s" % ip4sV
  }

lazy val commonDependencies = Seq(
  dependencies.logback,
  dependencies.catsCore,
  dependencies.scalatest % "test",
  dependencies.scalacheck % "test"
)

// SETTINGS

lazy val settings =
  commonSettings ++
    wartremoverSettings ++
    scalafmtSettings

lazy val compilerOptions = Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xfuture", // Turn on future language features.
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
  "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match", // Pattern match may not be typesafe.
  "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ypartial-unification", // Enable partial unification in type constructor inference
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:params", // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates", // Warn if a private member is unused.
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val wartremoverSettings = Seq(
  wartremoverWarnings in (Compile, compile) ++= Warts.allBut(Wart.Throw)
)

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true,
    scalafmtTestOnCompile := true,
    scalafmtVersion := "1.2.0"
  )

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _                             => MergeStrategy.first
  }
)
