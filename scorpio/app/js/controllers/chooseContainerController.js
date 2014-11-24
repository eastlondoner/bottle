define(['angular', 'controllersModule'], function (angular, controllers) {
    'use strict';
    /**
     * Allow the user to delete data files from a container
     */
    controllers.controller('ChooseContainerController', function ($scope, $state, containers, jar, iaModalSheet, containerService, iaLoadingSpinner) {

        $scope.containers = containers;
        $scope.selected = {
            container: containers[0]
        };
        $scope.message = "choose the CONTAINER to run on JOB " + jar.name;
        iaModalSheet.show({
            title: "Choose Container"
        });


        $scope.confirm = function (selectedContainer) {
            $state.go("startJob", {
                jarId: jar.id,
                containerId: $scope.selected.container.id
            });
        }

    });
});
