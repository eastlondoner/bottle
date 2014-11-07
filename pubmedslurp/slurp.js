var _ = require("underscore"),
    http = require("http"),
    querystring = require("querystring"),
    fs = require("fs"),
    XmlStream = require("xml-stream"),
    events = require('events'),
    pkgcloud = require('pkgcloud'),
    ss = require('stream-stream');

var PUBMED_DB_NAME = "pubmed";
var ID_FILE_NAME = "ids.txt";
var OUT_FILE_NAME = "out/out.txt";

var entrezBaseURL = {
    hostname: "eutils.ncbi.nlm.nih.gov",
    path: "/entrez/eutils/",
    port: 80,
    method: "GET"
};


var idsFile = fs.createWriteStream(ID_FILE_NAME);
var outStream = new ss();


/* Basic Search
 esearch.fcgi?db=<database>&term=<query>

 Input: Entrez database (&db); Any Entrez text query (&term)

 Output: List of UIDs matching the Entrez query
 */
function search(database, query, cb) {
    console.log('SEARCHING: ' + database + ' FOR: ' + query);

    var endpoint = "esearch.fcgi";
    var count = 0;
    var params = {
        db: database,
        term: query,
        retmax: 100000,
        retstart: 0
    };

    var eventEmitter = new events.EventEmitter();

    try {

        function responseCallback(res) {
            try {
                //Logging
                console.log('STATUS: ' + res.statusCode);
                console.log('HEADERS: ' + JSON.stringify(res.headers));
                res.setEncoding('utf8');

                //Let's parse the stream to XML
                var xml = new XmlStream(res);

                //Get total number of matches for search (not number of elements returned)
                xml.once('text:eSearchResult>Count', function (countText) {
                    count = parseInt(countText.$text);
                });

                //On identifying each ID
                xml.on('text:IdList>Id', function (id) {
                    if (!cb(null, id.$text)) {
                        xml.pause();
                        console.warn("pausing stream while output drains");
                        idsFile.once('drain', function () {
                                console.log("RESUMING READ STREAM");
                                xml.resume();
                            }
                        );
                    }
                });

                xml.once('endElement: eSearchResult', _.partial(onResponseEnd, eventEmitter));
                /*
                 res.on('end', function(){
                 console.log("REQUEST CLOSED");
                 });
                 var resCount = 0;
                 res.on('data', function (chunk) {
                 resCount++;
                 console.log('BODY (' + resCount + '): ' + chunk);
                 });
                 */
            } catch (e) {
                console.error(e);
                return cb ? cb(e) : null;
            }
        }

        function makeRequest() {

            var reqOptions = _.extend(
                {},
                entrezBaseURL,
                {
                    path: [entrezBaseURL.path, endpoint, "?", querystring.stringify(params)].join("")
                }
            );
            console.log(reqOptions);
            var req = http.request(reqOptions);
            req.on('response', responseCallback);

            if (cb) {
                req.on('error', cb);
            }

            //req.write( data? )
            req.end();

        }

        function moreWork() {
            return count >= params.retstart + params.retmax;
        }

        function onResponseEnd(eventEmitter) {
            try {
                if (moreWork()) {
                    //more work to do
                } else {
                    //Callback with null to indicate end of stream (nasty?)
                    cb(null, null);
                }
                eventEmitter.emit('end');
            } catch (e) {
                console.error(e);
            }
        }

        makeRequest();

        eventEmitter.on('end', function () {
            if (moreWork()) {
                params.retstart = params.retstart + params.retmax;
                makeRequest();
            } else {
                console.log("RESULTS PROCESSED: " + count);
                eventEmitter.removeAllListeners();
            }
        });


    } catch (e) {
        console.error(e);
        cb(e);
    }
}


/* Basic Search
 esearch.fcgi?db=<database>&term=<query>

 Input: Entrez database (&db); Any Entrez text query (&term)

 Output: List of UIDs matching the Entrez query
 */
