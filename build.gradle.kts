plugins {
  maven
  scala
  `java-library`
}
val scalaCompilerPlugin: Configuration = configurations.create("scalaCompilerPlugin")

tasks.getByName<Wrapper>("wrapper") {
  gradleVersion = "5.2.1"
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
  jcenter()
}
dependencies {
  scalaCompilerPlugin("org.scalamacros:paradise_2.12.8:2.1.1")
  api("org.scala-lang:scala-library:2.12.8")

  implementation("org.smali:dexlib2:2.2.6")
}

tasks.withType<ScalaCompile> {
  scalaCompileOptions.additionalParameters = listOf(
      "-Xplugin:" + scalaCompilerPlugin.asPath,
      "-Ypartial-unification",
      "-language:higherKinds")
}
