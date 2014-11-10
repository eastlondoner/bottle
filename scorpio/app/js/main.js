require.config({
    paths: {
        'angular-mocks': '../../bower_components/angular-mocks/angular-mocks',
        'angular-scenario': '../../bower_components/angular-scenario/angular-scenario',
        angular: '../../bower_components/angular/angular',
        'requirejs-text': '../../bower_components/requirejs-text/text',
        requirejs: '../../bower_components/requirejs/require',
        'angular-ui-router': '../../bower_components/angular-ui-router/release/angular-ui-router',
        underscore: '../../bower_components/underscore-amd/underscore',
        'angular-ui-bootstrap': '../../bower_components/angular-ui-bootstrap-bower/ui-bootstrap-tpls',
        'angular-file-upload-shim': '../../bower_components/ng-file-upload/angular-file-upload-shim.min',
        'angular-file-upload': '../../bower_components/ng-file-upload/angular-file-upload.min'
    },
    baseUrl: 'js',
    shim: {
        'underscore': {
            exports: '_'
        },
        'angular-file-upload-shim':{

        },
        'angular-file-upload':{
            deps: [
                'angular-file-upload-shim',
                'angular'
            ]
        },
        angular: {
            deps: [
                'angular-file-upload-shim'
            ],
            exports: 'angular'
        },
        'angular-ui-router': {
            deps: [
                'angular'
            ]
        },
        'angular-ui-bootstrap': {
            deps: [
                'angular'
            ]
        },
        angularMocks: {
            deps: [
                'angular'
            ],
            exports: 'angular.mock'
        }
    },
    priority: [
        'angular'
    ]
});

// hey Angular, we're bootstrapping manually!
window.name = "NG_DEFER_BOOTSTRAP!";
window.TEST = true;

require([
    'angular',
    'app',
    'routes',
    'underscore'
], function (angular, app, routes) {
    'use strict';
    var $html = angular.element(document.getElementsByTagName('html')[0]);

    angular.element().ready(function () {
        $html.addClass('ng-app');
        angular.bootstrap($html, [app['name']]);
        angular.resumeBootstrap();
    });
});
