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
    Busboy = require('busboy'),
    CloudFileStream = require('cloudfs'),
    session = require('express-session'),
    crypto = require("cryptiles"),
    bodyParser = require('body-parser'),
    DataCloud = require('datacloud'),
    uuidProvider = require('node-uuid'),
    config = require('config')
    ;

// TODO: Accept a command line argument to set config file
config = (function () { //self executing function for closure
    var config = require('config');
    var moduleName = 'scorpio';
    var defaults = {
        PORT: 8000,
        jarContainer: "z_DO_NOT_DELETE_scorpio_JARS"
    };
    config.util.setModuleDefaults(moduleName, defaults);
    return {
        get: function (property) {
            if (_.isUndefined(defaults[property])) {
                console.warn("No default set for property " + property + " in module " + moduleName);
            }
            return config.get(moduleName + '.' + property);
        }
    }
})();


//var smtpTransport = nodemailer.createTransport("SMTP", config.mail);
var formBodyParser = bodyParser.urlencoded({extended: false});

var app = express();

app.use(express.logger());


app.use(session({
    secret: crypto.randomString(36),
    cookie: { secure: false, maxAge: 600000 }
}));

//redirect to login if you hve no session
function getRackspaceCredentials(req) {
    return {username: req.session.rackspace.username, password: req.session.rackspace.password};
}
function setRackspaceStorage(req) {
    req.rackspaceStorage = new CloudFileStream(getRackspaceCredentials(req));
}
app.use(function (req, res, next) {
    if (!req.session || !req.session.rackspace) {
        if (req.path == '/login') {
            return next();
        }
        res.redirect("/login");
    } else {
        setRackspaceStorage(req);
        next();
    }
});


// Server the files for the client side app
app.use('/', express.static(__dirname + "/app"));
app.use('/bower_components', express.static(__dirname + "/bower_components"));
app.use('/dist', express.static(__dirname + "/dist"));

// TODO: Config
var port = config.get("PORT");
app.listen(port, function () {
    console.log("Listening on port " + port);
});


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
        var uploadStream = req.rackspaceStorage.getFileAsWritableStream(filename, req.params.container);
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
    req.rackspaceStorage.getContainerFiles(containerName, function (err, files) {
        if (!handleError(res, err)) {
            res.json(files);
        }
    })
});

app.get("/containers", function (req, res) {
    req.rackspaceStorage.getContainers(function (err, containers) {
        if (!handleError(res, err)) {
            res.json(containers);
        }
    })
});


app.put("/containers/:container", function (req, res) {
    var containerName = req.params.container;
    req.rackspaceStorage.createContainer(containerName, function (err, container) {
        if (!handleError(res, err)) {
            res.status(201);
        }
    })
});

app.get("/containers/:container/:file", function (req, res) {
    var containerName = req.params.container;
    var fileName = req.params.file;
    req.rackspaceStorage.getFileDetails(fileName, containerName, function (err, file) {
        if (!handleError(res, err)) {
            res.json(file);
        }
    });
});

app.get("/containers/:container/download/:file", function (req, res) {
    var containerName = req.params.container;
    var fileName = req.params.file;
    var stream = req.rackspaceStorage.getFileAsReadableStream(fileName, containerName, function (err) {
        handleError(res, err)
    });
    stream.pipe(res);
});

app.delete("/containers/:container/:file", function (req, res) {
    var containerName = req.params.container;
    var fileName = req.params.file;
    req.rackspaceStorage.removeFile(fileName, containerName, function (err) {
        if (!handleError(res, err)) {
            res.status(204);
        }
        res.end();
    });
});

app.post("/clusters", formBodyParser, function (req, res) {
    var postBody = _.omit(req.body, function (value, key, object) {
        return value === "" || _.isNull(value) || _.isUndefined(value) || _.isNaN(value);
    });
    var dataCloud = new DataCloud();
    var credentials = getRackspaceCredentials(req);
    var jobId = uuidProvider.v1();

    if (!postBody.cluster_name) postBody.cluster_name = jobId;
    dataCloud.writePostInstallScript(
        jobId,
        _.extend({
            jar_container: config.get("jarContainer")
        }, postBody),
        function (err) {
            if (!handleError(res, err)) {
                console.log("wrote install script for job: " + jobId);
                try {
                    var job = dataCloud.startCluster(jobId, _.extend(postBody, credentials));
                    job.on("error", function(err){
                        handleError(err);
                    });
                    job.on("auth", function(){
                        res.redirect("~/#/?jobId="+encodeURIComponent(jobId));
                    });
                    job.on("delete", function(){
                        console.log("job " + jobId + " complete!");
                    });
                } catch (e) {
                    console.error("error triggering cluster");
                    handleError(res, e);
                }
            }
        }
    );

});


app.post("/login", formBodyParser, function (req, res) {
    req.session.rackspace = req.body;
    if (req.session.rackspace.username === "TEST") {
        console.log("TEST MODE");
        res.redirect("/");
        return;
    }
    setRackspaceStorage(req);
    req.rackspaceStorage.createContainerIfNotExists(config.get("jarContainer"), function (err, container) {
        if (err) {
            if (err.statusCode === 401 || err.failcode === "Unauthorized") {
                req.session.destroy();
                return res.redirect("/login?error=");
            }
            throw err;
        } else {
            console.log("jar container found");
            res.redirect("/");
        }
    });
});
app.get("/login", function (req, res) {
    res.sendfile(__dirname + "/app/partials/login.html")
});
app.get("/logout", function (req, res) {
    res.session.destroy();
});

function handleError(res, error) {
    if (error) {
        var status = 500;
        if (error instanceof Error) {
            if (error.code == "ENOTFOUND") {
                status = 503;
            }
        }
        res.status(status);
        return true;
    }
}

