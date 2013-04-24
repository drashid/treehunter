'use strict';

angular.module('publicApp')
  .controller('SearchListCtrl', ['$scope', '$http', '$routeParams', '$location', function ($scope, $http, $routeParams, $location) {

	$scope.typeFilter = 'Any';

	switch($routeParams.type){
		case 'source':
			$scope.source = $routeParams.className;

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

	var getHoursAndMinutes = function(timeString) {
		var time = timeString.match(/([0-9]{2}):([0-9]{2}) (AM|PM)/);
		var hour = parseInt(time[1]) + (time[3] === 'PM' ? 12 : 0);
		var minute = parseInt(time[2]);

		return {hours: hour, minutes: minute};
	};

	$scope.search = function(){
		var params = {};

		if($scope.source){
			params.source = $scope.source;
		}

		var now = new Date();
		if($scope.datepicker && $scope.datepicker.start){
			var time = {
				hours: now.getHours(),
				minutes: now.getMinutes()
			};

			if($scope.timepicker.start){
				time = getHoursAndMinutes($scope.timepicker.start);
			}

			// this date is in UTC, but we want to treat it as if it's in local time since the user set it
			var date = $scope.datepicker.start;
			var localDate = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), time.hours, time.minutes);

			params.startdate = localDate;
		}

		if($scope.datepicker && $scope.datepicker.end){
			var time = {
				hours: now.getHours(),
				minutes: now.getMinutes()
			};

			if($scope.timepicker.end){
				time = getHoursAndMinutes($scope.timepicker.end);
			}

			// this date is in UTC, but we want to treat it as if it's in local time since the user set it
			var date = $scope.datepicker.end;
			var localDate = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), time.hours, time.minutes);

			params.enddate = localDate;
		}

		if($scope.typeFilter !== 'Any'){
			params.type = $scope.typeFilter;
		}

		console.log("Querying with the following parameters: " + params);
	}

  }]);
