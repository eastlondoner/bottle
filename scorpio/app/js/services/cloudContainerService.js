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

        if(TEST) return;

        function alertError(response, detail) {
            alert(
                    detail + "\n" +
                    "Received error status: " + response.status + "\n" +
                    "Data from server:" + response.data
            );
        }

        services.factory('containerService', ['$http', '$q', '$upload', function ($http, $q, $upload) {
            return {
                getContainers: function () {
                    return $http.get("/containers").
                        success(function (data) {
                            return _.map(data || [], Container.build);
                        }).
                        catch(function (response) {
                            alertError(response, "Retrieving container list.");
                            return [];
                        });
                },
                getContainer: function (containerId) {
                    return new Container({name: containerId, id: containerId})
                },
                getFilesInContainer: function (containerId) {
                    return $http.get("/containers/" + containerId).
                        success(function (data) {
                            return _.map(data || [], File.build);
                        }).
                        catch(function (response) {
                            alertError(response, "Retrieving files for container: " + containerId);
                            return [];
                        });
                },
                getFileInContainer: function (fileId, containerId) {
                    return $http.get("/containers/" + containerId + "/" + fileId).
                        success(File.build).
                        catch(function (response) {
                            alertError(response, "Retrieving file: " + fileId);
                            return [];
                        });
                },
                deleteFileInContainer: function (fileId, containerId, cb) {
                    if(!cb) cb = _.identity;
                    return $http.delete("/containers/" + containerId + "/" + fileId).
                        success(_.partial(cb,null)).
                        catch(function (response) {
                            alertError(response, "Deleting file: " + fileId);
                            cb(response);
                            return null;
                        });
                },
                uploadFileToContainer: function(file, containerId, cb){
                    if(!cb) cb = _.identity;
                    return $upload.upload({
                        url: '/container/'+containerId,
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
                        cb(null, data);
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
