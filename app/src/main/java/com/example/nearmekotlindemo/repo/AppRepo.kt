package com.example.nearmekotlindemo.repo

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.net.Uri
import android.provider.SyncStateContract
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.SavedPlaceModel
import com.example.nearmekotlindemo.UserModel
import com.example.nearmekotlindemo.constant.AppConstant
import com.example.nearmekotlindemo.models.googlePlaceModel.GooglePlaceModel
import com.example.nearmekotlindemo.models.googlePlaceModel.Mess
import com.example.nearmekotlindemo.models.googlePlaceModel.PostType2
import com.example.nearmekotlindemo.models.googlePlaceModel.Star
import com.example.nearmekotlindemo.models.googlePlaceModel.StatusID
import com.example.nearmekotlindemo.models.googlePlaceModel.ToaDo
import com.example.nearmekotlindemo.network.RetrofitClient
import com.example.nearmekotlindemo.utility.State
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.Collections

class AppRepo {

    fun login(
        email: String,
        password: String
    ): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val auth = Firebase.auth
        val data = auth.signInWithEmailAndPassword(email, password).await()
        data?.let {
            if (auth.currentUser?.isEmailVerified!!) {
                emit(State.success("Đăng nhập thành công"))
            } else {
                auth.currentUser?.sendEmailVerification()?.await()
                emit(State.failed("Hãy xác nhận email"))
            }
        }
    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(
        Dispatchers.IO
    )

    fun signUp(
        email: String,
        password: String,
        username: String,
        image: Uri
    ): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))
        val auth = Firebase.auth
        val data = auth.createUserWithEmailAndPassword(email, password).await()
        data.user?.let {
            val path = uploadImage(it.uid, image).toString()
            val userModel = UserModel(
                email, username, path,"1"
            )

            createUser(userModel, auth)

            emit(State.success("link xác nhận đã được gửi tới gmail"))

        }

    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(Dispatchers.IO)

    private suspend fun uploadImage(uid: String, image: Uri): Uri {
        val firebaseStorage = Firebase.storage
        val storageReference = firebaseStorage.reference
        val task = storageReference.child(uid + AppConstant.PROFILE_PATH)
            .putFile(image).await()

        return task.storage.downloadUrl.await()

    }

    private suspend fun createUser(userModel: UserModel, auth: FirebaseAuth) {
        val firebase = Firebase.database.getReference("Users")
        firebase.child(auth.uid!!).setValue(userModel).await()
        val profileChangeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(userModel.username)
            .setPhotoUri(Uri.parse(userModel.image))
            .build()
        auth.currentUser?.apply {
            updateProfile(profileChangeRequest).await()
            sendEmailVerification().await()
        }
    }
    fun createFollowTaiXe(postId:String,gps: ToaDo){
        val firebase = Firebase.database.getReference("FollowTaiXe")
        firebase.child(postId).child(postId).setValue(gps)
    }
    fun createStar(){

        val auth = Firebase.auth
        var gps= Star(auth.currentUser?.email)
        var gmail=auth.currentUser?.email!!.replace("@gmail.com","")
        val database = Firebase.database.getReference("Star").child(gmail)
        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val star = snapshot.getValue(Star::class.java)
                Log.d(TAG,"kiem tra Star : "+star)
                if(star==null){
                    val firebase = Firebase.database.getReference("Star")
                    firebase.child(gmail).setValue(gps)
                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
    }
    fun FollowLocationTaiXe(postId: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))
        var userPlaces :ToaDo
        var userList = MutableLiveData<ToaDo> ()

        val database =
            Firebase.database.getReference("FollowTaiXe").child(postId)

        val data = database.get().await()
        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(ToaDo::class.java)
                Log.d(TAG,"kiem tra FollowLocationTaiXe : "+post)
                try {

                    var  userPlaces : List<ToaDo> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(ToaDo::class.java)!!

                    }
                    userList.value=userPlaces[0]

                    Log.d(TAG,"kiem tra FollowLocationTaiXe : "+ userList.value)

                }catch (e : Exception){


                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })


