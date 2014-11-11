var PythonShell = require('python-shell'),
    _ = require('underscore');

var options = {
    mode: 'text',
    pythonOptions: ['-u'],
    scriptPath: __dirname,
    args: ['value1', 'value2', 'value3']
};



function DataCloud(config){

    this.config = _.extend(options, config || JSON.parse(fs.readFileSync("./config/python.json")));
}

DataCloud.prototype.startServer = function() {
    var process = PythonShell.run('cbd_script.py', _.extend(this.config,{
        env: {
            "OS_USERNAME": "",
            "OS_PASSWORD": "",
            "OS_REGION_NAME": "LON"
        }
    }), function (err, results) {
        if (err) throw err;
        // results is an array consisting of messages collected during execution
        console.log('results: %j', results);
    });
    process.on("message", console.log);

};


//TEST
new DataCloud({}).startServer();

module.exports = DataCloud;