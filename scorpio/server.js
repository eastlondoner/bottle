/*jslint node: true */
"use strict";

/**
 * Server component of Scorpio. This is mostly just a simple set of
 * wrappers round the packagecloud
 *
 * Currently does NO auth! (for the demo the login is hard coded to
 * trial/trial on the client)
 */

var express = require('express'),
    _ = require('underscore'),
//    nodemailer = require("nodemailer"),
    fs = require('fs'),
    async = require('async'),
    argv = require('optimist').argv,
    Busboy = require('busboy'),
    CloudFileStream = require('cloudfs');

// TODO: Accept a command line argument to set config file
var config = JSON.parse(fs.readFileSync('config/serverConfig.json'));

var rackspaceStorage = new CloudFileStream();

//var smtpTransport = nodemailer.createTransport("SMTP", config.mail);

var app = express();

app.use(express.logger());

/// Basic Auth
app.use(express.basicAuth(function (user, pass) {
    return user === 'trial' && pass === 'trial';
}));

// Server the files for the client side app
app.use('/', express.static(__dirname + "/app"));
app.use('/bower_components', express.static(__dirname + "/bower_components"));
app.use('/dist', express.static(__dirname + "/dist"));

// TODO: Config
var port = argv.port || config.server.port;
app.listen(port);
console.log("Listenting on port " + port);

["services", "controllers", "directives", "filters", "classes"].forEach(function (thing) {
    app.get("/app/" + thing, function (req, res) {
        fs.readdir(__dirname + "/app/js/" + thing, function (err, files) {
            files = _.filter(files, function (file) {
                return file.indexOf(".js") > 0;
            });
            if (err) {
                console.error(err);
                return res.status(403);
            }
            res.json(files);
        })
    });
});

app.post("/containers/:container", function (req, res) {
    console.log('upload recieved');

    var busboy = new Busboy({ headers: req.headers });
    busboy.on('file', function (fieldname, file, filename, encoding, mimetype) {
        var uploadStream = rackspaceStorage.getFileAsWritableStream(filename, req.params.container);
        console.log('File [' + fieldname + ']: filename: ' + filename + ', encoding: ' + encoding + ', mimetype: ' + mimetype);
        file.on('end', function () {
            uploadStream.end();
            console.log('File [' + fieldname + '] Finished');
        });

        file.pipe(uploadStream)
    });
    busboy.on('field', function (fieldname, val, fieldnameTruncated, valTruncated) {
        console.log('Field [' + fieldname + ']: value: ' + inspect(val));
    });
    busboy.on('finish', function () {
        console.log('Done parsing form!');
        res.writeHead(200, { Connection: 'close'});
        res.end();
    });
    req.pipe(busboy);
});

app.get("/containers/:container", function (req, res) {
    var containerName = req.params.container;
    rackspaceStorage.getContainerFiles(containerName, function(err, files){
        if(!handleError(res,err)){
            res.json(files);
        }
    })
});

app.get("/containers", function (req, res) {
    rackspaceStorage.getContainers(function(err, containers){
        if(!handleError(res,err)){
            res.json(containers);
        }
    })
});


app.put("/containers/:container", function (req, res) {
    var containerName = req.params.container;
    rackspaceStorage.createContainer(containerName, function(err, container){
        if(!handleError(res,err)){
            res.status(201);
        }
    })
});

app.get("/containers/:container/:file", function (req, res) {
    var containerName = req.params.container;
    var fileName = req.params.file;
    var stream = rackspaceStorage.getFileAsReadableStream(fileName, containerName, function(err){
        handleError(res,err)
    });
    stream.pipe(res);
});

function handleError(res, error){
    if(error){
        if(error instanceof Error){
            if(error.code == "ENOTFOUND"){
                res.status(503);
            }
        } else{
            res.status(500);
            return true;
        }
    }
}