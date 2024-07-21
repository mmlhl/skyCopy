package me.mm.sky.urlopen

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.mm.sky.urlopen.database.AppDatabase
import me.mm.sky.urlopen.database.NfcCard
import me.mm.sky.urlopen.mmui.ListCard
import me.mm.sky.urlopen.mmui.NfcCardViewModel
import me.mm.sky.urlopen.mmui.NfcCardViewModelFactory
import me.mm.sky.urlopen.mmui.SharedViewModel
import me.mm.sky.urlopen.ui.theme.链接打开器Theme

class MainActivity : ComponentActivity() {
    private val dataBase: AppDatabase by lazy { AppDatabase.getInstance(this) }
    private var nfcAdapter: NfcAdapter? = null
    private val sharedViewModel: SharedViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("StateFlowValueCalledInComposition", "CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = NfcCardViewModelFactory(dataBase.nfcCardDao())
        val nfcCardViewModel: NfcCardViewModel by viewModels { factory }
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        enableEdgeToEdge()
        setContent {
            链接打开器Theme {
                lifecycleScope.launch {
                    // nfcCardViewModel.deleteAllNfcCard()
                }

                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "主页")
                                Icon(imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 5.dp)
                                        .clickable {
                                            nfcCardViewModel.showAddDialog.value = true
                                        })

                            }
                        }, modifier = Modifier.fillMaxWidth())
                    }
                ) { innerPadding ->

                    ShowEditCardDialog(
                        viewModel = nfcCardViewModel,
                        sharedViewModel = sharedViewModel
                    )
                    ShowDeleteDialog(viewModel = nfcCardViewModel)
                    ListCard(
                        viewModel = nfcCardViewModel,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED,
            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                val messages = rawMessages?.map { it as NdefMessage }?.toTypedArray()
                messages?.let {
                    for (message in it) {
                        for (record in message.records) {
                            val payload = String(record.payload)
                            if (payload.contains("sky")) {
                                val index = payload.indexOf("sky")
                                sharedViewModel.updateNfcUrl("https://${payload.substring(index)}")
                            }
                        }
                    }
                } ?: run {
                    val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                    tag?.let {

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)

        val intentFiltersArray = arrayOf(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        )

        // 允许所有技术类型的 NFC 标签
        val techListsArray = arrayOf(arrayOf<String>())

        nfcAdapter?.enableForegroundDispatch(
            this,
            pendingIntent,
            intentFiltersArray,
            techListsArray
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

}


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ShowEditCardDialog(viewModel: NfcCardViewModel, sharedViewModel: SharedViewModel) {
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val nfcUrl by sharedViewModel.nfcUrl.collectAsState()

    if (showAddDialog || showEditDialog) {
        var name by remember { mutableStateOf("") }
        var url by remember { mutableStateOf("") }  // Initialize with nfcUrl
        var description by remember { mutableStateOf("") }
        var packageName by remember { mutableStateOf("") }
        if (showEditDialog) {
            name = viewModel.currentAddCard.value.name
            url = viewModel.currentAddCard.value.url
            description = viewModel.currentAddCard.value.description
            packageName = viewModel.currentAddCard.value.packageName
        }
        if (nfcUrl.isNotEmpty()) {
            url = nfcUrl
        }
        AlertDialog(
            title = {
                Text(text = "${if (showAddDialog) "添加" else "编辑"}卡片")

                    },
            dismissButton = {
                Button(onClick = {
                    sharedViewModel.updateNfcUrl("")
                    viewModel.showAddDialog.value = false
                    viewModel.showEditDialog.value = false
                }) {
                    Text(text = "取消")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val editRowModifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp)
                    Row(
                        modifier = editRowModifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "名称")
                        OutlinedTextField(value = name, onValueChange = { name = it },placeholder = {
                            Text(text = "请给卡片起一个名字", color = Color.Gray)
                        })
                    }
                    Row(
                        modifier = editRowModifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "描述")
                        OutlinedTextField(value = description, onValueChange = { description = it }, placeholder = {
                            Text(text = "请输入对卡片的描述", color = Color.Gray)
                        })
                    }
                    Row(
                        modifier = editRowModifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "链接")
                        OutlinedTextField(value = url, onValueChange = { url = it },placeholder = {
                            Text(text = "扫描NFC徽章自动填充/更新", color = Color.Gray)
                        })
                    }
                    Row(
                        modifier = editRowModifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "包名")
                        OutlinedTextField(value = packageName, onValueChange = { packageName = it },placeholder = {
                            Text(text = "应用包名，可不填", color = Color.Gray)
                        })
                    }
                }
            },
            onDismissRequest = {
                sharedViewModel.updateNfcUrl("")
                viewModel.showAddDialog.value = false
                viewModel.showEditDialog.value = false
            },
            confirmButton = {
                Button(onClick = {
                    if (showAddDialog) {
                        viewModel.insertNfcCard(
                            NfcCard(
                                name = name,
                                description = description,
                                url = url,
                                packageName = packageName,
                                tags = listOf()
                            )
                        )
                    }
                    if (showEditDialog) {
                        viewModel.updateNfcCard(
                            NfcCard(
                                id = viewModel.currentAddCard.value.id,
                                name = name,
                                description = description,
                                url = url,
                                packageName = packageName,
                                tags = listOf()
                            )
                        )
                    }
                    sharedViewModel.updateNfcUrl("")
                    viewModel.showAddDialog.value = false
                    viewModel.showEditDialog.value = false
                }) {
                    Text(text = "确定")
                }
            })
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ShowDeleteDialog(viewModel: NfcCardViewModel) {
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.showDeleteDialog.value = false
            },
            title = { Text(text = "删除卡片") },
            text = {
                Text(text = "确定要删除${viewModel.currentAddCard.value.name}卡片吗？")
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.showDeleteDialog.value = false
                }) {
                    Text(text = "取消")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.showDeleteDialog.value = false
                    viewModel.deleteCurrentNfcCard()
                }) {
                    Text(text = "确定")
                }
            }
        )
    }
}
