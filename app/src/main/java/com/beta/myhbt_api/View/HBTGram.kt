package com.beta.myhbt_api.View

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.GetAllHBTGramPostService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterHBTGramPost
import kotlinx.android.synthetic.main.activity_hbtgram.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HBTGram : AppCompatActivity() {
    // Array of HBTGram posts
    private var hbtGramPosts = ArrayList<HBTGramPost>()

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterHBTGramPost?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hbtgram)

        // Instantiate the recycler view
        hbtGramPostsView.layoutManager = LinearLayoutManager(applicationContext)
        hbtGramPostsView.itemAnimator = DefaultItemAnimator()

        // Execute the AsyncTask to get all HBTGram posts
        //GetAllPostsTask().execute()
    }
    /*
    // AsyncTask to get all posts from the database
    inner class GetAllPostsTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            // Create the get all posts service
            val getAllPostService: GetAllHBTGramPostService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(GetAllHBTGramPostService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getAllPostService.getAllPosts()

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        val body = response.body()
                        print(body)
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        for (post in data["documents"] as ArrayList<Map<String, Any>>) {
                            val postId = post["_id"] as String
                            val content = post["content"] as String
                            val writer = post["writer"] as String
                            val numOfImages = (post["numOfImages"] as Double).toInt()
                            val orderInCollection = (post["orderInCollection"] as Double).toInt()
                            val dateCreated = post["dateCreated"] as String

                            val postObject = HBTGramPost(postId, content, writer, numOfImages, orderInCollection, dateCreated)

                            hbtGramPosts.add(postObject)
                        }
                        /*
                        // Get the array of posts
                        hbtGramPosts = data["documents"] as ArrayList<HBTGramPost>

                        print(hbtGramPosts)
                         */

                        // Update the adapter
                        adapter = RecyclerViewAdapterHBTGramPost(hbtGramPosts, this@HBTGram, this@HBTGram)

                        // Add adapter to the RecyclerView
                        hbtGramPostsView.adapter = adapter
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

     */

    // The function to reload the RecyclerView
    fun reloadRecyclerView () {
        hbtGramPostsView.adapter!!.notifyDataSetChanged()
    }
}
