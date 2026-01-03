package com.kaarigar.ui.gemini

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kaarigar.data.Resource
import com.kaarigar.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var viewModel: GeminiViewModel
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory =
                com.kaarigar.ui.ViewModelFactory(
                        geminiRepository = com.kaarigar.data.repository.GeminiRepository()
                )
        viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[GeminiViewModel::class.java]

        chatAdapter = ChatAdapter()
        binding.rvChatHistory.adapter = chatAdapter
        binding.rvChatHistory.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(context)

        binding.btnSend.setOnClickListener {
            val prompt = binding.etPrompt.text.toString().trim()
            if (prompt.isNotEmpty()) {
                // Add User Message
                chatAdapter.addMessage(Message(prompt, true))
                binding.rvChatHistory.scrollToPosition(chatAdapter.itemCount - 1)

                viewModel.askGemini(prompt)
                binding.etPrompt.text.clear()
            } else {
                Toast.makeText(requireContext(), "Please enter a prompt", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.geminiResponse.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Resource.Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                Resource.Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data?.let { response ->
                        chatAdapter.addMessage(Message(response, false))
                        binding.rvChatHistory.scrollToPosition(chatAdapter.itemCount - 1)
                    }
                }
                Resource.Status.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
