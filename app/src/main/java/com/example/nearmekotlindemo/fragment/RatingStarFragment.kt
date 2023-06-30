package com.example.nearmekotlindemo.fragment

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.databinding.FragmentDialogmessBinding
import com.example.nearmekotlindemo.databinding.FragmentRatingStarBinding
import com.example.nearmekotlindemo.models.googlePlaceModel.Star
import com.example.nearmekotlindemo.models.googlePlaceModel.ToaDo
import com.example.nearmekotlindemo.viewModels.LocationViewModel
import com.example.nearmekotlindemo.viewModels.PostViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class RatingStarFragment(gmail:String,post:String) : DialogFragment() {
    // TODO: Rename and change types of parameters
    private lateinit var viewModel : LocationViewModel
    private lateinit var postViewModel : PostViewModel
    private lateinit var binding: FragmentRatingStarBinding


    var gmailRating=MutableLiveData<String>(gmail)
    var postID=MutableLiveData<String>(post)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRatingStarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var ratingNumber = MutableLiveData<String>("0")

        viewModel = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)
        postViewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        binding.ratingVote.setOnRatingBarChangeListener { ratingBar, fl, b ->
            Log.i("firebase", "Got value ratingBar: ${ratingBar} ---fl: $fl ---b: $b")
            ratingNumber.value=fl.toString()
        }

        viewModel.rating.observe(viewLifecycleOwner, Observer {
            gmailRating.value=it
            Log.i("firebase", "get rating: $it")

        })
        binding.btnNo.setOnClickListener {
            dismiss()
        }
        binding.btnYes.setOnClickListener {


            val firebase = Firebase.database.getReference("Star")
//                .child(gmailRating.value!!)
                .get()
                .addOnSuccessListener {

                    postID.value?.let { it1 -> postViewModel.updateStatusPost(it1,"3") }
                    Log.i("firebase", "Got value ${it.value}")
                    var  userPlaces : List<Star> = it.children.map { dataSnapshot ->

                        dataSnapshot.getValue(Star::class.java)!!


                    }
                    if (userPlaces.isNotEmpty()){
                        for(i in userPlaces){
                            if(i.gmail==gmailRating.value!!){
                                var poit= i.poit
                                var vote= i.vote.toString().toDouble()
                                var a=ratingNumber.value!!.toDouble()


                                var sum = (poit!! * vote!!) +a
                                var avg =sum/(vote+1)*1.0
                                var firerating = Firebase.database.getReference("Star").child(gmailRating.value!!)
                                    .setValue(Star(gmailRating.value,avg ,vote.toInt()+1))
                            }
                        }
                        Log.i("check star", "check star: ${userPlaces}")


                    }
                }.addOnFailureListener{
                    Log.e("firebase", "Error getting data", it)
                }


            dismiss()
        }
    }
}