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

            $scope.selected = {value: null};
            function getSelectedContainer() {
                return $scope.selected.value;
            }

            $scope.getSelected = getSelectedContainer;


            $scope.listDataFiles = function (container) {
                iaLoadingSpinner.show();
                $state.go('listDataFiles', {
                    containerId: container.id
                }).then(iaLoadingSpinner.hide);
            };

            $scope.createContainer = function () {
                iaLoadingSpinner.show();
                $state.go('createContainer', {}).
                    then(iaLoadingSpinner.hide);
            };

            $scope.deleteSelectedContainer = function () {
                iaLoadingSpinner.show();
                $state.go('deleteContainer', {
                    containerId: getSelectedContainer().id
                }).then(iaLoadingSpinner.hide);
            };

            $scope.linkContainerToJar = function(params){
                $state.go("linkContainerToJar", params);
            }
        });
    }
);
