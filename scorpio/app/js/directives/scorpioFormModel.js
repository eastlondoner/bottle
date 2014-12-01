define(['angular', 'directivesModule'], function (angular, directives) {
    'use strict';

    directives.directive('scorpioFormModel', function () {
        return {
            scope:{
                "scorpioFormModel":"=",
                "scorpioFormSubmit":"="
            },
            restrict: 'A',
            link: function (scope, element, attrs) {
                element.getValues();
            }
        };
    });

});