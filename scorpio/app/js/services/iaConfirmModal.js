define(['angular', 'servicesModule'], function (angular, services) {
  'use strict';
  /**
   * Display a modal confirm dialog box
   */
  services.factory('iaConfirmModal', ['$modal', function ($modal) {
    var confirmModal = {
      open: function (options) {
        return $modal.open({
          templateUrl: 'partials/confirmModal.html',
          controller: function ($scope, $modalInstance) {
            $scope.message = options.message;
            $scope.confirmLabel = options.confirmLabel;
            $scope.cancelLabel = options.cancelLabel;
            $scope.buttonColor = options.buttonColor;
            $scope.confirm = function () {
              $modalInstance.close();
            };
            $scope.cancel = function () {
              $modalInstance.dismiss('cancel');
            };
          }
        });

      }
    };
    return confirmModal;
  }]);
});
