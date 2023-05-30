package com.example.nearmekotlindemo.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.fragment.CheckInfoFragment
import com.example.nearmekotlindemo.models.googlePlaceModel.Mess
import com.example.nearmekotlindemo.viewModels.PostViewModel

class NearPlacePostAdapter(val itemClick: (List<Post>) -> Unit): RecyclerView.Adapter<NearPlacePostAdapter.MyViewHolder>() {

    private val userList = ArrayList<Post>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.near_place_item,
            parent,false
        )
        return MyViewHolder(itemView)

    }
    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val currentitem = userList[position]

//        holder.Destination.text = currentitem.end
//        holder.StartPosition.text = currentitem.start
//        holder.TimeStart.text = currentitem.time
        holder.money.setText(currentitem.money)
        holder.time.setText(currentitem.timeStart)
        holder.Timedate.setText(currentitem.timeEnd)
        holder.Vehicle.setText(currentitem.vehicle)
        holder.desccriblen.setText(currentitem.describe)

        holder.item.setOnClickListener (
            object :View.OnClickListener{
                override fun onClick(p0: View?) {
                    itemClick(userList)
//                    if (p0 != null) {
//                        Navigation.findNavController(p0)
//                            .navigate(R.id.action_nearPlaceFragment_to_checkInfo)
//                    }



                }

            }
        )
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateUserList(mess : List<Post>){

        this.userList.clear()
        this.userList.addAll(mess)
        notifyDataSetChanged()

    }

    class  MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val money : TextView = itemView.findViewById(R.id.txtMoneyN)
        val time : TextView = itemView.findViewById(R.id.textTimeStartN)
        val Timedate: TextView = itemView.findViewById(R.id.textdateN)
        val Vehicle: TextView = itemView.findViewById(R.id.textVehicleN)
        val desccriblen: TextView = itemView.findViewById(R.id.txtDescribleN)
        val item: CardView  = itemView.findViewById(R.id.postItemN)


    }

}