package com.example.collab

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.collab.UserInfo.userInfoEmail
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_team_info.*
import kotlinx.android.synthetic.main.manage_team_row.view.*
import kotlinx.android.synthetic.main.name_card_row.view.*
import kotlinx.android.synthetic.main.team_info_row.view.*

class ManageAdapter(val items: ArrayList<UserData>,val name:String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var firestore : FirebaseFirestore?= null
    var data_member : ArrayList<String>? = null
    var data_join : ArrayList<String>? = null
    var isMember : ArrayList<Boolean> = ArrayList()
    var master : String? = null

    init {
        firestore = FirebaseFirestore.getInstance()
        //val teamName = "ABC"
        firestore?.collection("Team")
            ?.document(name)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                data_member = querySnapshot?.get("member") as ArrayList<String>
                if(querySnapshot?.contains("join")==true){
                    data_join = querySnapshot?.get("join") as ArrayList<String>
                }else {
                    data_join = null
                }
                isMember.clear()
                master = null
                firestore?.collection("User")
                    ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        items.clear()
                        for (snapshot in querySnapshot!!.documents) {
                            for (member in data_member!!) {
                                if (snapshot.toString().contains(member)) {
                                    var item = snapshot.toObject(UserData::class.java)
                                    items.add(item!!)
                                    isMember.add(true)
                                }
                            }
                            if(data_join != null){
                                for(member in data_join!!){
                                    if(snapshot.toString().contains(member)){
                                        var item = snapshot.toObject(UserData::class.java)
                                        items.add(item!!)
                                        isMember.add(false)
                                    }
                                }
                            }
                        }
                        notifyDataSetChanged()
                    }
            }
    }

    interface OnItemClickListener{
        fun OnItemClick(data: UserData, isMember:Boolean)
    }
    var itemClickListener:OnItemClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view =
            LayoutInflater.from(parent.context).inflate(R.layout.manage_team_row, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener {
                itemClickListener?.OnItemClick(items[absoluteAdapterPosition], isMember[absoluteAdapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var viewHolder = (holder as ViewHolder).itemView
        viewHolder.memberName.text = items[position].name.toString()
        if(isMember[position]) {
            viewHolder.memberStatus.text = "팀원"
        }else{
            viewHolder.memberStatus.text = "신청자"
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}