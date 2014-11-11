Bottle
======

This is an example project demonstrating using Horton Dataworks on Rackspace akak "Rackspace Big Data"

Web Server
=========

Scorpio is the node.js web server that provides the user interface. If you want to run this checkout the readme in the scorpio\ folder.

cloudfs is a node module which is mostly a wrapper around the pkgcloud node module for managing OpenStack (and similar) systems. Cloudfs is used by Scorpio.

Example Map Reduce Job
======================

The pubmed* modules are all related to an example map reduce job that uses pubmed abstracts as the source data.

Wordcount contains a variation on the implementation of the canonical map reduce 'hello world' job.

