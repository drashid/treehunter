'use strict';

angular.module('publicApp', ['ngResource'])
  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
      .when('/overview', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
	  .when('/source/:className', {
	    templateUrl: 'views/searchList.html',
		controller: 'SearchListCtrl'
      })
      .otherwise({
        redirectTo: '/overview'
      });
  }]);
