#!/bin/sh

sudo yum install git

#git clone https://github.com/eastlondoner/bottle.git
#cd bottle

#sudo su hdfs

#hadoop fs -mkdir /bottle

#hadoop fs -chown andrew /bottle

#exit

hadoop fs -mkdir /bottle/wordcount /bottle/wordcount/input



hadoop fs -put wordcount/example.txt /bottle/wordcount/input

hadoop jar jars/bottle-unspecified.jar '' /bottle/wordcount/input /bottle/wordcount/output

hadoop fs -put pubmedcount/example.txt /bottle/pubmedcount/input
[andrew@GATEWAY-1 bottle]$ hadoop jar jars/bottle-unspecified.jar com.geneix.bottle.PubMedCount /bottle/wordcount/input /bottle/wordcount/output

hadoop jar jars/bottle-all.jar com.geneix.bottle.MapReduceRunner com.geneix.bottle.PubMedCount /bottle/wordcount/input /bottle/wordcount/output

hadoop jar jars/bottle-all.jar com.geneix.bottle.MapReduceRunner com.geneix.bottle.GeneratePubMedData 100 /bottle/wordcount/output

hadoop fs -mkdir /bottle/pubmedgenerate
hadoop fs -put pubmedgenerate/exampleSeed.txt /bottle/pubmedgenerate
# sshfs andrew@162.13.18.79:/home/andrew/bottle /home/andrew/remote/


hadoop jar jars/bottle-all.jar com.geneix.bottle.MapReduceRunner com.geneix.bottle.PubMedCount swift://pubmed-data.rack-lon/pharmacogeneticsData /bottle/wordcount/output


yarn logs -applicationId application_1414593539759_0006 > logs.txt

hadoop jar jars/bottle-all.jar com.geneix.bottle.MapReduceRunner com.geneix.bottle.GeneratePubMedData 200000 swift://pubmed-data.rack-lon/output/1/
