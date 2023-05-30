package com.example.nearmekotlindemo.fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.databinding.FragmentCheckInfoBinding
import com.example.nearmekotlindemo.databinding.FragmentCreatePostAnotherPlaceBinding
import com.example.nearmekotlindemo.models.googlePlaceModel.Mess
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.utility.State
import com.example.nearmekotlindemo.viewModels.LocationViewModel
import com.example.nearmekotlindemo.viewModels.PostViewModel
import kotlinx.coroutines.delay


class CheckInfoFragment : Fragment() {

    private val locationViewModel: LocationViewModel by viewModels<LocationViewModel>()
    private val postViewModel: PostViewModel by viewModels()
    private lateinit var binding: FragmentCheckInfoBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var viewModel : LocationViewModel
    lateinit var model: LocationViewModel
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
        model = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)

        if(arguments?.getString("idPost") !=null){
            viewModel.getPostWithUnversityAndIDcheck(arguments?.getString("idPost")!!)

        }
//        locationViewModel.post1.observe(viewLifecycleOwner,Observer{
//            Log.d(TAG,"kiem tra idPost:"+it)
//            viewModel.getPostWithUnversityAndIDcheck(it)
//        })
        viewModel.post.observe(viewLifecycleOwner, Observer {

            if(it.toString().isNotEmpty()){

                for(item in it){
                    model.setpost12(item)
                    idPost=item.postId
                    binding.txtDestination.text=item.destination
                    binding.textVehicle.text=item.vehicle
                    binding.txtMoney.text=item.money+" VND"
                    binding.textdate.text=item.timeEnd
                    binding.textTimeStart.text=item.timeStart
                    binding.txtDescrible.text=item.describe
                }


            }
        })
        binding.btnNearPlace.setOnClickListener {
            val fragment: Fragment = NearPlaceFragment()
            val transaction = fragmentManager?.beginTransaction()
            transaction?.replace(R.id.fragmentContainer,fragment)?.commit()
        }
        binding.btnPut.setOnClickListener {
            var mess: Mess =Mess(
                binding.textInputdestination.text.toString(),
                binding.textInputTime.text.toString(),
                idPost,
                "",
                binding.textInputPhone.text.toString(),
                binding.textInputDescribe.text.toString(),
                "1"
              )
            lifecycleScope.launchWhenStarted {
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
}