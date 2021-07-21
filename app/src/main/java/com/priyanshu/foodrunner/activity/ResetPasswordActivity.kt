package com.priyanshu.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.priyanshu.foodrunner.R
import com.priyanshu.foodrunner.util.ConnectionManager
import com.priyanshu.foodrunner.util.RESET_PASSWORD
import org.json.JSONException
import org.json.JSONObject

class ResetPasswordActivity : AppCompatActivity() {

    lateinit var etOTP: EditText
    lateinit var etNewPassword: EditText
    lateinit var etConfirmPassword: EditText
    lateinit var btnSubmit: Button
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        etOTP = findViewById(R.id.etOTP)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {

            val queue = Volley.newRequestQueue(this@ResetPasswordActivity)
            val jsonParam = JSONObject()
            jsonParam.put("otp", etOTP.text.toString())
            jsonParam.put("password", etNewPassword.text.toString())

            if (ConnectionManager().isNetworkAvailable(this@ResetPasswordActivity)) {

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.POST,
                    RESET_PASSWORD,
                    jsonParam,
                    Response.Listener<JSONObject> { response ->

                        try {
                            val data = response.getJSONObject("data")
                            val success = data.getBoolean("success")
                            if (success) {
                                Toast.makeText(
                                    this@ResetPasswordActivity,
                                    data.getString("successMessage"),
                                    Toast.LENGTH_SHORT
                                ).show()
                                sharedPreferences.edit().clear().apply()
                                startActivity(
                                    Intent(
                                        this@ResetPasswordActivity,
                                        LoginActivity::class.java
                                    )
                                )
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error: VolleyError? ->
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            error?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {

                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "67b301f68b6810"
                        return headers
                    }
                }

                queue.add(jsonObjectRequest)

            } else {
                val builder = AlertDialog.Builder(this@ResetPasswordActivity)
                builder.setTitle("Error")
                builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
                builder.setCancelable(false)
                builder.setPositiveButton("Ok") { _, _ ->
                    ActivityCompat.finishAffinity(this@ResetPasswordActivity)
                }
                builder.create()
                builder.show()
            }
        }
    }
}