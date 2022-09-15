package com.example.diaryapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.itemdiary.view.*
import com.example.diaryapp.ListDiary.*


class storyAdapter(private val listDiary: ArrayList<Story>) : RecyclerView.Adapter<storyAdapter.ListViewHolder>() {
    //SHARED PREFERENCES
    val NmPref = "CobaPref"
    lateinit var sP : SharedPreferences



    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtTgl : TextView = itemView.tglDiary
        var txtJudul : TextView = itemView.judulDiary
        var txtIsi : TextView = itemView.isiDiary
        var imgFoto : ImageView = itemView.fotoDiary
        var imgFeeling : ImageView = itemView.feelingEmoticonList
        var editDiary : ImageButton = itemView.btnEdit
        var deleteDiary : ImageButton = itemView.btnDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view : View = LayoutInflater.from(parent.context).inflate(
            R.layout.itemdiary,
            parent,
            false
        )
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        var diary = listDiary[position]
        val db = FirebaseFirestore.getInstance()
        val dbCol = "Stories"
        var uniqueKey = ""



        holder.txtTgl.setText(diary.tglDiary)
        holder.txtJudul.setText(diary.judulDiary)
        if(diary.isiDiary == null) {
            diary.isiDiary = ""
        }
        if(diary.isiDiary!!.count()  > 200) {
            holder.txtIsi.setText(diary.isiDiary!!.substring(0, 199) + " [..]")
        } else {
            holder.txtIsi.setText(diary.isiDiary!!)
        }
        if (diary.fotoDiary == "default")
        {
            holder.imgFoto.setImageResource(R.drawable.diarypic)
        }else{
            Picasso.get().load(diary.fotoDiary).into(holder.imgFoto)
        }
        if (diary.feeling == "sad") {
            holder.imgFeeling.setImageResource(R.drawable.sademo)
        } else if (diary.feeling == "happy") {
            holder.imgFeeling.setImageResource(R.drawable.happyemo)
        } else {
            holder.imgFeeling.setImageResource(R.drawable.ic_baseline_sentiment_satisfied_24)
        }

        holder.editDiary.setOnClickListener {
            sP = it.context.getSharedPreferences(NmPref, Context.MODE_PRIVATE)
            //AMBIL UNIQUE KEY LALU PINDAH KE FRAGMENT EDIT DIARY
            db.collection(dbCol)
                .get()
                .addOnSuccessListener { documents ->
                    for(document in documents){
                        var data = document.data as MutableMap<String, String>
                        val judul = data.getValue("judulDiary")
                        val username = data.getValue("username")
                        val usernamePref = sP.getString("saveUsername", null)
                        if (judul == holder.txtJudul.text.toString() && username == usernamePref ){
                            Log.d("CobaFirebase", document.id)
                            val pDetail = Intent(it.context, editDiaryActivity::class.java)
                            pDetail.putExtra(editDiaryActivity.extraText, document.id)
                            it.context.startActivity(pDetail)
                        }

                    }
                }
                .addOnFailureListener { Log.d("CobaFirebase", "Failure") }
        }

        holder.deleteDiary.setOnClickListener{
            sP = it.context.getSharedPreferences(NmPref, Context.MODE_PRIVATE)
            //AMBIL UNIQUE KEY LALU HAPUS DATA
            db.collection(dbCol)
                .get()
                .addOnSuccessListener { documents ->
                    for(document in documents){
                        var data = document.data as MutableMap<String, String>
                        val judul = data.getValue("judulDiary")
                        val username = data.getValue("username")
                        val usernamePref = sP.getString("saveUsername", null)
                        if (judul == holder.txtJudul.text.toString() && username == usernamePref){
                            Log.d("CobaFirebase", document.id)
                            //HAPUS DATA
                            db.collection(dbCol).document(document.id)
                                .delete()
                                .addOnSuccessListener { Log.d("CobaFirebase", "Hapus berhasil") }
                                .addOnFailureListener { e-> Log.d("CobaFirebase", "Error", e) }
                        }

                    }
                }
                .addOnFailureListener { Log.d("CobaFirebase", "Failure") }
            listDiary.removeAt(position)
            notifyDataSetChanged()
        }

        holder.itemView.setOnClickListener {
            val detailIntent = Intent(holder.itemView.context, detailStory::class.java)
            detailIntent.putExtra("detTglDiary", diary.tglDiary)
            detailIntent.putExtra("detJudulDiary", diary.judulDiary)
            detailIntent.putExtra("detIsiDiary", diary.isiDiary)
            detailIntent.putExtra("detFotoDiary", diary.fotoDiary)
            //detailIntent.putExtra("detFeeling", diary.feeling)
            holder.itemView.context.startActivity(detailIntent)
        }
    }

    override fun getItemCount(): Int {
        return listDiary.size
    }
}