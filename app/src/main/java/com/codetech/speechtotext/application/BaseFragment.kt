package com.codetech.speechtotext.application

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.codetech.speechtotext.Helper.LocaleUtils
import com.codetech.speechtotext.Utils.TinyDB

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

open class BaseFragment<VB : ViewBinding>(
    private val inflate: Inflate<VB>
) : Fragment() {
    private var _binding: VB? = null
    open val binding get() = _binding
    private var drawerListener: DrawerLayout.DrawerListener? = null
    open lateinit var tinyDB: TinyDB


    override fun onAttach(context: Context) {
        super.onAttach(context)
        LocaleUtils.loadLocale(context)

        tinyDB = TinyDB(context)
        if (context is DrawerLayout.DrawerListener) {
            drawerListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        drawerListener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding?.root
    }

    fun backPress(callback: () -> Unit) {
        isFragmentVisible {
            requireActivity().onBackPressedDispatcher.addCallback(this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        callback()
                    }
                })
        }
    }

    private fun isFragmentVisible(doWork: () -> Unit) {
        if (isAdded && !isDetached)
            doWork.invoke()
    }
}
