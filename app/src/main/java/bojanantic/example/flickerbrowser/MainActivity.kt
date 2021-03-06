package bojanantic.example.flickerbrowser

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"

class MainActivity : BaseActivity(), GetRawData.OnDownloadComplete,
    GetFlickerJsonData.OnDataAvailable, RecyclerItemClickListener.OnRecyclerClickListener {

    private var flickerRecyclerViewAdapter = FlickerRecyclerViewAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activateToolbar(false)

        recyclert_view.layoutManager = LinearLayoutManager(this)
        recyclert_view.addOnItemTouchListener(RecyclerItemClickListener(this, recyclert_view, this))
        recyclert_view.adapter = flickerRecyclerViewAdapter

        Log.d(TAG, "onCreate ends")
    }

    override fun onItemClick(view: View, position: Int) {
        Log.d(TAG, ".onItemClicked: starts")
        Toast.makeText(this, "Normal tap at position $position", Toast.LENGTH_SHORT).show()
    }

    override fun onItemLongClick(view: View, position: Int) {
        Log.d(TAG, ".onItemLongCLick starts")
        val photo = flickerRecyclerViewAdapter.getPhoto(position)
        if (photo != null) {
            val intent = Intent(this, PhotoDetailsActivity::class.java)
            intent.putExtra(PHOTO_TRANSFER, photo)
            startActivity(intent)
        }
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
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
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

    override fun onResume() {
        Log.d(TAG, ".onResume called")
        super.onResume()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val queryResult = sharedPref.getString(FLICKER_QUERY, "")

        if (!queryResult.isNullOrEmpty()) {
            val getRowData = GetRawData(this)
            val url = buildUri(
                "https://www.flickr.com/services/feeds/photos_public.gne",
                queryResult,
                "en-us",
                true
            )
            getRowData.execute(url)
        }

        Log.d(TAG, ".onResume ends")
    }
}