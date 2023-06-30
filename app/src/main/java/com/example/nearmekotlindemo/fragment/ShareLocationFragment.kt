package com.example.nearmekotlindemo.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.activities.DirectionActivity
import com.example.nearmekotlindemo.adapter.DirectionStepAdapter
import com.example.nearmekotlindemo.adapter.GooglePlaceAdapter
import com.example.nearmekotlindemo.adapter.InfoWindowAdapter
import com.example.nearmekotlindemo.databinding.FragmentShareLocationBinding
import com.example.nearmekotlindemo.interfaces.NearLocationInterface
import com.example.nearmekotlindemo.models.googlePlaceModel.GooglePlaceModel
import com.example.nearmekotlindemo.models.googlePlaceModel.StatusID
import com.example.nearmekotlindemo.models.googlePlaceModel.ToaDo
import com.example.nearmekotlindemo.models.googlePlaceModel.directionPlaceModel.DirectionLegModel
import com.example.nearmekotlindemo.models.googlePlaceModel.directionPlaceModel.DirectionResponseModel
import com.example.nearmekotlindemo.models.googlePlaceModel.directionPlaceModel.DirectionRouteModel
import com.example.nearmekotlindemo.permissions.AppPermissions
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.utility.State
import com.example.nearmekotlindemo.viewModels.LocationViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ShareLocationFragment : Fragment(), OnMapReadyCallback, NearLocationInterface,
    GoogleMap.OnMarkerClickListener {
    private lateinit var binding: FragmentShareLocationBinding
    private var mGoogleMap: GoogleMap? = null
    private lateinit var appPermission: AppPermissions
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var permissionToRequest = mutableListOf<String>()
    private var isLocationPermissionOk = false
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var currentLocation: Location
    private var currentMarker: Marker? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private var isTrafficEnable: Boolean = false
    private var radius = 1500
    private val locationViewModel: LocationViewModel by viewModels<LocationViewModel>()
    private lateinit var viewModel : LocationViewModel
    private lateinit var googlePlaceList: ArrayList<GooglePlaceModel>
    private lateinit var googlePlaceAdapter: GooglePlaceAdapter
    private var userSavedLocaitonId: ArrayList<String> = ArrayList()
    private var infoWindowAdapter: InfoWindowAdapter? = null
    private lateinit var adapterStep: DirectionStepAdapter
    private var endLat: Double = 16.075558
    private var endLng: Double = 108.212324
    val currentLocationStart = MutableLiveData<ToaDo>()
    val i = MutableLiveData<Int>(1)
    val currentLocationEnd = MutableLiveData<ToaDo>()
    var postId="0"

//    var a = mGoogleMap?.addMarker(MarkerOptions().position(LatLng(endLat,endLng)).icon(getCustomIcon()).title(""))
    private lateinit var mMap: GoogleMap
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentShareLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments?.getString("idPost") !=null){
            postId= arguments?.getString("idPost")!!
        }


        i.value?.let { loop(it) }

//        (activity as AppCompatActivity?)!!.supportActionBar!!.title = "Grap Share"
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        appPermission = AppPermissions()
        loadingDialog = LoadingDialog(requireActivity())
        firebaseAuth = Firebase.auth
        googlePlaceList = ArrayList()
        adapterStep = DirectionStepAdapter()
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isLocationPermissionOk =
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                            && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

                if (isLocationPermissionOk)
                    setUpGoogleMap()
                else
                    Snackbar.make(binding.root, "Location permission denied", Snackbar.LENGTH_LONG)
                        .show()

            }
        setUpGoogleMap()
        var userList = MutableLiveData<ToaDo> ()

        val database = Firebase.database.getReference("FollowTaiXe").child(postId.toString())
        var a : Marker? =null
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                val post = snapshot.getValue(ToaDo::class.java)
//                Log.d(TAG,"kiem tra FollowLocationTaiXe : "+post)
                try {

                    var  userPlaces : List<ToaDo> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(ToaDo::class.java)!!

                    }
                    userList.value=userPlaces[0]

