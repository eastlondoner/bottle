/* * * * *
 * Rackspace Node.js API Library
 *   api.js
 *
 *   This object (Api) represents an entry point to the Rackspace Cloud API via
 *   the authentication gate and service catalog.
 * * */

var https = require('https');

exports.serializeQueryString = function (values) {
    var qsArr = [];
    for (var key in values) {
        if (Object.prototype.toString.call(values[key]) === '[object Array]') {
            var arrStr = '';
            for (var i in values[key]) {
                if (i > 0) arrStr += '&';
                arrStr += key + '=' + values[key][i];
            }
            qsArr.push(arrStr);
        } else {
            qsArr.push(key + "=" + values[key])
        }
    }
    return "?" + qsArr.join("&");
};


exports.callApi = function (opts, data, callback) {
    data = (typeof data === "undefined" ? "" : data);

    var o = {
        port: 443,
        headers: {
            "Content-Type": "application/json"
        }
    };

    for (var opt in opts) {
        o[opt] = opts[opt];
    }

    if (data) {
        o.headers['Content-Length'] = data.length;
    } else {
        delete o.headers['Content-Length'];
    }

    var content = "";

    // debugger;  // Common debug point
    // console.log(o);
    // console.log(data);
    // console.log();


    var req = https.request(o, function (res) {
        var content = "";
        res.on('data', function (chunk) {
            content += chunk;
        });
        res.on('end', function () {
            var obj = {};
            try {
                obj = {
                    "statusCode": res.statusCode,
                    "headers": res.headers,
                    "body": JSON.parse(content)
                };
                callback(null, obj);
            } catch (e) {
                obj = {
                    "statusCode": res.statusCode,
                    "headers": res.headers,
                    "body": content
                };
                return callback(e, obj)
            }
        });
    });
    if (data) {
        req.end(data);
    } else {
        req.end();
    }
    req.on('error', function (e) {
        console.log("Error calling the Rackspace API: " + e);
        callback(e);
    });
};

