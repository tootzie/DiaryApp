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
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_new_story.*
import java.io.IOException
import java.util.*

class newStory : AppCompatActivity() {

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

    //flag for button feeling
    var feel = "netral"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_story)

        //get username from shared preferences
        sP = getSharedPreferences(NmPref, Context.MODE_PRIVATE)
        //Toast.makeText(this, "Username:" + sP.getString("saveUsername", null), Toast.LENGTH_SHORT).show()

        //Enabled Button Save
        btnSave.isEnabled = true
        btnSave.setBackgroundResource(R.drawable.ic_baseline_check_24)

        //enabled button netral feeling
        btnnetral.setBackgroundResource(R.drawable.ic_baseline_sentiment_satisfied_24)
        btnsad.setBackgroundResource(R.drawable.disabledsad)
        btnhappy.setBackgroundResource(R.drawable.disabledhappy)

        btnhappy.setOnClickListener{
            btnhappy.setBackgroundResource(R.drawable.happyemo)
            btnnetral.setBackgroundResource(R.drawable.disablednetral)
            btnsad.setBackgroundResource(R.drawable.disabledsad)

            feel = "happy"
            Toast.makeText(this, feel, Toast.LENGTH_SHORT).show()
        }
        btnsad.setOnClickListener{
            btnhappy.setBackgroundResource(R.drawable.disabledhappy)
            btnnetral.setBackgroundResource(R.drawable.disablednetral)
            btnsad.setBackgroundResource(R.drawable.sademo)

            feel = "sad"
            Toast.makeText(this, feel, Toast.LENGTH_SHORT).show()
        }
        btnnetral.setOnClickListener {
            btnhappy.setBackgroundResource(R.drawable.disabledhappy)
            btnnetral.setBackgroundResource(R.drawable.ic_baseline_sentiment_satisfied_24)
            btnsad.setBackgroundResource(R.drawable.disabledsad)

            feel = "netral"
            Toast.makeText(this, feel, Toast.LENGTH_SHORT).show()
        }


        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DATE)

        val sdf = SimpleDateFormat("dd/M/yyyy")
        val currentDate = sdf.format(Date())
        tvDate.setText(currentDate.toString())


        storage = FirebaseStorage.getInstance() //PENTING UTK STORAGE
        storageReference = FirebaseStorage.getInstance().reference //PENTING UTK STORAGE

        btnDate.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener
            { view, year, monthOfYear, dayOfMonth -> tvDate.setText("" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year)}, year,month, day)
            dpd.show()
        }

        //Intent implicit ambil foto dr gallery
        btnOpenGallery.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)

        }
        btnSave.setOnClickListener {
            //disabled button save supaya gak ke klik lebih dr skali
            btnSave.isEnabled = false
            btnSave.setBackgroundResource(R.drawable.checkdisabled)
            uploadSemuaData()
        }
        btnCancel.setOnClickListener {
            finish()
        }


    }

    //Function UPLOAD SEMUA DATA TERMASUK FOTONYA KE DATABASE DAN JUGA STORAGE (downloadUri FOTO NYA TTP HRS DICATAT KE DATABASE)
    private fun uploadSemuaData(){

        if (etTitle.text.toString() == "" || etIsiDiary.text.toString() == "")
        {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Ooopss...")
            builder.setMessage("Please fill your title and story..")

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                Toast.makeText(applicationContext,
                    android.R.string.ok, Toast.LENGTH_SHORT).show()

                //enabled button save supaya bs di klik lagi utk save
                btnSave.isEnabled = true
                btnSave.setBackgroundResource(R.drawable.ic_baseline_check_24)

            }
            builder.show()
        }
        else{
            if(filepath != null){
                val ref : StorageReference? = storageReference?.child(UUID.randomUUID().toString())
                val uploadTask : UploadTask? = ref?.putFile(filepath!!) //ini yg starting process kirim ke firebase storage nya.

                val urlTask : Task<Uri>? = uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot,
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
                            var tglDiary   = tvDate.text.toString()
                            var judulDiary = etTitle.text.toString()
                            var isiDiary   = etIsiDiary.text.toString()
                            var foto       = downloadUri.toString()
                            var feelmood       = feel.toString()


                            val data = hashMapOf(
                                "username" to username,
                                "tglDiary" to tglDiary,
                                "judulDiary" to judulDiary,
                                "isiDiary" to isiDiary,
                                "fotoDiary" to foto,
                                "feeling" to feelmood

                                )
                            db.collection(dbCol).document().set(data as Map<String, Any>)

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
                builder.setTitle("You don't choose your diary photo yet..")
                builder.setMessage("Do you want default diary photo ?")

                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                    Toast.makeText(applicationContext,
                        android.R.string.yes, Toast.LENGTH_SHORT).show()

                    if (etTitle.text.toString() == "" || etIsiDiary.text.toString() == "")
                    {
                        Toast.makeText(this,"Your Title and Your Stories must be filled !", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        //Kirim data without foto from user
                        var username   = sP.getString("saveUsername", null)
                        var tglDiary   = tvDate.text.toString()
                        var judulDiary = etTitle.text.toString()
                        var isiDiary   = etIsiDiary.text.toString()
                        var foto       = "default"
                        var feelmood   = feel.toString()


                        val data = hashMapOf(
                            "username" to username,
                            "tglDiary" to tglDiary,
                            "judulDiary" to judulDiary,
                            "isiDiary" to isiDiary,
                            "fotoDiary" to foto,
                            "feeling" to feelmood

                            )
                        db.collection(dbCol).document().set(data as Map<String, Any>)

                        finish()
                    }

                }

                builder.setNegativeButton(android.R.string.no) { dialog, which ->
                    Toast.makeText(applicationContext,
                        android.R.string.no, Toast.LENGTH_SHORT).show()

                    //disabled button save supaya gak ke klik lebih dr skali
                    btnSave.isEnabled = true
                    btnSave.setBackgroundResource(R.drawable.ic_baseline_check_24)
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
                imgViewDiary.setImageBitmap(bitmap)
            } catch (e : IOException){
                e.printStackTrace()
            }
        }

    }
}