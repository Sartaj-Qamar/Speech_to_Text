package com.codetech.speechtotext.Dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.codetech.speechtotext.R
import com.codetech.speechtotext.databinding.RateUsDialogBinding

fun showRateUsDialog(context: Activity) {
    val rateUsDialogBinding: RateUsDialogBinding by lazy {
        RateUsDialogBinding.inflate(LayoutInflater.from(context))
    }

    val dialog = Dialog(context)
    dialog.setContentView(rateUsDialogBinding.root)
    dialog.setCancelable(true)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    dialog.show()
    
    rateUsDialogBinding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
        if(rating <= 1){
            rateUsDialogBinding.rateUsEmoji.setImageResource(R.drawable.very_sad)
        }
        if(rating <= 2 && rating > 1){
            rateUsDialogBinding.rateUsEmoji.setImageResource(R.drawable.sad)
        }
        if(rating <= 3 && rating > 2){
            rateUsDialogBinding.rateUsEmoji.setImageResource(R.drawable.neutral)
        }
        if(rating <=4 && rating > 3){
            rateUsDialogBinding.rateUsEmoji.setImageResource(R.drawable.happy)
        }
        if(rating <=5 && rating > 4){
            rateUsDialogBinding.rateUsEmoji.setImageResource(R.drawable.very_happy)
        }
    }


    rateUsDialogBinding.notNowBtn.setOnClickListener {
        dialog.dismiss()
    }


}
