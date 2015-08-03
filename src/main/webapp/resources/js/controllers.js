var app = angular.module('statelessApp', ['ngCookies'])
.factory('TokenAuthInterceptor', function($q, $rootScope) {
	return {
		request: function(config) {
			var authToken = localStorage.getItem('auth_token');
			if (authToken) {
				config.headers['X-AUTH-TOKEN'] = authToken;
			}
			return config;
		},
		responseError: function(error) {
			if (error.status === 401 || error.status === 403) {
				localStorage.removeItem('auth_token');
				$rootScope.authenticated = false;
			}
			return $q.reject(error);
		}
	};
}).config(function($httpProvider) {
	$httpProvider.interceptors.push('TokenAuthInterceptor');
});

app.controller('AuthCtrl', function ($scope, $rootScope, $http, $cookies) {
	$rootScope.authenticated = false;
	$scope.token; // For display purposes only
	
	$scope.init = function () {
		var authCookie = $cookies['AUTH-TOKEN'];
		if (authCookie) {
			localStorage.setItem('auth_token', authCookie);
			delete $cookies['AUTH-TOKEN'];
			$http.get('/api/user/current').success(function (user) {
                if (user.username) {
                    $rootScope.authenticated = true;
                    $scope.username = user.username;
                    
                    // For display purposes only
                    $scope.token = JSON.parse(atob(localStorage.getItem('auth_token').split('.')[0]));
                } else {
                    // TODO: does it mean that we are not authenticated again?
                }
            });
		}
	};

	$scope.logout = function () {
		localStorage.removeItem('auth_token');
		$rootScope.authenticated = false;
	};
	
	$scope.getSocialDetails = function() {
		$http.get('/api/facebook/details').success(function (socialDetails) {
			$scope.socialDetails = socialDetails;
		});
	};
	
	$scope.getTrajectoryEndpoints = function() {
        $http.get('/api/trajectory/endpoints').success(function (trajectoryEndpoints) {
            $scope.trajectoryEndpoints = trajectoryEndpoints;
        });
    };
});