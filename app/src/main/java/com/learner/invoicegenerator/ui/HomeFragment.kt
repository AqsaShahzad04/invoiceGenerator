package com.learner.invoicegenerator.ui

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.FragmentHomeBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.utils.AvatarUtils
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val clientViewModel: ClientViewModel by activityViewModels()
    private val itemViewModel: ItemViewModel by activityViewModels()
    private val workspaceViewModel: WorkspaceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId()

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
        val step3Done = false 
        val step4Done = itemsCount > 0
        
        activeWorkspace?.let {
            binding.myBusiness.text = it.name
            binding.avatar.text = AvatarUtils.getLetter(it.name)
            binding.avatar.background.setTint(android.graphics.Color.parseColor(AvatarUtils.getColor(it.name)))
        }

        var completedCount = 0
        if (step1Done) completedCount++
        if (step2Done) completedCount++
        if (step3Done) completedCount++
        if (step4Done) completedCount++

        updateStepUI(step1Done, binding.homeNumCircle, binding.homeNumCircle1done, binding.nameWorkspace, binding.setupBtn, binding.cardone)
        updateStepUI(step2Done, binding.homeNumCircle2, binding.homeNumCircle2done, binding.addFirstCient, binding.AddBtn, binding.cardtwo)
        updateStepUI(step3Done, binding.homeNumCircle3, binding.homeNumCircle3done, binding.setCurrency, binding.chooseBtn, binding.cardThree)
        updateStepUI(step4Done, binding.homeNumCircle4, binding.homeNumCircle4done, binding.addItems, binding.Add4Btn, binding.cardFour)

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