exports.Api = function (username, password, authType, authEndpoint) {
    this.username = username;
    this.password = password;
    this.authType = (authType == "api" ? "api" : "password");
    this.authEndpoint = (authEndpoint.toLowerCase() == "uk" ? "lon.identity.api.rackspacecloud.com" : "identity.api.rackspacecloud.com");
    this.access = {};

    this.serializeQueryString = function (values) {
        var qsArr = [];
        for (var key in values) {
            qsArr.push(key + "=" + values[key])
        }
        return "?" + qsArr.join("&");
    };

    this.authenticate = function (callback) {
        if (!this.username) {
            return {"success": false, "message": "No username was supplied"};
        } else if (!this.password) {
            var ret = {"success": false};
            if (this.authType == "api") {
                ret.message = "Authentication type is \"api\", but no API key supplied.";
            } else {
                ret.message = "Authentication type is \"password\", but no password was supplied.";
            }
            return ret;
        } else {
            var data = '{"auth":{"';
            switch (this.authType) {
                case "password":
                    data += 'passwordCredentials":{"username":"' + this.username + '", "password":"' + this.password + '"}}}';
                    break;
                case "api":
                    data += 'RAX-KSKEY:apiKeyCredentials":{"username":"' + this.username + '", "apiKey":"' + this.password + '"}}}';
                    break;
            }

            var o = {
                path: "/v2.0/tokens",
                method: "POST",
                hostname: this.authEndpoint
            };

            exports.callApi(o, data, function (res) {
                var CloudServerOpenStackEndpoint = require('./cloudserveropenstackendpoint.js');
                var CloudFilesEndpoint = require('./cloudfilesendpoint.js');
                var CloudFilesCDNEndpoint = require('./cloudfilescdnendpoint.js');
                var CloudDNSEndpoint = require('./clouddnsendpoint.js');
                var CloudDatabasesEndpoint = require('./clouddatabasesendpoint.js');
                var CloudLoadBalancersEndpoint = require('./cloudloadbalancersendpoint.js');
                var CloudNetworksEndpoint = require('./cloudnetworksendpoint.js');
                var jsonContent = res.body.access;
                this.access = jsonContent;
                var apiObjects = {
                    "cloudServersOpenStack": {}
                };
                var serviceObjects = {};

                for (var i in jsonContent.serviceCatalog) {
                    switch (jsonContent.serviceCatalog[i].name) {
                        case "cloudServersOpenStack":
                            if (!serviceObjects.cloudServersOpenStack)
                                serviceObjects.cloudServersOpenStack = {};
                            if (!serviceObjects.cloudNetworks)
                                serviceObjects.cloudNetworks = {};
                            for (var j in jsonContent.serviceCatalog[i].endpoints) {
                                serviceObjects.cloudServersOpenStack[jsonContent.serviceCatalog[i].endpoints[j].region] =
                                    new CloudServerOpenStackEndpoint.CloudServerOpenStackEndpoint(
                                        jsonContent.token,
                                        jsonContent.serviceCatalog[i].endpoints[j].publicURL
                                    );
                                serviceObjects.cloudNetworks[jsonContent.serviceCatalog[i].endpoints[j].region] =
                                    new CloudNetworksEndpoint.CloudNetworksEndpoint(
                                        jsonContent.serviceCatalog[i].endpoints[j].publicURL,
                                        jsonContent.token.id
                                    );
                            }
                            break;
                        case "cloudFiles":
                            if (!serviceObjects.cloudFiles)
                                serviceObjects.cloudFiles = {};
                            for (var j in jsonContent.serviceCatalog[i].endpoints) {
                                serviceObjects.cloudFiles[jsonContent.serviceCatalog[i].endpoints[j].region] =
                                    new CloudFilesEndpoint.CloudFilesEndpoint(
                                        jsonContent.serviceCatalog[i].endpoints[j].publicURL,
                                        jsonContent.token.id
                                    );
                            }
                            break;
                        case "cloudFilesCDN":
                            if (!serviceObjects.cloudFilesCDN)
                                serviceObjects.cloudFilesCDN = {};
                            for (var j in jsonContent.serviceCatalog[i].endpoints) {
                                serviceObjects.cloudFilesCDN[jsonContent.serviceCatalog[i].endpoints[j].region] =
                                    new CloudFilesCDNEndpoint.CloudFilesCDNEndpoint(
                                        jsonContent.serviceCatalog[i].endpoints[j].publicURL,
                                        jsonContent.token.id
                                    );
                            }
                            break;
                        case "cloudDNS":
                            if (!serviceObjects.cloudDNS)
                                serviceObjects.cloudDNS = {};
                            for (var j in jsonContent.serviceCatalog[i].endpoints) {
                                serviceObjects.cloudDNS =
                                    new CloudDNSEndpoint.CloudDNSEndpoint(
                                        jsonContent.serviceCatalog[i].endpoints[j].publicURL,
                                        jsonContent.token.id
                                    );
                            }
                            break;
                        case "cloudDatabases":
                            if (!serviceObjects.cloudDatabases)
                                serviceObjects.cloudDatabases = {};
                            for (var j in jsonContent.serviceCatalog[i].endpoints) {
                                serviceObjects.cloudDatabases[jsonContent.serviceCatalog[i].endpoints[j].region] =
                                    new CloudDatabasesEndpoint.CloudDatabasesEndpoint(
                                        jsonContent.serviceCatalog[i].endpoints[j].publicURL,
                                        jsonContent.token.id
                                    );
                            }
                            break;
                        case "cloudLoadBalancers":
                            if (!serviceObjects.cloudLoadBalancers)
                                serviceObjects.cloudLoadBalancers = {};
                            for (var j in jsonContent.serviceCatalog[i].endpoints) {
                                serviceObjects.cloudLoadBalancers[jsonContent.serviceCatalog[i].endpoints[j].region] =
                                    new CloudLoadBalancersEndpoint.CloudLoadBalancersEndpoint(
                                        jsonContent.serviceCatalog[i].endpoints[j].publicURL,
                                        jsonContent.token.id
                                    );
                            }
                            break;
                    }
                }
                callback(jsonContent, serviceObjects);
            });


        }
    };

    this.v1authenticate = function (callback) {
        if (!this.username) {
            return {"success": false, "message": "No username was supplied"};
        } else if (!this.password) {
            var ret = {"success": false};
            ret.message = "For version 1.0 authentication, an api key is required.";
            return ret;
        } else {
            var o = {
                path: "/v1.0/",
                method: "GET",
                hostname: this.authEndpoint,
                headers: {
                    'X-Auth-User': this.username,
                    'X-Auth-Key': this.password
                }
            };

            exports.callApi(o, null, function (res) {
                var CloudFilesEndpoint = require('./cloudfilesendpoint.js');
                if (res.statusCode == 204) {
                    // Successful auth
                    var serviceObjects = {
                        "cloudFiles": new CloudFilesEndpoint.CloudFilesEndpoint(
                            res.headers['x-storage-url'],
                            res.headers['x-auth-token']
                        )
                    };
                    callback(serviceObjects);
                } else {

                }
            });
        }
    };
}
