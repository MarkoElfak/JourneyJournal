package com.elfak.journeyjournal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the fragment's layout
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Find the ScrollView within the fragment's view hierarchy
        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)

        // Create a LinearLayout to hold your dynamic content
        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.VERTICAL

        // Create and add dynamic elements (text and image) to the LinearLayout
        for (i in 0 until 5) {  // Adjust the loop range as needed
            // Create a LinearLayout for each item
            val itemLayout = LinearLayout(requireContext())
            itemLayout.orientation = LinearLayout.HORIZONTAL

            // Create a TextView for text
            val textView = TextView(requireContext())
            textView.text = "Item $i: Your dynamic text content here"
            textView.contentDescription = "Item $i"
            // You can customize TextView properties here

            // Create an ImageView for the image
            val imageView = ImageView(requireContext())
            // Set the image resource or other properties as needed


            // imageView.setImageResource(R.drawable.your_image_resource)               !!!!!
            imageView.contentDescription = "Image for Item $i"



            // Add TextView and ImageView to the item's LinearLayout
            itemLayout.addView(textView)
            itemLayout.addView(imageView)

            // Add the item's LinearLayout to the main LinearLayout
            linearLayout.addView(itemLayout)
        }

        // Add the main LinearLayout to the ScrollView
        scrollView.addView(linearLayout)

        return view
    }
}
