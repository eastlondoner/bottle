var PythonShell = require('python-shell'),
    _ = require('underscore'),
    fs = require('fs'),
    stream = require('stream'),
    ss = require('stream-stream'),
    ip = require('node-ip');

var options = {
    mode: 'text',
    pythonOptions: ['-u'],
    scriptPath: __dirname,
    args: ['value1', 'value2', 'value3']
};

var startupArguments = {
    rack_region: "LON",
    cluster_name: "alex-fire-test",

    input_data_container: "benchmark",
    input_file: "flows-10g.tsv",
//  jar_container - this is supplied from the config
    //TODO create a config module
    output_data_container: "benchmark-out",
    output_folder: "flows-10g-out",
    status_file: "COMPLETED",
    input_jar: "FlowsCount.jar",
    mr_options: "FlowsCount -D mapred.reduce.tasks: 2"
};

var getServiceNetIpAddress = _.once(function(){
    ip.address('private')
});

function DataCloud(config) {

    this.config = _.extend(options, config || JSON.parse(fs.readFileSync("./config/python.json")));
}

DataCloud.prototype.getPostInstallScriptStream = function(opts){
    var args = _.extend({}, startupArguments, opts);
    var outStream = new ss();
    var s = new stream.Readable();

    s.push("#!/bin/sh");
    _.forEach(args, function(value, key){
        s.push(key + "=\"" + value.toString() +"\"") //TODO escape quotations & etc!
    });
    s.push(null);

    outStream.write(s);
    outStream.write(fs.createReadStream(__dirname + "\cbd_postinstall_and_forget_10g.sh"));
    outStream.end();
    return outStream;
};


DataCloud.prototype.startCluster = function (opts) {
    var envVars = {
        "OS_USERNAME": opts.username,
        "OS_PASSWORD": opts.password,
        "OS_REGION_NAME": opts.region || "LON"
    };

    //Delete these because they don't match the naming of vars in the python
    delete opts.username;
    delete opts.password;
    delete opts.region;

    _.extend(envVars, opts);

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