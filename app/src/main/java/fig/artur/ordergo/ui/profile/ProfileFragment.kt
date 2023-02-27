package fig.artur.ordergo.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fig.artur.ordergo.databinding.FragmentProfileBinding
import fig.artur.ordergo.logreg.LoginActivity
import fig.artur.ordergo.BuildConfig
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(BuildConfig.DBK)

        if(auth.currentUser != null) {
            val usersRef = database.getReference("users")
            val currentUserRef = usersRef.child(auth.currentUser!!.uid)

            currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val email = dataSnapshot.child("email").getValue(String::class.java)
                    val username = dataSnapshot.child("username").getValue(String::class.java)
                    val phone = dataSnapshot.child("phone").getValue(String::class.java)

                    username_profile_frag.text = "$username"
                    email_profile_frag.text = "$email"
                    phone_profile_frag.text = "$phone"

                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "ERROR! Something went wrong, try again.", Toast.LENGTH_SHORT).show()
                }
            })
        }else{
            Toast.makeText(context, "ERROR! Something went wrong, try again.", Toast.LENGTH_SHORT).show()
        }


        val btn : Button = binding.btnLogout
        btn.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)}

        return root
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}