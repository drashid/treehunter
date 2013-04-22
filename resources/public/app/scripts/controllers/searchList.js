'use strict';

angular.module('publicApp')
  .controller('SearchListCtrl', ['$scope', '$http', '$routeParams', function ($scope, $http, $routeParams) {

	switch($routeParams.type){
		case 'source':
			$scope.items = $http({
							url: "/api/search",
							method: "GET",
							params: {source: $routeParams.className, limit: 10}});
			break;
	}


  }]);
