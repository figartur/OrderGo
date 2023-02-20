package fig.artur.ordergo.logreg

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import fig.artur.ordergo.R
import kotlinx.android.synthetic.main.activity_recover_password.*

class RecoverPasswordActivity : AppCompatActivity() {

    private lateinit var etEmailRecover: EditText
    private lateinit var btnRecover: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        tv_back.setOnClickListener {
            onBackPressed()
        }

        etEmailRecover = findViewById(R.id.et_email_recover)
        btnRecover = findViewById(R.id.btn_recover)

        auth = FirebaseAuth.getInstance()

        btnRecover.setOnClickListener {
            val emRecover = etEmailRecover.text.toString()
            auth.sendPasswordResetEmail(emRecover)
                .addOnSuccessListener {
                    Toast.makeText(this, "Please, check your E-mail.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                }

        }

    }
}