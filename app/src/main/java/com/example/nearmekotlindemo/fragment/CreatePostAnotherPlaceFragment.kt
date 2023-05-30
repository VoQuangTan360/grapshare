package com.example.nearmekotlindemo.fragment

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Location
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.databinding.FragmentCreatePostAnotherPlaceBinding
import com.example.nearmekotlindemo.databinding.FragmentCreatePostBinding
import com.example.nearmekotlindemo.models.googlePlaceModel.PostType2
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.viewModels.PostViewModel
import com.google.android.gms.location.LocationServices
import java.util.Calendar


class CreatePostAnotherPlaceFragment : Fragment() {
    lateinit var model: PostViewModel
    private val locationViewModel: PostViewModel by viewModels<PostViewModel>()
    private lateinit var currentLocation: Location
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var binding: FragmentCreatePostAnotherPlaceBinding
    var post = MutableLiveData<PostType2>(
        PostType2("1","2","tanvo360","Su Pham Ky Thuat","","2","car",
        "8:30","9:00","di le dai hanh",12.000,13.000,"")
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreatePostAnotherPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCurrentLocation()

        model = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        model.setStatusPickDialog("")

        val itemDistrict = listOf("Cẩm Lệ","Hải Châu","Liên Chiểu","Ngũ Hành Sơn")
        val adapterDistrict = ArrayAdapter(requireContext() ,R.layout.list_item_ward,itemDistrict)
        val autoCompleteDistrict : AutoCompleteTextView =binding.autoSlectDistrict
        autoCompleteDistrict.setAdapter(adapterDistrict)

        autoCompleteDistrict.onItemClickListener=
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                post.value?.district=adapterView.getItemAtPosition(i).toString()
            }

        val itemWard = listOf("Hòa An","Hòa Thọ Đông","Hòa Xuân","Hòa Phát")
        val adapterWard = ArrayAdapter(requireContext() ,R.layout.list_item_ward,itemWard)
        val autoCompleteWard : AutoCompleteTextView =binding.autoSlectWard
        autoCompleteWard.setAdapter(adapterWard)

        autoCompleteWard.onItemClickListener=
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                post.value?.ward=adapterView.getItemAtPosition(i).toString()
            }

        val itemXe = listOf("Car","Motorcycle","Bicycle")
        val adapterXe = ArrayAdapter(requireContext() ,R.layout.list_item_xe,itemXe)
        val autoCompleteXe : AutoCompleteTextView =binding.autoSlectTranaport
        autoCompleteXe.setAdapter(adapterXe)

        autoCompleteXe.onItemClickListener=
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

            post.value?.vehicle=adapterView.getItemAtPosition(i).toString()
        }

        binding.btnStart.setOnClickListener {
            val datePickerFragment = DatePickerFragment()
            val supportFragmentManager = requireActivity().supportFragmentManager

            // we have to implement setFragmentResultListener
            supportFragmentManager.setFragmentResultListener(
                "REQUEST_KEY",
                viewLifecycleOwner
            ) { resultKey, bundle ->
                if (resultKey == "REQUEST_KEY") {
                    val date = bundle.getString("SELECTED_DATE")
                    binding.textTimeStart.text=date.toString()
                    post.value?.timeStart=date.toString()
                }
            }
            // show
            datePickerFragment.show(supportFragmentManager, "DatePickerFragment")
        }
//        binding.btnEnd.setOnClickListener {
//            val datePickerFragment = DatePickerFragment()
//            val supportFragmentManager = requireActivity().supportFragmentManager
//
//            // we have to implement setFragmentResultListener
//            supportFragmentManager.setFragmentResultListener(
//                "REQUEST_KEY",
//                viewLifecycleOwner
//            ) { resultKey, bundle ->
//                if (resultKey == "REQUEST_KEY") {
//                    val date = bundle.getString("SELECTED_DATE")
//                    binding.textTimeEnd.text=date.toString()
//                    post.value?.timeEnd=date.toString()
//                }
//            }
//            // show
//            datePickerFragment.show(supportFragmentManager, "DatePickerFragment")
//        }

        binding.btnNext.setOnClickListener {
            post.value?.money=binding.EDTPayMoney.text.toString()
            post.value?.describe=binding.textdescribe.text.toString()
            post.value?.postId= Calendar.getInstance().time.toString().replace(" ","")
            post.value?.let { it1 -> model.setDataPostType2(it1) }
            val  showDialog =DialogCreatePostAnotherPlace()
            showDialog.show((activity as AppCompatActivity).supportFragmentManager,"thong bao")
        }
        model.statusfinishPostType2.observe(viewLifecycleOwner, Observer {
            if(it.length==2){
                Log.d(TAG,"co chay o day")
                Navigation.findNavController(view)
                    .navigate(R.id.action_createPostAnotherPlaceFragment_to_btnSavedPlaces)
            }
        })

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

            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {

            currentLocation = it
            var a=currentLocation.latitude
            var b=currentLocation.longitude

            post.value?.lat=a
            post.value?.lng=b

            Log.d(ContentValues.TAG,"thongtin: "+post.value?.lat+"////"+ post.value?.lng+"...."+currentLocation)
//            infoWindowAdapter = null
//            infoWindowAdapter = InfoWindowAdapter(currentLocation, requireContext())
//            mGoogleMap?.setInfoWindowAdapter(infoWindowAdapter)
//            moveCameraToLocation(currentLocation)
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "$it", Toast.LENGTH_SHORT).show()
        }
    }
}