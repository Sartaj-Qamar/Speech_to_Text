package com.codetech.speechtotext.koin

import com.codetech.speechtotext.Utils.TinyDB
import org.koin.dsl.module


val diModule = module {

    single {
        TinyDB(get())
    }

//    viewModel{
//        RemoteConfigVIewModel(get(),get())
//    }

//    single {
//        RemoteClient().init(get())
//    }
//    single {
//        RemoteConfigRepository(get())
//    }

//    single { RepositoryPremium(get()) }

//    viewModel {
//        ViewModelPremium(get(), get())
//    }



}