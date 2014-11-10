define(['angular', 'controllersModule'], function (angular, controllers) {
    'use strict';
    /**
     * Allow the user to find a patient via their number and confirm
     * that they have the correct patient
     */
    controllers.controller('ListDataFilesController', function ($scope, $rootScope, $state, files, iaLoadingSpinner, iaModalSheet) {
        $scope.containerId = $state.params.containerId;
        $scope.files = files;

        $scope.selected = {value: null};
        $scope.getSelected = function () {
            return $scope.selected.value;
        };


        $scope.uploadDataFile = function () {
            $state.go('uploadDataFile');
        };

        $scope.downloadDataFile = function () {
            if (!$scope.getSelected()) {
                throw "no file selected";
            }
            iaLoadingSpinner.show();
            $state.go('downloadDataFile', {file: $scope.getSelected()}).
                then(iaLoadingSpinner.hide);
        };

        $scope.deleteDataFile = function () {
            if (!$scope.getSelected()) {
                throw "no file selected";
            }
            iaLoadingSpinner.show();
            $state.go('deleteDataFile', {fileId: $scope.getSelected().id}).
                then(iaLoadingSpinner.hide);
        };
    });
});