function fetch(database, file, cb) {
    var fetchStartTime = process.hrtime()[0];
    console.log('FETCHING RECORDS FROM: ' + database);

    var idStream = new XmlStream(fs.createReadStream(file));
    //idStream.pause();

    var endpoint = "efetch.fcgi";
    var count = 0;
    var params = {
        db: database,
        rettype: "medline",
        retmode: "text"
    };

    var eventEmitter = new events.EventEmitter();

    try {
        function responseCallback(res) {
            try {
                //Logging
                console.log('STATUS: ' + res.statusCode);
                console.log('HEADERS: ' + JSON.stringify(res.headers));
                res.setEncoding('utf8');

                cb(null, res);
                //On data for now
                /*
                 res.on('data', function (data) {
                 //console.log(data);
                 if (!cb(null, data)) {
                 res.pause();
                 console.warn("pausing read while output drains");
                 outStream.once('drain', function () {
                 console.log("RESUMING READ STREAM");
                 res.resume();
                 });
                 }
                 });
                 */

                res.once('end', _.partial(onResponseEnd, eventEmitter));
            } catch (e) {
                console.error(e);
                return cb ? cb(e) : null;
            }
        }

        var batch = [];

        function makeRequest() {

            var reqOptions = _.extend(
                {},
                entrezBaseURL,
                {
                    method: "POST",
                    path: [entrezBaseURL.path, endpoint].join(""),
                    headers: {'Content-Type': "application/x-www-form-urlencoded"}
                }
            );

            function onEnd() {
                if (batch.length > 0) {
                    send();
                }
                moreWorkFlag = false;
                idStream.removeAllListeners();
            }

            idStream.on('endElement:IdList', onEnd);

            function send() {
                var body = querystring.stringify(_.extend(params, {id: batch.join(',')}));
                reqOptions.headers["Content-Length"] = body.length;
                console.log(reqOptions);
                var req = http.request(reqOptions);
                req.on('response', responseCallback);

                if (cb) {
                    req.on('error', cb);
                }

                req.end(body);
                count += batch.length;
                batch = [];
            }

            idStream.on('text:Id', function (element) {
                batch.push(element.$text);
                if (batch.length >= 10000) {
                    send();
                    idStream.pause();
                    idStream.removeAllListeners('text:Id');
                }
            });
        }

        var moreWorkFlag = true;

        function moreWork() {
            return moreWorkFlag;
        }

        function onResponseEnd(eventEmitter) {
            try {
                if (moreWork()) {
                    //more work to do
                } else {
                    //Callback with null to indicate end of stream (nasty?)
                    cb(null, null);
                }
                eventEmitter.emit('end');
            } catch (e) {
                console.error(e);
            }
        }

        makeRequest();

        eventEmitter.on('end', function () {
            var timeTaken = process.hrtime()[0] - fetchStartTime;
            console.log("%d BYTES WRITTEN IN %d SECONDS", uploadSocket.bytesWritten, timeTaken);
            console.log("%d MB/s", (uploadSocket.bytesWritten / 1000000) / timeTaken);
            if (moreWork()) {
                console.log("PROCESSING: " + count);
                //params.retstart = params.retstart + params.retmax;
                //makeRequest();
                idStream.resume();
            } else {
                console.log("ALL RESULTS PROCESSED: " + count);
                eventEmitter.removeAllListeners();
            }
        });

    } catch (e) {
        console.error(e);
        cb(e);
    }
}

function fetchCallback(err, response) {
    try {
        if (err) {
            console.log("Something went wrong");
            console.error(err);
            return null;
        }

        if (response === null) {
            //outStream.write("</ResultList>");
            return outStream.end();
        }

        outStream.write(response);
        //return outStream.write(["<Result>",data,"</Result>\n"].join(''));
    } catch (e) {
        console.error(e);
    }
}

var uploadSocket;

function getCloudFile(options, cb) {

    var config = JSON.parse(fs.readFileSync("../.config/rackspace.json"));
    var rackspace = pkgcloud.storage.createClient(_.extend(config, {
        provider: 'rackspace',
        region: 'LON'
    }));
    rackspace.createContainer('pubmed-data', function (err, container) {
        if (err) {
            console.error(err);
            return cb(err);
        }
        console.log("CONTAINER CREATE COMPLETE");
        console.log(container);

        uploadSocket = rackspace.upload({
                container: container, // this can be either the name or an instance of container
                remote: '2013Data', // name of the new file,
                stream: outStream
            },
            function (err, result) {
                if (err) {
                    console.error(err);
                    return cb(err);
                }
                console.log("UPLOAD COMPLETE");
                console.log(result);
            });
        cb(null, uploadSocket);
    });
}

idsFile.on("open", function () {
    console.log("FILE OPENED");
    idsFile.write("<IdList>\n");
    search(PUBMED_DB_NAME,'("2013/1/1"[Date - Publication] : "2014/1/1"[Date - Publication])', function(err, data){
    //search(PUBMED_DB_NAME, '(mouse[title])', function (err, data) {
        try {
            if (err) {
                console.log("Something went wrong");
                return null;
            }

            if (data === null) {
                idsFile.write("</IdList>");
                return idsFile.end();
            }
            return idsFile.write(["<Id>", data, "</Id>\n"].join(''));
        } catch (e) {
            console.error(e);
        }
    });
});

idsFile.on("error", function (e) {
    console.error(e);
});

idsFile.on("finish", function () {
    console.log("FILE CLOSED");
    //outStream.write("<ResultList>");
    getCloudFile({},function(err, writableStream){
        if (err) {
            console.error(err);
            throw err;
        }
        fetch(PUBMED_DB_NAME, ID_FILE_NAME, fetchCallback);
    })
});


//fetch(PUBMED_DB_NAME, ID_FILE_NAME, fetchCallback);


