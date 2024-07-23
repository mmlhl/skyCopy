package me.mm.sky.urlopen.mmui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.WindowInsets
import android.view.WindowMetrics
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.mm.sky.urlopen.MainActivity
import me.mm.sky.urlopen.database.NfcCard

@SuppressLint("ScheduleExactAlarm")
@Composable
fun CardItem(
    card: NfcCard,
    viewModel: NfcCardViewModel,
    onDeleted: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current as MainActivity
    Card(modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp), onClick = { /*TODO*/ }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Row {
                Column(
                    modifier = Modifier.padding(
                        start = 15.dp,
                        end = 10.dp,
                        top = 10.dp,
                        bottom = 10.dp
                    )
                ) {
                    Text(text = card.name, fontSize = 20.sp)
                    Text(text = card.description)
                }

            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(end = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .clickable {
                                viewModel.currentAddCard.value = card
                                onEdit()
                            })
                    Icon(imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .clickable {
                                viewModel.currentAddCard.value = card
                                onDeleted()
                            })
                    Button(onClick = {
                        val intent = Intent()
                        intent.setAction("android.nfc.action.NDEF_DISCOVERED")
                        val dataUri =
                            Uri.parse(card.url)
                        intent.setData(dataUri)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        if (card.packageName != "") {
                            intent.setPackage(card.packageName)
                        }
                        context.startActivity(intent)
                    }) {
                        Text(text = "启动")
                    }
                }


            }
        }
    }
}

@Composable
fun ListCard(viewModel: NfcCardViewModel, modifier: Modifier = Modifier) {

    val cardList by viewModel.cards.collectAsState()
    LazyColumn(
        modifier = modifier,
    ) {
        items(cardList) { card ->
            CardItem(card = card, viewModel = viewModel, onDeleted = {
                viewModel.showDeleteDialog.value = true
                viewModel.currentAddCard.value = card
            }, onEdit = {
                viewModel.showEditDialog.value = true
                viewModel.currentAddCard.value = card
            })
        }
    }
}