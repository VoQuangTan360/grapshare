package com.example.nearmekotlindemo.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.databinding.FragmentCreatePostBinding
import com.example.nearmekotlindemo.models.googlePlaceModel.StatusID
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.utility.State
import com.example.nearmekotlindemo.viewModels.PostViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import java.util.Calendar


class UpdatePostFragment : Fragment() {
    private val locationViewModel: PostViewModel by viewModels<PostViewModel>()
    private lateinit var currentLocation: Location
    private lateinit var loadingDialog: LoadingDialog
    lateinit var model: PostViewModel
    var post = MutableLiveData<Post>(Post("0","2","tanvo360","Su Pham Ky Thuat","1","car",
     "8:30","9:00","di le dai hanh",12.000,13.000,""))
//    val post= Post("1","2","tanvo360","Su Pham Ky Thuat","car",
//        "8:30","9:00","di le dai hanh",12.000,13.000)
    private lateinit var binding: FragmentCreatePostBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }
    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity())
        model = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        val mIssuePosts = ArrayList<Post>()
        var idpost :String=""
        if(arguments?.getString("idPost") !=null){
            model.setstatustChooseChane(StatusID(arguments?.getString("idPost")!!,"0"))
            idpost=arguments?.getString("idPost")!!
        }

        val db = Firebase.firestore
        Log.d(TAG, "kiem tra gia tri cua idpost trong van post: " +id)
        db.collection("PostWithUniversity")
            .whereEqualTo("postId", idpost)
            .get()
            .addOnSuccessListener { result ->
                if(!result.isEmpty){
                    for(data in result.documents){
                        Log.d(TAG, "kiem tra truy van post voi id:" +data.toString())
                        val postItem:Post? = data.toObject(Post::class.java)
                        if(postItem!=null){
                            mIssuePosts.add(postItem)
                            binding.textTimeStart.text=postItem.timeStart
                            binding.EDTPayMoney.hint=postItem.money
                            binding.textdescribe.hint=postItem.describe
                        }

                    }

                }
                for (document in result) {

                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }



        val item = listOf("DH Su Pham Ky Thuat","DH Duy Tan (CS1)","DH Kinh Te")

        val adapter = ArrayAdapter(requireContext() ,R.layout.list_item_school,item)
        val autoComplete : AutoCompleteTextView=binding.autoSlect
        autoComplete.setAdapter(adapter)


        autoComplete.onItemClickListener=AdapterView.OnItemClickListener { adapterView, view, i, l ->

            post.value?.destination=adapterView.getItemAtPosition(i).toString()
        }


        val itemXe = listOf("Ô tô","Xe máy","Xe đạp")
        val adapterXe = ArrayAdapter(requireContext() ,R.layout.list_item_xe,itemXe)
        val autoCompleteXe : AutoCompleteTextView=binding.autoSlectTranaport
        autoCompleteXe.setAdapter(adapterXe)

        autoCompleteXe.onItemClickListener=AdapterView.OnItemClickListener { adapterView, view, i, l ->

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
        binding.btnChooseOneDay.setOnClickListener {
            post.value?.timeEnd="To day"
            binding.btnChooseOneDay
                .setBackgroundColor(R.color.accentColor)

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
        }
        binding.btnNext.setOnClickListener {
            post.value?.money=binding.EDTPayMoney.text.toString()
            post.value?.describe=binding.textdescribe.text.toString()
            post.value?.postId= Calendar.getInstance().time.toString().replace(" ","")
            Log.d(TAG,"post goc ${post.value}")
            post.value?.let { it1 -> locationViewModel.setData(it1) }

//            if(post.value?.lat!= 12.000 && post.value?.lng!=13.000){
                lifecycleScope.launchWhenStarted {
                    getCurrentLocation()
                    loadingDialog.startLoading()
                    delay(2000)
                    val db = Firebase.firestore
                    db.collection("PostWithUniversity")
                        .whereEqualTo("postId", idpost)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                Log.d(TAG, "xem co update postid khong"+idpost)
                                db.collection("PostWithUniversity")
                                    .add(post.value as Post )
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting documents: ", exception)
                        }
                    loadingDialog.stopLoading()
                    val  showDialog =dialogmess()
                    showDialog.show((activity as AppCompatActivity).supportFragmentManager,"thong bao")
                }

//

//            }


        }
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

            Log.d(TAG,"thongtin: "+post.value?.lat+"////"+ post.value?.lng+"...."+currentLocation)
//            infoWindowAdapter = null
//            infoWindowAdapter = InfoWindowAdapter(currentLocation, requireContext())
//            mGoogleMap?.setInfoWindowAdapter(infoWindowAdapter)
//            moveCameraToLocation(currentLocation)
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "$it", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCustomDiaLogBox(message: String?){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnYes: Button =dialog.findViewById(R.id.btnYes)
        val btnNo: Button =dialog.findViewById(R.id.btnNo)

        btnYes.setOnClickListener {  }
        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

    }
}