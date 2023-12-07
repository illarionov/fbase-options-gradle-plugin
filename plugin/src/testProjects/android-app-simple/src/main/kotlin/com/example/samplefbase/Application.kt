package com.example.samplefbase

import android.app.Application
import com.example.samplefbase.config.GeneratedFirebaseOptions
import com.google.firebase.FirebaseApp

class Application : Application(){
    public var firebaseApp: FirebaseApp? = null

    override fun onCreate() {
        super.onCreate()
        firebaseApp = FirebaseApp.initializeApp(this, GeneratedFirebaseOptions.firebaseOptions)
    }
}
