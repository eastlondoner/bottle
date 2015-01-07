define(['angular', 'controllersModule'], function (angular, controllers) {
    'use strict';
    /**
     * Allow the user to delete data files from a container
     */
    controllers.controller('DeleteContainerController', function ($scope, $state, container, iaModalSheet, containerService, iaLoadingSpinner) {

        $scope.message = "Delete a CONTAINER";
        iaModalSheet.show({
            title: "Delete Container"
        });
        $scope.container = container;

        $scope.confirm = function () {
            iaLoadingSpinner.show();
            containerService.deleteContainer(container.id)
                .then(_.partial($state.go,'^',{},{location:'replace', reload:true}))
                .then(iaLoadingSpinner.hide);
        }

    });
});
