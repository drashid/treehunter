'use strict';

angular.module('publicApp')
  .controller('SearchListCtrl', ['$scope', '$http', '$routeParams', '$location', function ($scope, $http, $routeParams, $location) {

	$scope.typeFilter = 'Any';

	switch($routeParams.type){
		case 'source':
			$http({
  				url: "/api/search",
				method: "GET",
				params: {source: $routeParams.className, limit: 10}
			}).success(function(data){
				$scope.items = data;
			});
			break;
	}

	$scope.redirectItem = function(item){
		$location.path("/detail/item/" + item._id);
	};

  }]);
