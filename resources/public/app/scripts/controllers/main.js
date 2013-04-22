'use strict';

angular.module('publicApp')
  .controller('MainCtrl', ['$scope', '$resource', '$location', function ($scope, $resource, $location) {

	$scope.stats = $resource('/api/stats/counts').get();

	$scope.keys = function(stats){
		return _.keys(stats);
	}

	$scope.counts = function(statKey, countKeys){
		var logDetails = $scope.stats[statKey];
		return _.map(countKeys, function(key){
			var entry = _.find(logDetails, function(detail){ return detail['type'] === key; });
			if(entry){
				return entry['count'];
			} else {
				return 0;
			}
		});
	}

	$scope.details = function(sourceKey){
		$location.path('/source/' + sourceKey);
	}

  }]);
