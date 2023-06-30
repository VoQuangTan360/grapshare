package com.example.nearmekotlindemo.fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.databinding.FragmentCreatePostBinding
import com.example.nearmekotlindemo.databinding.FragmentDialogChooseTypeCreatepostBinding
import com.example.nearmekotlindemo.databinding.FragmentDialogFeedbackBinding
import com.example.nearmekotlindemo.databinding.FragmentDialogHuyBinding
import com.example.nearmekotlindemo.databinding.FragmentDialogmessBinding
import com.example.nearmekotlindemo.models.googlePlaceModel.GooglePlaceModel
import com.example.nearmekotlindemo.models.googlePlaceModel.Mess
import com.example.nearmekotlindemo.models.googlePlaceModel.StatusID
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.utility.State
import com.example.nearmekotlindemo.viewModels.PostViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class DialogHuy(mess:String) : DialogFragment() {
    lateinit var model: PostViewModel
    private val postViewModel: PostViewModel by viewModels()
    val l=mess

     lateinit var binding: com.example.nearmekotlindemo.databinding.FragmentDialogHuyBinding
//    var data = MutableLiveData<Post>()

    private lateinit var loadingDialog: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDialogHuyBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val model1 = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        binding.txtMessNotification.text=l
        model1.postMess.observe(viewLifecycleOwner,Observer{
            model1.getRequseStar(it.gmail)

//            Glide.with(requireContext()).load(it.image)
//                .into(binding.imgProfileRequest)

//           binding.textMess.text=it.
            Log.d(TAG,"check StatusID(it.id,it.idPost): "+it)
            model1.setidRequest(StatusID(it.id,it.postId))

        })
        model1.star.observe(viewLifecycleOwner, Observer {
//            binding.txtPlaceDRating.text=it.poit.toString()
//            binding.txtPlaceDRatingCount.text="("+it.vote+")"
        })




        binding.btnYes.setOnClickListener{
            val firebase = Firebase.database.getReference("CheckFollow")
            model1.idRequest.observe(viewLifecycleOwner,Observer{
                postViewModel.updateStatusPost(it.status,"1")
                firebase.child(it.status).setValue(StatusID(it.status,"0"))
                postViewModel.updateStatusRequest(it.id,"0")
            })


//            model1.idPost.observe(viewLifecycleOwner,Observer{
//                postViewModel.getRequseMess(StatusID(it.id,"2") )
//            })
            dismiss()
        }
        binding.btnNo.setOnClickListener {
            dismiss()
        }
    }



}