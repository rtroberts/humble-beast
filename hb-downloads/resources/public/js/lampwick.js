var lampwick = angular.module('lampwick', ['ui.bootstrap', 'ngCookies']);

lampwick.controller('mediaListCtrl', function($scope, $cookies, $window, $http, $sce, $timeout) {
  $scope.oneAtATime = true;
  $scope.enableNonMandatory = false;
  $scope.requiredActions = 3;
  $scope.isFirstOpen = true;
  $scope.postInProgress = false;

  $scope.token = angular.element(document.querySelector('#__anti-forgery-token'))[0].value;
  $scope.album = angular.element(document.querySelector('#album'))[0].value;

  // This is the function called by the 'save' button on the email form.
  $scope.save = function(data) {
    $scope.status.isFirstOpen = false;
    $scope.user = data;
    $scope.user.onetime_key = $scope.onetime_key;
    $scope.user.album = $scope.album;
    $scope.status.disableOtherFields = false;
    $scope.requiredActions = $scope.requiredActions - 1;
  };

  $scope.postDownloader = function() {
    // Turn off the submit button until we have
    $scope.postInProgress = true;
    $http.post(
      '/hb/downloaders/create',
      JSON.stringify($scope.user), {
        headers: {
          'Content-Type': 'application/json',
          'X-CSRF-Token': $scope.token
        }
      }
    ).then(function(data) {
      $window.location.href = '/hb/email';
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

  $scope.renderHtml = function(html) {
    return $sce.trustAsHtml(html);
  };



  // Does the cookie exist, with the appropriate key, with 'true' as its value? 
  // If so, they're following us.
  $scope.isFollowing = function(platform) {
    var hbCookie = $scope.ourCookie().toLowerCase();
    if (hbCookie.includes(platform + "=true")) {
      $scope.requiredActions = $scope.requiredActions--;
      return true;
    } else {
      return false;
    }
  };

  $scope.userSaved = function() {
    return $scope.user !== undefined;
  };

  $scope.actionsComplete = function() {
    var following = $scope.medias.filter(function(x) {
      return x.isFollowing;
    });
    if ((following.length < $scope.requiredActions) || !($scope.userSaved())) {
      return false;
    } else {
      return true;
    }
  };

  $scope.share = {
    facebook: function() {
      var uri = 'https://www.facebook.com/humblebeastrecords';
      var mywindow = $window.open(uri, "facebookWindow", "width=800,height=650,0,status=0,");
      mywindow.moveTo(300, 100);
    },
    instagram: function() {
      var uri = 'https://www.instagram.com/humblebeastrecords/';
      //$window.open(uri + [gUrl].join('&'));
      var mywindow = $window.open(uri, "instagramWindow", "width=800,height=650,0,status=0,");
      mywindow.moveTo(300, 100);
    },
    twitter: function() {
      var tVia = 'via=hbdownloads';
      var tScreen = 'screen_name=humblebeast';
      var uri = 'http://twitter.com/intent/follow?';

      var mywindow = $window.open(uri + [tScreen, tVia].join('&'), "testingWindow", "width=800,height=650,0,status=0,");
      mywindow.moveTo(300, 100);
    },
    youtube: function() {
      var uri = 'https://www.youtube.com/user/HumbleBeastVideo?sub_confirmation=1';
      var mywindow = $window.open(uri, "youTubeWindow", "width=800,height=650,0,status=0,");
      mywindow.moveTo(300, 100);
    },
  };

  $scope.setFollowStatus = function(mediaObj) {
    var platform = mediaObj['name'];
    if ($scope.isFollowing(platform)) {
      mediaObj['isFollowing'] = true;
      return true;
    } else {
      // Append new cookie values.
      var newCookie = platform + "=true;" + $scope.ourCookie();
      $cookies.put('humblebeast-lampwick', newCookie);
      mediaObj['isFollowing'] = true;
      return true;
    }
  };

  $scope.checkFollowAPI = function(media, cb) {
    //NYI. Just set a cookie.
    cb(media);
  };

  $scope.checkFollowStatus = function(mediaObj) {
    if ($scope.isFollowing(mediaObj['name'])) {
      return true;
    } else {
      mediaObj.isLoading = true;
      $timeout(function() {
        mediaObj.isLoading = false;
      }, 3000);
      $scope.checkFollowAPI(mediaObj, $scope.setFollowStatus);
    }
  };

  $scope.status = {
    isFirstOpen: true,
    isFirstDisabled: false,
    disableOtherFields: true,
    downloadEnabled: $scope.isDownloadEnabled
  };

  $scope.createButtonHTML = function(classname, text) {
    return `<div class="media-button"> <a class="btn btn-block btn-social btn-${classname}"> <span class="fa fa-${classname}"></span> ${text} </a> </div>`;
  };

  $scope.medias = [{
      'name': "instagram",
      'heading': "Follow @humblebeastrecords on Instagram",
      'content': '',
      'button': $scope.share.instagram,
      'buttonHTML': $scope.createButtonHTML('instagram', 'Follow @humblebeastrecords'),
      'isFollowing': $scope.isFollowing('instagram'),
      'isLoading': false,
    },

    {
      'name': "facebook",
      'heading': "Visit Humble Beast on Facebook",
      'content': '',
      'button': $scope.share.facebook,
      'buttonHTML': $scope.createButtonHTML('facebook', 'Visit Humble Beast'),
      'isFollowing': $scope.isFollowing('facebook'),
      'isLoading': false,
    },

    {
      'name': "twitter",
      'heading': "Follow @humblebeast on Twitter",
      'content': '',
      'button': $scope.share.twitter,
      'buttonHTML': $scope.createButtonHTML('twitter', 'Follow @humblebeast'),
      'isFollowing': $scope.isFollowing('twitter'),
      'isLoading': false,
    },

    {
      'name': "youtube",
      'heading': "Subscribe to our YouTube channel",
      'content': '',
      'button': $scope.share.youtube,
      'buttonHTML': '<div class = "media-button"> <a class = "btn btn-block btn-social btn-google"> <span class = "fa fa-youtube"> </span> Subscribe to Humble Beast Video </a> </div>',
      'isFollowing': $scope.isFollowing('youtube'),
      'isLoading': false,
    },


  ];


  $scope.actionsCompleted = function(coll) {
    coll.filter(function(item) {
      return item['isFollowing'];
    });
  };

  $scope.isDownloadEnabled = function() {
    if ($scope.requiredActions == 0) {
      return true;
    } else {
      return false;
    }
  };

});

// Prevent the required actions math from going negative
lampwick.filter('noNeg', function() {
  return function(input) {
    return Math.max(0, input);
  };
});


lampwick.controller('friendsCtrl', function($scope, $cookies, $window, $http, $timeout) {

  $scope.postInProgress = false;
  $scope.emailsRemaining = 3;
  $scope.outOfEmails = function() {
    if ($scope.emailsRemaining < 1) {
      return true;
    } else {
      return false;
    }
  };

  $scope.token = angular.element(document.querySelector('#__anti-forgery-token'))[0].value;
  $scope.album = angular.element(document.querySelector('#album'))[0].value;


  $scope.postDownloader = function(user) {
    $scope.postInProgress = true;
    user["__anti-forgery-token"] = $scope.token;
    user.album = $scope.album;
    $http.post(
      '/hb/downloaders/create',
      JSON.stringify(user), {
        headers: {
          'Content-Type': 'application/json',
          'X-CSRF-Token': $scope.token
        }
      }
    ).then(function(data) {
      // Successfully posted
      $timeout(function() {
        $scope.postInProgress = false;
        $scope.friendsForm.$setPristine();
        $scope.emailsRemaining = $scope.emailsRemaining - 1;
      }, 500);

    }, function(data) {
      $scope.postInProgress = true;
    });
  };


  //var albumURL = angular.element(document.querySelector('#album-download'))[0].value;

});
