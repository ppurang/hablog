import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.purang"
  val buildVersion      = "0.0.1"
  val buildScalaVersion = "2.9.2"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xcheckinit", "-encoding", "utf8"),
    shellPrompt  := ShellPrompt.buildShellPrompt
  )
}

// Shell prompt which show the current project, 
// git branch and build version
object ShellPrompt {
  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }
  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
  )

  val buildShellPrompt = { 
    (state: State) => {
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (
        currProject, currBranch, BuildSettings.buildVersion
      )
    }
  }
}

object Resolvers {
  val sunrepo    = "Sun Maven2 Repo" at "http://download.java.net/maven/2"
  val sunrepoGF  = "Sun GF Maven2 Repo" at "http://download.java.net/maven/glassfish" 
  val oraclerepo = "Oracle Maven2 Repo" at "http://download.oracle.com/maven"
  val sonatype = "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/"
  val typesafe = "typesafe snapshots" at "http://repo.typesafe.com/typesafe/snapshots"

  
  val oracleResolvers = Seq (sunrepo, sunrepoGF, oraclerepo, sonatype, typesafe)
}

object Dependencies {
  val logbackVer = "1.0.6"
  val logbackCore    = "ch.qos.logback" % "logback-core"     % logbackVer
  val logbackClassic = "ch.qos.logback" % "logback-classic"  % logbackVer
  val liftJsonScalaz = "net.liftweb" % "lift-json_2.9.1" % "2.4"
  //val scalatest	= "org.scalatest" % "scalatest_2.9.2" % "1.8" % "test"
  val scalatest = "org.scalatest" % "scalatest_2.9.1" % "1.6.1" % "test" withSources()

}

object HABlogBuild extends Build {
  import Resolvers._
  import Dependencies._
  import BuildSettings._

  val printClasspath = TaskKey[File]("print-class-path")

  def printCp = (target, fullClasspath in Compile, compile in Compile) map { (out, cp, analysis) =>
    println(cp.files.map(_.getName).mkString("\n"))
    println("----")
    println(analysis.relations.allBinaryDeps.toSeq.mkString("\n"))
    println("----")
    println(out)
    out
  }
 
  lazy val hablog = Project (
    "hablog",
    file ("."),
    settings = buildSettings
  ) aggregate (domain)

  lazy val domain = Project (
    "domain",
    file ("domain"),
    settings = buildSettings ++ Seq (resolvers := oracleResolvers, libraryDependencies ++= Seq(liftJsonScalaz, scalatest))
  )
}