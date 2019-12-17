package bojanantic.example.flickerbrowser

import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

enum class DownloadStatus {
    OK, IDLE, NOT_INITIALISED, FAILED_OR_EMPTY, PERMISSION_ERROR, ERROR
}

private const val TAG = "GetRawData"

class GetRawData(private val listener: OnDownloadComplete) : AsyncTask<String, Void, String>() {

    interface OnDownloadComplete {
        fun onDownloadComplete(data: String, status: DownloadStatus)
    }

    private var downloadStatus = DownloadStatus.IDLE
//    private var listener: MainActivity? = null
//
//    fun onDownloadCompletedListener(callbackObject: MainActivity?) {
//        listener = callbackObject
//    }

    override fun onPostExecute(result: String) {
        Log.d(TAG, "onPostExecute called, data is $result")
        listener.onDownloadComplete(result, downloadStatus)
    }

    override fun doInBackground(vararg params: String?): String {
        if (params[0] == null) {
            downloadStatus = DownloadStatus.NOT_INITIALISED
            return "No URL specified."
        }
        try {
            downloadStatus = DownloadStatus.OK
            return URL(params[0]).readText()
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is MalformedURLException -> {
                    downloadStatus = DownloadStatus.NOT_INITIALISED
                    "doInBackground: Invalid URL ${e.message}"
                }
                is IOException -> {
                    downloadStatus = DownloadStatus.FAILED_OR_EMPTY
                    "doInBackground: IO exception reading data: ${e.message}"
                }
                is SecurityException -> {
                    downloadStatus = DownloadStatus.PERMISSION_ERROR
                    "doInBackground: Security Exception. Needs permission? ${e.message}"
                }
                else -> {
                    downloadStatus = DownloadStatus.ERROR
                    "doInBackground: UNKNOWN ERROR ${e.message}"
                }
            }
            Log.d(TAG, errorMessage)
            return errorMessage
        }
    }
}