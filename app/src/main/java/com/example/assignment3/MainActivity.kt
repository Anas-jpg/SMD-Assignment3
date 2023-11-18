package com.example.assignment3

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.assignment3.ui.theme.Assignment3Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Assignment3Theme {
                // Use the ContactPicker here
                ContactPicker(
                    onContactPicked = { contact ->
                        // Handle the picked contact here.
                        Toast.makeText(
                            this,
                            "Picked contact: ${contact.name}, ${contact.phoneNumber}",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onImportAllContacts = {
                        // Handle the logic to import all contacts here.
                        // You can use the getContacts function or any other logic you prefer.
                    //    val allContacts = getContacts(this)
                        // Do something with allContacts, e.g., save them to your app's database.
                    }
                )

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Assignment3Theme {
        Greeting("Android")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPicker(onContactPicked: (Contact) -> Unit, onImportAllContacts: () -> Unit) {
    val context = LocalContext.current
    val contactList = remember { mutableStateListOf<Contact>() }

    LaunchedEffect(Unit) {
        // Launch a coroutine scope to call the suspend function
        withContext(Dispatchers.IO) {
            contactList.addAll(getContacts(context))
        }
    }

    Column {
        LazyColumn {
            items(contactList) { contact ->
                Text(
                    text = "${contact.name}, ${contact.phoneNumber}",
                    modifier = Modifier.clickable { onContactPicked(contact) }
                )
            }
        }

        Button(onClick = { onImportAllContacts() }) {
            Text("Import All Contacts")
        }
    }
}

data class Contact(val name: String, val phoneNumber: String)

@SuppressLint("Range")
suspend fun getContacts(context: Context): List<Contact> {
    val contacts = mutableListOf<Contact>()

    withContext(Dispatchers.IO) {
        val resolver: ContentResolver = context.contentResolver
        val cursor: Cursor? = resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val id: String = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                    val name: String = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                    if (it.getInt(it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        val pCur: Cursor? = resolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id), null
                        )
                        pCur?.use {
                            while (pCur.moveToNext()) {
                                val phoneNo: String = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                contacts.add(Contact(name, phoneNo))
                            }
                        }
                    }
                } while (it.moveToNext())
            }
        }
    }

    return contacts
}
