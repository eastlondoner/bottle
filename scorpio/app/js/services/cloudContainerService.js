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

        services.factory('containerService', ['$http', '$q', function ($http, $q) {
            return {
                getContainers: function () {
                    return $http.get("/containers").then(function(containers){
                        return _.map(containers.data, function(entry){
                            return new Container(entry);
                        })
                    });
                },
                getContainer: function (containerId) {
                    return new Container({name: containerId, id: containerId})
                },
                getFilesInContainer: function (containerId) {
                    return $http.get("/containers/"+containerId).then(function(result){
                        return _.map(result.data, function (entry){return new File(entry)});
                    });
                },
                getFileInContainer: function (fileId, containerId) {
                    return $http.get("/containers/"+containerId + "/" + fileId);
                },
                deleteFileInContainer: function (fileId, containerId) {
                    return $q(function (resolve, reject) {
                        setTimeout(function () {
                            testContainers[containerId] = _.reject(testContainers[containerId], function (item) {
                                return item.id === fileId
                            });
                            resolve();
                        }, 500)
                    })
                }
            };

        }
        ])
        ;
    })
;
