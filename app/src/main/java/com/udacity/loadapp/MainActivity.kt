package com.udacity.loadapp

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.loadapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private const val LOAD_APP_URL = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val GLIDE_URL = "https://github.com/bumptech/glide/archive/master.zip"
        private const val RETROFIT_URL = "https://github.com/square/retrofit/archive/master.zip"

        private const val EMPTY_SELECTION = -1
    }

    private var downloadID: Long = 0
    private var downloadUrl: String = ""
    private var downloadTitle: String = ""
    private var downloadDescription: String = ""

    private lateinit var notificationManager: NotificationManager

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        notificationManager = ContextCompat.getSystemService(this@MainActivity, NotificationManager::class.java) as NotificationManager

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.contentMain.customButton.setOnClickListener {
            binding.contentMain.customButton.setState(ButtonState.Clicked)
            download()
        }

        radioGroupCheckedChangeListener()
    }

    private fun radioGroupCheckedChangeListener() {
        binding.contentMain.rgDownloadOptions.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_glide -> {
                    setDownloadUrlAndTexts(
                        url = GLIDE_URL,
                        title = getString(R.string.glide_option),
                        description = getString(R.string.glide_option_description)
                    )
                }
                R.id.rb_load_app -> {
                    setDownloadUrlAndTexts(
                        url = LOAD_APP_URL,
                        title = getString(R.string.load_app_option),
                        description = getString(R.string.load_app_option_description)
                    )
                }
                R.id.rb_retrofit -> {
                    setDownloadUrlAndTexts(
                        url = RETROFIT_URL,
                        title = getString(R.string.retrofit_option),
                        description = getString(R.string.retrofit_option_description)
                    )
                }
            }
        }

    }

    private fun setDownloadUrlAndTexts(url: String, title: String, description: String) {
        downloadUrl = url
        downloadTitle = title
        downloadDescription = description
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (downloadID == id) {
                if (intent.action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    val query = DownloadManager.Query().setFilterById(downloadID)
                    val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        if (cursor.count > 0) {
                            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                            val title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                binding.contentMain.customButton.setState(ButtonState.Completed)

                                notificationManager.sendNotification(
                                    applicationContext = application,
                                    notificationBody = NotificationBody(
                                        title = title,
                                        status = getString(R.string.success),
                                        description = getString(R.string.success)
                                    )
                                )
                                cursor.close()
                            } else {
                                notificationManager.sendNotification(
                                    applicationContext = application,
                                    notificationBody = NotificationBody(
                                        title = title,
                                        status = getString(R.string.failed),
                                        description = getString(R.string.failed)
                                    )
                                )
                                cursor.close()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun download() {
        if (binding.contentMain.rgDownloadOptions.checkedRadioButtonId == EMPTY_SELECTION) {
            Toast.makeText(this@MainActivity, getString(R.string.empty_option), Toast.LENGTH_LONG).show()
        } else {
            binding.contentMain.customButton.setState(ButtonState.Loading)
            val request =
                DownloadManager.Request(Uri.parse(downloadUrl))
                    .setTitle(downloadTitle)
                    .setDescription(downloadDescription)
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID = downloadManager.enqueue(request) // enqueue puts the download request in the queue.
        }
    }
}
