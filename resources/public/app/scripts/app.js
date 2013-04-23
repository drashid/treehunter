'use strict';

angular.module('publicApp', ['ngResource', '$strap.directives'])
  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
      .when('/overview', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
	  .when('/overview/:type/:className', {
	    templateUrl: 'views/searchList.html',
		controller: 'SearchListCtrl'
      })
      .otherwise({
        redirectTo: '/overview'
      });
  }]);
