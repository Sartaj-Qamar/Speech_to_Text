package com.codetech.speechtotext.Utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.codetech.speechtotext.data_source.AppDatabase
import com.codetech.speechtotext.data_source.Translation

class DBViewModel(val context: Context) : ViewModel() {

    val appDatabase: AppDatabase = AppDatabase.getDatabase(context)

    fun updateFavourite(translation: Translation) {

        //   val list = appDatabase.translationDao().getAllTranslations()

//        for (item in list) {
//
//            if (translation.timestamp == item.timestamp) {
//
//                Log.e("favvv112", "updateFavourite: ${item.isFavorite}")
//                Log.e("favvv112", "after: ${!item.isFavorite}")
//                // appDatabase.translationDao().updateTranslationNew(item.id, !item.isFavorite)
//            }
//        }

    }

}