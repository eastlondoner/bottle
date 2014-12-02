var PythonShell = require('python-shell'),
    _ = require('underscore'),
    fs = require('fs'),
    stream = require('stream'),
    ss = require('stream-stream'),
    ip = require('ip'),
    express = require('express');

var config = (function(){ //self executing function for closure
    var config = require('config');
    var moduleName = 'datacloud';
    var defaults = {
        PORT: 8010,
        POST_INSTALL_SCRIPT_DIR: "./postinstallscripts/"
    };
    config.util.setModuleDefaults(moduleName, defaults);
    return {
        get : function(property){
            if(_.isUndefined(defaults[property])){
                console.warn("No default set for property " + property + " in module " + moduleName);
            }
            return config.get(moduleName + '.' + property);
        }
    }
})();



var options = {
    mode: 'text',
    pythonOptions: ['-u'],
    scriptPath: __dirname,
    args: ['value1', 'value2', 'value3']
};

var startupArguments = {
    rack_region: "LON",
//cluster_name - this defaults to the job id

    input_data_container: "scorpio",
//    input_file: "flows-10g.tsv", - there is no point having a default of this
//  jar_container - this is supplied from elsewhere
//    output_data_container: "benchmark-out",
//    output_folder: "results",  -use job id by default
    status_file: "COMPLETED",
//    input_jar: "FlowsCount.jar", - there is no point having a default of this
    mr_options: ""
};


var getPostInstallScriptDir = _.once(function(){
    return config.get("POST_INSTALL_SCRIPT_DIR");
});

if (!fs.existsSync(getPostInstallScriptDir())) {
    console.log("Could not find post install script dir ... creating it");
    fs.mkdirSync(getPostInstallScriptDir());
    console.log("Created post install script dir");
}

//Host the post install scripts directory
var app = express();
app.use('/', express.static(getPostInstallScriptDir()));
var PORT = config.get("PORT");
app.listen(PORT, function () {
    console.log("POST INSTALL SCRIPT server listening on port: " + PORT)
});


var getServiceNetIpAddress = _.once(function () {
    return ip.address('private');
});

function getPostInstallScriptFilename(jobId) {
    return jobId + ".sh";
}

function getPostInstallScriptPath(jobId) {
    return getPostInstallScriptDir() + getPostInstallScriptFilename(jobId);
}

function DataCloud(config) {
    this.config = _.extend(options, config || JSON.parse(fs.readFileSync("./config/python.json")));
}

function getPostInstallScriptStream(opts) {

    var outStream = new ss();
    var s = new stream.Readable();

    s.push("#!/bin/sh\n");
    _.forEach(opts, function (value, key) {
        var keyStr = key + "=\"" + value.toString() + "\"\n";
        console.log("Writing post install script property: " + keyStr);
        s.push(keyStr); //TODO escape quotations & etc!
    });
    s.push(null);

    outStream.write(s);

    outStream.write(fs.createReadStream(__dirname + "/cbd_postinstall_and_forget_10g.sh"));
    outStream.end();
    return outStream;
}


function getPostInstallScriptUrl(jobId) {
    return "http://" + getServiceNetIpAddress() + ":" + PORT + "/" + getPostInstallScriptFilename(jobId);
}


DataCloud.prototype.writePostInstallScript = function (jobId, opts, cb) {
    console.log("Write install script called");
    try {
        var self = this;

        var dest = fs.createWriteStream(getPostInstallScriptPath(jobId));
        opts = _.extend({}, startupArguments, opts); // we copy the options object here so if it gets mutated externally its not an issue

        //Set the output folder to use the job id by default
        if(!opts.output_folder) opts.output_folder = jobId;


        if (!opts.input_jar) {
            return cb(new Error("Input Jar required"));
        }
        if (!opts.input_file) {
            return cb(new Error("Input file required"));
        }

        if (!opts.cluster_name) opts.cluster_name = jobId;
        if (!opts.output_data_container) opts.output_data_container = opts.input_data_container + "-out";

        var callBackOnce = _.once(cb);
        dest.on("error", function(err){
            callBackOnce(err);
        });
        dest.on("finish", function () {
            console.log("finished writing post install script");
            self._postInstallScriptUrl = getPostInstallScriptUrl(jobId);
            callBackOnce();
        });
        getPostInstallScriptStream(opts).pipe(dest);
    } catch(e){
        cb(e);
    }
};

DataCloud.prototype.startCluster = function (opts, cb) {
    console.log("StartCluster called");
    var envVars = {
        "OS_USERNAME": opts.username,
        "OS_PASSWORD": opts.password,
        "OS_REGION_NAME": opts.region || "LON",
        "post_install_script" : this._postInstallScriptUrl
    };

    _.extend(envVars, opts); // copy options to local var for safety

    //Delete these because they don't match the naming of vars in the python
    delete envVars.username;
    delete envVars.password;
    delete envVars.region;
    
    var process = PythonShell.run('cbd_fire_and_forget.py', _.extend(this.config, {
        env: envVars
    }), function (err, results) {
        if (err) return cb(err);
        // results is an array consisting of messages collected during execution
        console.log('results: %j', results);
        cb();
    });
    process.on("message", console.log);
    console.log("python script started");
};


module.exports = DataCloud;