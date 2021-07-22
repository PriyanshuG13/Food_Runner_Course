package com.priyanshu.foodrunner.adapter

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.priyanshu.foodrunner.R
import com.priyanshu.foodrunner.activity.RestaurantDetailsActivty
import com.priyanshu.foodrunner.database.RestaurantDatabase
import com.priyanshu.foodrunner.database.RestaurantEntity
import com.priyanshu.foodrunner.model.Restaurants
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class AllRestaurantsAdapter(private var restaurants: ArrayList<Restaurants>, val context: Context) :
    RecyclerView.Adapter<AllRestaurantsAdapter.AllRestaurantsViewHolder>(), Filterable {

    var restaurantsFilterList: ArrayList<Restaurants> = arrayListOf()

    init {
        restaurantsFilterList = restaurants
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AllRestaurantsViewHolder {
        val itemView = LayoutInflater.from(p0.context)
            .inflate(R.layout.restaurants_custom_row, p0, false)

        return AllRestaurantsViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return restaurantsFilterList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    restaurantsFilterList = restaurants
                } else {
                    val resultList = ArrayList<Restaurants>()
                    for (row in restaurants) {
                        if (row.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    restaurantsFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = restaurantsFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                restaurantsFilterList = results?.values as ArrayList<Restaurants>
                notifyDataSetChanged()
            }

        }
    }

    override fun onBindViewHolder(p0: AllRestaurantsViewHolder, p1: Int) {
        val resObject = restaurantsFilterList.get(p1)

        p0.restaurantName.text = resObject.name
        p0.rating.text = resObject.rating
        val costForTwo = "${resObject.costForTwo}/person"
        p0.cost.text = costForTwo
        Picasso.get().load(resObject.imageUrl).error(R.drawable.res_image).into(p0.resThumbnail)

        val listOfFavourites = GetAllFavAsyncTask(context).execute().get()

        if (listOfFavourites.isNotEmpty() && listOfFavourites.contains(resObject.id.toString())) {
            p0.favImage.setImageResource(R.drawable.ic_action_fav_checked)
        } else {
            p0.favImage.setImageResource(R.drawable.ic_action_fav)
        }

        p0.favImage.setOnClickListener {
            val restaurantEntity = RestaurantEntity(
                resObject.id,
                resObject.name,
                resObject.rating,
                resObject.costForTwo.toString(),
                resObject.imageUrl
            )

            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
                val async =
                    DBAsyncTask(context, restaurantEntity, 2).execute()
                val result = async.get()
                if (result) {
                    p0.favImage.setImageResource(R.drawable.ic_action_fav_checked)
                }
            } else {
                val async = DBAsyncTask(context, restaurantEntity, 3).execute()
                val result = async.get()

                if (result) {
                    p0.favImage.setImageResource(R.drawable.ic_action_fav)
                }
            }
        }

        p0.cardRestaurant.setOnClickListener {
            val intent = Intent(context, RestaurantDetailsActivty::class.java)
            val bundle = Bundle()

            bundle.putString("title", resObject.name)
            bundle.putString("rating", resObject.rating)
            bundle.putInt("costForTwo", resObject.costForTwo)
            bundle.putString("imageUrl", resObject.imageUrl)

            intent.putExtra("details", bundle)
            intent.putExtra("resId", resObject.id)

            context.startActivity(intent)
        }
    }

    class AllRestaurantsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val resThumbnail = view.findViewById(R.id.imgRestaurantThumbnail) as ImageView
        val restaurantName = view.findViewById(R.id.txtRestaurantName) as TextView
        val rating = view.findViewById(R.id.txtRestaurantRating) as TextView
        val cost = view.findViewById(R.id.txtCostForTwo) as TextView
        val cardRestaurant = view.findViewById(R.id.cardRestaurant) as CardView
        val favImage = view.findViewById(R.id.imgIsFav) as ImageView
    }

    class DBAsyncTask(context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {

                1 -> {
                    val res: RestaurantEntity? =
                        db.restaurantDao().getRestaurantById(restaurantEntity.id.toString())
                    db.close()
                    return res != null
                }

                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }

                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
            }

            return false
        }

    }

    class GetAllFavAsyncTask(
        context: Context
    ) :
        AsyncTask<Void, Void, List<String>>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): List<String> {

            val list = db.restaurantDao().getAllRestaurants()
            val listOfIds = arrayListOf<String>()
            for (i in list) {
                listOfIds.add(i.id.toString())
            }
            return listOfIds
        }
    }
}