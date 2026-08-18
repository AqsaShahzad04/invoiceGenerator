package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.local.entity.Item
import com.learner.invoicegenerator.data.repository.ItemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ItemViewModel(private val repository: ItemRepository) : ViewModel() {

    private val _itemState = MutableLiveData<ItemState>()
    val itemState: MutableLiveData<ItemState> get() = _itemState

    private val workspaceIdFlow = MutableStateFlow(1)
    
    private val searchQuery = MutableStateFlow("")
    val currentQuery: Flow<String> get() = searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val allItems: Flow<List<Item>> = combine(workspaceIdFlow, searchQuery) { id, query ->
        Pair(id, query)
    }.flatMapLatest { (id, query) ->
        if (query.isEmpty()) {
            repository.getAllItems(id)
        } else {
            repository.searchItems(query, id)
        }
    }

    fun setWorkspaceId(id: Int) {
        workspaceIdFlow.value = id
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun addItems(item: Item) {
        viewModelScope.launch {
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
                _itemState.value = ItemState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _itemState.value = ItemState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getItemById(item: Item) {
        viewModelScope.launch {
            try {
                repository.getItemById(item.id)
                _itemState.value = ItemState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _itemState.value = ItemState.Error(e.message ?: "Unknown error")
            }
        }
    }
}