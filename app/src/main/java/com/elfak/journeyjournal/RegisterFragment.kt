package com.elfak.journeyjournal

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class RegisterFragment : Fragment() {

    private lateinit var editTextUsername: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText

    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth


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

        auth = Firebase.auth

        registerButton.setOnClickListener {

            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()
            val email = editTextEmail.text.toString()

            if (username.isEmpty())
            {
                Toast.makeText(context, "Enter username!", Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }

            createUser(email, password)

            println("Username: $username")
            println("Password: $password")
            println("Confirm Password: $confirmPassword")
            println("Email: $email")
        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(context, "User already signed-in!", Toast.LENGTH_SHORT).show();
        }
    }

    fun createUser(email : String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        requireContext(),
                        "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // User is authenticated, navigate to another fragment or perform other UI updates
            // For example, navigate to a HomeFragment
            val homeFragment = ProfileFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit()
        } else {
            // User is not authenticated, you may handle this case differently
        }
    }


}