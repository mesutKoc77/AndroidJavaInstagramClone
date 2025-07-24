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

                    for (DocumentSnapshot snapshot : value.getDocuments()) //yani donen Dokumanlarin dizisinden, her biri icerisinde DocumentSnapshot'lari al ve snapshot
                        //isismli dokumantasyona kayit et.
                    {
                        Map<String, Object> data = snapshot.getData();
                        //Casting
                        String  userEmail = (String) data.get("useremail");
                        String  comment = (String) data.get("comment");
                        String  downloadUrl = (String) data.get("downloadurl");

                        Post post=new Post(userEmail, comment, downloadUrl, snapshot.getId());
                        postArrayList.add(post);
                    }
                    System.out.println("0"+postArrayList.get(0));
                    System.out.println("1"+postArrayList.get(1));
                    System.out.println("2"+postArrayList.get(2));
                    System.out.println("3"+postArrayList.get(3));
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

//1-




/*
Firebase'de "Get data once" ve "Get realtime updates" arasındaki temel fark, veri alım yöntemlerinin sürekliliği ve güncellik sıklığıdır.

1. **Get Data Once (Bir Defaya Mahsus Veri Alma):**
   - "Get data once" yöntemi, verilerin bir kez alınmasını ve daha sonra güncellemelerin takip edilmemesini sağlar.
   - Bu yöntem genellikle uygulamanın başlangıcında veya belirli bir zaman aralığında veri almak için kullanılır.
   - Veriler, bu yöntemle alındıktan sonra, sonraki değişikliklerden haberdar olunmaz ve tekrar istek gönderilmedikçe güncellenmez.

2. **Get Realtime Updates (Gerçek Zamanlı Güncellemeler Almak):**
   - "Get realtime updates" yöntemi, verilerin sürekli olarak izlenmesini ve herhangi bir değişiklik olduğunda anında bildirilmesini sağlar.
   - Bu yöntem, Firebase Realtime Database veya Firebase Cloud Firestore gibi gerçek zamanlı veritabanlarıyla kullanılır.
   - Veriler, uygulama açık olduğu sürece veya belirli bir dinleme işlemi devam ettiği sürece sürekli olarak güncellenir ve değişiklikler anında bildirilir.

Özetle, "Get data once" bir defaya mahsus veri alır ve sonraki güncellemeleri takip etmezken, "Get realtime updates" gerçek zamanlı güncellemeler alır ve verilerin sürekli olarak izlenmesini sağlar. Hangi yöntemin kullanılacağı, uygulamanın ihtiyaçlarına ve veri alım gereksinimlerine bağlıdır.

Tabii, bir simitçi örneği üzerinden "Get Data Once" ve "Get Realtime Updates" yöntemlerinin nasıl kullanılabileceğini ve olası sorunları açıklayalım:

**Simitçi Örneği:**

1. **Get Data Once:**
   - Bir simitçi dükkânı düşünelim. Bir müşteri ilk kez dükkâna gelir ve belirli bir simit alır. Kasadaki çalışan müşterinin ilk siparişini alır ve bunu kaydeder. Müşteri ödemesini yapar ve dükkândan ayrılır.
   - Burada "Get Data Once" yöntemi kullanılabilir. Çünkü müşterinin ilk siparişi alındığında, işlem tamamlanmıştır ve sonraki seferlerde bu siparişin güncellenmesi veya değiştirilmesi gerekmez.

2. **Get Realtime Updates:**
   - Ancak simitçi dükkânında stoğun güncellenmesi durumunda "Get Realtime Updates" yöntemi daha uygundur. Örneğin, bir müşteri bir simit satın aldığında, stoğun güncellenmesi gerekir. Eğer bir başka müşteri aynı anda bir simit daha alırsa veya birisi ekstra simit stoku getirirse, bu değişikliklerin tüm çalışanlar ve müşteriler tarafından anında görülmesi önemlidir.
   - Bu durumda "Get Realtime Updates" yöntemi, stoğun gerçek zamanlı olarak güncellenmesini sağlar. Herhangi bir değişiklik yapıldığında, dükkândaki tüm kullanıcılar ve çalışanlar anında bilgilendirilir ve güncel stok durumu gösterilir.

**Sorunlar ve Dezavantajlar:**
- "Get Data Once" yöntemi kullanıldığında, güncellemelerin anında görülmediği için müşterilerin ve çalışanların stok durumu hakkında yanlış bilgilere sahip olma riski vardır. Özellikle yoğun saatlerde veya hızlı satış durumlarında, güncel stok bilgisi önemlidir.
- "Get Realtime Updates" yöntemi kullanıldığında ise, sürekli olarak gerçek zamanlı güncellemeler alındığı için sistemdeki gereksiz yük artabilir. Bu, performans sorunlarına veya kaynak kullanımı artışına yol açabilir.
- Her iki yöntem de uygulanırken, kullanım senaryoları ve gereksinimler dikkate alınmalıdır.
 */



