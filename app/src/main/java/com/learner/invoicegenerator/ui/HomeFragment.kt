package com.learner.invoicegenerator.ui

import android.content.Context
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import android.view.animation.AnimationUtils
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.FragmentHomeBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.utils.AvatarUtils
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
        val sessionManager = SessionManager.getInstance(requireContext())
        val userId = sessionManager.getUserId()
        binding.rippleDots.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        val rippleView=binding.rippleDots
        val rippleAnimation= AnimationUtils.loadAnimation(requireContext(), R.anim.ripple_anim)
        rippleView.startAnimation(rippleAnimation)


        binding.catalogueChiv.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_itemsFragment)
        }
        binding.clientChiv.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_clientFragment)
        }
        binding.AddBtn.setOnClickListener {
            AddClientBottomSheet().show(parentFragmentManager, "Add Client")
        }

        binding.Add4Btn.setOnClickListener {
            findNavController().navigate(R.id.bottomSheetAddFirstItem)
        }

        binding.setupBtn.setOnClickListener {
            BottomSheetSetupWorkspace().show(parentFragmentManager, "Setup Workspace")
        }

        binding.card1.setOnClickListener {
            BottomSheetSwitchWorkspace().show(parentFragmentManager, "Switch Workspace")
        }

        binding.chooseBtn.setOnClickListener {
            BottomSheetCurrencyPicker().show(parentFragmentManager,"currencyPickerBottomSheet")
        }

        // 1. Reactive Header Update (Dedicated Flow)
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                sessionManager.activeWorkspaceId,
                workspaceViewModel.getWorkspacesByUserId(userId)
            ) { activeId, workspaces ->
                workspaces.find { it.id == activeId }
            }.collect { activeWorkspace ->
                updateHeaderUI(activeWorkspace)
            }
        }

        // 2. Reactive Data Counts Update
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                clientViewModel.allClients,
                itemViewModel.allItems,
                sessionManager.activeWorkspaceId,
               sessionManager.currencyCode
            ) { clients, items, _ ,currencyCode->
                Triple(clients.size, items.size,currencyCode)
            }.collect { (clientsCount, itemsCount,_) ->
                updateProgressUI(clientsCount,
                    itemsCount,
                    sessionManager.hasCurrencySelectedByUser()
                )
            }
        }
    }

    private fun updateHeaderUI(activeWorkspace: com.learner.invoicegenerator.data.local.entity.Workspace?) {
        activeWorkspace?.let {
            binding.myBusiness.text = it.name
            if(!activeWorkspace.logoUri.isNullOrEmpty()){
                binding.homeFragmentLogo.visibility=View.VISIBLE
                binding.avatar.visibility=View.GONE
                binding.homeFragmentLogo.setImageURI(Uri.parse(activeWorkspace.logoUri))
            }
            else{
                binding.homeFragmentLogo.visibility=View.GONE
                binding.avatar.visibility=View.VISIBLE
                binding.avatar.text = AvatarUtils.getLetter(it.name)
                binding.avatar.background.setTint(android.graphics.Color.parseColor(AvatarUtils.getColor(it.name)))
            }
        } ?: run {
            binding.myBusiness.text = "set Business name"
            binding.avatar.text = "?"
        }
    }

    private fun updateProgressUI(clientsCount: Int, itemsCount: Int,currencyselectedByUser:Boolean) {
        val sessionManager = SessionManager.getInstance(requireContext())
        val step1Done = sessionManager.getActiveWorkspaceId() > 0 // Simplification for now
        val step2Done = clientsCount > 0
        val step3Done = currencyselectedByUser
        val step4Done = itemsCount > 0

        var completedCount = 0
        if (step1Done) completedCount++
        if (step2Done) completedCount++
        if (step3Done) completedCount++
        if (step4Done) completedCount++

        if(completedCount==4){
            sessionManager.setintialStepsCompleted()

        }

            if (sessionManager.isintitalSetupCompleted()) {
                binding.homeInitialSetupCards.visibility =View.GONE
                binding.homeScreenParentCard.visibility=View.VISIBLE
            } else {
                binding.homeInitialSetupCards.visibility = View.VISIBLE
                binding.homeScreenParentCard.visibility=View.GONE
            }


        updateStepUI(step1Done, binding.homeNumCircle, binding.homeNumCircle1done, binding.nameWorkspace, binding.setupBtn, binding.cardone)
        updateStepUI(step2Done, binding.homeNumCircle2, binding.homeNumCircle2done, binding.addFirstCient, binding.AddBtn, binding.cardtwo)
        updateStepUI(step3Done, binding.homeNumCircle3, binding.homeNumCircle3done, binding.setCurrency, binding.chooseBtn, binding.cardThree)
        updateStepUI(step4Done, binding.homeNumCircle4, binding.homeNumCircle4done, binding.addItems, binding.Add4Btn, binding.cardFour)

        binding.progressText.text = "$completedCount/4"
        binding.progressbarEmpty.post {

            val progressWidth = binding.progressbarEmpty.width
            val filledWidth = (progressWidth * (completedCount / 4f)).toInt()

            val params = binding.progressbarFilled.layoutParams
            params.width = filledWidth

            binding.progressbarFilled.layoutParams = params
        }
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