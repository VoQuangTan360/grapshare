package com.example.nearmekotlindemo.viewModels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.models.googlePlaceModel.Mess
import com.example.nearmekotlindemo.models.googlePlaceModel.PostType2
import com.example.nearmekotlindemo.models.googlePlaceModel.StatusID
import com.example.nearmekotlindemo.repo.AppRepo
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext


class PostViewModel:ViewModel() {

    val data = MutableLiveData<Post>()
    val _data :LiveData<Post> =data

    val statusPickDialog = MutableLiveData<String>()
    fun setStatusPickDialog(string1: String){
        statusPickDialog.value=string1
    }
    val idPost = MutableLiveData<StatusID>()
    fun setidPost(string1: StatusID){
        idPost.value=string1
    }
    val statusPost = MutableLiveData<String>()
    fun setstatusPost(string1: String){
        statusPost.value=string1
    }
    val postMess = MutableLiveData<Mess>()
    fun setpostMess(string1: Mess){
        postMess.value=string1
    }
    val idRequest = MutableLiveData<StatusID>()
    fun setidRequest(string1: StatusID){
        idRequest.value=string1
    }
    val statusfinish = MutableLiveData<String>()
    fun setStatusfinish(string1: String){
        statusfinish.value=string1
    }
    val statusfinishPostType2 = MutableLiveData<String>()
    fun setStatusfinishPostType2(string1: String){
        statusfinishPostType2.value=string1
    }
    val dataPostType2 = MutableLiveData<PostType2>()
    fun setDataPostType2(string1: PostType2){
        dataPostType2.value=string1
    }

    private val repo = AppRepo()
    fun addPost(post: Post)=repo.addPosttype1(post)
    fun addMessage(mess: Mess)=repo.addMessage(mess)
    fun addPost2(post: PostType2)=repo.addPosttype2(post)
    private val repository : AppRepo

    private val _allUsers = MutableLiveData<List<Post>>()
    val allUsers : LiveData<List<Post>> = _allUsers

    private val _mess = MutableLiveData<List<Mess>>()
    val mess : LiveData<List<Mess>> = _mess

    private val _request = MutableLiveData<List<Mess>>()
    val request : LiveData<List<Mess>> = _request
    fun getRequseMess(statusID: StatusID){
        repository.getRequestWithID(statusID,_mess)

    }
    fun getMyRequseMess(){
        repository.getMyRequestWithID(_request)

    }
    fun updateStatusPost(id:String,status:String){
        repository.updateStatusPostWithID(id,status)
    }
    fun updateStatusRequest(id:String,status:String){
        repository.updateStatusRequestWithID(id,status)
    }
    fun addPostAnotherPlace(status:PostType2){
        repository.addPostAnotherPlace(status)
    }

    init {

        repository = AppRepo().getInstance()
//        repository.getMyPost(_allUsers)
        repository.getMyPostWithUnversity(_allUsers)
        Log.d(TAG,"kiem tra co chay PostViewModel _allUsers: "+_allUsers.value)
        Log.d(TAG,"kiem tra co chay PostViewModel allUsers: "+ allUsers.value)

    }
    fun setData(newData:Post){
        data.value=newData
        Log.d(TAG,"check post as viewmodel ${data.value}")

    }



//    suspend fun getMyPost(): ArrayList<Post> {

//        return withContext(viewModelScope.coroutineContext) {
//            val data = async { repo.getMyPost() }
//            data
//        }.await()
//    }
}