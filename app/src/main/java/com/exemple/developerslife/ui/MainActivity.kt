package com.exemple.developerslife.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.exemple.developerslife.R
import com.exemple.developerslife.databinding.ActivityMainBinding
import com.exemple.developerslife.models.StoryItem
import com.exemple.developerslife.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.currentIndex == -1) {
            viewModel.getNext()
        } else {
            viewModel.getCurrent()
        }

        binding.apply {
            buttonForward.setOnClickListener {
                viewModel.getNext()
            }
            buttonBack.setOnClickListener {
                viewModel.getPrevious()
            }
            textViewRetry.setOnClickListener {
                viewModel.getNext()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.mainEvent.collect { event ->
                when (event) {
                    is MainViewModel.MainEvent.ShowProgressBar -> {
                        showProgressBar(event.visible)
                    }
                    is MainViewModel.MainEvent.ShowError -> {
                        showError(event.visible)
                    }
                    is MainViewModel.MainEvent.ShowStory -> {
                        showStory(event.story)
                    }
                    is MainViewModel.MainEvent.EnableBackButton -> {
                        enableBackButton(event.isOn)
                    }
                }.exhaustive
            }
        }
    }

    private fun showStory(story: StoryItem) {
        binding.apply {
            layoutError.isVisible = false
            cardView.isVisible = true
            buttonForward.isVisible = true
            buttonBack.isVisible = true
        }
        Glide.with(this)
            .load(story.gifURL)
            .fitCenter()
            .error(R.drawable.ic_not_interested)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    showProgressBar(false)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    showProgressBar(false)
                    return false
                }
            })
            .into(binding.imageView)
        binding.textViewDescription.setText(String.format("%s", story.description))
    }

    private fun showError(visible: Boolean) {
        showProgressBar(false)
        binding.apply {
            layoutError.isVisible = visible
            cardView.isVisible = !visible
            buttonForward.isVisible = !visible
            buttonBack.isVisible = !visible
        }
    }

    private fun showProgressBar(visible: Boolean) {
        binding.progressBar.isVisible = visible
    }

    private fun enableBackButton(isOn: Boolean) {
        binding.buttonBack.apply {
            isClickable = isOn
            if (isOn) {
                setColorFilter(ContextCompat.getColor(context, R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN)
            } else {
                setColorFilter(ContextCompat.getColor(context, R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }
    }


}