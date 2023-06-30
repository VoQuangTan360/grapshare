package com.example.nearmekotlindemo.fragment

import android.content.ContentValues.TAG
import android.location.Location
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
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.adapter.NearPlacePostAdapter
import com.example.nearmekotlindemo.databinding.FragmentNearPlaceBinding
import com.example.nearmekotlindemo.viewModels.LocationViewModel
import com.example.nearmekotlindemo.viewModels.PostViewModel
import org.checkerframework.checker.units.qual.A
import java.lang.Math.acos
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.Math.toRadians
import kotlin.math.roundToInt


class NearPlaceFragment : Fragment() {
    private lateinit var binding: FragmentNearPlaceBinding
    private lateinit var userRecyclerView: RecyclerView
    lateinit var adapterpost: NearPlacePostAdapter
    private val locationViewModel: LocationViewModel by viewModels<LocationViewModel>()
    private lateinit var viewModel : LocationViewModel
    val currentLocationStart = MutableLiveData<Location>(Location("dummyprovider"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNearPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        if(arguments?.getString("idPost") !=null){
            viewModel.getPostWithUnversityAndIDcheck(arguments?.getString("idPost")!!)

        }
        userRecyclerView = view.findViewById(R.id.NearRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(context)
        userRecyclerView.setHasFixedSize(true)
        var distance:String="0"
        adapterpost = NearPlacePostAdapter {
            //locationViewModel .setpost(it)
            it.let {

                val fragment: Fragment = CheckInfoFragment()
                val bundle=Bundle()
                bundle.putString("idPost", it[0].postId)
                fragment.arguments=bundle
                val transaction = fragmentManager?.beginTransaction()
                transaction?.replace(R.id.fragmentContainer,fragment)?.commit()
            }
        }
        userRecyclerView.adapter = adapterpost


        val itemDistrict = listOf("200","400","500","600")
        val adapterDistrict = ArrayAdapter(requireContext() ,R.layout.list_item_ward,itemDistrict)
        val autoCompleteDistrict : AutoCompleteTextView =binding.autoSlectDistance
        autoCompleteDistrict.setAdapter(adapterDistrict)

        autoCompleteDistrict.onItemClickListener=
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                distance=adapterView.getItemAtPosition(i).toString()
                viewModel.postN.observe(viewLifecycleOwner, Observer {
                    Log.d(TAG,"kiem tra data viewModel.post: "+it)
                    val list = ArrayList<Post>()
                    if(it.toString().isNotEmpty()){
                        for(item in it){
                            currentLocationStart.value?.latitude=item.lat
                            currentLocationStart.value?.longitude=item.lng
//                    Log.d(TAG,"kiem tra currentLocationStart checkInfo: "+currentLocationStart.value?.latitude+"->"+currentLocationStart.value?.latitude)
                            viewModel.allUsers.observe(viewLifecycleOwner, Observer {
                                Log.d(TAG,"kiem tra data NearPlace allUsers: "+it)
                                if(it.isNotEmpty() && currentLocationStart.value?.latitude!=0.0){
                                    var location=createNewLocation(0.0,0.0)
                                    var locationStart=createNewLocation(currentLocationStart.value!!.latitude,currentLocationStart.value!!.longitude)
                                    locationStart?.longitude=currentLocationStart.value!!.longitude
                                    locationStart?.latitude=currentLocationStart.value!!.latitude
                                    for(item in it){
                                        location?.latitude =item.lat
                                        location?.longitude =item.lng
                                        var distance=location?.distanceTo(locationStart)?.roundToInt()
                                        if(adapterView.getItemAtPosition(i).toString()!=null){
                                            Log.d(TAG,"kiem tra adapterView.getItemAtPosition(i) "+adapterView.getItemAtPosition(i).toString())
                                            if(distance?.toInt()!! <=adapterView.getItemAtPosition(i).toString().toInt()){
                                                Log.d(TAG,"kiem tra distance "+item.postId+"->"+distance)
                                                list.add(item)
                                            }
                                        }else{
                                            if(distance?.toInt()!! <=200){
                                                list.add(item)
                                            }
                                        }
//                                        Log.d(TAG,"kiem tra distance "+item.postId+"->"+distance)
//                                        Log.d(TAG,"kiem tra location "+location?.latitude+"->"+locationStart?.latitude)



                                    }
                                    viewModel.setNearPost(list)
                                    binding.txtCountPost.text=list.count().toString()
                                    adapterpost.updateUserList(list)

                                }
                                else {
                                    Log.d(TAG, "kiem tra data HomeFragent itemLocation null: ")

                                }


                            })
                        }


                    }
                })
            }
        viewModel._post.observe(viewLifecycleOwner, Observer {
            Log.d(TAG,"kiem tra data viewModel.post: "+it)
            val list = ArrayList<Post>()
            if(it.toString().isNotEmpty()){
                for(item in it){
                    currentLocationStart.value?.latitude=item.lat
                    currentLocationStart.value?.longitude=item.lng
//                    Log.d(TAG,"kiem tra currentLocationStart checkInfo: "+currentLocationStart.value?.latitude+"->"+currentLocationStart.value?.latitude)
                    viewModel.allUsers.observe(viewLifecycleOwner, Observer {
                        Log.d(TAG,"kiem tra data NearPlace allUsers: "+it)
                        if(it.isNotEmpty() && currentLocationStart.value?.latitude!=0.0){
                            var location=createNewLocation(0.0,0.0)
                            var locationStart=createNewLocation(currentLocationStart.value!!.latitude,currentLocationStart.value!!.longitude)
                            locationStart?.longitude=currentLocationStart.value!!.longitude
                            locationStart?.latitude=currentLocationStart.value!!.latitude
                            for(item in it){
                                location?.latitude =item.lat
                                location?.longitude =item.lng
                                var distancePost=location?.distanceTo(locationStart)?.roundToInt()

                                    if(distancePost?.toInt()!! <=200){
                                        list.add(item)
                                    }

//                                        Log.d(TAG,"kiem tra distance "+item.postId+"->"+distance)
//                                        Log.d(TAG,"kiem tra location "+location?.latitude+"->"+locationStart?.latitude)



                            }
//                            viewModel.setNearPost(list)
                            binding.txtCountPost.text=list.count().toString()
                            adapterpost.updateUserList(list)

                        }
                        else {
                            Log.d(TAG, "kiem tra data HomeFragent itemLocation null: ")

                        }


                    })
                }


            }
        })
        val model1 = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)
        model1.post12.observe(viewLifecycleOwner,Observer{
            if(it.toString().isNotEmpty()){
                Log.d(TAG,"check 123 co data hay ko")
//                    locationViewModel.getNearPostWithUnversity(it.destination,it.lat,it.lng,0.01)
//                    locationViewModel.getNearPostWithUnversity("DH Su Pham Ky Thuat",it.lat,it.lng,0.001)
            }

        })

//        viewModel.nearPosr.observe(viewLifecycleOwner, Observer {
//            if(it.toString()!=""){
//            adapterpost.updateUserList(it)
//            }
//        })


    }
    fun createNewLocation(longitude: Double, latitude: Double): Location? {
        val location = Location("dummyprovider")
        location.longitude = longitude
        location.latitude = latitude
        return location
    }

}