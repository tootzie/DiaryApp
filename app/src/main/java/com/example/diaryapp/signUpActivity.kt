package com.example.diaryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_up.*

class signUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btnSignUp.setOnClickListener {
            val txtUsername = txtUsernameSignUp.text.toString()
            val txtPass = txtPassSignUp.text.toString()
            val txtConfirmPass = txtConfirmPassSignUp.text.toString()
            var uniqueUsername = 1

            val db = FirebaseFirestore.getInstance()
            val dbCol = "Accounts"

            if (txtUsername != "" && txtPass != ""){
                if (txtPass == txtConfirmPass){
                    //Cek ada username dobel atau tidak
                    db.collection(dbCol)
                        .get()
                        .addOnSuccessListener {
                                documents ->
                            for(document in documents){
                                var data = document.data as MutableMap<String, String>
                                Log.d("CobaFireBase", data.getValue("Username").toString())
                                if (txtUsername == data.getValue("Username").toString()){
                                    Log.d("CobaFireBase", "Masuk if")
                                    uniqueUsername = 0
                                }
                            }

                            if (uniqueUsername == 1){
                                //Masukkan data ke database
                                val data = hashMapOf(
                                    "Username" to txtUsername,
                                    "Password" to txtPass
                                )
                                db.collection(dbCol).document().set(data as Map<String, Any>)
                                Toast.makeText(applicationContext,"Sign up success",Toast.LENGTH_SHORT).show()
                                //Pindah ke halaman sign in
                                val pIntent = Intent(this@signUpActivity, MainActivity::class.java)
                                startActivity(pIntent)
                            }
                            else{
                                Toast.makeText(applicationContext,"Username already taken",Toast.LENGTH_SHORT).show()
                            }
                        } //end of cek username
                }
                else{
                    Toast.makeText(applicationContext,"Password doesn't match",Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(applicationContext,"Fields cannot be empty",Toast.LENGTH_SHORT).show()
            }
        }

        btnSignIn.setOnClickListener {
            val pIntent = Intent(this@signUpActivity, MainActivity::class.java)
            startActivity(pIntent)
        }
    }
}