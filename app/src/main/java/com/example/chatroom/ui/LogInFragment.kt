package com.example.chatroom.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.chatroom.R
import com.example.chatroom.databinding.FragmentLogInBinding
import com.example.chatroom.domain.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LogInFragment : Fragment() {

    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var mAuth: FirebaseAuth

    private val viewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireActivity().intent.extras?.getString("uid") != null && mAuth.currentUser != null) {
            val uid = requireActivity().intent.extras?.getString("uid")
            val userName = requireActivity().intent.extras?.getString("userName")
            uid?.let { viewModel.setUid(it) }
            userName?.let { viewModel.setName(it) }
            uid?.let { viewModel.getFCMTokenFromDatabase(it) }
            requireActivity().intent.removeExtra("uid")
            requireActivity().intent.removeExtra("userName")
            findNavController().navigate(R.id.action_logInFragment_to_chatFragment)
        } else {
            setUpViews()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpViews() {
        //hide action bar
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        //close app after push "back"
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            activity?.finish()
        }

        binding.logInScreenSignUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_logInFragment_to_signUpFragment)
        }

        binding.logInScreenLogInButton.setOnClickListener {
            val email = binding.logInScreenEmailEditText.text.toString()
            val password = binding.logInScreenPasswordEditText.text.toString()
            try {
                login(email, password)
            } catch (_: IllegalArgumentException) {
                Toast.makeText(
                    requireContext(),
                    "Enter your name, email and password!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun login(email: String, password: String) {
        //logic of log in user
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Log in success, update UI with the log-in user's information
                    viewModel.addFCMTokenToDatabase()
                    findNavController().navigate(R.id.action_logInFragment_to_listOfUsersFragment)
                } else {
                    // If log in fails, display a message to the user.
                    Toast.makeText(requireContext(), "User does not exist", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}