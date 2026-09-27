package com.elfak.journeyjournal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class MainMenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_menu, container, false)

        // Set click listeners for the buttons
        view.findViewById<Button>(R.id.loginButton).setOnClickListener {
            // Navigate to the LoginFragment
            (activity as MainActivity).showLoginFragment()
        }

        view.findViewById<Button>(R.id.registerButton).setOnClickListener {
            // Navigate to the RegisterFragment
            (activity as MainActivity).showRegisterFragment()
        }

        return view
    }

    // Additional fragment logic goes here
}