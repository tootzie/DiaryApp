package com.example.diaryapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_list_diary.*
import com.example.diaryapp.MainActivity.Companion.sP
import kotlinx.android.synthetic.main.activity_nav_drawer.*
import kotlinx.android.synthetic.main.itemdiary.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ListDiary.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListDiary : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var adapter : storyAdapter

    private lateinit var dtTgl : Array<String>
    private lateinit var dtJudul : Array<String>
    private lateinit var dtIsi : Array<String>
    private lateinit var dtFoto : Array<String>
    private lateinit var dtFeel : Array<String>

    var arDiary = arrayListOf<Story>()

    fun prepareStory() {
        val db = FirebaseFirestore.getInstance()
        val dbCol = "Stories"

        var dtTgl_ : Array<String> = arrayOf()
        var dtJudul_ : Array<String> = arrayOf()
        var dtIsi_ : Array<String> = arrayOf()
        var dtFoto_ : Array<String> = arrayOf()
        var dtFeel_ : Array<String> = arrayOf()

        db.collection(dbCol)
            .get()
            .addOnSuccessListener {
                    documents ->
                for(document in documents){
                    var data = document.data as MutableMap<String, Any>
                    if(sP.getString("saveUsername", null) == data.getValue("username").toString()) {
                        dtTgl_ += data.getValue("tglDiary").toString()
                        dtJudul_ += data.getValue("judulDiary").toString()
                        dtIsi_ += data.getValue("isiDiary").toString()
                        dtFoto_ += data.getValue("fotoDiary").toString()
                        dtFeel_ += data.getValue("feeling").toString()
                    }
                }
                dtTgl = dtTgl_
                dtJudul = dtJudul_
                dtIsi = dtIsi_
                dtFoto = dtFoto_
                dtFeel = dtFeel_
                addStory()
                showStory()
            }
    }

    fun refreshData() {
        arDiary.clear()
        prepareStory()
    }

    private fun addStory() {
        for(position in dtTgl.indices) {
            val data = Story(sP.getString("saveUsername", null), dtTgl[position], dtJudul[position], dtIsi[position], dtFoto[position], dtFeel[position])
            arDiary.add(data)
            Log.d("Data", "Data added!")
        }
    }

    private fun showStory() {
        rvListDiary.layoutManager = LinearLayoutManager(ListDiary().context)
        adapter = storyAdapter(arDiary)
        rvListDiary.adapter = adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_diary, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListDiary.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListDiary().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}