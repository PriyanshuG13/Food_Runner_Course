package com.priyanshu.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
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
import com.priyanshu.foodrunner.util.LOGIN
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var registerYourself: TextView
    private lateinit var login: Button
    private lateinit var etMobileNumber: EditText
    private lateinit var etPassword: EditText
    private lateinit var txtForgotPassword: TextView
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etPassword)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        registerYourself = findViewById(R.id.txtRegisterYourself)
        login = findViewById(R.id.btnLogin)

        login.setOnClickListener {
            val queue = Volley.newRequestQueue(this@LoginActivity)
            val jsonParam = JSONObject()
            jsonParam.put("mobile_number", etMobileNumber.text.toString())
            jsonParam.put("password", etPassword.text.toString())

            if (ConnectionManager().isNetworkAvailable(this@LoginActivity)) {

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.POST,
                    LOGIN,
                    jsonParam,
                    Response.Listener<JSONObject> { response ->

                        try {
                            val data = response.getJSONObject("data")
                            val success = data.getBoolean("success")
                            val cred = data.getJSONObject("data")
                            if (success) {
                                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                                sharedPreferences.edit()
                                    .putString("user_id", cred.getString("user_id")).apply()
                                sharedPreferences.edit().putString("name", cred.getString("name"))
                                    .apply()
                                sharedPreferences.edit().putString("email", cred.getString("email"))
                                    .apply()
                                sharedPreferences.edit()
                                    .putString("mobile_number", cred.getString("mobile_number"))
                                    .apply()
                                sharedPreferences.edit()
                                    .putString("address", cred.getString("address")).apply()
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error: VolleyError? ->
                        Toast.makeText(this@LoginActivity, error?.message, Toast.LENGTH_SHORT)
                            .show()
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
                val builder = AlertDialog.Builder(this@LoginActivity)
                builder.setTitle("Error")
                builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
                builder.setCancelable(false)
                builder.setPositiveButton("Ok") { _, _ ->
                    ActivityCompat.finishAffinity(this@LoginActivity)
                }
                builder.create()
                builder.show()
            }
        }

        txtForgotPassword.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
        }
        registerYourself.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

    }
}
