package com.example.nearmekotlindemo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.models.googlePlaceModel.Mess
import com.example.nearmekotlindemo.viewModels.PostViewModel

class RequestMyPostAdapter(val itemClick: (Mess) -> Unit): RecyclerView.Adapter<RequestMyPostAdapter.MyViewHolder>() {

    private val userList = ArrayList<Mess>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.request_mypost_item,
            parent,false
        )
        return MyViewHolder(itemView)

    }
    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val currentitem = userList[position]

        holder.Destination.text = currentitem.end
        holder.StartPosition.text = currentitem.start
        holder.TimeStart.text = currentitem.time

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

        val Destination : TextView = itemView.findViewById(R.id.txtDestinationR)
        val StartPosition : TextView = itemView.findViewById(R.id.txtStartPositionR)
        val TimeStart : TextView = itemView.findViewById(R.id.textTimeStartR)
        val item: CardView  = itemView.findViewById(R.id.postItemR)


    }

}