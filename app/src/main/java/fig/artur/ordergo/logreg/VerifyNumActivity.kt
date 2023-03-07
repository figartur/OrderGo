package fig.artur.ordergo.logreg

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import fig.artur.ordergo.BuildConfig
import fig.artur.ordergo.MainActivity
import fig.artur.ordergo.R
import kotlinx.android.synthetic.main.activity_verify_num.*
import java.util.concurrent.TimeUnit

class VerifyNumActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var OTP : String
    private lateinit var resendToken : PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber : String
    private lateinit var username : String
    private lateinit var password : String
    private lateinit var email : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_num)

        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
        phoneNumber = intent.getStringExtra("phoneNumber")!!
        username = intent.getStringExtra("username")!!
        password = intent.getStringExtra("password")!!
        email = intent.getStringExtra("email")!!


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(BuildConfig.DBK)

        addTextChangeListener()
        resendOTPvVisibility()

        tv_resend_verification_code.setOnClickListener {
            resendVerificationCode()
            resendOTPvVisibility()
        }

        btn_continue.setOnClickListener{
            val typedOTP = (et_verify_code1.text.toString() + et_verify_code2.text.toString() + et_verify_code3.text.toString()
                    + et_verify_code4.text.toString() + et_verify_code5.text.toString() + et_verify_code6.text.toString())

            if(typedOTP.isNotEmpty()){
                if(typedOTP.length == 6){
                    val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        OTP, typedOTP
                    )
                    signInWithPhoneAuthCredential(credential)
                }else{
                    Toast.makeText(this, "Enter the correct verification code.", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Enter the verification code.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resendOTPvVisibility(){
        et_verify_code1.setText("")
        et_verify_code2.setText("")
        et_verify_code3.setText("")
        et_verify_code4.setText("")
        et_verify_code5.setText("")
        et_verify_code6.setText("")
        tv_resend_verification_code.visibility = View.INVISIBLE
        tv_resend_verification_code.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            tv_resend_verification_code.visibility = View.VISIBLE
            tv_resend_verification_code.isEnabled = true
        }, 60000)
    }

    private fun resendVerificationCode(){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            }
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            OTP = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                val delUserNum = FirebaseAuth.getInstance().currentUser
                if (task.isSuccessful) {
                    Toast.makeText(this, "Authenticate Successfully", Toast.LENGTH_SHORT).show()

                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{ it ->
                        if(it.isSuccessful){
                            val databaseRef = database.reference.child("users").child(auth.currentUser!!.uid)
                            val users : Users = Users(username=username, email=email,  phone=phoneNumber, profilepic=null, uid=auth.currentUser!!.uid)

                            databaseRef.setValue(users).addOnCompleteListener{
                                if(it.isSuccessful){
                                    startActivity(Intent(this@VerifyNumActivity, MainActivity::class.java))
                                }else{
                                    val delUser = FirebaseAuth.getInstance().currentUser
                                    delUserNum?.delete()?.addOnCompleteListener { task ->
                                        if(task.isSuccessful){
                                            delUser?.delete()?.addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(this, "ERROR! Something went wrong, try again.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }else{
                            delUserNum?.delete()?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "ERROR! Something went wrong, try again.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }}
                } else {
                    Log.d(ContentValues.TAG, "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    }
                }
            }
    }

    private fun addTextChangeListener(){
        et_verify_code1.addTextChangedListener(EditTextWatcher(et_verify_code1))
        et_verify_code2.addTextChangedListener(EditTextWatcher(et_verify_code2))
        et_verify_code3.addTextChangedListener(EditTextWatcher(et_verify_code3))
        et_verify_code4.addTextChangedListener(EditTextWatcher(et_verify_code4))
        et_verify_code5.addTextChangedListener(EditTextWatcher(et_verify_code5))
        et_verify_code6.addTextChangedListener(EditTextWatcher(et_verify_code6))
    }

    inner class EditTextWatcher(private val view : View) : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val text = s.toString()
            when(view.id){
                R.id.et_verify_code1 -> if(text.length == 1) et_verify_code2.requestFocus()
                R.id.et_verify_code2 -> if(text.length == 1) et_verify_code3.requestFocus() else if(text.isEmpty()) et_verify_code1.requestFocus()
                R.id.et_verify_code3 -> if(text.length == 1) et_verify_code4.requestFocus() else if(text.isEmpty()) et_verify_code2.requestFocus()
                R.id.et_verify_code4 -> if(text.length == 1) et_verify_code5.requestFocus() else if(text.isEmpty()) et_verify_code3.requestFocus()
                R.id.et_verify_code5 -> if(text.length == 1) et_verify_code6.requestFocus() else if(text.isEmpty()) et_verify_code4.requestFocus()
                R.id.et_verify_code6 -> if(text.isEmpty()) et_verify_code5.requestFocus()
            }
        }

    }



}