var mFirebaseRef = new Firebase("https://sisobustracker.firebaseio.com/RouteNumber/");

var mMap;
var mMapBounds;
var mRouteNumber = "9";
var mIsWaiting = true;
var mOfficeLocation;

var mOfficeMarker;
var mUserMarker;
var mBusMarker;

var mUserLatitude;
var mUserLongitude;

var mBusLatitude;
var mBusLongitude;

function initMap() {
  loadUi();

  mOfficeLocation = new google.maps.LatLng(12.980253, 77.697375);
  mMapBounds = new google.maps.LatLngBounds();
  mMapBounds.extend(mOfficeLocation);

  var mapOptions = {
    center: mOfficeLocation,
    zoom: 15,
    disableDefaultUI: true,
    mapTypeId: google.maps.MapTypeId.ROADMAP,
  };
  mMap = new google.maps.Map(document.getElementById('map'), mapOptions);

  mOfficeMarker = new google.maps.Marker({
    position: mOfficeLocation,
    title: "Office",
    icon: "res/office_building.png",
  });
  mOfficeMarker.setMap(mMap);

  mUserMarker = new google.maps.Marker({
    title: "User",
    icon: "res/user.png",
  });
  mUserMarker.setMap(mMap);
  getUserLocation();

  mBusMarker = new google.maps.Marker({
    title: "Bus",
    icon: "res/bus.png",
  });
  mBusMarker.setMap(mMap);
  getBusLocation();
}

function getUserLocation() {
  if (navigator.geolocation == null) {
    return;
  }

  navigator.geolocation.watchPosition(onGetUserLocation);
}

function onGetUserLocation(position) {
  mUserLatitude = position.coords.latitude;
  mUserLongitude = position.coords.longitude;
  setUserLocation();
}

function setUserLocation() {
  var shouldResizeBounds = false;
  if (mUserMarker.getPosition() == null) {
    shouldResizeBounds = true;
  }
  mUserMarker.setPosition(new google.maps.LatLng(mUserLatitude, mUserLongitude));
  if (shouldResizeBounds) {
    mMapBounds.extend(mUserMarker.getPosition());
    updateMapBounds();
  }

  if (!mIsWaiting) {
    updateBusLocation(mUserLatitude, mUserLongitude);
  }
}

function getBusLocation() {
  mFirebaseRef.child(mRouteNumber).on("value", function(snapshot) {
    mBusLatitude = snapshot.val().latitude;
    mBusLongitude = snapshot.val().longitude;
    setBusLocation();
  }, function(error) {
    console.log(error.code);
  });
}

function setBusLocation() {
  var shouldResizeBounds = false;
  if (mBusMarker.getPosition() == null) {
    shouldResizeBounds = true;
  }
  mBusMarker.setPosition(new google.maps.LatLng(mBusLatitude, mBusLongitude));
  if (shouldResizeBounds) {
    mMapBounds.extend(mBusMarker.getPosition());
    updateMapBounds();
  }
}

function updateBusLocation(lat, long) {
  mFirebaseRef.child(mRouteNumber).set({latitude: lat, longitude: long});
}

function updateMapBounds() {
  mMap.fitBounds(mMapBounds);
}

function loadUi() {
  document.getElementById("routeNumber").innerHTML = "R:" + mRouteNumber;
  document.getElementById("waitingButton").checked = "checked";
}

function onStateChange(isWaiting) {
  if (isWaiting) {
    mIsWaiting = true;
    document.getElementById("waitingButton").checked = "checked";
  } else {
    showInsideConfirmation(true)
  }
}

function showInsideConfirmation(show) {
  var dialog = document.getElementById("insideConfirmation");
  if (show) {
    dialog.className = "visible";
  } else {
    dialog.className = "invisible";
  }
}

function onConfirmation(value) {
  if (value) {
    mIsWaiting = false;
    document.getElementById("insideButton").checked = "checked";
  } else {
    document.getElementById("waitingButton").checked = "checked";
  }
  showInsideConfirmation(false);
}
