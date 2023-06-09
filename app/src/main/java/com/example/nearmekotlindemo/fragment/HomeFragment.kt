package com.example.nearmekotlindemo. fragment

import android.Manifest
import com.example.nearmekotlindemo.R
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.activities.DirectionActivity
import com.example.nearmekotlindemo.adapter.DirectionStepAdapter
import com.example.nearmekotlindemo.adapter.GooglePlaceAdapter
import com.example.nearmekotlindemo.adapter.InfoWindowAdapter
import com.example.nearmekotlindemo.constant.AppConstant
import com.example.nearmekotlindemo.databinding.FragmentHomeBinding
import com.example.nearmekotlindemo.interfaces.NearLocationInterface
import com.example.nearmekotlindemo.models.googlePlaceModel.GooglePlaceModel
import com.example.nearmekotlindemo.models.googlePlaceModel.GoogleResponseModel
import com.example.nearmekotlindemo.models.googlePlaceModel.PostInfo
import com.example.nearmekotlindemo.models.googlePlaceModel.ToaDo
import com.example.nearmekotlindemo.models.googlePlaceModel.University
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
import com.google.maps.android.SphericalUtil
import kotlin.math.roundToInt


class HomeFragment : Fragment(), OnMapReadyCallback, NearLocationInterface,
    GoogleMap.OnMarkerClickListener {


    private lateinit var binding: FragmentHomeBinding
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
    private var endLat: Double? = 16.077558
    private var endLng: Double? = 108.213324
    val currentLocationStart = MutableLiveData<Location>()
    val currentLocationEnd = MutableLiveData<LatLng>()
    private lateinit var mMap: GoogleMap
    var SPKyThuat = LatLng(16.077558, 108.213324)
    var DHDuyTan = LatLng(16.077085, 108.211112)
    private var locationArraylist: ArrayList<LatLng>?=null
    val checkStatus = MutableLiveData<Boolean>(false)
    var setdata=ArrayList<PostInfo?>()
    var universityList = MutableLiveData<List<University>> ()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var itemName = listOf("Khac")

        var autoComplete : AutoCompleteTextView =binding.autoSlect

        val item = listOf("DH Su Pham Ky Thuat","DH Duy Tan(CS1)","DH Kinh Te","Another place")
        var adapter = ArrayAdapter(requireContext() ,R.layout.list_item_school,itemName)
        val database = Firebase.database.getReference("University")
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG,"kiem tra onDataChange University  : "+snapshot.value)
                try {

                    var  userPlaces : List<University> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(University::class.java)!!

                    }

                    universityList.value=  userPlaces
                    for(i in userPlaces ){
                        itemName =itemName + listOf(i.name)
                    }
                    Log.d(TAG,"kiem tra University : "+userPlaces)
                    Log.d(TAG,"kiem tra name University : "+itemName)
                    adapter = ArrayAdapter(requireContext() ,R.layout.list_item_school,itemName)
                    autoComplete.setAdapter(adapter)
                }catch (e : Exception){

                }


            }

                override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
//        (activity as AppCompatActivity?)!!.supportActionBar!!.title = "Grap Share"
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        locationArraylist= ArrayList()
        locationArraylist!!.add(SPKyThuat)
        locationArraylist!!.add(DHDuyTan )
        appPermission = AppPermissions()
        loadingDialog = LoadingDialog(requireActivity())
        firebaseAuth = Firebase.auth
        googlePlaceList = ArrayList()
        adapterStep = DirectionStepAdapter()







        autoComplete.onItemClickListener= AdapterView.OnItemClickListener { adapterView, view, i, l ->
            mMap.clear()
            if(adapterView.getItemAtPosition(i).toString().equals("DH Su Pham Ky Thuat")){

                var td=LatLng(16.078179,108.212011)
                currentLocationEnd.value  =td

                Log.d(TAG,"kiem tra vi tri currentLocationEnd:"+currentLocationEnd.value.toString())

                mMap.addMarker(MarkerOptions().position(td).icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title("info.postId"))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(td))
            }
            if(adapterView.getItemAtPosition(i).toString().equals("Khac")){
                val fragment: Fragment = PostNotherPlaceFragment()
//                val bundle=Bundle()
//                bundle.putString("idPost",it.title)
//                fragment.arguments=bundle
                val transaction = fragmentManager?.beginTransaction()
                transaction?.replace(R.id.fragmentContainer,fragment)?.commit()
                }
//           var a= adapterView.getItemAtPosition(i).toString()
            Log.d(TAG,"kiem tra data  autoComplete.onItemClickListener: "+adapterView.getItemAtPosition(i).toString())
            viewModel.getPostWithUnversity(adapterView.getItemAtPosition(i).toString())

        }


        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isLocationPermissionOk =
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                            && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

                if (isLocationPermissionOk)
//                    setUpGoogleMap()
                else
                    Snackbar.make(binding.root, "Location permission denied", Snackbar.LENGTH_LONG)
                        .show()

            }

        val mapFragment =
            (childFragmentManager.findFragmentById(R.id.homeMap) as SupportMapFragment?)
        mapFragment?.getMapAsync(this)

