package com.example.nearmekotlindemo.viewModels

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.models.googlePlaceModel.GooglePlaceModel
import com.example.nearmekotlindemo.repo.AppRepo
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class LocationViewModel : ViewModel() {




    private val repo = AppRepo()
    private val repository : AppRepo

    private val _allUsers = MutableLiveData<List<Post>>()
    val allUsers : LiveData<List<Post>> = _allUsers
    private val _nearPosr = MutableLiveData<List<Post>>()
    val nearPosr : LiveData<List<Post>> = _nearPosr

    private val _post = MutableLiveData<List<Post>>()
    val post : LiveData<List<Post>> = _post
    fun setpost(string1: List<Post>){
        _post.value=string1
    }
    val post12 = MutableLiveData<Post>()
    fun setpost12(string1: Post){
        post12.value=string1
    }





    val idPost = MutableLiveData<String>()
    val post1 : LiveData<String> = idPost
    fun setidPost(string1: String){
        idPost.value=string1
        Log.d(TAG,"kiem tra gia tri LocationViewModel setidPost: "+idPost.value)
    }
    fun getPostWithUnversity(name:String){
        repository.getPostWithUnversity(name,_allUsers)
    }
    fun getNearPostWithUnversity(name:String,lat:Double,lng:Double,distance:Double){
        Log.d(TAG,"kiem tra gia tri LocationViewModel setidPost: "+idPost.value)
        repository.getNearPostWithUnversity(name,lat,lng,distance,_nearPosr)
    }

    init {
        Log.d(TAG,"xem so lan chay: +1"+post.value)
        repository = AppRepo().getInstance()
        repository.getAllPostWithUnversity(_allUsers)


    }
    fun getPostWithUnversityAndIDcheck(id:String){
        var i=id
        if(i.isEmpty()){
            i="0"
        }
        repository.getPostWithUnversityAndID(i,_post)
        Log.d(TAG,"kiem tra gia tri LocationViewModel idpost: "+post.value)
    }
    fun getNearByPlace(url: String) = repo.getPlaces(url)
    fun getNearByPlaceUnversity(url: String) = repo.getPlaces(url)
    fun removePlace(userSavedLocationId: ArrayList<String>) = repo.removePlace(userSavedLocationId)

    fun addUserPlace(googlePlaceModel: GooglePlaceModel, userSavedLocationId: ArrayList<String>) =
        repo.addUserPlace(googlePlaceModel, userSavedLocationId)

    suspend fun getUserLocationId(): ArrayList<String> {

        return withContext(viewModelScope.coroutineContext) {
            val data = async { repo.getUserLocationId() }
            data
        }.await()
    }

    fun getDirection(url: String) = repo.getDirection(url)

    fun getUserLocations() = repo.getUserLocations()

    fun updateName(name: String) = repo.updateName(name)

    fun updateImage(image: Uri) = repo.updateImage(image)

    fun confirmEmail(authCredential: AuthCredential) = repo.confirmEmail(authCredential)

    fun updateEmail(email: String) = repo.updateEmail(email)

    fun updatePassword(password: String) = repo.updatePassword(password)
}