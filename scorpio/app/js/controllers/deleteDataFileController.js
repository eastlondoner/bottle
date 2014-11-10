define(['angular', 'controllersModule'], function (angular, controllers) {
    'use strict';
    /**
     * Allow the user to delete data files from a container
     */
    controllers.controller('DeleteDataFileController', function ($scope, $state, file, container, iaModalSheet, containerService, iaLoadingSpinner) {

        $scope.message = "This will DELETE "+file.name+" IN " + container.name;

        iaModalSheet.show({
            title: "Delete File"
        });

        $scope.confirm = function(){
            iaLoadingSpinner.show();
            containerService.deleteFileInContainer(file.id, container.id)
                .then(_.partial($state.go,'^',{},{location:'replace', reload:true}))
                .then(iaLoadingSpinner.hide);
        }
    });
});
