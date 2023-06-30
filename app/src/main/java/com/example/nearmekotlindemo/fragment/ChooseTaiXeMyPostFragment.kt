package com.example.nearmekotlindemo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.adapter.RequestMyPostAdapter
import com.example.nearmekotlindemo.databinding.FragmentInfoMyPostBinding
import com.example.nearmekotlindemo.models.googlePlaceModel.Mess
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.viewModels.PostViewModel


class ChooseTaiXeMyPostFragment : Fragment() {
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var binding: FragmentInfoMyPostBinding
    private val postViewModel: PostViewModel by viewModels()
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var viewModel : PostViewModel
    lateinit var adapterpost: RequestMyPostAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val model1 = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        model1.setpostMess(Mess())
        binding = FragmentInfoMyPostBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val iddata = MutableLiveData<String>()

        val model1 = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        model1.idPost.observe(viewLifecycleOwner,Observer{
            postViewModel.getRequseMess(it)

        })

        model1.postMess.observe(viewLifecycleOwner,Observer{
            if(it.status=="2"){
                val fragment: Fragment = FollowTaiXeFragment()
                val bundle = Bundle()
                bundle.putString("idPost", it.postId)
                bundle.putString("gmail", it.gmail)
                fragment.arguments = bundle
                val transaction = fragmentManager?.beginTransaction()
                transaction?.replace(R.id.fragmentContainer,fragment)?.commit()
            }else
            if(it.postId.length!=0) {
                val showDialog = DialogFeeback()
                showDialog.show((activity as AppCompatActivity).supportFragmentManager, "Yêu cầu")
            }
        })

        viewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        viewModel.mess.observe(viewLifecycleOwner, Observer {
            adapterpost.updateUserList(it)

        })
        userRecyclerView = view.findViewById(R.id.recyclerViewR)
        userRecyclerView.layoutManager = LinearLayoutManager(context)
        userRecyclerView.setHasFixedSize(true)
        adapterpost = RequestMyPostAdapter { model1.setpostMess(it) }
        userRecyclerView.adapter = adapterpost


    }

}