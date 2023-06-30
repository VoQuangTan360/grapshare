package com.example.nearmekotlindemo.fragment

import android.content.ContentValues.TAG
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.UserModel
import com.example.nearmekotlindemo.databinding.FragmentCheckInfoBinding
import com.example.nearmekotlindemo.models.googlePlaceModel.Mess
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.utility.State
import com.example.nearmekotlindemo.viewModels.LocationViewModel
import com.example.nearmekotlindemo.viewModels.PostViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


class CheckInfoFragment : Fragment() {
    private val postViewModel: PostViewModel by viewModels()
    private lateinit var binding: FragmentCheckInfoBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var viewModel : LocationViewModel
    lateinit var model: LocationViewModel
    val currentLocationStart = MutableLiveData<Location>(Location("dummyprovider"))
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity())
        var idPost:String=""
        var vehicle:String=""
        val auth = Firebase.auth.currentUser?.photoUrl.toString()
        val idAuth=Firebase.auth.uid.toString()
        val name = Firebase.auth.currentUser?.displayName.toString()
        model = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)

        if(arguments?.getString("idPost") !=null){
            viewModel.getPostWithUnversityAndIDcheck(arguments?.getString("idPost")!!)

        }
//        locationViewModel.post1.observe(viewLifecycleOwner,Observer{
//            Log.d(TAG,"kiem tra idPost:"+it)
//            viewModel.getPostWithUnversityAndIDcheck(it)
//        })

        val itemXe = listOf("Ô tô","Xe máy","Xe đạp")
        val adapterXe = ArrayAdapter(requireContext() ,R.layout.list_item_xe,itemXe)
        val autoCompleteXe : AutoCompleteTextView =binding.autoSlectTranaport
        autoCompleteXe.setAdapter(adapterXe)

        autoCompleteXe.onItemClickListener=
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                vehicle=adapterView.getItemAtPosition(i).toString()
        }

        viewModel.post.observe(viewLifecycleOwner, Observer {

            if(it.toString().isNotEmpty()){

                for(itemPost in it){
                    model.setpost12(itemPost)
                    idPost=itemPost.postId
                    binding.txtDestination.text=itemPost.destination
                    binding.textVehicle.text=itemPost.vehicle
                    binding.txtMoney.text=itemPost.money+" VND"
                    binding.textdate.text=itemPost.timeEnd
                    binding.textTimeStart.text=itemPost.timeStart
                    binding.txtDescrible.text=itemPost.describe
                    currentLocationStart.value?.latitude=itemPost.lat
                    currentLocationStart.value?.longitude=itemPost.lng
                    Log.d(TAG,"kiem tra currentLocationStart checkInfo: "+currentLocationStart.value?.latitude+"->"+currentLocationStart.value?.latitude)
                    viewModel.allUsers.observe(viewLifecycleOwner, Observer {
                        Log.d(TAG,"kiem tra data HomeFragent allUsers: "+it)
                        if(it.isNotEmpty() && currentLocationStart.value?.latitude!=0.0){
                            var location=createNewLocation(0.0,0.0)
                            var locationStart=createNewLocation(currentLocationStart.value!!.latitude,currentLocationStart.value!!.longitude)
                            locationStart?.longitude=currentLocationStart.value!!.longitude
                            locationStart?.latitude=currentLocationStart.value!!.latitude
//                locationStart?.latitude= currentLocationStart.value!!.latitude
//
//                locationStart?.longitude=currentLocationStart.value!!.longitude
                            for(item in it){
                                location?.latitude =item.lat
                                location?.longitude =item.lng
                                var distance=location?.distanceTo(locationStart)?.roundToInt()
                                if(distance?.toInt()!! <=200 && item.postId!=itemPost.postId){
                                   binding.txtStatus.isVisible=true
                                }
                                Log.d(TAG,"kiem tra distance "+item.postId+"->"+distance)
                                Log.d(TAG,"kiem tra location "+location?.latitude+"->"+locationStart?.latitude)



                            }
                        }
                        else {
                            Log.d(TAG, "kiem tra data HomeFragent itemLocation null: ")

                        }


                    })
                }


            }
        })

        binding.btnNearPlace.setOnClickListener {
            val fragment: Fragment = NearPlaceFragment()
            val bundle = Bundle()
            bundle.putString("idPost", idPost)
            fragment.arguments = bundle
            val transaction = fragmentManager?.beginTransaction()
            transaction?.replace(R.id.fragmentContainer,fragment)?.commit()




        }
        val firebase = Firebase.database.getReference("Users")
        firebase.child(idAuth).get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
            val user=it.getValue(UserModel::class.java)
            if(user?.permission!="2"){
                binding.btnPut.isVisible=false
            }

            if(user?.permission=="2"){
                binding.btnPut.isVisible=true
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
        binding.btnPut.setOnClickListener {
            var mess: Mess =Mess(
                binding.txtDestination.text.toString(),
//                binding.txtMoney.text.toString(),
                binding.textInputTime.text.toString(),
                idPost,
                "",
                auth,
                binding.textInputPhone.text.toString(),
                "1",
//                binding.textInputDescribe.text.toString(),
                "",
                name,
                vehicle
              )

            lifecycleScope.launchWhenStarted {
                postViewModel.updateStatusPost(idPost,"1")
                loadingDialog.startLoading()
                postViewModel.addMessage(mess).collect{
                when (it) {
                    is State.Loading -> {
                        if (it.flag == true) {
                            loadingDialog.startLoading()
                        }
                    }

                    is State.Success -> {


                        loadingDialog.stopLoading()


                    }
                    is State.Failed -> {


                    }
                }
            }
                delay(1000)
                loadingDialog.stopLoading()
                val fragment: Fragment = HomeFragment()
                val transaction = fragmentManager?.beginTransaction()
                transaction?.replace(R.id.fragmentContainer,fragment)?.commit()

            }
//            Navigation.findNavController(view)
//                .navigate(R.id.action_checkInfo_to_btnHome)
//            val fragment: Fragment = HomeFragment()
//            val transaction = fragmentManager?.beginTransaction()
//            transaction?.replace(R.id.fragmentContainer,fragment)?.commit()
        }
    }

    fun Distance(location: Location, marker: Marker){
        val distance = SphericalUtil.computeDistanceBetween(
            LatLng(
                location.latitude, location.longitude
            ), marker.position
        )
        Log.d(TAG,"kiem tra vi tri: "+distance)
        if (distance.roundToInt() > 1000) {
            val kilometers = (distance / 1000.0)
//            binding.txtKhoanCach.text = "$kilometers KM"
        } else {
//            binding.txtKhoanCach.text = "${distance.roundToLong()} Meters"

        }
    }
    fun createNewLocation(longitude: Double, latitude: Double): Location? {
        val location = Location("dummyprovider")
        location.longitude = longitude
        location.latitude = latitude
        return location
    }
}