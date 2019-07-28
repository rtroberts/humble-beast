var lampwick = angular.module('lampwick', ['ui.bootstrap', 'ngCookies']);


lampwick.controller('mediaListCtrl', function($scope, $cookies, $window, $http, $sce, $timeout) {
  $scope.oneAtATime = true;
  $scope.enableNonMandatory = false;
  $scope.requiredActions = 1;
  $scope.isFirstOpen = true;
  $scope.postInProgress = false;


  $scope.token = angular.element(document.querySelector('#__anti-forgery-token'))[0].value;


  $scope.postDownloader = function(inp) {
    $scope.user = inp;
    $scope.user.album = "Canvas Conference Live Stream"
    // Turn off the submit button until we have
    $scope.postInProgress = true;
    $http.post(
      '/downloaders/create',
      JSON.stringify($scope.user), {
        headers: {
          'Content-Type': 'application/json',
          'X-CSRF-Token': $scope.token
        }
      }
    ).then(function(data) {
      var attr = ["name", "email", "zip_code", "album"];
      var newCookie;

      for (i = 0; i < attr.length; i++) {
        newCookie = attr[i] + '=' + $scope.user[attr[i]].toString() + ';' + newCookie;
      };
      newCookie = newCookie + $scope.ourCookie();
      $cookies.put('humblebeast-lampwick', newCookie);

      $scope.actionsComplete = true;

    }, function(data) {
      console.log("Something failed " + JSON.stringify(data));
      // We should throw an error here. A modal, perhaps.
      $scope.postInProgress = false;
    });
  };

  $scope.ourCookie = function() {
    var hbCookie = $cookies.get('humblebeast-lampwick');
    if (hbCookie === undefined) {
      return "";
    } else {
      return hbCookie;
    }
  };


  // Does the cookie exist, with the appropriate key, with 'true' as its value? 
  // If so, they're following us.
  $scope.isFollowing = function() {
    var hbCookie = $scope.ourCookie().toLowerCase();
    if (hbCookie.includes("email=") || hbCookie.includes("=true")) {
      return true;
    } else {
      return false;
    }
  };


  $scope.status = {
    isFirstOpen: true,
    isFirstDisabled: false,
    disableOtherFields: true,
    downloadEnabled: $scope.isDownloadEnabled
  };

  $scope.isDownloadEnabled = function() {
    if ($scope.requiredActions == 0) {
      return true;
    } else {
      return false;
    }
  };

  $scope.actionsComplete = $scope.isFollowing();

});



// Prevent the required actions math from going negative
lampwick.filter('noNeg', function() {
  return function(input) {
    return Math.max(0, input);
  };
});