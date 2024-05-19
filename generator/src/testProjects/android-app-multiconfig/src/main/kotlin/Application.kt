/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.example.samplefbase

import android.app.Application
import com.example.samplefbase.config.firebaseOptions
import com.example.samplefbase.config.firebaseOptionsOrg2
import com.example.samplefbase.config.firebaseOptionsOrg3
import com.google.firebase.FirebaseApp

class Application : Application() {
    public var firebaseApp: FirebaseApp? = null
    public var firebaseAppOrg2: FirebaseApp? = null
    public var firebaseAppOrg3: FirebaseApp? = null

    override fun onCreate() {
        super.onCreate()
        firebaseApp = FirebaseApp.initializeApp(this, firebaseOptions)
        firebaseAppOrg2 = FirebaseApp.initializeApp(this, firebaseOptionsOrg2)
        firebaseAppOrg3 = FirebaseApp.initializeApp(this, firebaseOptionsOrg3)
    }
}
