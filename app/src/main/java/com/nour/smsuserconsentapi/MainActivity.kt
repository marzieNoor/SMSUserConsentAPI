package com.nour.smsuserconsentapi

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.phone.SmsRetriever
import java.util.regex.Matcher
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private val REQ_USER_CONSENT = 200
    var smsBroadcastReceiver: SmsBroadcastReceiver? = null
    lateinit var verifyOTP: Button
    lateinit var textViewMessage: TextView
    lateinit var otpText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // find view by ids
        verifyOTP = findViewById(R.id.button);
        textViewMessage = findViewById(R.id.textViewMessage);
        otpText = findViewById(R.id.editTextOTP);

        verifyOTP.setOnClickListener {
            startSmsUserConsent();
        }
    }

    private fun startSmsUserConsent() {
        val client = SmsRetriever.getClient(this)
        //We can add sender phone number or leave it blank
        // I'm adding null here
        client.startSmsUserConsent(null).addOnSuccessListener {
            Toast.makeText(
                applicationContext,
                "On Success",
                Toast.LENGTH_LONG
            ).show()
            registerBroadcastReceiver()
        }.addOnFailureListener {
            Toast.makeText(applicationContext, "On OnFailure", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_USER_CONSENT) {
            if (resultCode == RESULT_OK && data != null) {
                //That gives all message to us.
                // We need to get the code from inside with regex
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                textViewMessage.setText(
                    String.format(
                        "%s - %s",
                        "received_message",
                        message
                    )
                )
                getOtpFromMessage(message)
            }
        }
    }

    private fun getOtpFromMessage(message: String?) {
        // This will match any 6 digit number in the message
        val pattern: Pattern = Pattern.compile("(|^)\\d{6}")
        val matcher: Matcher = pattern.matcher(message)
        if (matcher.find()) {
            otpText.setText(matcher.group(0))
        }
    }

    private fun registerBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver!!.smsBroadcastReceiverListener =
            object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent?) {
                    //  startActivityForResult(intent!!, REQ_USER_CONSENT)
                    resultLauncher.launch(intent)
                }

                override fun onFailure() {
                    Log.e("MainActivity", "registerBroadcastReceiver onFailure ")
                }
            }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter, SmsRetriever.SEND_PERMISSION, null)
    }

    override fun onStart() {
        super.onStart()
//        registerBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // There are no request codes
            val data: Intent? = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                //That gives all message to us.
                // We need to get the code from inside with regex
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                textViewMessage.setText(
                    String.format(
                        "%s - %s",
                        "received_message",
                        message
                    )
                )
                getOtpFromMessage(message)
            }

        }
}