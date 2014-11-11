define(['angular', 'controllersModule'],
    function (angular, controllers) {
        'use strict';
        /**
         * Allow the user to find a patient via their number and confirm
         * that they have the correct patient
         */
        controllers.controller('ListJarsController', function ($scope, $rootScope, $state, jars, jarService, iaLoadingSpinner, iaModalSheet) {
            $scope.jars = jars;
            $scope.jarContainer = jarService.getJarContainer();

            $scope.selected = {value: null};

            $scope.getSelected = function () {
                return $scope.selected.value;
            };


        });

    });
