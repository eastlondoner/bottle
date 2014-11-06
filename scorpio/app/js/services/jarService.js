define(['angular', 'servicesModule'], function (angular, services) {
    'use strict';
    /**
     * The container service is responsible for getting information about
     * Cloud Files containers and the files that they contain
     */
    services.factory('jarService', ['$http', '$q', function ($http, $q) {
        return {
            getJars: function () {
                return $q(function (resolve, reject) {
                    setTimeout(function () {
                        resolve(_.range(3).map(function (n) {
                            return {id: n, name: "Jar " + n};
                        }));
                    }, 1000)
                });
            }
        };
    }
    ])
    ;
})
;