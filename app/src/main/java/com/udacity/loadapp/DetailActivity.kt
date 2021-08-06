package com.udacity.loadapp

import android.app.NotificationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.loadapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private val notificationManager by lazy { ContextCompat.getSystemService(this@DetailActivity, NotificationManager::class.java) as NotificationManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        notificationManager.cancelNotifications()

        val notification = intent.getSerializableExtra(NOTIFICATION_BODY) as NotificationBody
        binding.contentDetail.tvFileName.text = notification.title
        binding.contentDetail.tvStatus.text = notification.status

        when (notification.status) {
            getString(R.string.success) ->
                binding.contentDetail.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.green_A700))
            getString(R.string.failed) ->
                binding.contentDetail.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.red_400))
        }

        binding.contentDetail.mbtDetailsOk.setOnClickListener {
            onBackPressed()
        }
    }
}
