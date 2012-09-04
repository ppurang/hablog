import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "hablog-play"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.purang" % "hablog-domain_2.9.1" % "0.0.1" withSources(),
    "org.purang" % "hablog-backend_2.9.1" % "0.0.1" withSources(),
    "org.purang.net" % "asynch_2.9.1" % "0.2.5" withSources(),
    "kafka" % "kafka" % "0.7.1"from "file:///home/ppurang/.ivy2/local/kafka/core-kafka_2.9.1/0.7.1/jars/kafka.jar",
    "org.apache.zookeeper" % "zookeeper" % "3.3.4" withSources() excludeAll(
        ExclusionRule(organization = "log4j"),
        ExclusionRule(organization = "javax"),
        ExclusionRule(organization = "com.sun.jdmk"),
        ExclusionRule(organization = "com.sun.jmx"),
        ExclusionRule(organization = "mail"),
        ExclusionRule(organization = "jms"),
        ExclusionRule(organization = "javax"),
        ExclusionRule(organization = "jline")
      ),
  "com.github.sgroschupf" % "zkclient" % "0.1" excludeAll(
          ExclusionRule(organization = "log4j"),
          ExclusionRule(organization = "javax"),
          ExclusionRule(organization = "com.sun.jdmk"),
          ExclusionRule(organization = "com.sun.jmx"),
          ExclusionRule(organization = "mail"),
          ExclusionRule(organization = "jms"),
          ExclusionRule(organization = "javax"),
          ExclusionRule(organization = "jline")
        )
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
