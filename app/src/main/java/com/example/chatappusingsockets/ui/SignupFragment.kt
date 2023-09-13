package com.example.chatappusingsockets.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.chatappusingsockets.databinding.FragmentSignupScreenBinding

class SignupFragment : Fragment() {
    private lateinit var fragmentSignupScreenBinding: FragmentSignupScreenBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSignupScreenBinding = FragmentSignupScreenBinding.inflate(inflater)
        return fragmentSignupScreenBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}