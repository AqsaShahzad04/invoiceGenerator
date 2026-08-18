package com.learner.invoicegenerator.ui

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.repository.ClientRepository
import com.learner.invoicegenerator.data.repository.ItemRepository
import com.learner.invoicegenerator.data.repository.WorkspaceRepository
import com.learner.invoicegenerator.databinding.FragmentHomeBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModelFactory
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkSpaceViewModelFactory
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModelFactory
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeFragment: Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val database = DatabaseProvider.getDatabase(requireContext())
        
        val clientDao = database.clientDao()
        val clientRepository = ClientRepository(clientDao)
        val clientViewModelFactory = ClientViewModelFactory(clientRepository)
        val clientViewModel = ViewModelProvider(this, clientViewModelFactory).get(ClientViewModel::class.java)

        val itemDao = database.itemDao()
        val itemRepository = ItemRepository(itemDao)
        val itemViewModelFactory = ItemViewModelFactory(itemRepository)
        val itemViewModel = ViewModelProvider(this, itemViewModelFactory).get(ItemViewModel::class.java)

        val workspaceDao = database.workspaceDao()
        val workspaceRepository = WorkspaceRepository(workspaceDao)
        val workspaceViewModelFactory = WorkSpaceViewModelFactory(workspaceRepository)
        val workspaceViewModel = ViewModelProvider(this, workspaceViewModelFactory).get(WorkspaceViewModel::class.java)

        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId()

        val activeWorkspaceId = sessionManager.getActiveWorkspaceId()
        clientViewModel.setWorkspaceId(activeWorkspaceId)
        itemViewModel.setWorkspaceId(activeWorkspaceId)

        parentFragmentManager.setFragmentResultListener("workspace_changed", viewLifecycleOwner) { _, bundle ->
            val newId = bundle.getInt("workspace_id")
            clientViewModel.setWorkspaceId(newId)
            itemViewModel.setWorkspaceId(newId)
        }

        binding.AddBtn.setOnClickListener {
            AddClientBottomSheet().show(parentFragmentManager, "Add Client")
        }

        binding.Add4Btn.setOnClickListener {
            findNavController().navigate(R.id.itemsFragment)
        }

        binding.setupBtn.setOnClickListener {
            BottomSheetSetupWorkspace().show(parentFragmentManager, "Setup Workspace")
        }

        binding.card1.setOnClickListener {
            BottomSheetSwitchWorkspace().show(parentFragmentManager, "Switch Workspace")
        }

        binding.chooseBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Currency selection coming soon", Toast.LENGTH_SHORT).show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                clientViewModel.allClients, 
                itemViewModel.allItems,
                workspaceViewModel.getWorkspacesByUserId(userId)
            ) { clients, items, workspaces ->
                val activeWorkspace = workspaces.find { it.id == sessionManager.getActiveWorkspaceId() }
                Triple(clients.size, items.size, activeWorkspace)
            }.collect { (clientsCount, itemsCount, activeWorkspace) ->
                updateUI(activeWorkspace, clientsCount, itemsCount)
            }
        }
    }

    private fun updateUI(activeWorkspace: com.learner.invoicegenerator.data.local.entity.Workspace?, clientsCount: Int, itemsCount: Int) {
        val step1Done = activeWorkspace != null
        val step2Done = clientsCount > 0
        val step3Done = false // Disabled until Currency feature is built
        val step4Done = itemsCount > 0
        
        // Update Header
        activeWorkspace?.let {
            binding.myBusiness.text = it.name
            binding.avatar.text = com.learner.invoicegenerator.utils.AvatarUtils.getLetter(it.name)
            binding.avatar.background.setTint(android.graphics.Color.parseColor(com.learner.invoicegenerator.utils.AvatarUtils.getColor(it.name)))
        }

        var completedCount = 0
        if (step1Done) completedCount++
        if (step2Done) completedCount++
        if (step3Done) completedCount++
        if (step4Done) completedCount++

        // Update Step 1 (Workspace)
        updateStepUI(step1Done, binding.homeNumCircle, binding.homeNumCircle1done, binding.nameWorkspace, binding.setupBtn, binding.cardone)
        
        // Update Step 2 (Clients)
        updateStepUI(step2Done, binding.homeNumCircle2, binding.homeNumCircle2done, binding.addFirstCient, binding.AddBtn, binding.cardtwo)
        
        // Update Step 3 (Currency)
        updateStepUI(step3Done, binding.homeNumCircle3, binding.homeNumCircle3done, binding.setCurrency, binding.chooseBtn, binding.cardThree)
        
        // Update Step 4 (Catalogue)
        updateStepUI(step4Done, binding.homeNumCircle4, binding.homeNumCircle4done, binding.addItems, binding.Add4Btn, binding.cardFour)

        // Progress Bar Update
        binding.progressText.text = "$completedCount/4"
        val params = binding.progressbarFilled.layoutParams as ConstraintLayout.LayoutParams
        params.matchConstraintPercentWidth = completedCount / 4f
        binding.progressbarFilled.layoutParams = params
    }

    private fun updateStepUI(isDone: Boolean, circle: View, doneCircle: View, text: android.widget.TextView, button: View, card: View) {
        if (isDone) {
            circle.visibility = View.INVISIBLE
            doneCircle.visibility = View.VISIBLE
            text.paintFlags = text.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            text.setTextColor(requireContext().getColor(R.color.text_grey))
            button.visibility = View.GONE
            card.setBackgroundResource(R.drawable.bg_onboarding_steps_cards_unselected)
        } else {
            circle.visibility = View.VISIBLE
            doneCircle.visibility = View.GONE
            text.paintFlags = text.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            text.setTextColor(requireContext().getColor(R.color.btn_text_dark))
            button.visibility = View.VISIBLE
            card.setBackgroundResource(R.drawable.bg_onboarding_steps_cards_selected)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

