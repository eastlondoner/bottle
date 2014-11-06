define(['angular', 'servicesModule'], function (angular, services) {
  'use strict';
  /**
   * Show a modal loading spinner that covers the entire app
   *
   *     iaLoadingSpinner.show();
   *     iaLoadingSpinner.hide();
   */
  services.factory('iaLoadingSpinner', ['$rootScope', function ($rootScope) {
    return {
      show: function ( ) {$rootScope.showLoadingSpinner = true; },
      hide: function ( ) {$rootScope.showLoadingSpinner = false; }
    };
  }]);
});
