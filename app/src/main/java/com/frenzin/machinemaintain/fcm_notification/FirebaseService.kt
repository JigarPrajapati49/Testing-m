package com.frenzin.machinemaintain.fcm_notification

import android.util.Log
import android.widget.Toast
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.utills.AppPref
import com.frenzin.machinemaintain.utills.PreferenceKeys
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseService : FirebaseMessagingService() {

    private val TAG = "FirebaseService"
    private var appPref: AppPref? = null

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate: ------------FirebaseService---------", )
        appPref = AppPref.getInstance(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.e(TAG, "onMessageReceived: -------- $remoteMessage")
    }

    override fun onNewToken(fcmToken: String) {
        super.onNewToken(fcmToken)
        Log.e(TAG, "onNewToken() --------- : $fcmToken")
        appPref?.set(PreferenceKeys.FCM_TOKEN,fcmToken)
    }

}