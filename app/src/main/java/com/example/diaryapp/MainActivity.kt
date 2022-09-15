package com.example.diaryapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header_nav.*
import kotlin.math.sign

class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var sP : SharedPreferences
    }

    val NmPref = "CobaPref"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Cek sudah sign in atau belum
        sP = getSharedPreferences(NmPref, Context.MODE_PRIVATE)
        val statusSignIn = sP.getString("signInStatus", null)
        if (statusSignIn == "1"){
            Toast.makeText(applicationContext,"Logged in as " + sP.getString("saveUsername", null), Toast.LENGTH_SHORT).show()
            val pIntent = Intent(this@MainActivity, ActivityNavDrawer::class.java)
            startActivity(pIntent)
        }

        btnSignIn.setOnClickListener {
            val txtUsername = txtUsername.text.toString()
            val txtPassword = txtPass.text.toString()
            var signInSuccess = 0

            val db = FirebaseFirestore.getInstance()
            val dbCol = "Accounts"

            if (txtUsername != "" && txtPassword != ""){
                //Cek username & password valid atau tidak
                db.collection(dbCol)
                    .get()
                    .addOnSuccessListener {
                            documents ->
                        for(document in documents){
                            var data = document.data as MutableMap<String, String>
                            if (txtUsername == data.getValue("Username").toString() && txtPassword == data.getValue("Password").toString()){
                                signInSuccess = 1
                            }
                        }

                        if (signInSuccess == 1){
                            //Save sign in status di shared preferences
                            val editor : SharedPreferences.Editor = sP.edit()
                            editor.putString("signInStatus", "1")
                            editor.putString("saveUsername", txtUsername)
                            editor.apply()
                            //Toast & pindah page
                            Toast.makeText(applicationContext,"Sign in Success", Toast.LENGTH_SHORT).show()
                            val pIntent = Intent(this@MainActivity, ActivityNavDrawer::class.java)
                            startActivity(pIntent)
                        }
                        else{
                            Toast.makeText(applicationContext,"Sign in failed", Toast.LENGTH_SHORT).show()
                        }
                    } //end of cek username & password
            }
            else{
                Toast.makeText(applicationContext,"Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }

        }

        btnSignUp.setOnClickListener {
            val pIntent = Intent(this@MainActivity, signUpActivity::class.java)
            startActivity(pIntent)
        }
    }
}