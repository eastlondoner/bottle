define(
    [
        'angular',
        'servicesModule',
        'File',
        'Container'
    ],
    function (angular, services, File, Container) {
        'use strict';
        /**
         * The container service is responsible for getting information about
         * Cloud Files containers and the files that they contain
         */

        if (TEST) return;

        function alertError(response, detail) {
            if(response.status === 401){
                //We handle this elsewhere!
                return;
            }
            alert(
                    detail + "\n" +
                    "Received error status: " + response.status + "\n" +
                    "Data from server:" + response.data
            );
        }

        services.factory('containerService', ['$http', '$q', '$upload', function ($http, $q, $upload) {

            function getFilesInContainer(containerId) {
                return $q(function (resolve, reject) {
                    $http.get("/containers/" + containerId).
                        success(function (data) {
                            resolve(_.map(data || [], File.build));
                        }).
                        catch(function (response) {
                            alertError(response, "Retrieving files for container: " + containerId);
                            resolve([]);
                        });
                });
            }

            return {
                getContainers: function () {
                    return $q(function (resolve, reject) {
                        $http.get("/containers").
                            success(function (data) {
                                resolve(_.map(data || [], Container.build));
                            }).
                            catch(function (response) {
                                alertError(response, "Retrieving container list.");
                                resolve([]);
                            });
                    });
                },
                getContainer: function (containerId) {
                    return new Container({name: containerId, id: containerId})
                },
                getFilesInContainer: getFilesInContainer,
                getFileInContainer: function (fileId, containerId) {
                    return $q(function (resolve, reject) {
                        $http.get("/containers/" + containerId + "/" + fileId).
                            success(_.compose(resolve, File.build)).
                            catch(function (response) {
                                alertError(response, "Retrieving file: " + fileId);
                                resolve([]);
                            });
                    });
                },
                createContainer: function (containerId, cb) {
                    if (!cb) cb = _.identity;
                    return $http.put("/containers/" + containerId).
                        success(_.partial(cb, null)).
                        catch(function (response) {
                            alertError(response, "Creating container: " + containerId);
                            cb(response);
                            return null;
                        });
                },
                deleteContainer: function (containerId, cb) {
                    if (!cb) cb = _.identity;
                    return $http.delete("/containers/" + containerId).
                        success(_.partial(cb, null)).
                        catch(function (response) {
                            alertError(response, "Deleting container: " + containerId);
                            cb(response);
                            return null;
                        });
                },
                deleteFileInContainer: function (fileId, containerId, cb) {
                    if (!cb) cb = _.identity;
                    return $http.delete("/containers/" + containerId + "/" + fileId).
                        success(_.partial(cb, null)).
                        catch(function (response) {
                            alertError(response, "Deleting file: " + fileId);
                            cb(response);
                            return null;
                        });
                },
                uploadFileToContainer: function (file, containerId, cb) {
                    if (!cb) cb = _.identity;
                    return $upload.upload({
                        url: '/containers/' + containerId,
                        method: 'POST', //or 'PUT',
                        //data: {myObj: $scope.myModelObj},
                        file: file // or list of files ($files) for html5 only
                        //fileName: 'doc.jpg' or ['1.jpg', '2.jpg', ...] // to modify the name of the file(s)
                        // customize file formData name ('Content-Disposition'), server side file variable name.
                        //fileFormDataName: myFile, //or a list of names for multiple files (html5). Default is 'file'
                        // customize how data is added to formData. See #40#issuecomment-28612000 for sample code
                        //formDataAppender: function(formData, key, val){}
                    }).progress(function (evt) {
                        console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
                    }).success(function (data, status, headers, config) {
                        // file is uploaded successfully
                        // data is the response from the server
                        console.log(data);

                        //Deliberately wait because it takes time for the new file to register with rackspace!

                        var retries = 0;
                        function checkForFile() {
                            if(retries++ > 8){
                                //it's uploaded ok but it's not showing up ... return anyway, user will figure it out
                                return cb(null, data);
                            }
                            getFilesInContainer(containerId).then(function (files) {
                                return _.findWhere(files, {name: file.name})
                            }).then(function (found) {
                                if (found) {
                                    return cb(null, data);
                                }
                                setTimeout(checkForFile,500);
                            })
                        }
                        //Poll till file shows up!
                        checkForFile();
                    }).catch(function (err) {
                        console.error(err);
                        cb(err);
                    });

                    //.then(success, error, progress);
                    // access or attach event listeners to the underlying XMLHttpRequest.
                    //.xhr(function(xhr){xhr.upload.addEventListener(...)})
                }
            };
        }
        ])
        ;
    })
;
