package com.example.androidjavainstagramclone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidjavainstagramclone.databinding.CommentRowBinding;
import com.example.androidjavainstagramclone.model.Comment;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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
        Comment current = commentArrayList.get(position);
        holder.binding.commentUserEmail.setText(current.userEmail);
        holder.binding.commentText.setText(current.text);
        holder.binding.likeCountTextView.setText(String.valueOf(current.likes));

        holder.binding.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore.getInstance()
                        .collection("posts")
                        .document(current.postId)
                        .collection("comments")
                        .document(current.commentId)
                        .update("likes", FieldValue.increment(1));
            }
        });
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
