define(['angular', 'servicesModule', 'classes', 'Container'], function (angular, services, classes, Container) {
        'use strict';
        /**
         * The container service is responsible for getting information about
         * JAR files. These are stored in a special container and we just use whatever
         * containerService is currently configured to get them.
         */

        var JAR_CONTAINER_NAME = "z_DO_NOT_DELETE_scorpio_JARS";
        services.factory('jarService', ['$http', '$q', 'containerService', function ($http, $q, containerService) {
                return {

                    getJars: _.partial(containerService.getFilesInContainer, JAR_CONTAINER_NAME),

                    uploadJar: _.partial(containerService.uploadFileToContainer, _, JAR_CONTAINER_NAME),

                    getJarContainer: function(){
                        return new Container({name: "JARS", id: JAR_CONTAINER_NAME});
                    }


                };
            }
        ]);
    }
);