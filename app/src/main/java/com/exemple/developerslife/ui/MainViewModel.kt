package com.exemple.developerslife.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.developerslife.api.DevelopersApi
import com.exemple.developerslife.models.StoryItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(private val api: DevelopersApi) : ViewModel() {

    private val mainEventChannel = Channel<MainEvent>()
    val mainEvent = mainEventChannel.receiveAsFlow()

    private val listStories = mutableListOf<StoryItem>()
    var currentIndex = -1
        set(value) {
            field = value
            setStateBackButton(field)
        }
        get() {
            setStateBackButton(field)
            return field
        }

    fun getNext() {
        if (currentIndex < 0) {
            getRandom()
        } else if (currentIndex == listStories.size - 1) {
            getRandom()
        } else {
            viewModelScope.launch {
                currentIndex++
                mainEventChannel.send(MainEvent.ShowStory(listStories[currentIndex]))
            }
        }
    }

    fun getPrevious() {
        currentIndex--
        viewModelScope.launch {
            mainEventChannel.send(MainEvent.ShowStory(listStories[currentIndex]))
        }
    }

    fun getCurrent() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.ShowStory(listStories[currentIndex]))
    }

    private fun getRandom() = viewModelScope.launch {
        mainEventChannel.send(MainEvent.ShowProgressBar(true))
        try {
            val response = api.getRandom()
            if (response.isSuccessful) {
                response.body()?.let {
                    setStory(it)
                    listStories.add(it)
                    currentIndex++
                }
            } else {
                mainEventChannel.send(MainEvent.ShowError(true))
            }
        } catch (e: Exception) {
            mainEventChannel.send(MainEvent.ShowError(true))
        }
    }

    private fun setStory(item: StoryItem) = viewModelScope.launch {
        mainEventChannel.send(MainEvent.ShowStory(item))
    }

    private fun setStateBackButton(position: Int) = viewModelScope.launch {
        if (position <= 0) {
            mainEventChannel.send(MainEvent.EnableBackButton(false))
        } else {
            mainEventChannel.send(MainEvent.EnableBackButton(true))
        }
    }

    sealed class MainEvent {
        data class ShowProgressBar(val visible: Boolean) : MainEvent()
        data class ShowError(val visible: Boolean) : MainEvent()
        data class ShowStory(val story: StoryItem) : MainEvent()
        data class EnableBackButton(val isOn: Boolean) : MainEvent()
    }

}