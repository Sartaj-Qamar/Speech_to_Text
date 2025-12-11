package com.codetech.speechtotext.Dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import com.codetech.speechtotext.databinding.ExitDialogBinding
import kotlin.system.exitProcess

fun showExitAppDialog(context: Activity) {
    val exitDialogBinding: ExitDialogBinding by lazy {
        ExitDialogBinding.inflate(LayoutInflater.from(context))
    }

    val dialog = Dialog(context)
    dialog.setContentView(exitDialogBinding.root)
    dialog.setCancelable(true)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    dialog.show()


    exitDialogBinding.discard.setOnClickListener {
        dialog.dismiss()
    }

    exitDialogBinding.yes.setOnClickListener {
        dialog.dismiss()
        exitProcess(0)
    }
}
