define(['angular', 'controllersModule'],
    function (angular, controllers) {
        'use strict';
        /**
         * Allow the user to find a patient via their number and confirm
         * that they have the correct patient
         */
        controllers.controller('ListJobsController', function ($scope, $rootScope, $state, jobs, iaLoadingSpinner, iaModalSheet) {
            $scope.jobs = jobs;

            $scope.selected = {value: null};

            $scope.getSelected = function () {
                return $scope.selected.value;
            };
        });
    });
