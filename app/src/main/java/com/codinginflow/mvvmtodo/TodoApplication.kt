package com.codinginflow.mvvmtodo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

//Necessary to activate dagger Hilt.
@HiltAndroidApp
class TodoApplication : Application() {
}