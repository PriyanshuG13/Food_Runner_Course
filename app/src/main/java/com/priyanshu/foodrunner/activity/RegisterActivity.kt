package com.priyanshu.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.priyanshu.foodrunner.R
import com.priyanshu.foodrunner.util.ConnectionManager
import com.priyanshu.foodrunner.util.REGISTER
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var btnRegister: Button
    lateinit var etName: EditText
    lateinit var etPhoneNumber: EditText
    lateinit var etPassword: EditText
    lateinit var etEmail: EditText
    lateinit var etAddress: EditText
    lateinit var etConfirmPassword: EditText
    lateinit var rlRegister: RelativeLayout
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Register Yourself"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }

        rlRegister = findViewById(R.id.rlRegister)
        etName = findViewById(R.id.etName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etAddress = findViewById(R.id.etAddress)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {

            val queue = Volley.newRequestQueue(this@RegisterActivity)
            val jsonParam = JSONObject()
            jsonParam.put("name", etName.text.toString())
            jsonParam.put("mobile_number", etPhoneNumber.text.toString())
            jsonParam.put("password", etPassword.text.toString())
            jsonParam.put("address", etAddress.text.toString())
            jsonParam.put("email", etEmail.text.toString())

            if (ConnectionManager().isNetworkAvailable(this@RegisterActivity)) {

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.POST,
                    REGISTER,
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
                                startActivity(
                                    Intent(
                                        this@RegisterActivity,
                                        HomeActivity::class.java
                                    )
                                )
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error: VolleyError? ->
                        Toast.makeText(this@RegisterActivity, error?.message, Toast.LENGTH_SHORT)
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
                val builder = AlertDialog.Builder(this@RegisterActivity)
                builder.setTitle("Error")
                builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
                builder.setCancelable(false)
                builder.setPositiveButton("Ok") { _, _ ->
                    ActivityCompat.finishAffinity(this@RegisterActivity)
                }
                builder.create()
                builder.show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
