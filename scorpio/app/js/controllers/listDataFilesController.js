define(['angular', 'controllersModule', 'File'], function (angular, controllers, File) {
    'use strict';


    controllers.controller('ListDataFilesController', function ($scope, $rootScope, $state, files, iaLoadingSpinner, iaModalSheet) {
        $scope.containerId = $state.params.containerId;
        $scope.files = files;
        var folderTree = File.getFolderTree($scope.containerId);

        $scope.navigation = {
            currentFolder: ""
        };

        function getFolders() {
            var parts = $scope.navigation.currentFolder.split('/');
            if ( $scope.navigation.currentFolder != "") {
                return _.chain(parts).reduce(function (memo, part) {
                    return memo[part];
                }, folderTree).keys().value();
            }
            return _.keys(folderTree);
        }

        function setFolders(){
            $scope.folders = getFolders();
        }

        $scope.expandFolder = function(folder){
            if($scope.navigation.currentFolder == ""){
                $scope.navigation.currentFolder = folder;
            } else {
                $scope.navigation.currentFolder += '/' + folder;
            }
        };
        $scope.goToRootFolder = function(){
            $scope.navigation.currentFolder = "";
        };
        $scope.upOneFolder = function(){
            var parts = $scope.navigation.currentFolder.split('/');
            parts.pop();
            $scope.navigation.currentFolder = parts.join('/');
        };

        $scope.$watch('navigation.currentFolder', function (newVal, oldVal) {
            if (newVal != oldVal) {
                setFolders();
            }
        });

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

        setFolders();
    });
});
