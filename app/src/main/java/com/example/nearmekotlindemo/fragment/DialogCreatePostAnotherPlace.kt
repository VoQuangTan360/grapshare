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
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.databinding.FragmentCreatePostBinding
import com.example.nearmekotlindemo.databinding.FragmentDialogmessBinding
import com.example.nearmekotlindemo.models.googlePlaceModel.GooglePlaceModel
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.utility.State
import com.example.nearmekotlindemo.viewModels.PostViewModel
import com.google.android.material.snackbar.Snackbar


class DialogCreatePostAnotherPlace : DialogFragment() {
    lateinit var model1: PostViewModel
    val locationViewModel: PostViewModel by viewModels()
    private lateinit var binding: FragmentDialogmessBinding
//    private lateinit var loadingDialog: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDialogmessBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         model1 = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)

//        loadingDialog = LoadingDialog(requireActivity())




        binding.btnYes.setOnClickListener{
            model1.setStatusfinishPostType2("ok")
            model1.dataPostType2.observe(viewLifecycleOwner, Observer {
                Log.d(TAG,"co chay hay khong")
                model1.addPostAnotherPlace(it)
            })
            dismiss()



//            model1.dataPostType2.observe(viewLifecycleOwner, Observer {
//                lifecycleScope.launchWhenStarted {
//                locationViewModel.addPost2(it).collect{
//                    when (it) {
//                        is State.Loading -> {
//                            if (it.flag == true) {
////                                loadingDialog.startLoading()
//
//                            }
//                        }
//
//                        is State.Success -> {
//                            model1.setStatusfinishPostType2("ok")
////                            loadingDialog.stopLoading()
//                            dismiss()
//                        }
//                        is State.Failed -> {
////                            loadingDialog.stopLoading()
//                            dismiss()
//
//                        }
//                    }
//                }
//                }
//            })
        }
        binding.btnNo.setOnClickListener {
            dismiss()
        }
    }



}