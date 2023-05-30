package com.example.nearmekotlindemo.fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.adapter.NearPlacePostAdapter
import com.example.nearmekotlindemo.databinding.FragmentNearPlaceBinding
import com.example.nearmekotlindemo.viewModels.LocationViewModel
import com.example.nearmekotlindemo.viewModels.PostViewModel
import java.lang.Math.acos
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.Math.toRadians


class NearPlaceFragment : Fragment() {
    private lateinit var binding: FragmentNearPlaceBinding
    private lateinit var userRecyclerView: RecyclerView
    lateinit var adapterpost: NearPlacePostAdapter
    private val locationViewModel: LocationViewModel by viewModels<LocationViewModel>()
    private lateinit var viewModel : LocationViewModel


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
        userRecyclerView = view.findViewById(R.id.NearRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(context)
        userRecyclerView.setHasFixedSize(true)
        adapterpost = NearPlacePostAdapter {
            locationViewModel .setpost(it)
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


        val itemDistrict = listOf("10m","20m","50m","100m")
        val adapterDistrict = ArrayAdapter(requireContext() ,R.layout.list_item_ward,itemDistrict)
        val autoCompleteDistrict : AutoCompleteTextView =binding.autoSlectDistance
        autoCompleteDistrict.setAdapter(adapterDistrict)

        autoCompleteDistrict.onItemClickListener=
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

//                post.value?.district=adapterView.getItemAtPosition(i).toString()
            }
        val model1 = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)
        model1.post12.observe(viewLifecycleOwner,Observer{
            if(it.toString().isNotEmpty()){
                Log.d(TAG,"check 123 co data hay ko")


                    locationViewModel.getNearPostWithUnversity(it.destination,it.lat,it.lng,0.001)
//                    locationViewModel.getNearPostWithUnversity("DH Su Pham Ky Thuat",it.lat,it.lng,0.001)


            }

        })

        viewModel.nearPosr.observe(viewLifecycleOwner, Observer {
            if(it.toString()!=""){
            adapterpost.updateUserList(it)
            }
        })


    }


}