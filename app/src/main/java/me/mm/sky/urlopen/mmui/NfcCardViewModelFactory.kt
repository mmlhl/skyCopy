package me.mm.sky.urlopen.mmui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.mm.sky.urlopen.database.NfcCardDao

class NfcCardViewModelFactory(private val nfcCardDao: NfcCardDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NfcCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NfcCardViewModel(nfcCardDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
