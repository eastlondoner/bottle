#!/bin/sh

#git clone https://github.com/eastlondoner/bottle.git
#cd bottle

#sudo su hdfs

#hadoop fs -mkdir /bottle

#hadoop fs -chown andrew /bottle

#exit

hadoop fs -mkdir /bottle/wordcount /bottle/wordcount/input

hadoop fs -put wordcount/example.txt /bottle/wordcount/input

hadoop jar jars/bottle-unspecified.jar com.geneix.bottle.WordCount /bottle/wordcount/input /bottle/wordcount/output