var mFirebaseRef = new Firebase("https://sisobustracker.firebaseio.com/RouteNumber/");

var mMap;
var mMapBounds;
var mRouteNumber = "9";
var mIsWaiting = true;
var mOfficeLocation;

var mOfficeMarker;
var mUserMarker;
var mBusMarker;

var mUserLocationListener;

function initMap() {
  loadUi();

  mOfficeLocation = new google.maps.LatLng(12.980253, 77.697375);
  mMapBounds = new google.maps.LatLngBounds();
  mMapBounds.extend(mOfficeLocation);

  var mapOptions = {
    center: mOfficeLocation,
    zoom: 15
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

  mUserLsocationListener = navigator.geolocation.watchPosition(setUserLocation);
}

function setUserLocation(position) {
  var latitude = position.coords.latitude;
  var longitude = position.coords.longitude;
  var shouldResizeBounds = false;
  if (mUserMarker.getPosition() == null) {
    shouldResizeBounds = true;
  }
  mUserMarker.setPosition(new google.maps.LatLng(latitude, longitude));
  if (shouldResizeBounds) {
    mMapBounds.extend(mUserMarker.getPosition());
    updateMapBounds();
  }

  if (!mIsWaiting) {
    updateBusLocation(latitude, longitude);
  }
}

function getBusLocation() {
  mFirebaseRef.child(mRouteNumber).on("value", function(snapshot) {
    setBusLocation(snapshot.val().latitude, snapshot.val().longitude);
  }, function(error) {
    console.log(error.code);
  });
}

function setBusLocation(latitude, longitude) {
  var shouldResizeBounds = false;
  if (mBusMarker.getPosition() == null) {
    shouldResizeBounds = true;
  }
  mBusMarker.setPosition(new google.maps.LatLng(latitude, longitude));
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
  } else {
    document.getElementById("waitingButton").checked = "checked";
  }
  showInsideConfirmation(false);
}
