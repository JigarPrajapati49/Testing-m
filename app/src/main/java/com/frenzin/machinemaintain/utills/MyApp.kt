package com.frenzin.machinemaintain.utills

import android.app.Application

public class MyApp : Application() {
    companion object{
        private var myApp: MyApp? = null
        fun  getMyApp(): MyApp? {
            return myApp
        }
    }
    override fun onCreate() {
        super.onCreate()
        myApp = this
    }
}