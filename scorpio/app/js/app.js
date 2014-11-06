define([
    'angular',
    'classes',
    'services',
    'filters',
    'directives',
    'controllers',
    'angular-ui-router',
    'angular-ui-bootstrap',
    'angular-file-upload'
], function (angular, filters, services, directives, controllers) {
    'use strict';

    // document.ontouchmove =function(e){
    //   e.preventDefault();
    // };

    return angular.module('Scorpio', [
        'Scorpio.controllers',
        'Scorpio.filters',
        'Scorpio.services',
        'Scorpio.directives',
        'ui.router',
        'ui.bootstrap',
        'angularFileUpload'
    ]);
});
