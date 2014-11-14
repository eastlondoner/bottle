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
                if(_.isUndefined(scope.$root.globalSelected)){
                    scope.$root.globalSelected = {};
                }

                function setClass(){
                    if (scope.model === scope.$parent.selected.value) {
                        if (scope.model === scope.$root.globalSelected.value) {
                            scope.selectedClass = "active focus";
                        } else {
                            scope.selectedClass = "active";
                        }
                    } else {
                        // nope
                        scope.selectedClass = '';
                    }
                }

                scope.localFunction = function () {
                    scope.$parent.selected.value = scope.model;
                    scope.$root.globalSelected.value = scope.model;
                };

                scope.$parent.$watch('selected.value', function () {
                    // Is this set to my scope?
                    setClass();
                });



                scope.$root.$watch('globalSelected', function () {
                    // Is this set to my scope?
                    setClass();
                }, true);
            }
        };
    });

});