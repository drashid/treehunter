'use strict';

angular.module('publicApp')
  .controller('MainCtrl', ['$scope', '$resource', function ($scope, $resource) {
	$scope.stats = $resource('/api/stats').get();

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
  }]);
