package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Item
import com.learner.invoicegenerator.data.repository.ItemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ItemViewModel(
    private val repository: ItemRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _itemState = MutableLiveData<ItemState>(ItemState.Idle)
    val itemState: MutableLiveData<ItemState> get() = _itemState

    fun resetState() {
        _itemState.value = ItemState.Idle
    }

    private val searchQuery = MutableStateFlow("")
    val currentQuery: Flow<String> get() = searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val allItems: Flow<List<Item>> = combine(sessionManager.activeWorkspaceId, searchQuery) { id, query ->
        Pair(id, query)
    }.flatMapLatest { (id, query) ->
        if (query.isEmpty()) {
            repository.getAllItems(id)
        } else {
            repository.searchItems(query, id)
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun addItems(item: Item) {
        viewModelScope.launch {
            _itemState.value = ItemState.Loading
            try {
                repository.insertItem(item)
                _itemState.value = ItemState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _itemState.value = ItemState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateItem(item: Item) {
        viewModelScope.launch {
            _itemState.value = ItemState.Loading
            try {
                repository.updateItem(item)
                _itemState.value = ItemState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _itemState.value = ItemState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            try {
                repository.deleteItem(item)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getItemById(id: Int): Item? {
        return repository.getItemById(id)
    }
}