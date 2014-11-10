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

        if (!TEST) return;

        function generateTestData(N) {
            return _.range(N).map(function (n) {
                return {
                    name: "File " + n,
                    id: n.toString()
                };
            });
        }

        var testContainers = {
            "a": generateTestData(3),
            "b": generateTestData(5),
            "c": generateTestData(2),
            "test_container": [],
            "z_DO_NOT_DELETE_scorpio_JARS": [
                {id: "1", name: "JAR 1"},
                {id: "1", name: "JAR 2"},
                {id: "1", name: "JAR 3"}
            ]
        };

        services.factory('containerService', ['$http', '$q', function ($http, $q) {
            return {
                getContainers: function () {
                    return $q(function (resolve, reject) {
                        setTimeout(function () {
                            resolve(_.chain(testContainers).omit("z_DO_NOT_DELETE_scorpio_JARS").map(function (v, k) {
                                return new Container({name: "Container " + k, id: k});
                            }).value());
                        }, 500)
                    });
                },
                getContainer: function (containerId) {
                    return $q(function (resolve, reject) {
                        setTimeout(function () {
                            try {
                                resolve(new Container({name: "Container " + containerId, id: containerId}));
                            } catch (e) {
                                console.error(e);
                            }
                        }, 500)
                    });
                },
                getFilesInContainer: function (containerId) {
                    return $q(function (resolve, reject) {
                        setTimeout(function () {
                            resolve(_.map(testContainers[containerId], function (data) {
                                return new File(_.extend(data, {containerId: containerId}));
                            }));
                        }, 500);
                    });
                },
                getFileInContainer: function (fileId, containerId) {
                    return $q(function (resolve, reject) {
                        setTimeout(function () {
                            try {
                                resolve(
                                    new File(_.extend(_.findWhere(testContainers[containerId], {id: fileId}), {containerId: containerId}))
                                );
                            } catch (e) {
                                console.log(e);
                            }
                        }, 500);
                    });
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
        }]);
    }
);
