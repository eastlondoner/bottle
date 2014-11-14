define(['angular', 'controllersModule'], function (angular, controllers) {
    'use strict';
    /**
     * Allow the user to find a patient via their number and confirm
     * that they have the correct patient
     */
    controllers.controller('ScorpioBaseController', function ($scope, $rootScope, $state, iaLoadingSpinner, iaModalSheet) {

        $scope.jobs = "JOBS";
        $scope.containers = "CONTAINERS";
        $scope.jars = "JARS";
        $scope.selected = {};

        $scope.listContainers = function () {
            iaLoadingSpinner.show();
            $state.go('listContainers', {}).then(iaLoadingSpinner.hide);
        };

        $scope.listJars = function () {
            iaLoadingSpinner.show();
            $state.go('listJars', {}).then(iaLoadingSpinner.hide).catch(function (err) {
                console.error(err);
            });
        };

    });

});
