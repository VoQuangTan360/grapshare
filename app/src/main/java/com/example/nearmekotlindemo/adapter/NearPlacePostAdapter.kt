package com.example.nearmekotlindemo.adapter

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
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
//        binding.txtDestination.text=itemPost.destination
//        binding.textVehicle.text=itemPost.vehicle
//        binding.txtMoney.text=itemPost.money+" VND"
//        binding.textdate.text=itemPost.timeEnd
//        binding.textTimeStart.text=itemPost.timeStart
//        binding.txtDescrible.text=itemPost.describe

        holder.txtDestination.setText(currentitem.destination)
        holder.textVehicle.setText(currentitem.vehicle)
        holder.txtMoney.setText(currentitem.money+" VND")
        holder.textdate.setText(currentitem.timeEnd)
        holder.textTimeStart.setText(currentitem.timeStart)
        holder.txtDescrible.setText(currentitem.describe)

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
        Log.d(TAG,"kiem tra List<Post>: "+mess)
        this.userList.clear()
        this.userList.addAll(mess)
        notifyDataSetChanged()

    }

    class  MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val txtDestination : TextView = itemView.findViewById(R.id.txtDestination)
        val textVehicle : TextView = itemView.findViewById(R.id.textVehicle)
        val txtMoney: TextView = itemView.findViewById(R.id.txtMoney)
        val textdate: TextView = itemView.findViewById(R.id.textdate)
        val textTimeStart: TextView = itemView.findViewById(R.id.textTimeStart)
        val txtDescrible: TextView = itemView.findViewById(R.id.txtDescrible)
        val item: CardView  = itemView.findViewById(R.id.postItemN)


    }

}