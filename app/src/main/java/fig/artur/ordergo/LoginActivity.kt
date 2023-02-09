package fig.artur.ordergo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        //TODO("Change e-mail et, user can log in with phone, username or email")

        tv_register.setOnClickListener{
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        tv_forgotten_password.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RecoverPasswordActivity::class.java))
        }

        btn_login.setOnClickListener{
            when {
                TextUtils.isEmpty(et_email_login.text.toString().trim{ it <= ' ' }) -> {
                    emptyField()
                }

                TextUtils.isEmpty(et_password_login.text.toString().trim{ it <= ' ' }) -> {
                    emptyField()
                }
                else -> {
                    val email: String = et_email_login.text.toString().trim {it <= ' '}
                    val password: String = et_password_login.text.toString().trim {it <= ' '}

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->

                            if (task.isSuccessful) {
                                firebaseUser = task.result!!.user!!
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Successful logged in!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent =
                                    Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("user_id",
                                    FirebaseAuth.getInstance().currentUser!!.uid)
                                intent.putExtra("email_id", email)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "ERROR! LOGIN FAILED. PLEASE TRY AGAIN LATER.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }
    }

    private fun emptyField(){
        Toast.makeText(
            this@LoginActivity,
            "Please complete all required fields!",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onStart(){
        super.onStart()
        if(auth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}