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
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.databinding.FragmentCreatePostBinding
import com.example.nearmekotlindemo.databinding.FragmentDialogChooseTypeCreatepostBinding
import com.example.nearmekotlindemo.databinding.FragmentDialogmessBinding
import com.example.nearmekotlindemo.models.googlePlaceModel.GooglePlaceModel
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.utility.State
import com.example.nearmekotlindemo.viewModels.PostViewModel
import com.google.android.material.snackbar.Snackbar


class DialogChooseTypeCreatePost : DialogFragment() {
    lateinit var model: PostViewModel
    val locationViewModel: PostViewModel by viewModels()
     lateinit var binding: com.example.nearmekotlindemo.databinding.FragmentDialogChooseTypeCreatepostBinding
//    var data = MutableLiveData<Post>()

    private lateinit var loadingDialog: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDialogChooseTypeCreatepostBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        model.setStatusfinish("")
        binding.btnYes.setOnClickListener{
            model.setStatusPickDialog("1")
            dismiss()
        }
        binding.btnNo.setOnClickListener {
            model.setStatusPickDialog("11")
            dismiss()
        }
    }



}