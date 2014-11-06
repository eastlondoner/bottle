define(
    [
        'angular',
        'controllersModule'
    ],

    function (angular, controllers) {
        'use strict';
        /**
         * Allow the user to find a patient via their number and confirm
         * that they have the correct patient
         */
        controllers.controller('ListContainersController', function ($scope, $rootScope, $state, containers, iaLoadingSpinner, iaModalSheet) {

            $scope.containers = containers;

            $scope.listDataFiles = function (name) {
                iaLoadingSpinner.show();
                $state.go('listDataFiles', {
                    containerId: name
                }).then(iaLoadingSpinner.hide);
            };
        });
    }
);