//        if (data.exists()) {
//
//            for (ds in data.children) {
//
//                Log.d(TAG,"kiem tra FollowLocationTaiXe : "+data.value)
//
//                val placeId = ds.getValue(ToaDo::class.java) as ToaDo
//                Log.d(TAG,"kiem tra FollowLocationTaiXe1: "+placeId)
//                if (placeId != null) {
//                    userPlaces= placeId
//                    emit(State.success(userPlaces))
//
//                }
//            }
//        }
//        else{
//            emit(State.Failed("loi"))
//        }


    }.flowOn(Dispatchers.IO)
        .catch {
            if (it.message.isNullOrEmpty()) {
                emit(State.failed("No route found"))
            } else {
                emit(State.failed(it.message.toString()))
            }

        }

    fun forgetPassword(email: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))
        val auth = Firebase.auth
        auth.sendPasswordResetEmail(email).await()
        emit(State.success("Password reset email sent."))
    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(Dispatchers.IO)

    fun getPlaces(url: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))
        val response = RetrofitClient.retrofitApi.getNearByPlaces(url = url)

        Log.d("TAG", "getPlaces:  $response ")
        if (response.body()?.googlePlaceModelList?.size!! > 0) {
            Log.d(
                "TAG",
                "getPlaces:  Success called ${response.body()?.googlePlaceModelList?.size}"
            )

            emit(State.success(response.body()!!))
        } else {
            Log.d("TAG", "getPlaces:  failed called")
            emit(State.failed(response.body()!!.error!!))
        }


    }.catch {
        emit(State.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    suspend fun getUserLocationId(): ArrayList<String> {
        val userPlaces = ArrayList<String>()
        val auth = Firebase.auth
        val database =
            Firebase.database.getReference("Users").child(auth.uid!!).child("Saved Locations")

        val data = database.get().await()
        if (data.exists()) {
            for (ds in data.children) {
                val placeId = ds.getValue(String::class.java)
                userPlaces.add(placeId!!)
            }
        }

        return userPlaces
    }

    fun addUserPlace(googlePlaceModel: GooglePlaceModel, userSavedLocaitonId: ArrayList<String>) =
        flow<State<Any>> {
            emit(State.loading(true))
            val auth = Firebase.auth
            val userDatabase =
                Firebase.database.getReference("Users").child(auth.uid!!).child("Saved Locations")
            val database =
                Firebase.database.getReference("Places").child(googlePlaceModel.placeId!!).get()
                    .await()
            if (!database.exists()) {
                val savedPlaceModel = SavedPlaceModel(
                    googlePlaceModel.name!!, googlePlaceModel.vicinity!!,
                    googlePlaceModel.placeId, googlePlaceModel.userRatingsTotal!!,
                    googlePlaceModel.rating!!, googlePlaceModel.geometry?.location?.lat!!,
                    googlePlaceModel.geometry.location.lng!!
                )

                addPlace(savedPlaceModel)
            }

            userSavedLocaitonId.add(googlePlaceModel.placeId)
            userDatabase.setValue(userSavedLocaitonId).await()
            emit(State.success(googlePlaceModel))


        }.flowOn(Dispatchers.IO).catch { emit(State.failed(it.message!!)) }

    private suspend fun addPlace(savedPlaceModel: SavedPlaceModel) {
        val database = Firebase.database.getReference("Places")
        database.child(savedPlaceModel.placeId).setValue(savedPlaceModel).await()
    }
     fun  addPosttype1(post: Post)= flow<State<Any>> {
         emit(State.loading(true))
         val database = Firebase.database.getReference("Post")
//         val database = Firebase.database.getReference("PostType1")
         val auth = Firebase.auth.currentUser?.email
         var gmail=auth.toString().replace("@gmail.com","")
         gmail.let {
             post.gmail=it
             database.child("PostType1").child(post.gmail).child(post.postId).setValue(post).await()
             database.get()?.let {
                 emit(State.Success(true))
             }
         }

         //test
         val db = Firebase.firestore

//         val Post = hashMapOf(
//                "status" to post.status,
//                "postId" to post.postId,
//                "gmail" to post.gmail ,
//              "destination" to post.destination,
//              "typeDestination" to post.typeDestination,
//              "vehicle" to post.vehicle,
//              "timeStart" to post.timeStart,
//              "timeEnd" to post.timeEnd,
//              "describe" to post.describe,
//              "lat" to post.lat,
//              "lng" to post.lng,
//              "money" to post.money,
//         )
         val query = db.collection("PostWithUniversity")
         val countQuery = query.count()
         countQuery.get(AggregateSource.SERVER).addOnCompleteListener { task ->
             if (task.isSuccessful) {
                 // Count fetched successfully
                 val snapshot = task.result
                 Log.d(TAG, "Count: ${snapshot.count}")
                 var id=snapshot.count + 1
                 val Post = hashMapOf(
                     "status" to post.status,
                     "postId" to id.toString(),
                     "gmail" to post.gmail ,
                     "destination" to post.destination,
                     "typeDestination" to post.typeDestination,
                     "vehicle" to post.vehicle,
                     "timeStart" to post.timeStart,
                     "timeEnd" to post.timeEnd,
                     "describe" to post.describe,
                     "lat" to post.lat,
                     "lng" to post.lng,
                     "money" to post.money,
                 )
                 db.collection("PostWithUniversity").document(post.postId)
                     .set(Post)
                     .addOnSuccessListener {
                         Log.d(TAG, "DocumentSnapshot successfully written!") }
                     .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
             } else {
                 Log.d(TAG, "Count failed: ", task.getException())
             }
         }
//         db.collection("PostWithUniversity").document(post.postId)
//             .set(Post)
//             .addOnSuccessListener {
//                 Log.d(TAG, "DocumentSnapshot successfully written!") }
//             .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

    }
    fun  addMessage(mess: Mess)= flow<State<Any>> {
//        emit(State.loading(true))
        val auth = Firebase.auth.currentUser?.email
        var gmail=auth.toString().replace("@gmail.com","")
        mess.gmail=gmail
        val db = Firebase.firestore
//            val messItem = hashMapOf(
//                "time" to mess.time,
//                "postId" to mess.postId,
//                "gmail" to gmail ,
//                "start" to mess.start,
//                "mess" to mess.phone,
//                "end" to mess.end,
//                "status" to mess.status,
//                "id" to ""
//            )
            db.collection("Request")
                .add(mess)
                .addOnSuccessListener { documentReference ->
                    db.collection("Request").document(documentReference.id)
                        .update("id",documentReference.id)

                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

    }
    fun  addPostAnotherPlace(mess: PostType2) {
        val auth = Firebase.auth.currentUser?.email
        var gmail=auth.toString().replace("@gmail.com","")
        val db = Firebase.firestore
        val postType2 = PostType2(
            mess.status,
            mess.postId,
            gmail,
            mess.district,
            mess.ward,
            mess.typeDestination,
            mess.vehicle,
            mess.timeStart,
            mess.timeEnd,
            mess.describe,
            mess.lat,
            mess.lng,
            mess.money
        )

        db.collection("PostWithAnotherPlace")
            .add(postType2)
            .addOnSuccessListener { documentReference ->
                db.collection("PostWithAnotherPlace").document(documentReference.id)
                    .update("postId",documentReference.id)

            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

    }


    fun  addPosttype2(post: PostType2)= flow<State<Any>> {
        emit(State.loading(true))
        val database = Firebase.database.getReference("Post")
//         val database = Firebase.database.getReference("PostType1")
        val auth = Firebase.auth.currentUser?.email
        var gmail=auth.toString().replace("@gmail.com","")
        gmail.let {
            post.gmail=it
            database.child("PostType2").child(post.gmail).child(post.postId).setValue(post).await()
            database.get()?.let {
                emit(State.Success(true))
            }
        }


    }
    @Volatile private var INSTANCE : AppRepo?= null

    fun getInstance() : AppRepo{
        return INSTANCE ?: synchronized(this){

            val instance = AppRepo()
            INSTANCE = instance
            instance
        }


    }
    fun  getMyPost(userList : MutableLiveData<List<Post>>) {

        val userPlaces = ArrayList<Post>()
        val database = Firebase.database.getReference("Post").child("PostType1").child("tanvo360")
        val auth = Firebase.auth.currentUser?.email
        var gmail=auth.toString().replace("@gmail.com","")
        gmail?.let {

            database.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    try {

                        val _userList : List<Post> = snapshot.children.map { dataSnapshot ->

                            dataSnapshot.getValue(Post::class.java)!!

                        }

                        userList.postValue(_userList)

                    }catch (e : Exception){


                    }


                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }


            })


//            val data = database.get().await()
//            if (data.exists()) {
//                for (ds in data.children) {
//                    val placeId = ds.getValue(Post::class.java)
//                    userPlaces.add(placeId!!)
//                }
//            }
        }
//        return userPlaces
    }

    fun  getMyPostWithUnversity(userList : MutableLiveData<List<Post>>) {
        val mIssuePosts = ArrayList<Post>()
//        val database = Firebase.database.getReference("Post").child("PostType1").child("tanvo360")
        val auth = Firebase.auth.currentUser?.email
        var gmail=auth.toString().replace("@gmail.com","")
        val db = Firebase.firestore
        db.collection("PostWithUniversity")
        .whereEqualTo("gmail", gmail.toString())
            .get()
            .addOnSuccessListener { result ->
                if(!result.isEmpty){
                    for(data in result.documents){
                        val postItem:Post? = data.toObject(Post::class.java)
                        if(postItem!=null){
                            mIssuePosts.add(postItem)
                        }

                    }
                    userList.value=mIssuePosts
                }
                for (document in result) {

                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }

//        gmail?.let {
//
//            database.addValueEventListener(object : ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//
//                    try {
//
//                        val _userList : List<Post> = snapshot.children.map { dataSnapshot ->
//
//                            dataSnapshot.getValue(Post::class.java)!!
//
//                        }
//
//                        userList.postValue(_userList)
//
//                    }catch (e : Exception){
//
//
//                    }
//
//
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//
//
//            })
//
//
////            val data = database.get().await()
////            if (data.exists()) {
////                for (ds in data.children) {
////                    val placeId = ds.getValue(Post::class.java)
////                    userPlaces.add(placeId!!)
////                }
////            }
//        }
//        return userPlaces
    }
    fun  getAllPostWithUnversity(userList : MutableLiveData<List<Post>>) {
        val mIssuePosts = ArrayList<Post>()
        val auth = Firebase.auth.currentUser?.email
        var gmail=auth.toString().replace("@gmail.com","")
        val db = Firebase.firestore
        db.collection("PostWithUniversity")
            .whereNotEqualTo("gmail",gmail.toString())
            .get()
            .addOnSuccessListener { result ->
                if(!result.isEmpty){
                    for(data in result.documents){
                        val postItem:Post? = data.toObject(Post::class.java)
                        if(postItem!=null){
                            mIssuePosts.add(postItem)
                        }

                    }
                    userList.value=mIssuePosts
                }
                for (document in result) {

                    Log.d(TAG, "xem khoa idPost: ${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }
    fun  getPostWithUnversity(name:String,userList : MutableLiveData<List<Post>>) {
        Log.d(TAG, "xem co chay getPostWithUnversity: ")
        val mIssuePosts = ArrayList<Post>()
        val auth = Firebase.auth.currentUser?.email
        var gmail=auth.toString().replace("@gmail.com","")
        val db = Firebase.firestore
        db.collection("PostWithUniversity")
            .whereNotEqualTo("gmail",gmail.toString())
            .whereEqualTo("destination",name.toString())
            .get()
            .addOnSuccessListener { result ->
                if(!result.isEmpty){
                    Log.d(TAG, "xem co data tra ve hay khong getPostWithUnversity:")
                    for(data in result.documents){
                        val postItem:Post? = data.toObject(Post::class.java)
                        if(postItem!=null){
                            mIssuePosts.add(postItem)
                        }

                    }
                    userList.value=mIssuePosts
                }
                for (document in result) {

                    Log.d(TAG, "xem khoa idPost getPostWithUnversity: ${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }
    fun  getNearPostWithUnversity(name:String,lat:Double,lng:Double,distance:Double,userList : MutableLiveData<List<Post>>) {
        Log.d(TAG, "xem co chay getNearPostWithUnversity: ")
        val mIssuePosts = ArrayList<Post>()
        val auth = Firebase.auth.currentUser?.email
        var gmail=auth.toString().replace("@gmail.com","")

        var lat1=lat+distance
        var lng1=lng+distance
        var lat2=lat-distance
        var lng2=lng-distance
        val db = Firebase.firestore
        db.collection("PostWithUniversity")
//            .whereNotEqualTo("gmail",gmail.toString())
            .whereGreaterThanOrEqualTo("lat",lat2)
//            .whereGreaterThanOrEqualTo("lng",lng2)
            .whereLessThanOrEqualTo("lat",lat1)
//            .whereLessThanOrEqualTo("lng",lng1)
            .get()
            .addOnSuccessListener { result ->
                if(!result.isEmpty){

                    for(data in result.documents){
                        Log.d(TAG, "xem co data tra ve cua getNearPostWithUnversity:" +data)
                        val postItem:Post? = data.toObject(Post::class.java)
                        if(postItem!=null  && postItem.gmail != gmail
//                            && postItem.lng>=lng2
//                            && postItem.lng<=lng1
                            && postItem.destination.equals(name)){
                            Log.d(TAG, "xem co data hop le ko" +data)
                            mIssuePosts.add(postItem)
                        }

                    }
                    userList.value=mIssuePosts
                }
                for (document in result) {

                    Log.d(TAG, "xem co data tra ve cua getNearPostWithUnversity: ${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }
    fun updateStatusPostWithID(id:String,status:String){
        val db = Firebase.firestore
        db.collection("PostWithUniversity")
        .whereEqualTo("postId", id)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "xem co update postid khong"+id)
                    db.collection("PostWithUniversity").document(document.id)
                        .update("status",status)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun updateStatusRequestWithID(id:String,status:String){
        val db = Firebase.firestore
        db.collection("Request").document(id)
            .update("status",status)

    }

    fun updateRequestWithID(id:String,status:String){
        val db = Firebase.firestore
        db.collection("Request")
            .whereEqualTo("postId", id)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("PostWithUniversity").document(document.id)
                        .update("status",status)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

    }
    fun  getRequestWithID(statusID: StatusID,userList : MutableLiveData<List<Mess>>) {
        val mIssuePosts = ArrayList<Mess>()
        val db = Firebase.firestore
        if(statusID.status=="1"){
            db.collection("Request")
                .whereEqualTo("postId",statusID.id)
                .whereEqualTo("status","1")
                .get()
                .addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for(data in result.documents){

                            val postItem:Mess? = data.toObject(Mess::class.java)
                            Log.d(TAG,"kiem tra data getRequestWithID: "+postItem)
                            if(postItem!=null){
                                mIssuePosts.add(postItem)
                            }

                        }
                        userList.value=mIssuePosts
                    }
                    for (document in result) {

                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }
        }else{
            db.collection("Request")
                .whereEqualTo("postId",statusID.id)
                .whereEqualTo("status","2")
                .get()
                .addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for(data in result.documents){

                            val postItem:Mess? = data.toObject(Mess::class.java)
                            Log.d(TAG,"kiem tra data getRequestWithID: "+postItem)
                            if(postItem!=null){
                                mIssuePosts.add(postItem)
                            }

                        }
                        userList.value=mIssuePosts
                    }
                    for (document in result) {

                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }

        }
    }
    fun  getRequestStar(gmail: String,userList : MutableLiveData<Star>) {


        val databasUser = Firebase.database.getReference("Star")
            databasUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG,"kiem tra onDataChange Star  : "+snapshot.value)
                try {

                    var  userPlaces : List<Star> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(Star::class.java)!!

                    }
                    for(i in userPlaces){
                        if(i.gmail==gmail+"@gmail.com"){
                            userList.value=i
                        }
                    }
                }catch (e : Exception){
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })

    }
    @SuppressLint("SuspiciousIndentation")
    fun  getMyRequestWithID(userList : MutableLiveData<List<Mess>>) {
        val mIssuePosts = ArrayList<Mess>()
        val auth = Firebase.auth.currentUser?.email
        var gmail=auth.toString().replace("@gmail.com","")
        val db = Firebase.firestore
            db.collection("Request")
//                .whereEqualTo("gmail",gmail)
                .orderBy("status")
                .get()
                .addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for(data in result.documents){
                            val postItem:Mess? = data.toObject(Mess::class.java)
                            Log.d(TAG,"kiem tra data getMyRequestWithID: "+postItem)
                            if(postItem!=null && postItem.gmail==gmail){
                                mIssuePosts.add(postItem)
                            }

                        }
                        userList.value=mIssuePosts
                    }
                    for (document in result) {

                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }

    }

    fun  getPostWithUnversityAndID(id:String,userList : MutableLiveData<List<Post>>) {
        val mIssuePosts = ArrayList<Post>()

        val db = Firebase.firestore
        Log.d(TAG, "kiem tra gia tri cua idpost trong van post: " +id)
        db.collection("PostWithUniversity")
            .whereEqualTo("postId", id)

            .get()
            .addOnSuccessListener { result ->
                if(!result.isEmpty){
                    for(data in result.documents){
                        Log.d(TAG, "kiem tra truy van post voi id:" +data.toString())
                        val postItem:Post? = data.toObject(Post::class.java)
                        if(postItem!=null){
                            mIssuePosts.add(postItem)
                        }

                    }
                    userList.value=mIssuePosts
                }
                for (document in result) {

                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    fun removePlace(userSavedLocationId: ArrayList<String>) = flow<State<Any>> {
        emit(State.loading(true))
        val auth = Firebase.auth
        val database =
            Firebase.database.getReference("Users").child(auth.uid!!).child("Saved Locations")

        database.setValue(userSavedLocationId).await()
        emit(State.success("Remove Successfully"))
    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(Dispatchers.IO)

    fun getDirection(url: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val response = RetrofitClient.retrofitApi.getDirection(url)

        if (response.body()?.directionRouteModels?.size!! > 0) {
            emit(State.success(response.body()!!))
        } else {
            emit(State.failed(response.body()?.error!!))
        }
    }.flowOn(Dispatchers.IO)
        .catch {
            if (it.message.isNullOrEmpty()) {
                emit(State.failed("No route found"))
            } else {
                emit(State.failed(it.message.toString()))
            }

        }

    fun getUserLocations() = callbackFlow<State<Any>> {

        trySendBlocking(State.loading(true))

        val database: DatabaseReference?
        val placesList: ArrayList<SavedPlaceModel> = ArrayList()

        try {

            val auth = Firebase.auth
            val reference = Firebase.database.getReference("Places")
            database =
                Firebase.database.getReference("Users").child(auth.uid!!).child("Saved Locations")

            val eventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.children.forEach { ds ->
                            reference.child(ds.getValue(String::class.java)!!).get()
                                .addOnSuccessListener {
                                    placesList.add(it.getValue(SavedPlaceModel::class.java)!!)
                                }


                        }

                        trySendBlocking(State.success(placesList))
                    } else {
                        Log.d("TAG", "onDataChange: no data found")
                        trySendBlocking(State.failed("No data found"))
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            }

            database.addValueEventListener(eventListener)

            awaitClose {
                Log.d("TAG", "getUserLocations: await close ")
                database.removeEventListener(eventListener)
            }


        } catch (e: Throwable) {
            e.printStackTrace()
            close(e)
        }
    }

    fun updateName(name: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val auth = Firebase.auth
        val database = Firebase.database.getReference("Users").child(auth.uid!!)
        val profileChangeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        auth.currentUser?.updateProfile(profileChangeRequest)?.await()
        val map: MutableMap<String, Any> = HashMap()

        map["username"] = name
        database.updateChildren(map)
        emit(State.success("Username updated"))

    }.flowOn(Dispatchers.IO)
        .catch {
            emit(State.failed(it.message!!.toString()))
        }

    fun updateImage(image: Uri): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val auth = Firebase.auth
        val path = uploadImage(auth.uid!!, image).toString()
        val database = Firebase.database.getReference("Users").child(auth.uid!!)
        val profileChangeRequest = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(path))
            .build()

        auth.currentUser?.updateProfile(profileChangeRequest)?.await()

        val map: MutableMap<String, Any> = HashMap()

        map["image"] = path
        database.updateChildren(map)
        emit(State.success("Image updated"))

    }.flowOn(Dispatchers.IO)
        .catch {
            emit(State.failed(it.message!!.toString()))
        }


    fun confirmEmail(authCredential: AuthCredential): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val auth = Firebase.auth
        auth.currentUser?.reauthenticate(authCredential)?.await()
        emit(State.success("User authenticate"))
    }.flowOn(Dispatchers.IO)
        .catch {
            emit(State.failed(it.message!!.toString()))
        }

    fun updateEmail(email: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val auth = Firebase.auth
        val database = Firebase.database.getReference("Users").child(auth.uid!!)
        auth.currentUser?.updateEmail(email)?.await()
        val map: MutableMap<String, Any> = HashMap()
        map["email"] = email
        database.updateChildren(map).await()

        emit(State.success("Email updated"))
    }.flowOn(Dispatchers.IO)
        .catch {
            emit(State.failed(it.message!!.toString()))
        }

    fun updatePassword(password: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val auth = Firebase.auth
        auth.currentUser?.updatePassword(password)?.await()


        emit(State.success("Email updated"))
    }.flowOn(Dispatchers.IO)
        .catch {
            emit(State.failed(it.message!!.toString()))
        }

}