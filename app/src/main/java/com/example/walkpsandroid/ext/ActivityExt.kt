package com.example.walkpsandroid.ext

import android.app.Activity
import com.example.walkpsandroid.factory.ViewModelFactory

/**
 * Created by PS Wang on 2021/4/14
 */
fun Activity.getVmFactory(): ViewModelFactory {
    return ViewModelFactory()
}