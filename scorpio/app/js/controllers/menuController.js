define(['angular', 'controllersModule'], function (angular, controllers) {
  'use strict';
  /**
   */
  controllers.controller('MenuController', function ($scope,  $state, iaModalSheet) {
    $scope.menuShown = false;
    // Hide the menu whenever the route changes
    $scope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
      $scope.menuShown = false;
    });
    $scope.toggleMenu = function () {
      $scope.menuShown = !$scope.menuShown;
    };
    $scope.hideMenu = function () {
      $scope.menuShown = false;
    };
  });
  
});
