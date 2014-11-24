var PythonShell = require('python-shell'),
    _ = require('underscore'),
    fs = require('fs'),
    stream = require('stream'),
    ss = require('stream-stream'),
    ip = require('node-ip'),
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
            return config.get(moduleName + property);
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
    output_folder: "results",
    status_file: "COMPLETED",
//    input_jar: "FlowsCount.jar", - there is no point having a default of this
    mr_options: ""
};


var getPostInstallScriptDir = _.once(function(){
    config.get("POST_INSTALL_SCRIPT_DIR");
});

if (!fs.existsSync(getPostInstallScriptDir())) {
    console.log("Could not find post install script dir ... creating it");
    fs.mkdirSync(getPostInstallScriptDir());
    console.log("Created post install script dir");
}

//Host the post install scripts directory
var app = express();
app.use('/', express.static(getPostInstallScriptDir()));
app.listen(config.get("PORT"), function () {
    console.log("POST INSTALL SCRIPT server listening on port: " + PORT)
});


var getServiceNetIpAddress = _.once(function () {
    return ip.address('private');
});

function getPostInstallScriptPath(jobId) {
    return getPostInstallScriptDir() + jobId + ".sh";
}

function DataCloud(config) {
    this.config = _.extend(options, config || JSON.parse(fs.readFileSync("./config/python.json")));
}

function getPostInstallScriptStream(opts) {

    var outStream = new ss();
    var s = new stream.Readable();

    s.push("#!/bin/sh");
    _.forEach(opts, function (value, key) {
        s.push(key + "=\"" + value.toString() + "\"") //TODO escape quotations & etc!
    });
    s.push(null);

    outStream.write(s);
    outStream.write(fs.createReadStream(__dirname + "/cbd_postinstall_and_forget_10g.sh"));
    outStream.end();
    return outStream;
}


function getPostInstallScriptUrl(jobId) {
    return "http://" + getServiceNetIpAddress() + ":" + PORT + "/" + getPostInstallScriptPath(jobId);
}


DataCloud.prototype.writePostInstallScript = function (jobId, opts, cb) {
    var dest = fs.createWriteStream(getPostInstallScriptPath(jobId));
    opts = _.extend({}, startupArguments, opts); // we copy the options object here so if it gets mutated externally its not an issue

    if(!opts.input_jar){
        throw new Error("Input Jar required");
    }
    if(!opts.input_file){
        throw new Error("Input file required");
    }

    if(!opts.cluster_name) opts.cluster_name = jobId;
    if(!opts.output_data_container) opts.output_data_container = opts.input_data_container + "-out";


    getPostInstallScriptStream(opts).pipe(dest);
    var callBackOnce = _.once(cb);
    dest.on("error", _.partial(callBackOnce));
    dest.on("end", function () {
        callBackOnce(getPostInstallScriptUrl(jobId));
    });
};


DataCloud.prototype.startCluster = function (opts) {v
    var envVars = {
        "OS_USERNAME": opts.username,
        "OS_PASSWORD": opts.password,
        "OS_REGION_NAME": opts.region || "LON"
    };

    _.extend(envVars, opts); // copy options to local var for safety

    //Delete these because they don't match the naming of vars in the python
    delete envVars.username;
    delete envVars.password;
    delete envVars.region;

    var process = PythonShell.run('cbd_script.py', _.extend(this.config, {
        env: envVars
    }), function (err, results) {
        if (err) throw err;
        // results is an array consisting of messages collected during execution
        console.log('results: %j', results);
    });
    process.on("message", console.log);

};


module.exports = DataCloud;