//
//        for (placeModel in AppConstant.placesName) {
//            val chip = Chip(requireContext())
//            chip.text = placeModel.name
//            chip.id = placeModel.id
//            chip.setPadding(8, 8, 8, 8)
//            chip.setTextColor(resources.getColor(R.color.white, null))
//            chip.chipBackgroundColor = resources.getColorStateList(R.color.primaryColor, null)
//            chip.chipIcon = ResourcesCompat.getDrawable(resources, placeModel.drawableId, null)
//            chip.isCheckable = true
//            chip.isCheckedIconVisible = false
//            binding.placesGroup.addView(chip)
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


        binding.currentLocation.setOnClickListener { getCurrentLocation() }

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


        binding.drawLine.setOnClickListener {
            checkStatus.value=!checkStatus.value!!
            if(checkStatus.value ==true)
                binding.drawLine.setImageResource(R.drawable.ic_close)
            else
                binding.drawLine.setImageResource(R.drawable.ic_riseoutlined)
//            Log.d(TAG,"kiem tra drawLine.setOnClickListener: "+it)
    ////            getDirection("bicycling")
//            getDirection("walking")
        }
        binding.placesGroup.setOnCheckedChangeListener { _, checkedId ->

//            if (checkedId != -1) {
//                val placeModel = AppConstant.placesName[checkedId - 1]
//                binding.edtPlaceName.setText(placeModel.name)
//                getNearByPlace(placeModel.placeType)
//            }
        }
        viewModel.allUsers.observe(viewLifecycleOwner, Observer {
            Log.d(TAG,"kiem tra data HomeFragent allUsers: "+it)
            if(it.toString().isNotEmpty()){

                for(item in it){
                    var info:Post=item
                    var td=LatLng(info.lat,info.lng)
                    Log.d(TAG,"thong tin location university TTTT: "+info)
                    mMap.addMarker(MarkerOptions().position(td).title(info.postId))
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(td))
                }
            }


        })

        setUpRecyclerView()

        lifecycleScope.launchWhenStarted {
            userSavedLocaitonId = locationViewModel.getUserLocationId()
            Log.d("TAG", "onViewCreated: ${userSavedLocaitonId.size}")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mGoogleMap = googleMap
        mMap=googleMap
        val data = MutableLiveData<List<PostInfo>>()
        val dataItem = MutableLiveData<PostInfo>()
//        for (i in locationArraylist!!.indices){
//            mMap.addMarker(MarkerOptions().position(locationArraylist!![i]).title("spkt"))
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f))
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(locationArraylist!![i]))
//
//        }
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)