//2



/*
Elbette, iki farklı veri alım yöntemi olan "Get Data Once" ve "Get Realtime Updates"ın nasıl kodlandığını size örnekleyebilirim.

**Get Data Once:**
Bu yöntemde, verileri bir defaya mahsus alırız ve sonraki güncellemeleri takip etmeyiz. Örneğin, bir kullanıcının profil bilgilerini ilk kez almak için bu yöntemi kullanabiliriz. Kodlama süreci şu adımlardan oluşur:

```java
// Get Data Once yöntemiyle veri alma işlemi
public void getDataOnce() {
    // Veritabanından kullanıcının profil bilgilerini bir kez al
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
    databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Veri alındığında yapılacak işlemler
            User user = dataSnapshot.getValue(User.class);
            // Profil bilgilerini kullan
            displayUserProfile(user);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Hata durumunda yapılacak işlemler
            Log.e(TAG, "Veri alınamadı: " + databaseError.getMessage());
        }
    });
}
```

Bu kod, veritabanından kullanıcının profil bilgilerini bir defaya mahsus alır ve "addListenerForSingleValueEvent" metodunu kullanarak bir kez veri alındığında çalışır. Sonraki güncellemeleri takip etmez.

**Get Realtime Updates:**
Bu yöntemde, verileri sürekli olarak izleriz ve herhangi bir değişiklik olduğunda anında bildirilmesini sağlarız. Örneğin, bir sohbet uygulamasında yeni mesajları gerçek zamanlı olarak almak için bu yöntemi kullanabiliriz. Kodlama süreci şu adımlardan oluşur:

```java
// Get Realtime Updates yöntemiyle veri alma işlemi
public void getRealtimeUpdates() {
    // Veritabanından sohbet mesajlarını gerçek zamanlı olarak izle
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("messages");
    databaseRef.addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
            // Yeni bir mesaj eklendiğinde yapılacak işlemler
            Message message = dataSnapshot.getValue(Message.class);
            // Yeni mesajı ekrana göster
            displayNewMessage(message);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
            // Mesaj değiştirildiğinde yapılacak işlemler
            Message updatedMessage = dataSnapshot.getValue(Message.class);
            // Güncellenen mesajı ekrana göster
            displayUpdatedMessage(updatedMessage);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            // Mesaj silindiğinde yapılacak işlemler
            String deletedMessageId = dataSnapshot.getKey();
            // Silinen mesajı ekrandan kaldır
            removeDeletedMessage(deletedMessageId);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            // Mesajın konumu değiştiğinde yapılacak işlemler
            // Örneğin, sıralama değiştiyse
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Hata durumunda yapılacak işlemler
            Log.e(TAG, "Veri alınamadı: " + databaseError.getMessage());
        }
    });
}
```

Bu kod, veritabanındaki sohbet mesajlarını gerçek zamanlı olarak izler ve "addChildEventListener" metoduyla sürekli olarak veri değişikliklerini dinler. Yeni bir mesaj eklenmesi, bir mesajın güncellenmesi veya silinmesi gibi herhangi bir değişiklik olduğunda ilgili metodlar çalışır ve kullanıcıya anlık olarak bildirim yapılır.

Bu örneklerde, "Get Data Once" ve "Get Realtime Updates" yöntemlerinin kodlama açısından nasıl farklı olduğunu görebilirsiniz. İhtiyacınıza ve uygulamanızın gereksinimlerine bağlı olarak hangi yöntemi kullanmanız gerektiğini belirleyebilirsiniz.
 */

//3

