package com.learner.invoicegenerator.ui.items

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.entity.Workspace
import com.learner.invoicegenerator.databinding.ManageWorkspaceRowBinding
import com.learner.invoicegenerator.utils.AvatarUtils

class ManageWorkspaceAdapter(
    private var activeWorkspaceId: Int,
    private val onWorkspaceClick: (Workspace) -> Unit,
    private val onEditClick: (Workspace) -> Unit
) : ListAdapter<Workspace, ManageWorkspaceAdapter.ViewHolder>(WorkspaceDiffCallback()) {

    fun setActiveId(id: Int) {
        activeWorkspaceId = id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ManageWorkspaceRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ManageWorkspaceRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(workspace: Workspace) {
            binding.apply {
                workspaceName.text = workspace.name
                workspaceSubtitle.text = workspace.email ?: ""
                workspaceNTN.text = if (!workspace.taxNumber.isNullOrEmpty()) "NTN ${workspace.taxNumber}" else ""

                if(!workspace.logoUri.isNullOrEmpty()){
                    binding.workspaceLogo.setImageURI(Uri.parse(workspace.logoUri))
                    binding.workspaceLogo.visibility=View.VISIBLE
                    binding.workspaceAvatar.visibility=View.GONE
                }
                else{
                    binding.workspaceLogo.visibility=View.GONE
                    binding.workspaceAvatar.visibility=View.VISIBLE
                    workspaceAvatar.text = AvatarUtils.getLetter(workspace.name)
                }

                
                if (workspace.id == activeWorkspaceId) {
                    rootLayout.setBackgroundResource(R.drawable.bg_active_workspace)
                    activeLabel.visibility = View.VISIBLE
                    workspaceAvatar.background.setTint(Color.parseColor("#0C861A"))
                } else {
                    rootLayout.setBackgroundResource(R.drawable.bg_workspace)
                    activeLabel.visibility = View.GONE
                    workspaceAvatar.background.setTint(Color.parseColor(AvatarUtils.getColor(workspace.name)))
                }

                rootLayout.setOnClickListener { onWorkspaceClick(workspace) }
                btnEdit.setOnClickListener { onEditClick(workspace) }
            }
        }
    }

    class WorkspaceDiffCallback : DiffUtil.ItemCallback<Workspace>() {
        override fun areItemsTheSame(oldItem: Workspace, newItem: Workspace): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Workspace, newItem: Workspace): Boolean = oldItem == newItem
    }
}