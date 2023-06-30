package com.example.nearmekotlindemo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.adapter.MyRequestAdapter
import com.example.nearmekotlindemo.adapter.NearPlacePostAdapter
import com.example.nearmekotlindemo.adapter.RequestMyPostAdapter
import com.example.nearmekotlindemo.databinding.FragmentInfoMyPostBinding
import com.example.nearmekotlindemo.databinding.FragmentMyPostINeedBinding
import com.example.nearmekotlindemo.models.googlePlaceModel.Mess
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.viewModels.LocationViewModel
import com.example.nearmekotlindemo.viewModels.PostViewModel


class MyRequestFragment : Fragment() {

    private lateinit var loadingDialog: LoadingDialog
    private lateinit var binding: FragmentMyPostINeedBinding
    private val postViewModel: PostViewModel by viewModels()
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var viewModel : PostViewModel
    lateinit var adapterpost: MyRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyPostINeedBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PostViewModel::class.java)
        viewModel.getMyRequseMess()
        viewModel.request.observe(viewLifecycleOwner, Observer {
            adapterpost.updateUserList(it)
        })
        userRecyclerView = view.findViewById(R.id.recyclerViewMR)
        userRecyclerView.layoutManager = LinearLayoutManager(context)
        userRecyclerView.setHasFixedSize(true)
        adapterpost = MyRequestAdapter {
//            val fragment: Fragment = FollowTaiXeFragment()
//            val transaction = fragmentManager?.beginTransaction()
//            transaction?.replace(R.id.fragmentContainer,fragment)?.commit()
            if(it.status=="2") {
                val fragment: Fragment = ShareLocationFragment()
                val bundle = Bundle()
                bundle.putString("idPost", it.postId)
                fragment.arguments = bundle
                val transaction = fragmentManager?.beginTransaction()
                transaction?.replace(R.id.fragmentContainer, fragment)?.commit()
            }
        }
//            locationViewModel .setpost(it)
//            it.let {
//
//                val fragment: Fragment = CheckInfoFragment()
//                val bundle=Bundle()
//                bundle.putString("idPost", it[0].postId)
//                fragment.arguments=bundle
//                val transaction = fragmentManager?.beginTransaction()
//                transaction?.replace(R.id.fragmentContainer,fragment)?.commit()
//            }

        userRecyclerView.adapter = adapterpost

    }
}