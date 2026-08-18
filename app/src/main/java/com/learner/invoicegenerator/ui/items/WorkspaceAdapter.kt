package com.learner.invoicegenerator.ui.items

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.learner.invoicegenerator.data.local.entity.Workspace
import com.learner.invoicegenerator.databinding.WorkspaceRowBinding
import com.learner.invoicegenerator.utils.AvatarUtils

class WorkspaceAdapter(
    private val activeWorkspaceId: Int,
    private val onWorkspaceClick: (Workspace) -> Unit
) : ListAdapter<Workspace, WorkspaceAdapter.WorkspaceViewHolder>(WorkspaceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkspaceViewHolder {
        val binding = WorkspaceRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkspaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkspaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WorkspaceViewHolder(private val binding: WorkspaceRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(workspace: Workspace) {
            binding.apply {
                workspaceName.text = workspace.name
                workspaceSubtitle.text = "Workspace ID: ${workspace.id}" // Placeholder for real subtitle
                
                workspaceAvatar.text = AvatarUtils.getLetter(workspace.name)
                workspaceAvatar.background.setTint(Color.parseColor(AvatarUtils.getColor(workspace.name)))

                if (workspace.id == activeWorkspaceId) {
                    activeIndicator.visibility = View.VISIBLE
                    workspaceName.setTextColor(Color.parseColor("#0C861A"))
                } else {
                    activeIndicator.visibility = View.GONE
                    workspaceName.setTextColor(Color.parseColor("#171817"))
                }

                rootLayout.setOnClickListener { onWorkspaceClick(workspace) }
            }
        }
    }

    class WorkspaceDiffCallback : DiffUtil.ItemCallback<Workspace>() {
        override fun areItemsTheSame(oldItem: Workspace, newItem: Workspace): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Workspace, newItem: Workspace): Boolean = oldItem == newItem
    }
}