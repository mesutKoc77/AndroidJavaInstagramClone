package com.example.androidjavainstagramclone.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.androidjavainstagramclone.R;
import com.example.androidjavainstagramclone.adapter.PostAdapter;
import com.example.androidjavainstagramclone.databinding.ActivityFeedBinding;
import com.example.androidjavainstagramclone.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {

    //buradan FirbaseStore dan bilgileri cekecegz. Real time cekmek istersek .addSnapShotListener ile tum sisttemi esanli ve surekli dinliyouz mesela bu chat uygulamsi gibi anlik mesajlasma vb
    //durumlarda yani guncewl bilgilwerin onemli oldugu sistemlerde kullanilabilir.
    //ama sistemde surekli guncellemenin dinlenilmesi gerekli degilse bu  durumda surekliolarak sistemi dinlemek, uygulamyi yorabilir. Ornegin kullanicionin profil bilgilerini bir kereye
    //mahsus cekmemiz bize yeterli. Bu durumda da . get... methofunu kullansak bize yeterli gelecektir ve sistemi surekli dinlemek uygulamayi yoracaktir. Firebaasestore de bunlar icin ayri
    //kodlamalar vard. Ve bu sayfanin en altinda 5 de dahil tum notlar okunabilir.
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    ArrayList<Post> postArrayList;

    private ActivityFeedBinding binding;

    PostAdapter postAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityFeedBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        postArrayList=new ArrayList<>();
        auth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        getData();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this)); //ben recyler view imda alt alta gosterecegimi soyluyorum.
        postAdapter=new PostAdapter(postArrayList);
        binding.recyclerView.setAdapter(postAdapter);





    }

    private void getData (){

        //DocumentReference documentReference=firebaseFirestore.collection("posts").document("dfsssa");  dokumantasyonadaki cozum yontemi cunku burada dokuman ismini
        //kendin vermis oluyorsun ya da,
        //CollectionReference documentReference=firebaseFirestore.collection("posts");
        //asagidaki koddan hata aliyrsan bu ikisini de deneyabilirsin.

        firebaseFirestore.collection("posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>()
            //burada collection dan sonra .where diyerek cesitli filtrelemler yaparak ornegin sadece takip ettiklerim veya suna esit olan seyleri getir diyebiliyouz.
        {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error!=null) {
                    Toast.makeText(FeedActivity.this,error.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
                if (value!=null) {

                    postArrayList.clear();
                    for (DocumentSnapshot snapshot : value.getDocuments()) {
                        Map<String, Object> data = snapshot.getData();
                        String userEmail = (String) data.get("useremail");
                        String comment = (String) data.get("comment");
                        String downloadUrl = (String) data.get("downloadurl");

                        Post post = new Post(userEmail, comment, downloadUrl);
                        postArrayList.add(post);
                    }
                    postAdapter.notifyDataSetChanged();

                }

            }
        });





    };

    //simdi gittik bir menu item layout u olusturduk ve olusturdugumuz o layotu asinda biz burda kullanacgimiz icin, buraya baglamamiz gerekiyr
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //burada baglama islemini yani, menu.xml i Feed Activity e bagliyoruz.
        //simdi menu yu inflater ile buraya cagiriyoruz.
        //menu infkater, bizim xml de yazdiklarimiz ile burada yani activity e yazdigimiz kodu birbrine baglayan bir methiod idi
        MenuInflater menuInflater=getMenuInflater();
        //simdi menuyu buraya baglayalim yani olusturdugumuz option menu xml imizi buraya baglayalim yani inflate edelim.
        menuInflater.inflate(R.menu.option_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //burada ise menulerde bulunan seceneklere tiklanmasi durumunda ne olacagini seciyoruz.

        if (item.getItemId() ==R.id.add_post){
            //Upload Activity e gidecegz
            Intent intentToUpload = new Intent(FeedActivity.this, UploadActivity.class);
            startActivity(intentToUpload);
            //finish(); //finish demedim cunku belki kullanici upload etmekten vazgecebilir ve post larin oldugu feed activity e donmek ister.

        } else if (item.getItemId() ==R.id.signout){
            //cikis yapacagiz
            auth.signOut();
            Intent intentToMain = new Intent(FeedActivity.this, MainActivity.class);
            startActivity(intentToMain);
            finish(); //cikis yaptigina gore artik buraya donemez.
        }
        return super.onOptionsItemSelected(item);
    }
}
