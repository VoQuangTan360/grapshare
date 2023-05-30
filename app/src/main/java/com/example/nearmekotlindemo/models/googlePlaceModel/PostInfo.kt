package com.example.nearmekotlindemo.models.googlePlaceModel

import com.example.nearmekotlindemo.Post
import com.google.android.gms.maps.model.LatLng

data class PostInfo(
    var locale: LatLng =LatLng(0.0,0.0),
    var post:Post
)
