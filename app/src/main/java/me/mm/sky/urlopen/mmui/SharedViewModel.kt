package me.mm.sky.urlopen.mmui
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel : ViewModel() {
    private val _nfcUrl = MutableStateFlow("")
    val nfcUrl: StateFlow<String> = _nfcUrl

    fun updateNfcUrl(url: String) {
        _nfcUrl.value = url
    }
}