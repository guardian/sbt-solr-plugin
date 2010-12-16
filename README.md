SBT Solr Plugin
===============
SBT plugin for Solr projects. Downloads and constructs a Solr context to
contain specified conf files.

Usage
=====
Plugins file:

    import sbt._

    class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
      val guardian = "Guardian GitHub" at "http://guardian.github.com/maven/repo-releases"
      val solr = "com.gu" % "sbt-solr-plugin" % "0.2"
    }


Project file:

    import sbt._
    import com.gu.solr.SolrProject

    class MyProject(info: ProjectInfo) extends SlorProject(info) {
	...
    }

Put your Solr conf directory in src/main/solr. This is copied on prepare-webapp
and forms the Solr home for the container.

Use `jetty-run` to start. Uses port 8983 by default, override by changing
`jettyPort` in your project definition. Hangs off a `/solr` context in
Jetty, so URLs are the same as a Solr started with `start.jar`. You can change
this context by overriding `jettyContextPath` in your project definition.

At present, defaults to Solr 1.4.1. Change this by overriding `solrVersion` in
your project definition.  You will also need to place a copy of the Solr war for
this version in a repository somewhere with the group identifier
`org.apache.solr` and artifact identifier `solr-webapp`. You will also need to
rename the war from `solr.war` to `solr-webapp-[version].war`. This repository
will have to be added to your project definition.

You can get a development trunk build of Solr 4.0 by using:

      val guardian_github = "Guardian GitHub" at "http://guardian.github.com/maven/repo-snapshots"
      override def solrVersion = "4.0-trunk-build-1343"

Gotcha
======
Developing with `jetty-run`, `~prepare-webapp` may not work as expected. You may
want to `jetty-restart` instead.
