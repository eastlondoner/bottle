var pkgcloud = require('pkgcloud'),
    fs = require("fs"),
    ss = require('stream-stream'),
    _ = require('underscore');

function handleError(err, msg) {
    if (err) {
        console.error(msg);
        console.error(err);
        return true;
    }
}

function CloudFileManager(config, opts) {
    var config = config || JSON.parse(fs.readFileSync("../.config/rackspace.json"));
    var opts = _.extend({
        containerFilters: [/^Z_DO_NOT_DELETE/i]
    }, opts || {});
    var rackspaceStorage = pkgcloud.storage.createClient(_.extend({
        provider: 'rackspace',
        region: 'LON'
    }, config));

    function uploadWrapper(opts, cb) {
        var wrappedCallback;
        if (cb) {
            wrappedCallback = function (err, result) {
                if (err) {
                    console.error(err);
                    return cb(err);
                }
                console.log("UPLOAD COMPLETE");
                console.log(result);
            }
        }
        return rackspaceStorage.upload(opts, wrappedCallback);
    }

    this.getFileAsStream = function (fileName, containerName) {
        var opts = {
            remote: fileName, // name of the new file
            container: containerName // this can be either the name or an instance of container
        };
        return uploadWrapper(opts); // This approach (returning a stream) doesnt take a callback
    };

// This returns a 'stream stream' to which you write readable streams
// in this respect it acts like an open socket that you can keep writing streams to until you choose to close it
    this.getFileAsSocket = function (fileName, containerName, cb) {
        var outStream = new ss();
        var opts = {
            remote: fileName,
            container: containerName,
            stream: outStream
        };
        cb(null, outStream);
        return outStream;
    };

    this.getContainerFiles = function (containerName, cb) {
        rackspaceStorage.getFiles(containerName, function (err, files) {
            if (handleError(err, 'Error getting container files')) {
                cb(err)
            }
            cb(err, files);
        })
    };

    this.getContainerFileNames = function (containerName, cb) {
        this.getContainerFiles(containerName, function (err, files) {
            cb(err, _.pluck(files, 'name'));
        });
    };

    this.getContainers = function (cb) {
        rackspaceStorage.getContainers(function (err, containers) {
            if (handleError(err, 'Error getting containers')) {
                return cb(err);
            }
            containers = _.filter(containers,
                function (container) {
                    var name = container.name;
                    return !_.isUndefined(name) && !_.isEmpty(name) && !_.any(opts.containerFilters, function (filter) {
                        return filter.test(name);
                    });
                }
            );
            cb(err, containers);
        })
    };

    this.getContainerNames = function (cb) {
        this.getContainers(function (err, containers) {
            cb(err, _.pluck(containers, 'name'));
        });
    };

    this.createContainer = function (containerName, cb) {
        rackspaceStorage.createContainer(containerName, function (err, files) {
            if (handleError(err, 'Error creating container')) err(err);
            cb(err, files);
        })
    }

}
module.exports = CloudFileManager;