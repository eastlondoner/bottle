define(['angular', 'controllersModule'], function (angular, controllers) {
    'use strict';
    /**
     * Allow the user to delete data files from a container
     */
    controllers.controller('FileUploadController', function ($scope, $state, container, iaModalSheet, containerService, iaLoadingSpinner) {

        $scope.onFileSelect = function ($files) {
            if ($files && $files.length) {
                $scope.files = $files;
            } else {
                $scope.files = null;
            }
        };

        $scope.message = "This will UPLOAD INTO " + container.name;

        iaModalSheet.show({
            title: "Upload File"
        });

        $scope.confirm = function () {
            iaLoadingSpinner.show();

            var $files = $scope.files;
            if (!$files) {
                throw "no files selected"
            }
            //$files: an array of files selected, each file has name, size, and type.
            var length = $files.length;

            $scope.upload = containerService.uploadFileToContainer(
                $files, container.id
            ).then(_.partial($state.go, '^', {}, {location: 'replace', reload: true})
            ).then(iaLoadingSpinner.hide);

        }
    });
});
