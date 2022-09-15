package com.example.diaryapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_nav_drawer.*
import kotlinx.android.synthetic.main.fragment_setting.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val NmPref = "CobaPref"
    lateinit var sP : SharedPreferences


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sP = this.activity!!.getSharedPreferences(NmPref, Context.MODE_PRIVATE)

        btnSignOut.setOnClickListener {
            val editor : SharedPreferences.Editor = sP.edit()
            editor.clear()
            editor.commit()

            val pIntent = Intent(activity, MainActivity::class.java)
            startActivity(pIntent)
        }

        btnAlarm.setOnClickListener {
            if(etJam.text.toString() != "" && etMenit.text.toString() != ""){
                val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    Log.d("AlarmFF", "Masuk2")
                    putExtra(AlarmClock.EXTRA_MESSAGE, "Diary Reminder")
                    putExtra(AlarmClock.EXTRA_HOUR, etJam.text.toString().toInt())
                    putExtra(AlarmClock.EXTRA_MINUTES, etMenit.text.toString().toInt())
                }
                startActivity(alarmIntent)
            } else {
                Toast.makeText(context,"Please fill all the fields", Toast.LENGTH_SHORT).show()
            }

        }

//        btnAlarm.setOnClickListener {
//            Log.d("AlarmFF", "Masuk")

//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}