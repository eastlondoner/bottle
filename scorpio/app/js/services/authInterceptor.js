define(
    [
        'angular',
        'servicesModule'
    ],
    function (angular, services) {
        'use strict';
        /**
         * The Auth Interceptor service is responsible for intercepting ajax requests and
         * redirecting to login page as necessary
         */


        services.factory('AuthInterceptor', ['$window', '$location', '$q', function ($window, $location, $q) {

            return {
                responseError: function(rejection){
                    if(rejection.status == 401){
                        window.location = "/login";
                    }
                    return $q.reject(rejection);
                }
            };
        }
        ])
        ;
    })
;
