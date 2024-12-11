package com.google.firebase.functions.ktx

import androidx.test.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallOptions
import com.google.firebase.functions.HttpsCallableOptions
import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.SSETaskListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class StreamTest {

  private lateinit var firebaseFunctions: FirebaseFunctions
  private lateinit var callableReference: HttpsCallableReference
  private lateinit var mockListener: SSETaskListener
  private lateinit var dummyOptions: HttpsCallOptions
  private lateinit var httpsCallableOptions: HttpsCallableOptions

  @Before
  fun setUp() {
    // Initialize Firebase app for testing
    FirebaseApp.initializeApp(
      androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
    )
    mockListener = mock(SSETaskListener::class.java)
    dummyOptions = HttpsCallOptions()
    httpsCallableOptions = HttpsCallableOptions.Builder().setLimitedUseAppCheckTokens(false).build()
    // Initialize Firebase Functions
    firebaseFunctions = FirebaseFunctions.getInstance()

    // Set up a callable reference
    callableReference = firebaseFunctions.getHttpsCallable("testFunction", httpsCallableOptions)
  }

  //
  //    @Test
  //    fun testStreamWithName() {
  //        val mockFunctionsClient =  spy(firebaseFunctions)
  //        // Arrange
  //        val testData = mapOf("key" to "value")
  //        `when`(mockFunctionsClient.stream(anyString(), any(), any(),
  // any())).thenReturn(Tasks.forResult(null))
  //
  //        // Act
  //        val result: Task<Void> = callableReference.stream(testData, mockListener)
  //
  //        // Assert
  //        assertThat(result.isSuccessful).isTrue()
  //        val nameCaptor = ArgumentCaptor.forClass(String::class.java)
  //        val dataCaptor = ArgumentCaptor.forClass(Any::class.java)
  //        val optionsCaptor = ArgumentCaptor.forClass(HttpsCallOptions::class.java)
  //        val listenerCaptor = ArgumentCaptor.forClass(SSETaskListener::class.java)
  //
  //        verify(mockFunctionsClient).stream(
  //            nameCaptor.capture(),
  //            dataCaptor.capture(),
  //            optionsCaptor.capture(),
  //            listenerCaptor.capture()
  //        )
  //
  //        assertThat(nameCaptor.value).isEqualTo("testFunction")
  //        assertThat(dataCaptor.value).isEqualTo(testData)
  //        assertThat(optionsCaptor.value).isEqualTo(dummyOptions)
  //        assertThat(listenerCaptor.value).isEqualTo(mockListener)
  //    }
  //
  //    @Test
  //    fun testStreamWithNameAndNullData() {
  //        // Arrange
  //        `when`(mockFunctionsClient.stream(anyString(), isNull(), any(),
  // any())).thenReturn(Tasks.forResult(null))
  //
  //        // Act
  //        val result: Task<Void> = testReference.stream(mockListener)
  //
  //        // Assert
  //        assertThat(result.isSuccessful).isTrue()
  //        verify(mockFunctionsClient).stream(eq("testFunction"), isNull(), eq(dummyOptions),
  // eq(mockListener))
  //    }
  //
  //    @Test
  //    fun testStreamWithURL() {
  //        // Arrange
  //        val testUrl = URL("https://test.url/endpoint")
  //        val testData = mapOf("key" to "value")
  //
  //        // Create a reference using URL
  //        val urlReference = HttpsCallableReference(
  //            functionsClient = mockFunctionsClient,
  //            url = testUrl,
  //            options = dummyOptions
  //        )
  //
  //        `when`(mockFunctionsClient.stream(any(URL::class.java), any(), any(),
  // any())).thenReturn(Tasks.forResult(null))
  //
  //        // Act
  //        val result: Task<Void> = urlReference.stream(testData, mockListener)
  //
  //        // Assert
  //        assertThat(result.isSuccessful).isTrue()
  //        verify(mockFunctionsClient).stream(eq(testUrl), eq(testData), eq(dummyOptions),
  // eq(mockListener))
  //    }
  //
  //    @Test
  //    fun testStreamWithURLAndNullData() {
  //        // Arrange
  //        val testUrl = URL("https://test.url/endpoint")
  //
  //        // Create a reference using URL
  //        val urlReference = HttpsCallableReference(
  //            functionsClient = mockFunctionsClient,
  //            url = testUrl,
  //            options = dummyOptions
  //        )
  //
  //        `when`(mockFunctionsClient.stream(any(URL::class.java), isNull(), any(),
  // any())).thenReturn(Tasks.forResult(null))
  //
  //        // Act
  //        val result: Task<Void> = urlReference.stream(mockListener)
  //
  //        // Assert
  //        assertThat(result.isSuccessful).isTrue()
  //        verify(mockFunctionsClient).stream(eq(testUrl), isNull(), eq(dummyOptions),
  // eq(mockListener))
  //    }
  //
  //    @Test
  //    fun testStreamHandlesFailure() {
  //        // Arrange
  //        val testData = mapOf("key" to "value")
  //        val exception = Exception("Stream failed")
  //        `when`(mockFunctionsClient.stream(anyString(), any(), any(),
  // any())).thenReturn(Tasks.forException(exception))
  //
  //        // Act
  //        val result: Task<Void> = testReference.stream(testData, mockListener)
  //
  //        // Assert
  //        assertThat(result.isSuccessful).isFalse()
  //        assertThat(result.exception).isEqualTo(exception)
  //        verify(mockFunctionsClient).stream(eq("testFunction"), eq(testData), eq(dummyOptions),
  // eq(mockListener))
  //    }

  @Test
  fun streamEmitsEvents() {
    // Arrange
    val testData = mapOf("key" to "value")
    val receivedEvents = mutableListOf<String>()
    val receivedErrors = mutableListOf<String>()

    val listener =
      object : SSETaskListener {
        override fun onEvent(event: Any) {
          receivedEvents.add(event.toString())
        }

        override fun onError(event: Any) {
          receivedErrors.add(event.toString())
        }

        override fun onComplete(event: Any) {
          receivedEvents.add("Complete: $event")
        }
      }

    // Act
    val task = callableReference.stream(testData, listener)
    task.addOnCompleteListener { assertThat(it.isSuccessful).isTrue() }

    // Assert
    assertThat(receivedEvents).containsAtLeast("Event 1", "Event 2", "Complete: Stream complete")
    assertThat(receivedErrors).isEmpty()
  }

  @Test
  fun streamHandlesErrorEvent() {
    // Arrange
    val testData = mapOf("key" to "value")
    val receivedEvents = mutableListOf<String>()
    val receivedErrors = mutableListOf<String>()

    val listener =
      object : SSETaskListener {
        override fun onEvent(event: Any) {
          receivedEvents.add(event.toString())
        }

        override fun onError(event: Any) {
          receivedErrors.add(event.toString())
        }

        override fun onComplete(event: Any) {
          receivedEvents.add("Complete: $event")
        }
      }

    // Act
    val task = callableReference.stream(testData, listener)
    task.addOnCompleteListener { assertThat(it.isSuccessful).isTrue() }

    // Assert
    assertThat(receivedEvents).contains("Event 1")
    assertThat(receivedErrors).contains("Error event")
    assertThat(receivedEvents).contains("Complete: Stream complete")
  }

  @Test
  fun streamWithNoEvents() {
    // Arrange
    val testData = mapOf("key" to "value")
    val receivedEvents = mutableListOf<String>()
    val receivedErrors = mutableListOf<String>()
    val mockClient = mock(OkHttpClient::class.java)
    val mockCall = mock(Call::class.java)
    val mockBody = ResponseBody.create(MediaType.parse("text/event-stream"), "data: Event 1\n\n")

    val mockResponse =
      Response.Builder()
        .request(Request.Builder().url("http://localhost").build())
        .protocol(Protocol.HTTP_1_1)
        .body(mockBody)
        .code(200)
        .message("OK")
        .build()

    `when`(mockClient.newCall(any())).thenReturn(mockCall)

    `when`(mockCall.enqueue(any())).thenAnswer {
      val callback = it.getArgument<Callback>(0)
      callback.onResponse(mockCall, mockResponse)
    }
    val listener =
      object : SSETaskListener {
        override fun onEvent(event: Any) {
          receivedEvents.add(event.toString())
        }

        override fun onError(event: Any) {
          receivedErrors.add(event.toString())
        }

        override fun onComplete(event: Any) {
          receivedEvents.add("Complete: $event")
        }
      }

    // Act
    val task = callableReference.stream(testData, listener)
    task.addOnCompleteListener { assertThat(it.isSuccessful).isTrue() }

    // Assert
    assertThat(receivedEvents).contains("Complete: Stream complete")
    assertThat(receivedErrors).isEmpty()
    assertThat(receivedEvents).doesNotContain("Event 1")
  }
}
