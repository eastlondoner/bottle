var DataCloud = require('./index.js'),
    _ = require('underscore');

var dataCloud = new DataCloud();
var credentials = {
    username: USERNAME,
    password: PASSWORD
};
var jobId = "testJob";

function handleError(err) {
    if (err) {
        console.error(err);
        return true;
    }
}

var props = {
    jar_container: "z_DO_NOT_DELETE_scorpio_JARS",
    cluster_name: jobId,
    input_jar: "bottle-all.jar",
    input_data_container: "pubmed-data",
    input_file: "pharmacogeneticsData"
};

dataCloud.writePostInstallScript(
    jobId,
    props,
    function (err) {
        if (!handleError(err)) {
            console.log("wrote install script for job: " + jobId);
            try {
                dataCloud.startCluster(
                    _.extend(props, credentials),
                    function (err) {
                        if (!handleError(err)) {
                            //TODO redirect to job started state
                            console.log("SUCCESS");
                            //res.redirect("./#/jobStarted");
                        }
                    }
                );
            } catch (e) {
                console.error("error triggering cluster");
                handleError(e);
            }
        }
    }
);