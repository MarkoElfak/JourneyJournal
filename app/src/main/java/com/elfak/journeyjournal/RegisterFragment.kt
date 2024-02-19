package com.elfak.journeyjournal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText

class RegisterFragment : Fragment() {

    private lateinit var editTextUsername: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText

    private lateinit var registerButton: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_register, container, false)

        editTextUsername = rootView.findViewById(R.id.editTextUsername)
        editTextPassword = rootView.findViewById(R.id.editTextPassword)
        editTextConfirmPassword = rootView.findViewById(R.id.editTextConfirmPassword)
        editTextEmail = rootView.findViewById(R.id.editTextEmail)

        registerButton = rootView.findViewById(R.id.registerButton)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerButton.setOnClickListener {

            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()
            val email = editTextEmail.text.toString()

            println("Username: $username")
            println("Password: $password")
            println("Confirm Password: $confirmPassword")
            println("Email: $email")

        }
    }
}