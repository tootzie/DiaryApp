package com.example.diaryapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_diary.*
import kotlinx.android.synthetic.main.activity_new_story.*
import kotlinx.android.synthetic.main.itemdiary.*
import java.io.IOException
import java.util.*

class editDiaryActivity : AppCompatActivity() {
    companion object {
        const val extraText = "index"
    }

    //Shared Preferences
    val NmPref = "CobaPref"
    lateinit var sP : SharedPreferences

    // PENTING UNTUK YANG AMBIL GAMBAR GALLERY LALU SIMPAN DI FIREBASE STORAGE
    private val PICK_IMAGE_REQUEST = 72 //angka request ini bebas
    private var filepath: Uri? = null // untuk tampung filepath
    private var storage : FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    //FIRESTORE
    val db = FirebaseFirestore.getInstance()
    val dbCol = "Stories"

    var feeling = "netral"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_diary)

        //AMBIL DATA INTENT
        val dataTerima = intent.getStringExtra(extraText)

        //LOAD DATA KE TEXT FIELD
        db.collection(dbCol)
            .get()
            .addOnSuccessListener {
                    documents ->
                for(document in documents){
                    var data = document.data as MutableMap<String, String>
                    if (document.id == dataTerima) {
                        val judul = data.getValue("judulDiary")
                        val isi = data.getValue("isiDiary")
                        val tgl = data.getValue("tglDiary")
                        val foto = data.getValue("fotoDiary")
                        val feelmood = data.getValue("feeling")
                        val editor : SharedPreferences.Editor = MainActivity.sP.edit()
                        editor.putString("gambarDiary", foto)
                        editor.apply()

                        //SET TO TEXT FIELD
                        tvDate2.setText(tgl)
                        etTitle2.setText(judul)
                        etIsiDiary2.setText(isi)
                        if (foto == "default") {
                            Picasso.get().load(R.drawable.diarypic).into(imgViewDiary2)
//                            imgViewDiary2.setImageResource(R.drawable.diarypic)
                        } else {
                            Picasso.get().load(data.getValue("fotoDiary")).into(imgViewDiary2)
                        }

                        //SET FEELING
                        if (feelmood == "happy"){
                            btnnetral2.setBackgroundResource(R.drawable.disablednetral)
                            btnsad2.setBackgroundResource(R.drawable.disabledsad)
                            btnhappy2.setBackgroundResource(R.drawable.happyemo)
                        }
                        else if (feelmood == "netral"){
                            btnnetral2.setBackgroundResource(R.drawable.ic_baseline_sentiment_satisfied_24)
                            btnsad2.setBackgroundResource(R.drawable.disabledsad)
                            btnhappy2.setBackgroundResource(R.drawable.disabledhappy)
                        }
                        else if (feelmood == "sad"){
                            btnnetral2.setBackgroundResource(R.drawable.disablednetral)
                            btnsad2.setBackgroundResource(R.drawable.sademo)
                            btnhappy2.setBackgroundResource(R.drawable.disabledhappy)
                        }
                    }
                }
            }
            .addOnFailureListener { Log.d("CobaFirebase", "Failure") }

        //get username from shared preferences
        sP = getSharedPreferences(NmPref, Context.MODE_PRIVATE)

        //Enabled Button Save
        btnSave2.isEnabled = true
        btnSave2.setBackgroundResource(R.drawable.ic_baseline_check_24)

        btnhappy2.setOnClickListener{
            btnhappy2.setBackgroundResource(R.drawable.happyemo)
            btnnetral2.setBackgroundResource(R.drawable.disablednetral)
            btnsad2.setBackgroundResource(R.drawable.disabledsad)

            feeling = "happy"
            Toast.makeText(this, feeling, Toast.LENGTH_SHORT).show()
        }
        btnsad2.setOnClickListener{
            btnhappy2.setBackgroundResource(R.drawable.disabledhappy)
            btnnetral2.setBackgroundResource(R.drawable.disablednetral)
            btnsad2.setBackgroundResource(R.drawable.sademo)

            feeling = "sad"
            Toast.makeText(this, feeling, Toast.LENGTH_SHORT).show()
        }
        btnnetral2.setOnClickListener {
            btnhappy2.setBackgroundResource(R.drawable.disabledhappy)
            btnnetral2.setBackgroundResource(R.drawable.ic_baseline_sentiment_satisfied_24)
            btnsad2.setBackgroundResource(R.drawable.disabledsad)

            feeling = "netral"
            Toast.makeText(this, feeling, Toast.LENGTH_SHORT).show()
        }

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DATE)

        storage = FirebaseStorage.getInstance() //PENTING UTK STORAGE
        storageReference = FirebaseStorage.getInstance().reference //PENTING UTK STORAGE


        btnDate2.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener
            { view, year, monthOfYear, dayOfMonth -> tvDate2.setText("" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year)}, year,month, day)
            dpd.show()
        }

        btnOpenGallery2.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }

        btnSave2.setOnClickListener {
            btnSave2.isEnabled = false
            btnSave2.setBackgroundResource(R.drawable.checkdisabled)
            uploadSemuaData()
        }

        btnCancel2.setOnClickListener {
            finish()
        }

    }

    //Function UPLOAD SEMUA DATA TERMASUK FOTONYA KE DATABASE DAN JUGA STORAGE (downloadUri FOTO NYA TTP HRS DICATAT KE DATABASE)
    private fun uploadSemuaData(){
        //AMBIL DATA INTENT
        val dataTerima = intent.getStringExtra(extraText)

        if (etTitle2.text.toString() == "" || etIsiDiary2.text.toString() == "")
        {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Ooopss...")
            builder.setMessage("Please fill your title and story..")

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                Toast.makeText(applicationContext,
                    android.R.string.ok, Toast.LENGTH_SHORT).show()

                //enabled button save supaya bs di klik lagi utk save
                btnSave2.isEnabled = true
                btnSave2.setBackgroundResource(R.drawable.ic_baseline_check_24)
            }
            builder.show()
        }
        else{
            if(filepath != null){
                val ref : StorageReference? = storageReference?.child(UUID.randomUUID().toString())
                val uploadTask : UploadTask? = ref?.putFile(filepath!!) //ini yg starting process kirim ke firebase storage nya.

                val urlTask : Task<Uri>? = uploadTask?.continueWithTask(Continuation <UploadTask.TaskSnapshot,
                        Task<Uri>> { task ->
                    if(!task.isSuccessful){
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation ref.downloadUrl
                })?.addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val downloadUri:Uri? = task.result
                        //Toast.makeText(baseContext, "upload gambar sukes " + downloadUri.toString(),Toast.LENGTH_SHORT).show()

                        //Kirim data selain foto ke database :: Yg Disimpan ke dtabase utk fotonya downloadUri nya bukan filepath
                        var username   = sP.getString("saveUsername", null)
                        var tglDiary   = tvDate2.text.toString()
                        var judulDiary = etTitle2.text.toString()
                        var isiDiary   = etIsiDiary2.text.toString()
                        var foto       = downloadUri.toString()
                        var feelmood       = feeling.toString()


                        val data = hashMapOf(
                            "username" to username,
                            "tglDiary" to tglDiary,
                            "judulDiary" to judulDiary,
                            "isiDiary" to isiDiary,
                            "fotoDiary" to foto,
                            "feeling" to feeling
                            )
                        db.collection(dbCol).document(dataTerima.toString()).set(data as Map<String, Any>)

                        finish()

                    } else{
                        // Handle Failures
                    }
                }?.addOnFailureListener {
                    Toast.makeText(baseContext, "Ada Kesalahan", Toast.LENGTH_SHORT).show()
                }
            } else{
                //Toast.makeText(baseContext, "Please upload an image", Toast.LENGTH_SHORT).show()

                val builder = AlertDialog.Builder(this)
                builder.setTitle("You haven't change the diary photo")
                builder.setMessage("Your diary photo will remain the same")

                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                    Toast.makeText(applicationContext,
                        android.R.string.yes, Toast.LENGTH_SHORT).show()

                    if (etTitle2.text.toString() == "" || etIsiDiary2.text.toString() == "")
                    {
                        Toast.makeText(this,"Your Title and Your Stories must be filled !", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        //Kirim data without foto from user
                        var username   = sP.getString("saveUsername", null)
                        var tglDiary   = tvDate2.text.toString()
                        var judulDiary = etTitle2.text.toString()
                        var isiDiary   = etIsiDiary2.text.toString()
                        var foto       = sP.getString("gambarDiary", "default")
                        var feelmood       = feeling.toString()


                        val data = hashMapOf(
                            "username" to username,
                            "tglDiary" to tglDiary,
                            "judulDiary" to judulDiary,
                            "isiDiary" to isiDiary,
                            "fotoDiary" to foto,
                            "feeling" to feeling
                            )
                        db.collection(dbCol).document(dataTerima.toString()).set(data as Map<String, Any>)

                        finish()
                    }

                }

                builder.setNegativeButton(android.R.string.no) { dialog, which ->
                    Toast.makeText(applicationContext,
                        android.R.string.no, Toast.LENGTH_SHORT).show()

                    //disabled button save supaya gak ke klik lebih dr skali
                    btnSave2.isEnabled = true
                    btnSave2.setBackgroundResource(R.drawable.ic_baseline_check_24)
                }
                builder.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST)
        {
            if(data == null || data.data == null)
            {
                return
            }

            filepath = data.data
            try{
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filepath)
                imgViewDiary2.setImageBitmap(bitmap)
            } catch (e : IOException){
                e.printStackTrace()
            }
        }

    }
}