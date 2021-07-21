package com.priyanshu.foodrunner.fragment


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.priyanshu.foodrunner.R

class ProfileFragment : Fragment() {

    lateinit var txtUserName: TextView
    lateinit var txtPhone: TextView
    lateinit var txtEmail: TextView
    lateinit var txtAddress: TextView
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_name),
            Context.MODE_PRIVATE
        )

        txtUserName = view.findViewById(R.id.txtUserName)
        txtPhone = view.findViewById(R.id.txtPhone)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtAddress = view.findViewById(R.id.txtAddress)

        txtUserName.text = sharedPreferences.getString("name", "Priyanshu").toString()
        txtPhone.text = sharedPreferences.getString("email", "0123456789").toString()
        txtEmail.text =
            sharedPreferences.getString("mobile_number", "runner@foodrunner.com").toString()
        txtAddress.text = sharedPreferences.getString("address", "Delhi").toString()

        return view
    }

}
