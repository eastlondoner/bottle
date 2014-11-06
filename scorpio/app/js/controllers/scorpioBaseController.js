define(['angular', 'controllersModule'], function (angular, controllers) {
    'use strict';
    /**
     * Allow the user to find a patient via their number and confirm
     * that they have the correct patient
     */
    controllers.controller('ScorpioBaseController', function ($scope, $rootScope, $state, iaLoadingSpinner, iaModalSheet) {

        $scope.listContainers = function () {
            iaLoadingSpinner.show();
            $state.go('listContainers', {}).then(iaLoadingSpinner.hide);
        };

        $scope.listJars = function () {
            iaLoadingSpinner.show();
            $state.go('listJars', {}).then(iaLoadingSpinner.hide);
        };

    });

});
