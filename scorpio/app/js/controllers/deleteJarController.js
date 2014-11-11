define(['angular', 'controllersModule'], function (angular, controllers) {
    'use strict';
    /**
     * Allow the user to delete data files from a container
     */
    controllers.controller('DeleteJarController', function ($scope, $state, jar, iaModalSheet, jarService, iaLoadingSpinner) {

        $scope.message = "This will DELETE "+jar.name+" JAR";

        iaModalSheet.show({
            title: "Delete Jar"
        });

        $scope.confirm = function(){
            iaLoadingSpinner.show();
            jarService.deleteJar(jar.id)
                .then(_.partial($state.go,'^',{},{location:'replace', reload:true}))
                .then(iaLoadingSpinner.hide);
        }
    });
});
