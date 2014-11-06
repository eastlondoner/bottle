define(['angular', 'controllersModule'], function (angular, controllers) {
  'use strict';
  /**
   */
  controllers.controller('ModalSheetController', function ($scope,  $state, $stateParams, iaModalSheet) {
    $scope.modal = iaModalSheet;
    // Copy custom data into the scope for use by the template
    $scope.$watch('modal.current.data', function (data) {
      $scope.data = data;
    });

    //iaModalSheet.show($stateParams);
  });
  
});
