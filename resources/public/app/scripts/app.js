'use strict';

angular.module('publicApp', ['ngResource'])
  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
	  .when('/source/:className', {
	    templateUrl: 'views/sourceDetail.html',
		controller: 'SourceDetailCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });
  }]);
