package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.data.repository.NotificationRepository
import com.learner.invoicegenerator.databinding.FragmentNotificationsBinding
import com.learner.invoicegenerator.ui.adaptor.NotificationAdapter
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.NotificationViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.NotificationViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val notificationViewModel: NotificationViewModel by activityViewModels {
        NotificationViewModelFactory(
            NotificationRepository(DatabaseProvider.getDatabase(requireContext()).notificationDao())
        )
    }

    private val invoiceViewModel: InvoiceViewModel by activityViewModels()

    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotificationAdapter(emptyList()) { notification ->
            notificationViewModel.markAsRead(notification.id)
            if (notification.invoiceId != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val invoice = invoiceViewModel.getInvoiceByInvoiceNum(notification.invoiceId.toString())
                    if (invoice != null) {
                        invoiceViewModel.updateInvoiceDraft(invoice)
                        invoiceViewModel.setSelectedTemplate(invoice.templateId)
                        val items = invoiceViewModel.getItemsbyInvoiceId(invoice.id).first()
                        invoiceViewModel.updateSelectedItems(items.toMutableList())
                        findNavController().navigate(R.id.action_notificationsFragment_to_fragmentFinalInvoice)
                    }
                }
            }
        }
        binding.rvNotifications.adapter = adapter

        binding.btnMarkAllRead.setOnClickListener {
            notificationViewModel.markAllAsRead()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    notificationViewModel.allNotifications.collect { notifications ->
                        adapter.notifications = notifications
                        adapter.notifyDataSetChanged()

                        if (notifications.isEmpty()) {
                            binding.emptyStateLayout.visibility = View.VISIBLE
                            binding.rvNotifications.visibility = View.GONE
                            binding.tvSectionHeader.visibility = View.GONE
                        } else {
                            binding.emptyStateLayout.visibility = View.GONE
                            binding.rvNotifications.visibility = View.VISIBLE
                            binding.tvSectionHeader.visibility = View.VISIBLE
                        }
                    }
                }

                launch {
                    notificationViewModel.unreadCount.collect { count ->
                        if (count > 0) {
                            binding.tvUnreadBadge.visibility = View.VISIBLE
                            binding.tvUnreadBadge.text = count.toString()
                        } else {
                            binding.tvUnreadBadge.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
