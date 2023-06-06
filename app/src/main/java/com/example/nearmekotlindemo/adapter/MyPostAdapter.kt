package com.example.nearmekotlindemo.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.nearmekotlindemo.Post
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.fragment.CreatePostFragment
import com.example.nearmekotlindemo.fragment.InfoMyPostFragment
import com.example.nearmekotlindemo.models.googlePlaceModel.StatusID

class MyPostAdapter(val itemClick: (StatusID) -> Unit): RecyclerView.Adapter<MyPostAdapter.MyViewHolder>() {

    private val userList = ArrayList<Post>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.mypost_item,
            parent,false
        )
        return MyViewHolder(itemView)

    }
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val currentitem = userList[position]


        holder.firstName.text = currentitem.destination
        holder.lastName.text = currentitem.timeStart
        holder.age.text = currentitem.money
        holder.id.text = currentitem.postId
        if(currentitem.status=="1") {
            holder.tb.setTextColor(Color.RED)
            holder.tb.text = "Có yêu cầu"
        }
        if(currentitem.status=="2") {
            holder.tb.setTextColor(Color.YELLOW)
            holder.tb.text="Đợi phản hồi"
        }
        if(currentitem.status=="3") {
            holder.tb.text="Đang giao dịch"
            holder.item.setBackgroundColor(R.color.teal_700)
        }

        holder.item.setOnClickListener (
            object :View.OnClickListener{
                override fun onClick(p0: View?) {

                    itemClick(StatusID(currentitem.postId.toString(),currentitem.status.toString()) )

                    if (p0 != null) {
                        Navigation.findNavController(p0)
                            .navigate(R.id.action_btnSavedPlaces_to_infoMyPostFragment)
                    }

//                        val activity=p0!!.context as AppCompatActivity
//                        val infoPost=InfoMyPostFragment()
//                        activity.supportFragmentManager.beginTransaction()
//                            .replace(R.id.fragmentContainer,infoPost).commit()

                }

            }
        )
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateUserList(userList : List<Post>){

        this.userList.clear()
        this.userList.addAll(userList)
        notifyDataSetChanged()

    }

    class  MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val firstName : TextView = itemView.findViewById(R.id.tvfirstName)
        val lastName : TextView = itemView.findViewById(R.id.tvlastName)
        val age : TextView = itemView.findViewById(R.id.tvage)
        val id : TextView = itemView.findViewById(R.id.txtIDM)
        val tb: TextView = itemView.findViewById(R.id.txtTB)
        val item : CardView = itemView.findViewById(R.id.postItem)

    }

}