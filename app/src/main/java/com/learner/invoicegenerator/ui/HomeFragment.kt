package com.learner.invoicegenerator.ui

import android.content.Context
import android.content.res.ColorStateList
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
import androidx.compose.ui.graphics.Path.Companion.combine
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.FragmentHomeBinding
import com.learner.invoicegenerator.ui.adaptor.InvoiceAdapter
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.util.conversions
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.data.repository.NotificationRepository
import com.learner.invoicegenerator.ui.auth.ViewModel.NotificationViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.NotificationViewModelFactory
import com.learner.invoicegenerator.utils.AvatarUtils
import com.learner.invoicegenerator.utils.CurrencyData
import android.widget.LinearLayout
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val clientViewModel: ClientViewModel by activityViewModels()
    private val itemViewModel: ItemViewModel by activityViewModels()
    private val workspaceViewModel: WorkspaceViewModel by activityViewModels()
    private val invoiceViewModel: InvoiceViewModel by activityViewModels()
    private val notificationViewModel: NotificationViewModel by activityViewModels {
        NotificationViewModelFactory(
            NotificationRepository(DatabaseProvider.getDatabase(requireContext()).notificationDao())
        )
    }
    var totalEarningThisMonth=0.0
    var totalOutstandingAmount=0.0
    var pendingTotalAmount=0.0
    var unpaidTotalAmount=0.0
    var earningPrevMonth=0.0
    val today= LocalDate.now()
    val currentMonth=today.month
    val previousMonth=currentMonth.minus(1)
    val startOfMonth = LocalDate.now().withDayOfMonth(1)
    val startOfPreviousMonth=startOfMonth.minusMonths(1)
    val startOfNextMonth = startOfMonth.plusMonths(1)

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
        val activeWorkspaceId=sessionManager.getActiveWorkspaceId()
        val userId = sessionManager.getUserId()
        binding.invoiceRv.layoutManager= LinearLayoutManager(requireContext())
        binding.rippleDots.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        val rippleView=binding.rippleDots
        val rippleAnimation= AnimationUtils.loadAnimation(requireContext(), R.anim.ripple_anim)
        rippleView.startAnimation(rippleAnimation)

        binding.liveDot.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        val headerRippleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.ripple_anim)
        binding.liveDot.startAnimation(headerRippleAnimation)

        loadStats(activeWorkspaceId)
        updateGreetingAndTime()
        updateHomeHeaderStyleUI()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                invoiceViewModel.getInvoicesByWorkspaceId(activeWorkspaceId).collect { invoices->
                    if(!invoices.isEmpty()){
                        val latest5Invoices=invoices.take(5)
                        val adapter= InvoiceAdapter(latest5Invoices) { selectedInvoice ->
                            openInvoiceDetails(selectedInvoice)
                        }
                        binding.invoiceRv.adapter=adapter
                        binding.noInvoicesPlaceholder.visibility= View.GONE
                    }
                    else{
                        binding.noInvoicesPlaceholder.visibility=View.VISIBLE
                        binding.invoiceRv.visibility=View.GONE
                    }

                }
            }

        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch{
                    invoiceViewModel.paidInvoices.collect { invoices->
                        totalEarningThisMonth = 0.0
                        invoices.forEach { invoice->
                            totalEarningThisMonth += invoice.totalAmount
                        }
                        binding.earnedThisMonthValue.text = conversions.formatAmount(totalEarningThisMonth)

                        val currencySymbol = CurrencyData.currencies.find { it.code == sessionManager.getCurrencyCode() }?.symbol ?: "$"
                        binding.tvEditorialEarnedValue.text = "$currencySymbol ${conversions.formatAmount(totalEarningThisMonth)}"

                        earningPrevMonth = 0.0
                        val prevMonthPaidInvoices = invoiceViewModel.fetchPrevMonthPaidNum(startOfPreviousMonth, startOfMonth, activeWorkspaceId)
                        prevMonthPaidInvoices.forEach { invoice->
                            earningPrevMonth += invoice.totalAmount
                        }
                        val monthOverMonthPercent = conversions.calculatePercentageChange(totalEarningThisMonth, earningPrevMonth)
                        if (monthOverMonthPercent >= 0.0) {
                            val sign = if (monthOverMonthPercent > 0.0) "+" else ""
                            binding.MOMValue.text = "$sign${String.format(java.util.Locale.US, "%.1f", monthOverMonthPercent)}%"
                            binding.insightArrow.setImageResource(R.drawable.insights_upper_arrow)
                            binding.MOMSection.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_green_alpha))
                            binding.MOMValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green))

                            binding.tvEditorialMOMPercent.text = "$sign${String.format(java.util.Locale.US, "%.1f", monthOverMonthPercent)}%"
                            binding.editorialMOMArrow.setImageResource(R.drawable.insights_upper_arrow)
                            binding.editorialMOMArrow.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_green))
                            binding.tvEditorialMOMPercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green))
                        } else {
                            binding.MOMValue.text = "${String.format(java.util.Locale.US, "%.1f", monthOverMonthPercent)}%"
                            binding.insightArrow.setImageResource(R.drawable.insights_down_arrrow)
                            binding.MOMSection.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.error_red_colour))
                            binding.MOMValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.carrot_red_shade))

                            binding.tvEditorialMOMPercent.text = "${String.format(java.util.Locale.US, "%.1f", monthOverMonthPercent)}%"
                            binding.editorialMOMArrow.setImageResource(R.drawable.insights_down_arrrow)
                            binding.editorialMOMArrow.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.carrot_red_shade))
                            binding.tvEditorialMOMPercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.carrot_red_shade))
                        }
                        binding.tvEditorialMOMVsMonth.text = "vs $previousMonth"
                        updateEditorialCardStats(currencySymbol)
                    }
                }
                launch {
                    invoiceViewModel.pendingInvoices.collect{invoices->
                        pendingTotalAmount=0.0
                        invoices.forEach { invoice->
                            pendingTotalAmount+=invoice.totalAmount
                        }
                        binding.pendingCountValue.text=invoices.size.toString()
                        totalOutstandingAmount=pendingTotalAmount+unpaidTotalAmount
                        binding.outstandingValue.text= conversions.formatAmount(totalOutstandingAmount)

                        val currencySymbol = CurrencyData.currencies.find { it.code == sessionManager.getCurrencyCode() }?.symbol ?: "$"
                        updateEditorialCardStats(currencySymbol)
                    }
                }
                launch{
                    invoiceViewModel.unpaidInvoices.collect { invoices->
                        unpaidTotalAmount=0.0
                        invoices.forEach { invoice->
                            unpaidTotalAmount+=invoice.totalAmount
                        }
                        binding.unpaidCountValue.text=invoices.size.toString()
                        totalOutstandingAmount=pendingTotalAmount+unpaidTotalAmount
                        binding.outstandingValue.text= conversions.formatAmount(totalOutstandingAmount)

                        val currencySymbol = CurrencyData.currencies.find { it.code == sessionManager.getCurrencyCode() }?.symbol ?: "$"
                        updateEditorialCardStats(currencySymbol)
                    }
                }


            }
        }

      viewLifecycleOwner.lifecycleScope.launch{
          viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
              itemViewModel.allItems.collect { items->
                  binding.noOfItems.text=items.size.toString()+(if(items.size<=1) " item" else " items")
              }
          }
      }
        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                clientViewModel.allClients.collect { clients->
                    binding.noOfClients.text=clients.size.toString()+(if(clients.size<=1) " client" else " clients")
                }
            }
        }


        binding.btnSearchHome.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_quickSearchFragment)
        }

        binding.cardCatalogue.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_itemsFragment)
        }
        binding.catalogueChiv.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_itemsFragment)
        }
        binding.cardClients.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_clientFragment)
        }
        binding.clientChiv.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_clientFragment)
        }
        binding.viewAllBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_invoicesFragment)
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

        binding.bellIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_notificationsFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationViewModel.unreadCount.collect { count ->
                    binding.bellNotificationDot.visibility = if (count > 0) View.VISIBLE else View.GONE
                }
            }
        }

        // 1. Reactive Header Update (Dedicated Flow)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    sessionManager.activeWorkspaceId,
                    workspaceViewModel.getWorkspacesByUserId(userId)
                ) { activeId, workspaces ->
                    workspaces.find { it.id == activeId }
                }.collect { activeWorkspace ->
                    updateHeaderUI(activeWorkspace)
                }
            }
        }

        // 2. Reactive Data Counts Update
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    clientViewModel.allClients,
                    itemViewModel.allItems,
                    sessionManager.activeWorkspaceId,
                    sessionManager.currencyCode
                ) { clients, items, _, currencyCode ->
                    Triple(clients.size, items.size, currencyCode)
                }.collect { (clientsCount, itemsCount, _) ->
                    updateProgressUI(
                        clientsCount,
                        itemsCount,
                        sessionManager.hasCurrencySelectedByUser()
                    )
                }
            }
        }
    }




    private fun updateHomeHeaderStyleUI() {
        val sessionManager = SessionManager.getInstance(requireContext())
        val style = sessionManager.getHomeHeaderStyle()
        if (style == "Editorial") {
            binding.editorialHeaderCard.visibility = View.VISIBLE
            binding.homeStatsCardsParent.visibility = View.GONE
        } else {
            binding.editorialHeaderCard.visibility = View.GONE
            binding.homeStatsCardsParent.visibility = View.VISIBLE
        }
    }

    private fun updateEditorialCardStats(currencySymbol: String) {
        val total = totalEarningThisMonth + totalOutstandingAmount
        val paidRatio = if (total > 0) (totalEarningThisMonth / total * 100).toInt() else 0

        binding.tvEditorialEarnedValue.text = "$currencySymbol ${conversions.formatAmount(totalEarningThisMonth)}"
        binding.tvEditorialOutstandingValue.text = "$currencySymbol ${conversions.formatAmount(totalOutstandingAmount)}"
        binding.tvLegendPaid.text = "Paid $paidRatio%"

        val paidW = if (total > 0) (totalEarningThisMonth / total * 100).coerceAtLeast(1.0).toFloat() else 1f
        val unpaidW = if (total > 0) (unpaidTotalAmount / total * 100).coerceAtLeast(1.0).toFloat() else 1f
        val pendingW = if (total > 0) (pendingTotalAmount / total * 100).coerceAtLeast(1.0).toFloat() else 1f

        (binding.barPaid.layoutParams as LinearLayout.LayoutParams).weight = paidW
        (binding.barUnpaid.layoutParams as LinearLayout.LayoutParams).weight = unpaidW
        (binding.barPending.layoutParams as LinearLayout.LayoutParams).weight = pendingW

        binding.barPaid.requestLayout()
        binding.barUnpaid.requestLayout()
        binding.barPending.requestLayout()
    }

    private fun loadStats(activeWorkspaceId:Int){
        invoiceViewModel.fetchPaidInvoices(startOfMonth,startOfNextMonth,activeWorkspaceId)
        invoiceViewModel.fetchUnpaidInvoices(today,activeWorkspaceId)
        invoiceViewModel.fetchPendingInvoices(today,activeWorkspaceId)
        val currentMonthName = currentMonth.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()).uppercase()
        val prevMonthName = previousMonth.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()).lowercase().replaceFirstChar { it.uppercase() }
        binding.currentMonth.text = "· $currentMonthName"
        binding.previousMonth.text = "vs $prevMonthName"
    }

    private fun openInvoiceDetails(invoice: com.learner.invoicegenerator.data.local.entity.Invoice) {
        invoiceViewModel.updateInvoiceDraft(invoice)
        invoiceViewModel.setSelectedTemplate(invoice.templateId)
        viewLifecycleOwner.lifecycleScope.launch {
            val items = invoiceViewModel.getItemsbyInvoiceId(invoice.id).first()
            invoiceViewModel.updateSelectedItems(items.toMutableList())
            findNavController().navigate(R.id.action_homeScreenFragment_to_fragmentFinalInvoice)
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
        val step1Done = sessionManager.getActiveWorkspaceId() > 0
        val step2Done = clientsCount > 0
        val step3Done = currencyselectedByUser
        val step4Done = itemsCount > 0

        var completedCount = 0
        if (step1Done) completedCount++
        if (step2Done) completedCount++
        if (step3Done) completedCount++
        if (step4Done) completedCount++

        if (completedCount == 4) {
            sessionManager.setInitialStepsCompleted()
        }

        // Determine the active step (next step to be completed)
        val activeStep = when {
            !step1Done -> 1
            !step2Done -> 2
            !step3Done -> 3
            !step4Done -> 4
            else -> 0 // All done
        }

        if (sessionManager.isInitialSetupCompleted()) {
            binding.homeInitialSetupCards.visibility = View.GONE
            binding.homeScreenParentCard.visibility = View.VISIBLE
        } else {
            binding.homeInitialSetupCards.visibility = View.VISIBLE
            binding.homeScreenParentCard.visibility = View.GONE
        }

        // Set click listeners for each step card
        setupStepCard(binding.cardone, 1, step1Done, activeStep == 1)
        setupStepCard(binding.cardtwo, 2, step2Done, activeStep == 2)
        setupStepCard(binding.cardThree, 3, step3Done, activeStep == 3)
        setupStepCard(binding.cardFour, 4, step4Done, activeStep == 4)

        updateStepUI(step1Done, activeStep == 1, binding.homeNumCircle, binding.homeNumCircle1done, binding.nameWorkspace, binding.setupBtn, binding.cardone)
        updateStepUI(step2Done, activeStep == 2, binding.homeNumCircle2, binding.homeNumCircle2done, binding.addFirstCient, binding.AddBtn, binding.cardtwo)
        updateStepUI(step3Done, activeStep == 3, binding.homeNumCircle3, binding.homeNumCircle3done, binding.setCurrency, binding.chooseBtn, binding.cardThree)
        updateStepUI(step4Done, activeStep == 4, binding.homeNumCircle4, binding.homeNumCircle4done, binding.addItems, binding.Add4Btn, binding.cardFour)

        binding.progressText.text = "$completedCount/4"
        binding.progressbarEmpty.post {

            val progressWidth = binding.progressbarEmpty.width
            val filledWidth = (progressWidth * (completedCount / 4f)).toInt()

            val params = binding.progressbarFilled.layoutParams
            params.width = filledWidth

            binding.progressbarFilled.layoutParams = params
        }
    }

    private fun setupStepCard(card: View, stepNum: Int, isDone: Boolean, isActive: Boolean) {
        card.setOnClickListener {
            if (!isDone) {
                // Navigate to the appropriate screen based on step
                when (stepNum) {
                    1 -> BottomSheetSetupWorkspace().show(parentFragmentManager, "Setup Workspace")
                    2 -> findNavController().navigate(R.id.action_homeScreenFragment_to_clientFragment)
                    3 -> BottomSheetCurrencyPicker().show(parentFragmentManager, "currencyPickerBottomSheet")
                    4 -> findNavController().navigate(R.id.action_homeScreenFragment_to_itemsFragment)
                }
            }
        }
    }

    private fun updateStepUI(isDone: Boolean, isActive: Boolean, circle: View, doneCircle: View, text: android.widget.TextView, button: View, card: View) {
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

            // Apply green stroke only to active step on circle
            if (isActive) {
                circle.setBackgroundResource(R.drawable.home_num_circle_active)
            } else {
                circle.setBackgroundResource(R.drawable.home_num_circle_inactive)
            }

            // Apply green stroke only to active step on card
            if (isActive) {
                card.setBackgroundResource(R.drawable.bg_onboarding_steps_cards_selected)
            } else {
                card.setBackgroundResource(R.drawable.bg_onboarding_steps_cards_unselected)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateGreetingAndTime()
    }

    private fun updateGreetingAndTime() {
        val now = LocalTime.now()
        val hour = now.hour
        val greeting = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
        val formattedTime = now.format(timeFormatter)
        binding.headerGreetingText.text = "$greeting · $formattedTime"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}