package com.example.diaryapp

import kotlinx.android.synthetic.main.activity_new_story.*

data class Story (
    var username  : String? = "",
    var tglDiary  : String? = "",
    var judulDiary: String? = "",
    var isiDiary  : String? = "",
    var fotoDiary :  String? = "",
    var feeling : String? = ""
)