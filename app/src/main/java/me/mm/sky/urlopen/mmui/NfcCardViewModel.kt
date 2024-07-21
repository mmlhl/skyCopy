package me.mm.sky.urlopen.mmui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.mm.sky.urlopen.database.NfcCard
import me.mm.sky.urlopen.database.NfcCardDao

class NfcCardViewModel(private val nfcCardDao: NfcCardDao) : ViewModel() {

    private val _cards = MutableStateFlow<List<NfcCard>>(emptyList())
    val cards: StateFlow<List<NfcCard>> = _cards.asStateFlow()
    val showAddDialog = MutableStateFlow(false)
    val showEditDialog = MutableStateFlow(false)
    val showDeleteDialog = MutableStateFlow(false)
    val currentAddCard = MutableStateFlow<NfcCard>(
        NfcCard(
        name = "",
        description = "",
        url = "",
        packageName = "",
        tags = listOf()
    )
    )
    init {
        loadNfcCards()
    }

    fun updateEditDialog(boolean: Boolean) {
        viewModelScope.launch {
            showEditDialog.value = boolean
        }
    }
    fun updateDeleteDialog(boolean: Boolean) {
        viewModelScope.launch {
            showDeleteDialog.value = boolean
        }
    }
    fun updateAddDialog(boolean: Boolean) {
        viewModelScope.launch {
            showAddDialog.value = boolean
        }
    }
    private fun loadNfcCards() {
        viewModelScope.launch {
            nfcCardDao.getAllNfcCards().collect{ cards->
                _cards.value=cards
            }
        }
    }

    fun insertNfcCard(nfcCard: NfcCard) {
        viewModelScope.launch {
            nfcCardDao.insert(nfcCard)
            loadNfcCards()
        }


    }
    fun updateNfcCard(nfcCard: NfcCard) {
        viewModelScope.launch {
            nfcCardDao.update(nfcCard)
            loadNfcCards()
        }
    }
    fun deleteNfcCard(nfcCard: NfcCard) {
        viewModelScope.launch {
            nfcCardDao.delete(nfcCard)
            loadNfcCards()
        }
    }
    fun deleteCurrentNfcCard() {
        viewModelScope.launch {
            nfcCardDao.delete(currentAddCard.value)
            loadNfcCards()
        }
    }
    fun deleteAllNfcCard() {
        viewModelScope.launch {
            nfcCardDao.deleteAll()
            loadNfcCards()
        }
    }
}
