package com.example.collab

import PersonalCalendarAdapter
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.collab.UserInfo.userInfoEmail
import com.example.collab.databinding.ActivityDashBoardBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_dash_board.*
import kotlinx.android.synthetic.main.plan_row.view.*
import kotlinx.android.synthetic.main.work_row.view.*

class DashBoardActivity : AppCompatActivity() {
    lateinit var binding: ActivityDashBoardBinding
    var context = this
    var firestore : FirebaseFirestore?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initLayout()
    }

    private fun initLayout() {
        var teamName = intent.getStringExtra("teamName")

        binding.apply{
            projectName.text = teamName

            progressLayout.setOnClickListener {
                Intent(this@DashBoardActivity,WorkActivity::class.java).apply{
                    putExtra("teamName", teamName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run{startActivity(this)}
            }
            // 팀장인 경우만 팀 관리 버튼 신청할 수 있도록해야 함
            teamSetting.setOnClickListener{
                Intent(this@DashBoardActivity,ManageTeamActivity::class.java).apply{
                    putExtra("teamName", teamName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run{startActivity(this)}
            }

            teamPlanAddBtn.setOnClickListener {
                Intent(this@DashBoardActivity,TeamCalendarActivity::class.java).apply{
                    putExtra("teamName",teamName)
                }.run{startActivity(this)}
            }


            val items = ArrayList<CalendarData>()
            teamPlanRecyclerView.adapter = PersonalCalendarAdapter(items)
            teamPlanRecyclerView.layoutManager = LinearLayoutManager(context)

            val noticeList = ArrayList<ProfileNoticeData>()
            teamNoticeRecyclerView.adapter = ProfileNoticeAdapter(noticeList)
            teamNoticeRecyclerView.layoutManager = LinearLayoutManager(context)

            firestore = FirebaseFirestore.getInstance()

            teamNoticeAddBtn.setOnClickListener {
                val dialog = Dialog(context)
                dialog.setContentView(R.layout.team_notice_add_dialog)
                dialog.window!!.setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                dialog.setCanceledOnTouchOutside(true)
                dialog.setCancelable(true)
                dialog.show()

                val noticeContentEdit = dialog.findViewById<EditText>(R.id.noticeContentEdit)
                dialog.findViewById<Button>(R.id.noticeAddBtn).setOnClickListener {
                    firestore?.collection("Team")
                        ?.document(teamName!!)
                        ?.get()?.addOnSuccessListener {
                            val str = noticeContentEdit.text.toString()

                            if(it?.contains("noticeList")==true){
                                firestore?.collection("Team")
                                    ?.document(teamName!!)
                                    ?.update("noticeList", FieldValue.arrayUnion(str))
                            }else{
                                val docData = hashMapOf(
                                    "noticeList" to arrayListOf(str)
                                )
                                firestore?.collection("Team")
                                    ?.document(teamName!!)
                                    ?.set(docData, SetOptions.merge())
                            }
                        }
                    dialog.dismiss()
                }
                dialog.findViewById<ImageView>(R.id.noticeAddCancelBtn).setOnClickListener {
                    dialog.dismiss()
                }
            }

            firestore?.collection("Team")
                ?.document(teamName!!)
                ?.addSnapshotListener { value, error ->
                    Log.i("data", value?.data.toString())

                    items.clear()
                    if(value?.contains("plans")==true){
                        val list = value?.get("plans") as ArrayList<String>
                        for(str in list!!){
                            val container = str.split("!")
                            items.add(CalendarData(
                                    container[0],
                                    container[1].split("/")[0],
                                    container[1].split("/")[1],
                                    container[2].split("/")[0],
                                    container[2].split("/")[1])) }
                        teamPlanRecyclerView.adapter?.notifyDataSetChanged()
                    }

                    if(value?.contains("todoList")==true){
                        val todoList = value?.get("todoList") as ArrayList<String>
                        firestore?.collection("Team")
                            ?.document(teamName!!)
                            ?.collection("info")
                            ?.document("todoList")
                            ?.addSnapshotListener { value2, error ->
                                var progress = 0
                                var progress_count = 0
                                if(value2?.exists()==true){
                                    for(todo in todoList){
                                        if(value2?.contains(todo+"_progress")){
                                            progress += (value2?.get(todo+"_progress") as Number).toInt()
                                            progress_count++
                                        }
                                    }
                                    if(progress_count == 0)
                                        progress = 0
                                    else
                                        progress /= progress_count
                                    totalProgressRateNum.text = progress.toString()
                                    totalProgressRate.progress = progress
                                }}
                    }

                    if(value?.contains("noticeList")==true){
                        val list = value?.get("noticeList") as ArrayList<String>
                        noticeList.clear()
                        for(notice in list!!){
                            noticeList.add(ProfileNoticeData(notice))
                        }
                        teamNoticeRecyclerView.adapter?.notifyDataSetChanged()
                    }


                }





        }
    }

}