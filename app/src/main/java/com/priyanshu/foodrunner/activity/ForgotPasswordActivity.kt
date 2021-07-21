package com.priyanshu.foodrunner.activity

import android.content.Intent
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
import com.priyanshu.foodrunner.util.FORGOT_PASSWORD
import org.json.JSONException
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var etForgotMobile: EditText
    lateinit var etForgotEmail: EditText
    lateinit var btnForgotNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_forgot_password)

        etForgotMobile = findViewById(R.id.etForgotMobile)
        etForgotEmail = findViewById(R.id.etForgotEmail)
        btnForgotNext = findViewById(R.id.btnForgotNext)

        btnForgotNext.setOnClickListener {

            val queue = Volley.newRequestQueue(this@ForgotPasswordActivity)
            val jsonParam = JSONObject()
            jsonParam.put("mobile_number", etForgotMobile.text.toString())

            if (ConnectionManager().isNetworkAvailable(this@ForgotPasswordActivity)) {

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.POST,
                    FORGOT_PASSWORD,
                    jsonParam,
                    Response.Listener<JSONObject> { response ->

                        try {
                            val data = response.getJSONObject("data")
                            val success = data.getBoolean("success")
                            if (success) {
                                Toast.makeText(
                                    this@ForgotPasswordActivity,
                                    "OTP Sent to ${etForgotEmail.text}.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(
                                    Intent(
                                        this@ForgotPasswordActivity,
                                        ResetPasswordActivity::class.java
                                    )
                                )
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error: VolleyError? ->
                        Toast.makeText(
                            this@ForgotPasswordActivity,
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
                val builder = AlertDialog.Builder(this@ForgotPasswordActivity)
                builder.setTitle("Error")
                builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
                builder.setCancelable(false)
                builder.setPositiveButton("Ok") { _, _ ->
                    ActivityCompat.finishAffinity(this@ForgotPasswordActivity)
                }
                builder.create()
                builder.show()
            }
        }
    }
}