/*
Tabii, sözel olarak da iki kod arasındaki farkı açıklayabilirim:

1. **Get Data Once (Veriyi Bir Kez Al):**
   Bu kod, veritabanından veriyi yalnızca bir kez alır. Yani, veriyi alır ve daha sonra o veriyle ilgili herhangi bir değişikliği takip etmez. Örneğin, kullanıcının profil bilgilerini almak için bu yöntemi kullanabilirsiniz. Kullanıcının profil bilgileri uygulamaya ilk giriş yaptığında alınır ve sonraki güncellemeleri izlemez.

2. **Get Realtime Updates (Gerçek Zamanlı Güncellemeleri Al):**
   Bu kod, veritabanındaki veriyi sürekli olarak izler ve herhangi bir değişiklik olduğunda anlık olarak bildirim almanızı sağlar. Yani, veritabanındaki verilerde herhangi bir değişiklik olduğunda, bu kod bu değişiklikleri otomatik olarak algılar ve size bildirir. Örneğin, bir sohbet uygulamasında yeni mesajları gerçek zamanlı olarak almak için bu yöntemi kullanabilirsiniz. Kullanıcılar yeni bir mesaj gönderdiğinde veya mevcut bir mesajı değiştirdiğinde, bu değişiklikler anında uygulamada görüntülenir.

Bu şekilde, "Get Data Once" ve "Get Realtime Updates" yöntemlerinin sözlü olarak farkını anlayabilirsiniz. Birinci yöntem veriyi bir kez alırken, ikinci yöntem verilerde herhangi bir değişiklik olduğunda bildirim almanızı sağlar.
 */

//4
/*
Eğer bir uygulamada hem "Get Data Once" hem de "Get Realtime Updates" ihtiyacı varsa, öncelikle hangi verilerin yalnızca bir kez alınması gerektiğini ve hangi verilerin gerçek zamanlı olarak izlenmesi gerektiğini belirlemeniz önemlidir. Daha sonra, bu iki durumu ayrı ayrı kodlayabilir veya birleştirebilirsiniz.

Örneğin, bir sosyal medya uygulamasında kullanıcı profillerini düşünelim. Kullanıcı profili bilgileri (ad, soyad, profil resmi vb.) genellikle kullanıcı uygulamaya ilk kez giriş yaptığında veya profil sayfasını ziyaret ettiğinde alınır. Bu durumda "Get Data Once" yöntemini kullanabilirsiniz.

Ancak, kullanıcının paylaştığı gönderileri veya diğer kullanıcılarla etkileşimde bulunduğu içerikleri gerçek zamanlı olarak izlemek isteyebilirsiniz. Yani, yeni bir gönderi paylaşıldığında veya bir gönderi beğenildiğinde hemen bildirim almak istersiniz. Bu durumda "Get Realtime Updates" yöntemini kullanabilirsiniz.

İşte bu senaryoda, her iki durumu da aynı uygulamada kullanmak için şu adımları izleyebilirsiniz:

1. **Get Data Once:**
   Kullanıcı profili gibi verilerin yalnızca bir kez alınması gereken durumlar için bu yöntemi kullanın. Örneğin, kullanıcı giriş yaptığında veya profil sayfasını ziyaret ettiğinde bu verileri alın.

2. **Get Realtime Updates:**
   Gerçek zamanlı olarak izlenmesi gereken veriler için bu yöntemi kullanın. Örneğin, yeni bir gönderi paylaşıldığında veya bir gönderi beğenildiğinde bu güncellemeleri anında alın.

Bu şekilde, uygulamanızın gereksinimlerine göre her iki yöntemi de uygun şekilde kullanarak kullanıcı deneyimini geliştirebilirsiniz. Kodlama sürecinde de bu iki yöntemi ayrı ayrı veya birleşik olarak uygulayabilirsiniz, böylece uygulamanızın gereksinimlerine ve karmaşıklığına bağlı olarak daha esnek bir yaklaşım benimsemiş olursunuz.
 */

//5

/*
 //mesela canli dinlemenin gerekli oldugu durumlar icin
    /*
    final DocumentReference docRef = db.collection("cities").document("SF");
docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
    @Override
    public void onEvent(@Nullable DocumentSnapshot snapshot,
                        @Nullable FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "Listen failed.", e);
            return;
        }

        if (snapshot != null && snapshot.exists()) {
            Log.d(TAG, "Current data: " + snapshot.getData());
        } else {
            Log.d(TAG, "Current data: null");
        }
    }
});
     */

//mesela bir defaya mahsus dinleme icin
    /*
    DocumentReference docRef = db.collection("cities").document("SF");
docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
    @Override
    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document.exists()) {
                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
            } else {
                Log.d(TAG, "No such document");
            }
        } else {
            Log.d(TAG, "get failed with ", task.getException());
        }
    }
});
     */