//        viewModel.allUsers.observe(viewLifecycleOwner, Observer {
//            Log.d(TAG,"kiem tra data HomeFragent allUsers: "+it)
//            if(it.toString().isNotEmpty()){
//
//                for(item in it){
//                    var info:Post=item
//                    var td=LatLng(info.lat,info.lng)
//                    Log.d(TAG,"thong tin location university TTTT: "+info)
//                    mMap.addMarker(MarkerOptions().position(td).title(info.postId))
//                    mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f))
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(td))
//
////                    dataItem.value?.post =item
////                    dataItem.value?.locale= LatLng(dataItem.value!!.post?.lat,dataItem.value!!.post?.lng)
////                    Log.d(TAG,"thong tin location university dataItem: "+dataItem.value)
////                    if(dataItem.value.toString().isNotEmpty()) {
////                        setdata.add(dataItem.value)
//////                        Log.d(TAG,"thong tin location university dataItem: "+dataItem.value)
////                    }
//                }
//            }
////            if(setdata.size!=0){
////                for (i in  setdata){
////                    var item= i?.locale
////                    Log.d(TAG,"thong tin location university setdata: "+item.toString())
////                    Log.d(TAG,"thong tin location size: "+setdata)
////                    if(item!= null) {
////                        mMap.addMarker(MarkerOptions().position(item).title("thongso"))
////                        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f))
////                        mMap.moveCamera(CameraUpdateFactory.newLatLng(item))
////                    }
////                }
////            }
//
//
//        })
//        for (i in  setdata){
//            var item= i?.locale
//            Log.d(TAG,"thong tin location university setdata"+item)
//            mMap.addMarker(MarkerOptions().position(item).title("thongso"))
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f))
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(item))
//
//        }
        mMap.setOnMarkerClickListener {

            if(checkStatus.value == true){

                getDirection("walking")


            }
            else {
                //  Take some action here
                viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
                it.title
                Log.d(TAG, "kiem tra gia tri it.title: " + it.title)
                viewModel.setidPost(it.title)
//            view?.let { it1 ->
//                Navigation.findNavController(it1)
//                    .navigate(R.id.action_btnHome_to_checkInfo)
//            }
                val fragment: Fragment = CheckInfoFragment()
                val bundle = Bundle()
                bundle.putString("idPost", it.title)
                fragment.arguments = bundle
                val transaction = fragmentManager?.beginTransaction()
                transaction?.replace(R.id.fragmentContainer, fragment)?.commit()
            }
            true
        }


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
                Toast.makeText(requireContext(), "Location update start", Toast.LENGTH_SHORT).show()
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
            currentLocationStart.value=it
            infoWindowAdapter = null
            infoWindowAdapter = InfoWindowAdapter(currentLocation, requireContext())
            mGoogleMap?.setInfoWindowAdapter(infoWindowAdapter)
            moveCameraToLocation(currentLocation)
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "$it", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveCameraToLocation(location: Location) {

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            LatLng(
                location.latitude,
                location.longitude
            ), 16f
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


    private fun getNearByPlace(placeType: String) {
        val url = ("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + currentLocation.latitude + "," + currentLocation.longitude
                + "&radius=" + radius + "&type=" + placeType + "&key=" +
                resources.getString(R.string.API_KEY))

        lifecycleScope.launchWhenStarted {
            locationViewModel.getNearByPlace(url).collect {
                when (it) {
                    is State.Loading -> {
                        if (it.flag == true) {
                            loadingDialog.startLoading()
                        }
                    }

                    is State.Success -> {
                        loadingDialog.stopLoading()
                        val googleResponseModel: GoogleResponseModel =
                            it.data as GoogleResponseModel

                        if (googleResponseModel.googlePlaceModelList !== null &&
                            googleResponseModel.googlePlaceModelList.isNotEmpty()
                        ) {
                            googlePlaceList.clear()
                            mGoogleMap?.clear()

                            for (i in googleResponseModel.googlePlaceModelList.indices) {

                                googleResponseModel.googlePlaceModelList[i].saved =
                                    userSavedLocaitonId.contains(googleResponseModel.googlePlaceModelList[i].placeId)
                                googlePlaceList.add(googleResponseModel.googlePlaceModelList[i])
                                addMarker(googleResponseModel.googlePlaceModelList[i], i)
                            }
                            googlePlaceAdapter.setGooglePlaces(googlePlaceList)
                        } else {
                            mGoogleMap?.clear()
                            googlePlaceList.clear()

                        }

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
    private fun clearUI() {
        mMap?.clear()

    }
    fun Distance(location: Location,marker: Marker){
        val distance = SphericalUtil.computeDistanceBetween(
            LatLng(
                location.latitude, location.longitude
            ), marker.position
        )

//        if (distance.roundToInt() > 1000) {
//            val kilometers = (distance / 1000).roundToInt()
//            binding.txtLocationDistance.text = "$kilometers KM"
//        } else {
//            binding.txtLocationDistance.text = "${distance.roundToInt()} Meters"
//
//        }
    }
    private fun getDirection(mode: String) {
        var startLocationLat:Double=currentLocationStart.value!!.latitude
        var startLocationLag:Double=currentLocationStart.value!!.longitude
        Log.d(TAG,"kiem tra vi tri: "+currentLocationEnd.value)
        Log.d(TAG,"kiem tra vi tri start: "+startLocationLat+"=>"+startLocationLag)


        if (isLocationPermissionOk) {
//            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
//                    "origin=" + currentLocation.latitude + "," + currentLocation.longitude +
//                    "&destination=" + endLat + "," + endLng +
//                    "&mode=" + mode +
//                    "&key=" + resources.getString(R.string.API_KEY)
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + startLocationLat + "," + startLocationLag +
                    "&destination=" + currentLocationEnd.value?.latitude + "," + currentLocationEnd.value?.longitude +
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

//                            mMap?.addMarker(
//                                MarkerOptions()
//                                    .position(endLocation)
//                                    .title("End Location")
//                            )

//                            mMap?.addMarker(
//                                MarkerOptions()
//                                    .position(startLocation)
//                                    .title("Start Location")
//                            )

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

        val background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location)
        background?.setTint(resources.getColor(R.color.quantum_purple800, null))
        background?.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            background?.intrinsicWidth!!, background.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)

    }

    private fun setUpRecyclerView() {
        val snapHelper: SnapHelper = PagerSnapHelper()
        googlePlaceAdapter = GooglePlaceAdapter(this)

        binding.placesRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            setHasFixedSize(false)
            adapter = googlePlaceAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val linearManager = recyclerView.layoutManager as LinearLayoutManager
                    val position = linearManager.findFirstCompletelyVisibleItemPosition()
                    if (position > -1) {
                        val googlePlaceModel: GooglePlaceModel = googlePlaceList[position]
                        mGoogleMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    googlePlaceModel.geometry?.location?.lat!!,
                                    googlePlaceModel.geometry.location.lng!!
                                ), 20f
                            )
                        )
                    }
                }
            })
        }

        snapHelper.attachToRecyclerView(binding.placesRecyclerView)
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
//        val markerTag = marker.tag as Int
//        Log.d("TAG", "onMarkerClick: $markerTag")
//        binding.placesRecyclerView.scrollToPosition(markerTag)
        return false
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
