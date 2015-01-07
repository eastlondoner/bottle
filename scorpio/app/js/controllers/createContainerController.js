define(['angular', 'controllersModule'], function (angular, controllers) {
    'use strict';
    /**
     * Allow the user to delete data files from a container
     */
    controllers.controller('CreateContainerController', function ($scope, $state, iaModalSheet, containerService, iaLoadingSpinner) {

        $scope.message = "Create a CONTAINER"
        iaModalSheet.show({
            title: "Create Container"
        });
        $scope.container = {};

        $scope.confirm = function () {
            iaLoadingSpinner.show();
            containerService.createContainer($scope.container.name)
                .then(_.partial($state.go,'^',{},{location:'replace', reload:true}))
                .then(iaLoadingSpinner.hide);
        }

    });
});
