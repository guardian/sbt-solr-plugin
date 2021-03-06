/*

   Copyright 2010 Guardian News and Media

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/
package com.gu.solr

import sbt._

class SolrProject(info: ProjectInfo) extends DefaultWebProject(info) {

  private implicit def pathFinder2head(finder: PathFinder) = new {
    def head = finder.get.toList.head
  }

  private implicit def basicDependencyPaths2Lib(project: BasicDependencyPaths) = new {
    def lib(filePattern: String) = (project.managedDirectoryName ** filePattern).head
  }

  def solrDirectory = mainSourcePath / "solr"
  def solrIndexDirectory = outputPath / "solr" / "data"

  private def outputSolrDirectory = outputPath / "solr"
  private def outputWebappDirectory = outputPath / "webapp"

  override val jettyPort = 8983
  override val jettyContextPath = "/solr"

  def jettyVersion = "6.1.14"
  def solrVersion = "1.4.1"

  val logbackConfiguration: Option[Path] = None

  private def solrWarName = logbackConfiguration match {
    case Some(_) => "solr-webapp-logback"
    case _ => "solr-webapp"
  }

  def solrWar = this.lib("%s-%s.war" format (solrWarName, solrVersion))

  val guardian = "Guardian GitHub" at "http://guardian.github.com/maven/repo-releases"

  override def libraryDependencies = super.libraryDependencies ++
      Set("org.apache.solr" % solrWarName % solrVersion % "test",
        "org.mortbay.jetty" % "jetty" % jettyVersion % "test",
        "org.mortbay.jetty" % "jsp-2.1" % jettyVersion % "test",
        "org.mortbay.jetty" % "jsp-api-2.1" % jettyVersion % "test")

  override def scanDirectories = super.scanDirectories ++ Set(solrDirectory) ++ logbackConfiguration.toList

  override def packageAction = task { None }

  override def prepareWebappAction = task {
    FileUtilities.clean(outputPath, log)
    FileUtilities.sync(solrDirectory, outputSolrDirectory, log)
    FileUtilities.unzip(solrWar, outputWebappDirectory, log)
    logbackConfiguration foreach { config =>
      FileUtilities.copyFilesFlat(List(config.asFile), outputWebappDirectory, log)
    }

    None
  }

  override def jettyRunClasspath = outputWebappDirectory / "WEB-INF" / "lib" * "*.jar"

  def properties = {
    val logback = logbackConfiguration.toList map { "logback.configurationFile" -> _.absolutePath }

    Map(
      "solr.solr.home" -> outputSolrDirectory.absolutePath,
      "solr.data.dir" -> solrIndexDirectory.absolutePath
    ) ++ logback
  }

  override def jettyRunAction = super.jettyRunAction dependsOn task {
    properties foreach { case (name, value) => System.setProperty(name, value) }

    None
  }
}
