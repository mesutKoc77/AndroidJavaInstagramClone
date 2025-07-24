package com.example.androidjavainstagramclone.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.androidjavainstagramclone.adapter.CommentAdapter;
import com.example.androidjavainstagramclone.databinding.ActivityCommentsBinding;
import com.example.androidjavainstagramclone.model.Comment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private ActivityCommentsBinding binding;
    private ArrayList<Comment> commentArrayList;
    private CommentAdapter commentAdapter;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        postId = getIntent().getStringExtra("postId");
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        commentArrayList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentArrayList);
        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.commentsRecyclerView.setAdapter(commentAdapter);

        getComments();
    }

    private void getComments() {
        if (postId == null) return;
        firebaseFirestore.collection("posts").document(postId).collection("comments")
                .orderBy("date", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(CommentsActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (value != null) {
                            commentArrayList.clear();
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                Comment comment = doc.toObject(Comment.class);
                                if (comment != null) {
                                    commentArrayList.add(comment);
                                }
                            }
                            commentAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    public void sendComment(View view) {
        String text = binding.commentEditText.getText().toString();
        if (text.isEmpty() || postId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("userEmail", auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "");
        data.put("text", text);
        data.put("date", Timestamp.now());
        data.put("postId", postId);

        CollectionReference ref = firebaseFirestore.collection("posts").document(postId).collection("comments");
        ref.add(data).addOnSuccessListener(new OnSuccessListener<com.google.firebase.firestore.DocumentReference>() {
            @Override
            public void onSuccess(com.google.firebase.firestore.DocumentReference documentReference) {
                binding.commentEditText.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CommentsActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
