package bojanantic.example.flickerbrowser

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), GetRawData.OnDownloadComplete,
    GetFlickerJsonData.OnDataAvailable {

    private var flickerRecyclerViewAdapter = FlickerRecyclerViewAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        recyclert_view.layoutManager = LinearLayoutManager(this)
        recyclert_view.adapter = flickerRecyclerViewAdapter

        val getRowData = GetRawData(this)
//        getRowData.onDownloadCompletedListener(this)
        val url = buildUri(
            "https://www.flickr.com/services/feeds/photos_public.gne",
            "motorcycle",
            "en-us",
            true
        )
        getRowData.execute(url)

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
        Log.d(TAG, "onCreate ends")
    }

    private fun buildUri(
        baseURL: String,
        searchParameter: String,
        lang: String,
        mathcAll: Boolean
    ): String {
        Log.d(TAG, ".buildUri started")
        return Uri.parse(baseURL).buildUpon()
            .appendQueryParameter("tags", searchParameter)
            .appendQueryParameter("lang", lang)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("tagmode", if (mathcAll) "ALL" else "ANY")
            .appendQueryParameter("nojsoncallback", "1")
            .build().toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG, "onCreateOptionsMenu called")
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Log.d(TAG, "onOptionsItemSelected called")
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

//    companion object {
//        private const val TAG = "MainActivity"
//    }

    override fun onDownloadComplete(data: String, status: DownloadStatus) {
        if (status == DownloadStatus.OK) {
            Log.d(TAG, "onDownloadDataCompleted called")

            val getFlickerJsonData = GetFlickerJsonData(this)
            getFlickerJsonData.execute(data)

        } else {
            // Download failed
            Log.d(TAG, "onDownlaodDataCompleted failed, status is $status. Error message is $data")
        }
    }

    override fun onDataAvailable(data: List<Photo>) {
        Log.d(TAG, ".onDataAvailable called")
        flickerRecyclerViewAdapter.loadNewData(data)
        Log.d(TAG, ".onDataAvailable ends")
    }

    override fun onError(e: Exception) {
        Log.d(TAG, ".onError called with ${e.message}")
    }
}