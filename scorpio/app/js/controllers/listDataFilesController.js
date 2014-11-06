define(['angular', 'controllersModule'], function (angular, controllers) {
    'use strict';
    /**
     * Allow the user to find a patient via their number and confirm
     * that they have the correct patient
     */
    controllers.controller('ListDataFilesController', function ($scope, $rootScope, $state, files, iaLoadingSpinner, iaModalSheet) {
        var containerId = $state.params.containerId;
        var asyncData = $state.params.asyncData;
        $scope.files = files;

        $scope.toggleSelected = function(file){
            if($scope.selected == file){
                delete $scope.selected;
            } else {
                $scope.selected = file;
            }
        };

        $scope.uploadDataFile = function(){
            $state.go('uploadDataFile');
        };

        $scope.downloadDataFile = function(){
            if(!$scope.selected){
                throw "no file selected";
            }
            $state.go('downloadDataFile', {file: $scope.selected});
        };

        $scope.deleteDataFile = function(){
            if(!$scope.selected){
                throw "no file selected";
            }
            $state.go('deleteDataFile', {fileId: $scope.selected.id});
        };
    });
});
