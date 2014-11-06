define(['angular', 'servicesModule'], function (angular, services) {
  'use strict';
  /**
   * The modal service allows display of the modal sheet (the one that
   * comes down from the top of the screen). Works in conjunction with
   * the HTML included in base.html and the controller in
   * modalSheetController.js
   *
   *     iaModalSheet.show({templateUrl: 'partials/test.html', title: 'hello world'});
   */
  services.factory('iaModalSheet', ['$rootScope', function ($rootScope) {
    var modalSheet = {
      current: {
        shown: false
      },
      show: function (options) {
        modalSheet.current.shown = true;
        modalSheet.current.title = options.title || 'Information';
        modalSheet.current.data = options.data || {};
      },
      hide: function () {
        modalSheet.current.shown = false;
        delete modalSheet.current.title;
        delete modalSheet.current.data;
      }
    };
    // Hide automatically when we change url
    $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
      modalSheet.hide();
    });
    return modalSheet;
  }]);
});
