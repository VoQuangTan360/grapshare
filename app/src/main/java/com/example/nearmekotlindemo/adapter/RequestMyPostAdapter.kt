package com.example.nearmekotlindemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.models.googlePlaceModel.Mess
import com.example.nearmekotlindemo.models.googlePlaceModel.Star
import com.example.nearmekotlindemo.viewModels.LocationViewModel
import com.example.nearmekotlindemo.viewModels.PostViewModel

class RequestMyPostAdapter(val itemClick: (Mess) -> Unit): RecyclerView.Adapter<RequestMyPostAdapter.MyViewHolder>() {

    private val userList = ArrayList<Mess>()
    private val userStart = ArrayList<Star>()
    private val image = ArrayList<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.my_request_item,
            parent,false
        )
        return MyViewHolder(itemView)

    }
    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val currentitem = userList[position]


        holder.name.text = currentitem.name
        holder.time.text = currentitem.time
        holder.mota.text = currentitem.mess
        holder.vehicle.text=currentitem.vehicle

        Glide.with(holder.itemView.getContext()).load(currentitem.image)
            .into(holder.image)

        holder.item.setOnClickListener (

            object :View.OnClickListener{
                override fun onClick(p0: View?) {
                    itemClick(currentitem)

                }

            }
        )
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateUserList(mess : List<Mess>){

        this.userList.clear()
        this.userList.addAll(mess)


        notifyDataSetChanged()

    }


    class  MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val name : TextView = itemView.findViewById(R.id.txtnameRequet)
        val time : TextView = itemView.findViewById(R.id.txtStartPositionR)
        val mota : TextView = itemView.findViewById(R.id.txtDestinationR)
        val vehicle : TextView = itemView.findViewById(R.id.txtvehicle)
        val image : ImageView = itemView.findViewById(R.id.imgProfileRequest)
        val item: CardView  = itemView.findViewById(R.id.postItemMR)


    }

}