//                    mGoogleMap?.addMarker(MarkerOptions().position( LatLng(userPlaces[0].latitude,userPlaces[0].longitude)).icon(getCustomIcon()).title(""))
//                    TrackingLocationTaxi(LatLng(userPlaces[0].latitude,userPlaces[0].longitude))

                    if(a==null &&currentLocationStart.value!=null) {
                        a = mMap?.addMarker(
                            MarkerOptions().position(LatLng(currentLocationStart.value!!.latitude, currentLocationStart.value!!.longitude)).icon(getCustomIcon())
                                .title("")
                        )
                    }
                    else{
                        a!!.position=LatLng(userPlaces[0].latitude,userPlaces[0].longitude)
                    }
                        Log.d(TAG,"kiem tra FollowLocationTaiXe : "+ userList.value)

                }catch (e : Exception){

                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })

        val databaseStatus = Firebase.database.getReference("CheckFollow")
        databaseStatus.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {

                    var  userPlaces : List<StatusID> = snapshot.children.map { dataSnapshot ->
                        dataSnapshot.getValue(StatusID::class.java)!!
                    }
                    for(item in userPlaces){
                        if(item.id==postId){
                            if(item.status=="0"){
                                Log.d(TAG,"Chuyến đi bị hủy: ")
                                val showDialog = DialogHuy("Chuyến đi bị hủy")
                                showDialog.show((activity as AppCompatActivity).supportFragmentManager, "Yêu cầu")
                                i.value=2
                                CoroutineScope(IO).launch {
                                    delay(5000)
                                    CoroutineScope(IO).launch {
                                        val fragment: Fragment = HomeFragment()
                                        val transaction = fragmentManager?.beginTransaction()
                                        transaction?.replace(R.id.fragmentContainer,fragment)?.commit()
                                    }
                                }
                            }
                        }
                    }


//                    mGoogleMap?.addMarker(MarkerOptions().position( LatLng(userPlaces[0].latitude,userPlaces[0].longitude)).icon(getCustomIcon()).title(""))
//                    TrackingLocationTaxi(LatLng(userPlaces[0].latitude,userPlaces[0].longitude))

                }catch (e : Exception){

                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
        val mapFragment =
            (childFragmentManager.findFragmentById(R.id.homeMap) as SupportMapFragment?)
        mapFragment?.getMapAsync(this)


//        lifecycleScope.launchWhenStarted {
//            locationViewModel.FollowLocationTaiXe("4").collect {
//                when (it) {
//                    is State.Loading -> {
//
//                    }
//                    is State.Success -> {
//                        var td=it.data as ToaDo
//                        TrackingLocationTaxi(LatLng(td.latitude,td.longitude))
//                    }
//                    is State.Failed -> {
//
//                    }
//                }
//            }
//        }

        binding.enableTraffic.setOnClickListener {

            if (isTrafficEnable) {
                mGoogleMap?.apply {
                    isTrafficEnabled = false
                    isTrafficEnable = false
                }
            } else {
                mGoogleMap?.apply {
                    isTrafficEnabled = true
                    isTrafficEnable = true
                }
            }
        }
        binding.huy.setOnClickListener {
            val showDialog = DialogHuy("Nếu hủy số sao của bạn sẽ bị trừ")
            showDialog.show((activity as AppCompatActivity).supportFragmentManager, "Yêu cầu")
        }
        binding.btnStart.setOnClickListener {

            databaseStatus.child(postId).setValue(StatusID(postId,"2"))
            binding.btnStart.setBackgroundColor(Color.YELLOW)
            binding.btnStart.setTextColor(Color.BLACK)
        }
        binding.btndatoi.setOnClickListener {

            databaseStatus.child(postId).setValue(StatusID(postId,"3"))
            binding.btndatoi.setBackgroundColor(Color.YELLOW)
            binding.btndatoi.setTextColor(Color.BLACK)
        }
        binding.btnTime.setOnClickListener {

            databaseStatus.child(postId).setValue(StatusID(postId,"5"))
            binding.btnTime.setBackgroundColor(Color.YELLOW)
            binding.btnTime.setTextColor(Color.BLACK)
        }

        binding.currentLocation.setOnClickListener {
           getCurrentLocation()
            getCurrentLocationtoShare()
           getDirection("walking")
//            TrackingLocationTaxi()

        }

        binding.btnMapType.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)

            popupMenu.apply {
                menuInflater.inflate(R.menu.map_type_menu, popupMenu.menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {

                        R.id.btnNormal -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                        R.id.btnSatellite -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        R.id.btnTerrain -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    }
                    true
                }

                show()
            }
        }


