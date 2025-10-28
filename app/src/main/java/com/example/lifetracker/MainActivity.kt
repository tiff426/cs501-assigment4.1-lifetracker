package com.example.lifetracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifetracker.ui.theme.LifeTrackerTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.launch
import java.text.DateFormat


// custom data type for life cycle event so can store event + timestamp
data class LifecycleEvent(val name:String, val timestamp: String, val color: Color)
class MainActivity : ComponentActivity() {

    private val TAG = "LifeTracker"
    private val viewModel: MyViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
//            LifeTrackerTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
            LifeTracker(viewModel=viewModel)
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
    LifeTrackerTheme {
        Greeting("Android")
    }
}

//
//• Use ViewModel to store a log of lifecycle events (e.g., onCreate, onStart, etc.).
//• Capture and record these events using LifecycleObserver or lifecycle callbacks.
//• Show real-time updates in the UI using a LazyColumn.
//• Persist logs in memory during configuration changes.
//• Include a timestamp and status color code for each event.
//• Show a snackbar notification on a lifecycle transition (configurable via settings).

@Composable
fun LifeTracker(lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current, viewModel: MyViewModel = MyViewModel()) {
    val TAG = "LifeTracker"
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var snackbarSet by rememberSaveable { mutableStateOf(true) }

//    val logList = viewModel.readLogList

    DisposableEffect(lifecycleOwner) {
        // Create an observer that logs lifecycle events.
        val observer = LifecycleEventObserver { _, event ->
            // We can log the event that the Composable's observer receives.
            // This shows how a Composable can react to the Activity's state.
            Log.d(TAG, "[Composable] Observed Event: ${event.name}")
            // also add it??
            // set up color
            var color = Color.White
            if (event.name == "ON_CREATE") {
                color = Color.Green
            } else if (event.name == "ON_START") {
                color = Color.Yellow
            } else if (event.name == "ON_RESUME") {
                color = Color.Cyan
            } else if (event.name == "ON_PAUSE") {
                color = Color.Magenta
            } else if (event.name == "ON_STOP") {
                color = Color.Red
            } else if (event.name == "ON_DESTROY") {
                color = Color.Gray
            }
            val millis = System.currentTimeMillis()
            val completeEvent = LifecycleEvent(event.name, DateFormat.getInstance().format(millis), color)
            viewModel.addLogMessage(completeEvent)
            // viewModel.addLogMessage(event)

            // snackbar
            // not on button click, on this actual lifecycle transition
            if (snackbarSet) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "lifecycle transitioned",
                        actionLabel = "got it",
                        duration = SnackbarDuration.Short,
                        withDismissAction = true
                    )
                }
            } // else do nothing
        }

        // Add the observer to the lifecycle of the owner (our Activity).
        lifecycleOwner.lifecycle.addObserver(observer)

        // The `onDispose` block is crucial. It's called when the Composable
        // is removed from the composition. We must clean up our observer here
        // to prevent memory leaks.
        onDispose {
            Log.d(TAG, "[Composable] Disposing Effect. Removing observer.")
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // the UI...
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) })
    { padding ->
        Column(
            modifier = Modifier.fillMaxSize()
            .padding(padding)) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Settings")
                Button({ snackbarSet = !snackbarSet }) {
                    Text(
                        if (snackbarSet) {
                            "disable snackbar"
                        } else {
                            "enable snackbar"
                        }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.readLogList) { event ->
                    //for (event in logList) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                //                        MaterialTheme.colorScheme.surfaceVariant,
                                //                        MaterialTheme.shapes.medium
                                event.color
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color dot
                        //                Box(
                        //                    modifier = Modifier
                        //                        .size(12.dp)
                        //                        .background(event.color, MaterialTheme.shapes.small)
                        //                )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Event name
                        Text(
                            text = event.name,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        // Timestamp
                        Text(text = event.timestamp)
                    }
                }
            }
        }
    }
}

class MyViewModel: ViewModel() {
    //var logList by mutableStateOf(listOf<LifecycleEvent>()) // inside of a viewmodel that will allow it to persist
    private val editLogList = mutableStateListOf<LifecycleEvent>()
    val readLogList = editLogList

    fun addLogMessage(message: LifecycleEvent) {
        editLogList.add(message)
    }
}

// on create
// on start
// on resume
// on pause
// on stop
// on destroy