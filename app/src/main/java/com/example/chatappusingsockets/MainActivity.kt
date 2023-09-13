package com.example.chatappusingsockets

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import io.socket.client.IO
import java.net.Socket

class MainActivity : AppCompatActivity() {
    lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val hostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment?
        navController = hostFragment?.navController!!
        navController.navigate(R.id.chatFragment)
}




}