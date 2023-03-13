package fig.artur.ordergo.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import fig.artur.ordergo.BuildConfig
import fig.artur.ordergo.R
import fig.artur.ordergo.databinding.FragmentProfileBinding
import fig.artur.ordergo.logreg.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var storage : FirebaseStorage
    private var imageUri : Uri? = null

    private var userId = FirebaseAuth.getInstance().currentUser?.uid
    private var userRef = FirebaseDatabase.getInstance().getReference("users").child(userId!!)

    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()){
        imageUri = it

        binding.profileFragImage.setImageURI(imageUri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(BuildConfig.DBK)
        storage = FirebaseStorage.getInstance()

        if(auth.currentUser != null){
            val myRef = database.getReference("users")
            /*
            val storageReference = storage.getReferenceFromUrl(userProfile?.profilepic) */

            val query = myRef.orderByChild("uid").equalTo(userId)

            var email = ""
            var phone = ""
            var username = ""
            var profilepic = ""

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {
                        email = childSnapshot.child("email").value.toString()
                        phone = childSnapshot.child("phone").value.toString()
                        username = childSnapshot.child("username").value.toString()
                        profilepic = childSnapshot.child("profilepic").value.toString()


                        val txtusername : TextView = binding.usernameProfileFrag
                        val txtemail : TextView = binding.emailProfileFrag
                        val txtphone : TextView = binding.phoneProfileFrag

                        txtusername.text = username
                        txtemail.text = email
                        txtphone.text = phone

                        Glide.with(this@ProfileFragment)
                            .load(storageReference)
                            .placeholder(R.drawable.userimg)
                            .into(binding.profileFragImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("TAG", "Failed to read value.", error.toException())
                }
            })
        }else{
            Toast.makeText(context, "ERROR! Something went wrong, try again.", Toast.LENGTH_SHORT).show()
        }


        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)}

        binding.profileFragImage.setOnClickListener{
            selectImage.launch("image/*")
        }

        binding.btnSave.setOnClickListener{
            validateData()
        }

        return root
    }

    private fun validateData() {
        if(imageUri == null){
            Toast.makeText(context,"Please enter all fields.",Toast.LENGTH_SHORT).show()
        }else{
            uploadImage()
        }
    }

    private fun uploadImage() {
        val storageRef = FirebaseStorage.getInstance().getReference("profile")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("profile.jpg")
        
        storageRef.putFile(imageUri!!)
            .addOnSuccessListener { 
                storageRef.downloadUrl.addOnSuccessListener { 
                    storeData(it)
                }.addOnFailureListener{
                    Toast.makeText(context,it.message,Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener{
                Toast.makeText(context,it.message,Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeData(imageUrl: Uri?) {
        val updates: MutableMap<String, Any> = HashMap()
        updates["profilepic"] = "imageUrl.toString()"

        userRef.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(context,"Profile pic updated.",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context,it.message,Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}