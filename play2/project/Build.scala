import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "hablog-play"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.purang" % "hablog-domain_2.9.1" % "0.0.1" withSources(),
    "org.purang.net" % "asynch_2.9.1" % "0.2.5" withSources()
    // Add your project dependencies here,
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    // Add your own project settings here
    /*resolvers ++=  Seq(
      "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository",
      //"Local Ivy Repository" at "file://" + Path.userHome + "/.ivy2/local",
      Resolver.file("Local ivy Repository", file("file://" + Path.userHome + "/.ivy2/local" ))

      //"Local ivy" at Path.userHome.asFile.toURI.toURL + ".ivy2/local"
    )*/

    resolvers ++= Seq(Resolver.file("Local ivy Repository", file(Path.userHome + "/.ivy2/local/"))(Resolver.ivyStylePatterns))
  )
}
