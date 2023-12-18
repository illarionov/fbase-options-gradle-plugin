/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.example.samplefbase.flavors

import android.app.Application
import com.example.samplefbase.config.benchmarkFirebaseOptions
import com.google.firebase.FirebaseApp

class Application : Application() {
    public var firebaseApp: FirebaseApp? = null

    override fun onCreate() {
        super.onCreate()
        firebaseApp = FirebaseApp.initializeApp(this, benchmarkFirebaseOptions)
    }
}
