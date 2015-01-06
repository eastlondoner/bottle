define(['angular', 'servicesModule', 'classes', 'Job'], function (angular, services, classes, Job) {
        'use strict';
        /**
         * The container service is responsible for getting information about
         * JAR files. These are stored in a special container and we just use whatever
         * containerService is currently configured to get them.
         */

        var testJobs = [
            {
                id: "1",
                name: "job1"
            }
        ];
        services.factory('jobService', ['$http', '$q', function ($http, $q) {
                return {
                    getJobs:  function() {
                        return $q(function (resolve, reject) {
                            setTimeout(function () {
                                resolve(_.map(testJobs, function (data) {
                                    return new Job(data);
                                }));
                            }, 500);
                        });
                    }
                };
            }
        ]);
    }
);