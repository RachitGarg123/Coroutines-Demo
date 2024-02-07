package com.example.demoproject.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import kotlin.coroutines.suspendCoroutine

import java.util.*

class MainViewModel: ViewModel() {

    fun hitApi() {
        viewModelScope.launch {
            waitForIt()
        }
    }

    private suspend fun waitForIt() {
        var a = 10
        Log.i("1 Value of a", "a---> $a")
        coroutineScope { // used for structured concurrency
            delay(2000)
            a += 20
            Log.i("2 Value of a", "a---> $a")
        }
        coroutineScope {
            delay(2000)
            a += 40
            Log.i("3 Value of a", "a---> $a")
        }
        viewModelScope.launch {// viewModelScope follows the lifecycle of viewModel
            launch {// retruns a job instance to control it's lifecycle
                delay(2000)
                a += 60
                Log.i("4 Value of a", "a---> $a")
            }
            launch {
                delay(2000)
                a += 80
                Log.i("5 Value of a", "a---> $a")
            }.join()
            async {// returns a deferred type object which we can fetch by adding .await()
                delay(2000)
                a += 100
                Log.i("6 Value of a", "a---> $a")
            }
            async {
                delay(2000)
                a += 120
                Log.i("7 Value of a", "a---> $a")
            }.await()
        }
        Log.i("8 Value of a", "a---> $a")
    }
}

/*-------output of above code will be ----------------
    a---> 10
    a---> 30
    a---> 70
    a---> 70
    a---> 130
    a---> 210
    a---> 310
    a---> 430*/

/*
 -- Continuation Passing Style (CPS) is used under the hood in the suspend functions to pass the data which is needed by the coroutine to perform its work.
    All the data needed is passed using an object of the continuation which is an anonymous class
 -- resumeWith {normal resume after suspension of the thread}
 -- resumeWithException {to catch exception}
 -- label to identify the current state of the thread {0 initially}
 -- COROUTINE_SUSPENDED {returns this to identify whether coroutine is in suspended state currently} and free the thread immediately
 -- job.cancel() works fine only when coroutine is cooperative otherwise coroutine is in cancelling state ad only gets cancelled when it hits the first suspension point
 -- invokeOnCancellation or withContext(NonCancellable) is used for doing things when job.cancel() hits
 -- yield() is used when we need to de-prioritize our currently running work {what it does is it immediately suspends and resume the current thread}
 -- ensureActive() is similar to isActive but code-wise looks better as it maintains the indentation
 -- yield() and ensureActive looks similar but yield() should be used in between CPU intensive tasks and ensureActive() should be used else where as it is lighter than yield() function
 -- various scopes are used such as GlobalScope{attached to application lifecycle and should be avoided as much as possible to avoid memory leaks},
    viewModelScope {attached to viewModel lifecycle} and lifeCycleScope {attached to activity lifecycle}
 -- we should use viewModelScope as much as possible to start a coroutine in the viewModel and change the dispatcher in repo function to make that function main safe
 -- we should pass the dispatcher in the parameter of repo to follow good practices
 -- coroutineScope should be used to follow structured concurrency in the coroutines
 -- Supervisor Job should be used when we want the sibling coroutines to be independent of each other and failure of one child coroutine does not fail the entire parent coroutine
 -- launch is known as fire and forget because it does not return anything whereas async returns a value of deferred type which we can use using .await() function
 -- We can start a job lazily using (start = CoroutineStart.LAZY) in the Coroutine Builder and then using job.start() to start the job
 -- We can use CoroutineName for naming a coroutine for debugging purposes
 -- We can create our own custom CoroutineContext
        for ex -> CoroutineScope(CoroutineName("BackgroundThread") + Dispatcher.IO + CoroutineExceptionHandler {coroutineContext, throwable -> })
 -- Since DEFAULT and IO dispatchers lie in the same thread pool it is more efficient to switch between these dispatchers
 -- Default Thread used be used for CPU intensive tasks
 -- IO thread should be used for Input output tasks such as reading or writing in database, making a network request e.t.c
 -- Main thread should be used only for interacting with UI and calling other functions
 -- DEFAULT Dispatcher means it will attach one thread to each CPU or micro-controller present in the system
        for ex -> Octa-Core device will have 8 parallel DEFAULT threads one for each core to perform CPU intensive tasks
                  But it has a minimum of 2 threads present even in case of a single core processor
 -- IO thread normally has 64 threads present in it to perform multi-tasks
 -- We can also limit the no of parallel threads by using Dispatchers.{$Thread_Pool_Name}.limitedParallelism(${number})
 -- There is one Dispatcher which should be avoided in most cases Dispatcher.UNCONFINED {it does not guarantee that the coroutine will resume on the same thread in which it is started}
 -- unlike isActive ensureActive() throws a cancellationException is a job is not active we can catch that exception in the try catch block
*/