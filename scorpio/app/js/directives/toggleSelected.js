define(['angular', 'directivesModule'], function (angular, directives) {
    'use strict';

    directives.directive('toggleSelected', function () {
        return {
            restrict: 'A',
            template: '<li ng-click=";localFunction()" ng-class="selectedClass"  ng-transclude></li>',
            replace: true,
            scope: {
                model: '=model'
            },
            transclude: true,
            link: function (scope, element, attrs) {
                scope.localFunction = function () {
                    if(scope.$parent.selected.value === scope.model){
                        scope.$parent.selected.value = null;
                    } else {
                        scope.$parent.selected.value = scope.model;
                    }
                };
                scope.$parent.$watch('selected.value', function () {
                    // Is this set to my scope?
                    if (scope.model === scope.$parent.selected.value) {
                        scope.selectedClass = "active";
                    } else {
                        // nope
                        scope.selectedClass = '';
                    }
                }, true);
            }
        };
    });

});