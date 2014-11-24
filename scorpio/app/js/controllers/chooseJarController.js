define(['angular', 'controllersModule'], function (angular, controllers) {
    'use strict';
    /**
     * Allow the user to delete data files from a container
     */
    controllers.controller('ChooseJarController', function ($scope, $state, container, jars, iaModalSheet, containerService, iaLoadingSpinner) {

        $scope.jars = jars;
        $scope.selected = {
            jar: jars[0]
        };

        $scope.message = "choose the JAR to run on CONTAINER " + container.name;
        iaModalSheet.show({
            title: "Choose JAR"
        });

        $scope.confirm = function () {
            $state.go("startJob", {
                jarId: $scope.selected.jar.id,
                containerId: container.id
            });
        }


    });
});