//        binding.drawLine.setOnClickListener {
//            Log.d(TAG,"kiem tra drawLine.setOnClickListener: "+it)
////            getDirection("bicycling")
//            getDirection("walking")
//        }

        binding.placesGroup.setOnCheckedChangeListener { _, checkedId ->

        }
        viewModel.allUsers.observe(viewLifecycleOwner, Observer {
            Log.d(TAG,"kiem tra data HomeFragent allUsers: "+it)
            if(it.toString().isNotEmpty()){

                for(item in it){
                    if(item.postId==postId) {
                        var info: Post = item
                        var td = LatLng(info.lat, info.lng)
                        currentLocationEnd.value= ToaDo(td.latitude,td.longitude)
                        Log.d(TAG, "thong tin location university TTTT: " + info)
                        mMap.addMarker(MarkerOptions().position(td).title(info.postId))
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(td))
                    }
                }
            }


        })
        lifecycleScope.launchWhenStarted {
            userSavedLocaitonId = locationViewModel.getUserLocationId()
            Log.d("TAG", "onViewCreated: ${userSavedLocaitonId.size}")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mGoogleMap = googleMap
        mMap=googleMap

        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
//        mMap.setOnMarkerClickListener {
//            //  Take some action here
//            viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
//            it.title
//            Log.d(TAG,"kiem tra gia tri it.title: "+it.title)
//            viewModel.setidPost(it.title)
////            view?.let { it1 ->
////                Navigation.findNavController(it1)
////                    .navigate(R.id.action_btnHome_to_checkInfo)
////            }
////            val fragment: Fragment = CheckInfoFragment()
////            val bundle=Bundle()
////            bundle.putString("idPost",it.title)
////            fragment.arguments=bundle
////            val transaction = fragmentManager?.beginTransaction()
////            transaction?.replace(R.id.fragmentContainer,fragment)?.commit()
//            true
//        }
//        getCurrentLocationtoShare()

        when {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                isLocationPermissionOk = true
//                setUpGoogleMap()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Location Permission")
                    .setMessage("Near me required location permission to access your location")
                    .setPositiveButton("Ok") { _, _ ->
                        requestLocation()
                    }.create().show()
            }

            else -> {
                requestLocation()
            }
        }


    }

    private fun requestLocation() {
        permissionToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissionToRequest.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        permissionLauncher.launch(permissionToRequest.toTypedArray())
    }
    private fun TrackingLocationTaxi(td:LatLng){
        if(currentLocationStart.value!=null) {
//        var td=LatLng(currentLocationStart.value!!.latitude,currentLocationStart.value!!.longitude)
//            mGoogleMap?.clear()


        }

//        }
    }
    fun loop(i:Int) {
        if(i==1) {
            CoroutineScope(IO).launch {
                delay(500)
                CoroutineScope(Main).launch {
                    getCurrentLocationtoShare()
                    loop(i)
                }
            }
        }
    }

    private fun setUpGoogleMap() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        mGoogleMap?.isMyLocationEnabled = true
        mGoogleMap?.uiSettings?.isTiltGesturesEnabled = true
        mGoogleMap?.setOnMarkerClickListener(this)

        setUpLocationUpdate()
    }

    private fun setUpLocationUpdate() {

        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                for (location in locationResult?.locations!!) {
                    Log.d("TAG", "onLocationResult: ${location.longitude} ${location.latitude}")
                }
            }
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Vị trí được cập nhật", Toast.LENGTH_SHORT).show()
            }
        }

        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {

            currentLocation = it
            infoWindowAdapter = null
            currentLocationStart.value=ToaDo(it.latitude,it.longitude)
            infoWindowAdapter = InfoWindowAdapter(currentLocation, requireContext())
            mGoogleMap?.setInfoWindowAdapter(infoWindowAdapter)
            moveCameraToLocation(currentLocation)
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "$it", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getCurrentLocationtoShare() {
        val context = context
        if(context!=null) {
            val fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireContext())

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                isLocationPermissionOk = false
                return
            }
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {

                locationViewModel.createFollowTaiXe(postId, ToaDo(it.latitude, it.longitude))
                Log.d(TAG, "kiem tra  co lay idPost hay khong " + arguments?.getString("idPost")!!)


//            infoWindowAdapter = InfoWindowAdapter(currentLocation, requireContext())
//            mGoogleMap?.setInfoWindowAdapter(infoWindowAdapter)
//            moveCameraToLocation(currentLocation)
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "$it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveCameraToLocation(location: Location) {

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            LatLng(
                location.latitude,
                location.longitude
            ), 17f
        )

        val markerOption = MarkerOptions()
            .position(LatLng(location.latitude, location.longitude))
            .title("Current Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .snippet(firebaseAuth.currentUser?.displayName)

        currentMarker?.remove()
        currentMarker = mGoogleMap?.addMarker(markerOption)
        currentMarker?.tag = 703
        mGoogleMap?.animateCamera(cameraUpdate)

    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        Log.d("TAG", "stopLocationUpdates: Location Update Stop")
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        if (fusedLocationProviderClient != null) {
            startLocationUpdates()
            currentMarker?.remove()
        }
    }

    private fun clearUI() {
        mMap?.clear()

    }
    private fun getDirection(mode: String) {

//        clearUI()
        if (isLocationPermissionOk && currentLocationStart.value!=null) {
//            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
//                    "origin=" + currentLocation.latitude + "," + currentLocation.longitude +
//                    "&destination=" + endLat + "," + endLng +
//                    "&mode=" + mode +
//                    "&key=" + resources.getString(R.string.API_KEY)
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + currentLocationEnd.value!!.latitude + "," + currentLocationEnd.value!! +
                    "&destination=" + currentLocation.latitude + "," + currentLocation.longitude +
                    "&mode=" + mode +
                    "&key=" + resources.getString(R.string.API_KEY)

            lifecycleScope.launchWhenStarted {
                locationViewModel.getDirection(url).collect {
                    when (it) {
                        is State.Loading -> {
                            if (it.flag == true) {
                                loadingDialog.startLoading()
                            }
                        }

                        is State.Success -> {
                            loadingDialog.stopLoading()
//                            clearUI()

                            val directionResponseModel: DirectionResponseModel =
                                it.data as DirectionResponseModel
                            Log.d(TAG,"kiem tra line tra ve: "+it.data.toString())
                            val routeModel: DirectionRouteModel =
                                directionResponseModel.directionRouteModels!![0]

                            val legModel: DirectionLegModel = routeModel.legs?.get(0)!!

                            adapterStep.setDirectionStepModels(legModel.steps!!)

                            val stepList: MutableList<LatLng> = ArrayList()

                            val options = PolylineOptions().apply {
                                width(25f)
                                color(Color.BLUE)
                                geodesic(true)
                                clickable(true)
                                visible(true)
                            }

                            val pattern: List<PatternItem>

                            if (mode == "walking") {
                                pattern = listOf(
                                    Dot(), Gap(10f)
                                )

                                options.jointType(JointType.ROUND)
                            } else {

                                pattern = listOf(
                                    Dash(30f)
                                )

                            }

                            options.pattern(pattern)
                            for (stepModel in legModel.steps) {
                                val decodedList = decode(stepModel.polyline?.points!!)
                                for (latLng in decodedList) {
                                    stepList.add(
                                        LatLng(
                                            latLng.lat,
                                            latLng.lng
                                        )
                                    )
                                }
                            }

                            options.addAll(stepList)
                            mMap?.addPolyline(options)
                            val startLocation = com.google.android.gms.maps.model.LatLng(
                                legModel.startLocation?.lat!!,
                                legModel.startLocation.lng!!
                            )

                            val endLocation = com.google.android.gms.maps.model.LatLng(
                                legModel.endLocation?.lat!!,
                                legModel.endLocation.lng!!
                            )

                            mMap?.addMarker(
                                MarkerOptions()
                                    .position(endLocation)
                                    .title("End Location")
                            )

                            mMap?.addMarker(
                                MarkerOptions()
                                    .position(startLocation)
                                    .title("Start Location")
                            )

                            val builder = LatLngBounds.builder()
                            builder.include(endLocation).include(startLocation)
                            val latLngBounds = builder.build()


                            mMap?.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(
                                    latLngBounds, 0
                                )
                            )


                        }
                        is State.Failed -> {

                            Log.d(TAG,"check loi API: "+it.error)
                            loadingDialog.stopLoading()
                            Snackbar.make(
                                binding.root, it.error,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }


    private fun addMarker(googlePlaceModel: GooglePlaceModel, position: Int) {
        val markerOptions = MarkerOptions()
            .position(
                LatLng(
                    googlePlaceModel.geometry?.location?.lat!!,
                    googlePlaceModel.geometry.location.lng!!
                )
            )
            .title(googlePlaceModel.name)
            .snippet(googlePlaceModel.vicinity)

        markerOptions.icon(getCustomIcon())
        mGoogleMap?.addMarker(markerOptions)?.tag = position

    }

    private fun getCustomIcon(): BitmapDescriptor {

        val background = ContextCompat.getDrawable(requireContext(), R.drawable.icon_car)
//        background?.setTint(resources.getColor(R.color.quantum_purple800, null))
        background?.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            background?.intrinsicWidth!!, background.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)

    }



    override fun onSaveClick(googlePlaceModel: GooglePlaceModel) {
        if (userSavedLocaitonId.contains(googlePlaceModel.placeId)) {
            AlertDialog.Builder(requireContext())
                .setTitle("Remove Place")
                .setMessage("Are you sure to remove this place?")
                .setPositiveButton("Yes") { _, _ ->
                    removePlace(googlePlaceModel)
                }
                .setNegativeButton("No") { _, _ -> }
                .create().show()
        } else {
            addPlace(googlePlaceModel)

        }
    }

    private fun addPlace(googlePlaceModel: GooglePlaceModel) {
        lifecycleScope.launchWhenStarted {
            locationViewModel.addUserPlace(googlePlaceModel, userSavedLocaitonId).collect {
                when (it) {
                    is State.Loading -> {
                        if (it.flag == true) {
                            loadingDialog.startLoading()
                        }
                    }

                    is State.Success -> {
                        loadingDialog.stopLoading()
                        val placeModel: GooglePlaceModel = it.data as GooglePlaceModel
                        userSavedLocaitonId.add(placeModel.placeId!!)
                        val index = googlePlaceList.indexOf(placeModel)
                        googlePlaceList[index].saved = true
                        googlePlaceAdapter.notifyDataSetChanged()
                        Snackbar.make(binding.root, "Saved Successfully", Snackbar.LENGTH_SHORT)
                            .show()

                    }
                    is State.Failed -> {
                        loadingDialog.stopLoading()
                        Snackbar.make(
                            binding.root, it.error,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    }

    @SuppressLint("ShowToast")
    private fun removePlace(googlePlaceModel: GooglePlaceModel) {
        userSavedLocaitonId.remove(googlePlaceModel.placeId)
        val index = googlePlaceList.indexOf(googlePlaceModel)
        googlePlaceList[index].saved = false
        googlePlaceAdapter.notifyDataSetChanged()

        Snackbar.make(binding.root, "Place removed", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                userSavedLocaitonId.add(googlePlaceModel.placeId!!)
                googlePlaceList[index].saved = true
                googlePlaceAdapter.notifyDataSetChanged()
            }
            .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    lifecycleScope.launchWhenStarted {
                        locationViewModel.removePlace(userSavedLocaitonId).collect {
                            when (it) {
                                is State.Loading -> {

                                }

                                is State.Success -> {
                                    Snackbar.make(
                                        binding.root,
                                        it.data.toString(),
                                        Snackbar.LENGTH_SHORT
                                    ).show()

                                }
                                is State.Failed -> {
                                    Snackbar.make(
                                        binding.root, it.error,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            })
            .show()

    }

    override fun onDirectionClick(googlePlaceModel: GooglePlaceModel) {
        val placeId = googlePlaceModel.placeId
        val lat = googlePlaceModel.geometry?.location?.lat
        val lng = googlePlaceModel.geometry?.location?.lng
        val intent = Intent(requireContext(), DirectionActivity::class.java)
        intent.putExtra("placeId", placeId)
        intent.putExtra("lat", lat)
        intent.putExtra("lng", lng)

        startActivity(intent)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val markerTag = marker.tag as Int
        Log.d("TAG", "onMarkerClick: $markerTag")
//        binding.placesRecyclerView.scrollToPosition(markerTag)
        return true
    }
    private fun decode(points: String): List<com.google.maps.model.LatLng> {
        val len = points.length
        val path: MutableList<com.google.maps.model.LatLng> = java.util.ArrayList(len / 2)
        var index = 0
        var lat = 0
        var lng = 0
        while (index < len) {
            var result = 1
            var shift = 0
            var b: Int
            do {
                b = points[index++].toInt() - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            result = 1
            shift = 0
            do {
                b = points[index++].toInt() - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            path.add(com.google.maps.model.LatLng(lat * 1e-5, lng * 1e-5))
        }
        return path
    }
}
