package com.example.androidjavainstagramclone.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidjavainstagramclone.databinding.CommentRowBinding;
import com.example.androidjavainstagramclone.model.Comment;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {

    private ArrayList<Comment> commentArrayList;

    public CommentAdapter(ArrayList<Comment> commentArrayList) {
        this.commentArrayList = commentArrayList;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CommentRowBinding binding = CommentRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CommentHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        holder.binding.commentUserEmail.setText(commentArrayList.get(position).userEmail);
        holder.binding.commentText.setText(commentArrayList.get(position).text);
    }

    @Override
    public int getItemCount() {
        return commentArrayList.size();
    }

    class CommentHolder extends RecyclerView.ViewHolder {
        CommentRowBinding binding;

        public CommentHolder(CommentRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
