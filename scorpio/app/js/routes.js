define(['angular', 'app'], function (angular, app) {
    'use strict';
    return app.config(['$stateProvider' , '$urlRouterProvider', '$httpProvider', function ($stateProvider, $urlRouterProvider, $httpProvider) {

        $httpProvider.interceptors.push(function($injector){
            return $injector.get('AuthInterceptor')
        });

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
                url: 'jars',
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

            .state('listJobs', {
                parent: 'scorpioBase',
                url: 'jobs',
                resolve: {
                    jobs: function (jobService, $stateParams) {
                        return jobService.getJobs();
                    }
                },
                views: {
                    secondList: {
                        templateUrl: 'partials/listJobs.html',
                        controller: 'ListJobsController'
                    },
                    thirdList: {
                        template: ""
                    }
                }
            })

            .state('uploadJar', {
                parent: 'listJars',
                url: '/upload',
                resolve: {
                    container: function (jarService) {
                        return jarService.getJarContainer();
                    }
                },
                views: {
                    'modalSheet@loggedIn': {
                        templateUrl: 'partials/confirmModal.html',
                        controller: 'FileUploadController'
                    },
                    '@uploadJar': {
                        templateUrl: 'partials/fileUploadModal.html'
                    }
                }
            })

            .state('deleteJar', {
                parent: 'listJars',
                url: '/delete/:jarId',
                resolve: {
                    jar: function (jarService, $stateParams) {
                        return jarService.getJar($stateParams.jarId);
                    }
                },
                views: {
                    'modalSheet@loggedIn': {
                        templateUrl: 'partials/confirmModal.html',
                        controller: 'DeleteJarController'
                    }
                }
            })


            .state('linkContainerToJar', {
                parent: 'listContainers',
                url: '/:containerId/job',
                resolve: {
                    container: function (containerService, $stateParams) {
                        return containerService.getContainer($stateParams.containerId);
                    },
                    jars: function (jarService, $stateParams) {
                        return jarService.getJars();
                    }
                },
                views: {
                    'modalSheet@loggedIn': {
                        templateUrl: 'partials/confirmModal.html',
                        controller: 'ChooseJarController'
                    },
                    '@linkContainerToJar': {
                        templateUrl: 'partials/chooseJarModal.html'
                    }
                }
            })

            .state('linkJarToContainer', {
                parent: 'listJars',
                url: '/:jarId/job',
                resolve: {
                    containers: function (containerService, $stateParams) {
                        return containerService.getContainers();
                    },
                    jar: function (jarService, $stateParams) {
                        return jarService.getJar($stateParams.jarId);
                    }
                },
                views: {
                    'modalSheet@loggedIn': {
                        templateUrl: 'partials/confirmModal.html',
                        controller: 'ChooseContainerController'
                    },
                    '@linkJarToContainer': {
                        templateUrl: 'partials/chooseContainerModal.html'
                    }
                }
            })

            .state('startJob', {
                parent: 'loggedIn',
                url: '/job/:jarId/:containerId',
                resolve: {
                    container: function (containerService, $stateParams) {
                        return containerService.getContainer($stateParams.containerId);
                    },
                    jar: function (jarService, $stateParams) {
                        return jarService.getJar($stateParams.jarId);
                    }
                },
                views: {
                    'modalSheet@loggedIn': {
                        templateUrl: 'partials/startJobModal.html',
                        controller: 'StartJobController'
                    }
                }
            })

            .state('listContainers', {
                parent: 'scorpioBase',
                url: 'containers',
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

            .state('uploadDataFile', {
                parent: 'listDataFiles',
                url: '/upload',
                resolve: {
                    container: function (containerService, $stateParams) {
                        return containerService.getContainer($stateParams.containerId);
                    }
                },
                views: {
                    'modalSheet@loggedIn': {
                        templateUrl: 'partials/confirmModal.html',
                        controller: 'FileUploadController'
                    },
                    '@uploadDataFile': {
                        templateUrl: 'partials/fileUploadModal.html'
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
                    'modalSheet@loggedIn': {
                        templateUrl: 'partials/confirmModal.html',
                        controller: 'DeleteDataFileController'
                    }
                }
            })

            .state('listDataFiles', {
                parent: 'listContainers',
                url: '/:containerId',
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
