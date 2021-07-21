package com.priyanshu.foodrunner.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.priyanshu.foodrunner.R
import com.priyanshu.foodrunner.adapter.AllRestaurantsAdapter
import com.priyanshu.foodrunner.adapter.RestaurantsMenuAdapter
import com.priyanshu.foodrunner.database.RestaurantEntity
import com.priyanshu.foodrunner.model.Menu
import com.priyanshu.foodrunner.util.ConnectionManager
import com.priyanshu.foodrunner.util.FETCH_RESTAURANTS
import org.json.JSONException
import org.json.JSONObject

class RestaurantDetailsActivty : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var progressBar: ProgressBar
    private lateinit var rlLoading: RelativeLayout
    private lateinit var recyclerMenu: RecyclerView
    private lateinit var restaurantsMenuAdapter: RestaurantsMenuAdapter
    private lateinit var btnAddtoFav: ImageButton
    private lateinit var btnProceedToCart: Button
    private var menuList = arrayListOf<Menu>()
    private lateinit var resInfo: Bundle
    private var resId: Int = 100
    lateinit var sharedPreferences: SharedPreferences
    private var title: String = "Restaurant"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_details)

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        btnAddtoFav = findViewById(R.id.btnAddtoFav)
        progressBar = findViewById(R.id.progressBar)
        rlLoading = findViewById(R.id.rlLoading)
        btnProceedToCart = findViewById(R.id.btnProceedToCart)
        rlLoading.visibility = View.VISIBLE

        toolbar = findViewById(R.id.toolbar)

        if (intent != null) {
            resInfo = intent.getBundleExtra("details")!!
            resId = intent.getIntExtra("resId", 100)
        } else {
            finish()
            Toast.makeText(
                this@RestaurantDetailsActivty,
                "Some unexpected error occurred",
                Toast.LENGTH_SHORT
            ).show()
        }

        title = resInfo.getString("title") as String
        val rating = resInfo.getString("rating") as String
        val costForTwo = resInfo.getInt("costForTwo").toString()
        val imageUrl = resInfo.getString("imageUrl") as String

        setSupportActionBar(toolbar)
        supportActionBar?.title = title

        setUpRecycler()

        val listOfFavourites =
            AllRestaurantsAdapter.GetAllFavAsyncTask(this@RestaurantDetailsActivty).execute().get()

        if (listOfFavourites.isNotEmpty() && listOfFavourites.contains(resId.toString())) {
            btnAddtoFav.setBackgroundResource(R.drawable.ic_action_fav_checked)
        } else {
            btnAddtoFav.setBackgroundResource(R.drawable.ic_action_fav)
        }

        btnAddtoFav.setOnClickListener {
            val restaurantEntity = RestaurantEntity(
                resId, title, rating, costForTwo, imageUrl
            )

            if (!AllRestaurantsAdapter.DBAsyncTask(
                    this@RestaurantDetailsActivty,
                    restaurantEntity,
                    1
                ).execute().get()
            ) {
                val async =
                    AllRestaurantsAdapter.DBAsyncTask(
                        this@RestaurantDetailsActivty,
                        restaurantEntity,
                        2
                    ).execute()
                val result = async.get()
                if (result) {
                    btnAddtoFav.setBackgroundResource(R.drawable.ic_action_fav_checked)
                }
            } else {
                val async = AllRestaurantsAdapter.DBAsyncTask(
                    this@RestaurantDetailsActivty,
                    restaurantEntity,
                    3
                ).execute()
                val result = async.get()

                if (result) {
                    btnAddtoFav.setBackgroundResource(R.drawable.ic_action_fav)
                }
            }
        }

    }

    private fun setUpRecycler() {
        recyclerMenu = findViewById(R.id.recyclerMenu)
        val url = "${FETCH_RESTAURANTS}/$resId"
        val userId = sharedPreferences.getString("user_id", "1")!!

        val queue = Volley.newRequestQueue(this@RestaurantDetailsActivty)

        if (ConnectionManager().isNetworkAvailable(this@RestaurantDetailsActivty)) {

            val jsonObjectRequest = object : JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                Response.Listener<JSONObject> { response ->
                    rlLoading.visibility = View.GONE

                    try {
                        val data = response.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {

                            val resArray = data.getJSONArray("data")
                            for (i in 0 until resArray.length()) {
                                val menuObject = resArray.getJSONObject(i)
                                val menu = Menu(
                                    menuObject.getString("id").toInt(),
                                    menuObject.getString("name"),
                                    menuObject.getString("cost_for_one").toInt(),
                                    menuObject.getString("restaurant_id").toInt()
                                )
                                menuList.add(menu)
                                restaurantsMenuAdapter = RestaurantsMenuAdapter(
                                    menuList,
                                    this@RestaurantDetailsActivty,
                                    userId, title,
                                    btnProceedToCart
                                )
                                val mLayoutManager =
                                    LinearLayoutManager(this@RestaurantDetailsActivty)
                                recyclerMenu.layoutManager = mLayoutManager
                                recyclerMenu.itemAnimator = DefaultItemAnimator()
                                recyclerMenu.adapter = restaurantsMenuAdapter
                                recyclerMenu.setHasFixedSize(true)
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error: VolleyError? ->
                    Toast.makeText(
                        this@RestaurantDetailsActivty,
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
            val builder = AlertDialog.Builder(this@RestaurantDetailsActivty)
            builder.setTitle("Error")
            builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
            builder.setCancelable(false)
            builder.setPositiveButton("Ok") { _, _ ->
                ActivityCompat.finishAffinity(this@RestaurantDetailsActivty)
            }
            builder.create()
            builder.show()
        }

    }
}