define(['angular', 'app'], function (angular, app) {
    'use strict';
    return app.config(['$stateProvider' , '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {

        $stateProvider
            .state('base', {
                abstract: true,
                templateUrl: 'partials/base.html'
            })

            .state('login', {
                parent: 'base',
                url: '/accounts/login',
                controller: 'LoginController',
                templateUrl: 'partials/login.html'
            })

            .state('loggedIn', {
                abstract: true,
                parent: 'base',
                templateUrl: 'partials/loggedIn.html'
            })

            .state('about', {
                parent: 'loggedIn',
                url: '/about',
                templateUrl: 'partials/static-pages/about-scorpio.html'
            })

            .state('scorpioBase', {
                parent: 'loggedIn',
                url: '/',
                templateUrl: 'partials/scorpioBase.html'
            })

            .state('listJars', {
                parent: 'scorpioBase',
                url: 'jars/',
                resolve: {
                    jars: function (jarService, $stateParams) {
                        return jarService.getJars();
                    }
                },
                views: {
                    secondList: {
                        templateUrl: 'partials/listJars.html',
                        controller: 'ListJarsController'
                    },
                    thirdList: {
                        template: ""
                    }
                }
            })

            .state('listContainers', {
                parent: 'scorpioBase',
                url: 'containers/',
                resolve: {
                    containers: function (containerService, $stateParams) {
                        return containerService.getContainers();
                    }
                },
                views: {
                    secondList: {
                        templateUrl: 'partials/listContainers.html',
                        controller: 'ListContainersController'
                    },
                    thirdList: {
                        template: ""
                    }
                }
            })

            .state('modalSheet', {
                abstract: true,
                parent: 'loggedIn',
                views: {
                    'modalSheet': {
                        templateUrl: 'partials/confirmModal.html'
                    }
                }
            })

            .state('uploadDataFile',{
                parent: 'listDataFiles',
                url: '/upload',
                resolve: {
                    container: function (containerService, $stateParams) {
                        return containerService.getContainer($stateParams.containerId);
                    }
                },
                views: {
                    'modalSheet@base': {
                        templateUrl: 'partials/fileUploadModal.html',
                        controller: 'FileUploadController'
                    }
                }
            })
            .state('deleteDataFile', {
                parent: 'listDataFiles',
                url: '/delete/:fileId',
                resolve: {
                    file: function (containerService, $stateParams) {
                        return containerService.getFileInContainer($stateParams.fileId, $stateParams.containerId);
                    },
                    container: function (containerService, $stateParams) {
                        return containerService.getContainer($stateParams.containerId);
                    }
                },
                views: {
                    'modalSheet@base': {
                        templateUrl: 'partials/confirmModal.html',
                        controller: 'DeleteDataFileController'
                    }
                }
            })

            .state('listDataFiles', {
                parent: 'listContainers',
                url: 'containers/:containerId',
                resolve: {
                    files: function (containerService, $stateParams) {
                        return containerService.getFilesInContainer($stateParams.containerId);
                    }
                },
                views: {
                    "thirdList@scorpioBase": {
                        templateUrl: 'partials/listDataFiles.html',
                        controller: 'ListDataFilesController'
                    }
                }
            })

        ;

        $urlRouterProvider.otherwise('/');
    }]).run(function ($state, $rootScope, $location) {
        //$state.go('scorpioBase')
    });

});
