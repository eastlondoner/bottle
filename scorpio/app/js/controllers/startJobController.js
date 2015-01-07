define(['angular', 'controllersModule'], function (angular, controllers) {
    'use strict';
    /**
     * Allow the user to delete data files from a container
     */
    controllers.controller('StartJobController', function ($scope, $state, jar, container, files, iaModalSheet, containerService, iaLoadingSpinner) {

        $scope.files = files;
        $scope.advancedMode = false;

        $scope.job = {
            form: {}
        };

        $scope.message = "This will START JOB";
        iaModalSheet.show({
            title: "Start Job"
        });

        $scope.jar = jar;
        $scope.container = container;


        $scope.confirm = function(){
            var result = $scope.jobForm;
        }

    });
});
