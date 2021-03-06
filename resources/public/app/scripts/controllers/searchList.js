'use strict';

angular.module('publicApp')
  .controller('SearchListCtrl', ['$scope', '$http', '$routeParams', '$location', function ($scope, $http, $routeParams, $location) {

	var defaultLimit = 100;

	function parseWords(messages){
		var words = {};
		_.each(messages, function(message){
			var pieces = message.split(/[^0-9A-Za-z]/);
			_.each(pieces, function(piece){
				words[piece] = 1;
			});
		});

		return _.keys(words);
	}

	function searchRequest(params){
		$http({
			url: "/api/search",
			method: "GET",
			params: params
		}).success(function(data){
			$scope.items = data;
			$scope.words = parseWords(_.pluck($scope.items, 'message'));
		}).error(function(){
			// TODO
		});
	}

	$scope.redirectItem = function(item){
		$location.path("/detail/item/" + item._id);
	};

	var getHoursAndMinutes = function(timeString) {
		var time = timeString.match(/([0-9]{2}):([0-9]{2}) (AM|PM)/);
		var hour = parseInt(time[1]) + (time[3] === 'PM' ? 12 : 0);
		var minute = parseInt(time[2]);

		return {hours: hour, minutes: minute};
	};

	$scope.search = function(){
		var params = {limit: defaultLimit};

		if($scope.source){
			params.source = $scope.source;
		}

		if($scope.datepicker && $scope.datepicker.start){
			var time = {
				hours: 0,
				minutes: 0
			};

			if($scope.timepicker.start){
				time = getHoursAndMinutes($scope.timepicker.start);
			}

			// this date is in UTC (datepicker default), but we want to treat it as if it's in local time since the user set it
			var date = $scope.datepicker.start;
			var localDate = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), time.hours, time.minutes);

			params.startdate = localDate.toISOString();
		}

		if($scope.datepicker && $scope.datepicker.end){
			var time = {
				hours: 0,
				minutes: 0
			};

			if($scope.timepicker.end){
				time = getHoursAndMinutes($scope.timepicker.end);
			}

			// this date is in UTC (datepicker default), but we want to treat it as if it's in local time since the user set it
			var date = $scope.datepicker.end;
			var localDate = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), time.hours, time.minutes);

			params.enddate = localDate.toISOString();
		}

		if($scope.typeFilter !== 'Any'){
			params.type = $scope.typeFilter;
		}

		console.log(params);
		console.log("Querying with the following parameters: " + JSON.stringify(params));

		// query
		searchRequest(params);
	}

	//
	// INIT
	//

	switch($routeParams.type){
		case 'source':
			$scope.source = $routeParams.className;
			$scope.type = 'Source Class';
			$scope.typeName = $scope.source;
			searchRequest({source: $scope.source, limit: defaultLimit});
			break;
	}

	$scope.typeFilter = 'Any';

}]);
