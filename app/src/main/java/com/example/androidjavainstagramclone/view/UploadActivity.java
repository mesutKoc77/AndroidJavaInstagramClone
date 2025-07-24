package com.example.androidjavainstagramclone.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.androidjavainstagramclone.databinding.ActivityUploadBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    ActivityResultLauncher<Intent> activityResultLauncher; //bu Launcher'lari eger onCreate icerisinde baslatmaz isem bu durum uygulamayi cokertir.
    ActivityResultLauncher<String> permissionLauncher;
    /*
    Birincisi (activityResultLauncher) Intent başlatmak için kullanılırken, ikincisi (permissionLauncher) izin istemek için kullanılır.
     */
    Uri imageData;
    private ActivityUploadBinding binding;
    //Bitmap selectedImage; //isternirse, bitmap ile de yapilabilir bu.

    private FirebaseStorage fireBaseStorage; //FirebaseStorage nesnesini kullanarak uygulama, dosyaları güvenli bir şekilde depolayabilir ve bu dosyalara erişim sağlayabilir. Bu sayede uygulama, kullanıcıların dosyalarını saklamak ve paylaşmak için güvenilir bir çözüm sunar.daha ayrintili bilgi en altta 5. bolumde
    private FirebaseAuth auth; //Bu nesne, kullanıcıların uygulamaya giriş yapması, kaydolması, şifrelerini sıfırlaması gibi kimlik doğrulama işlemlerini yönetir.daha ayrintili bilgi en altta 4. bolumde
    private FirebaseFirestore firebaseFirestore; //Firebase Firestore nesnesini oluşturmanın temel amacı, Firebase bulut tabanlı veritabanına erişmek ve bu veritabanında veri işlemleri yapmaktır. Bu nesne, Firebase Firestore veritabanına erişmek, belirli bir koleksiyon içinde belirli belgeleri okumak, yazmak, güncellemek veya silmek için kullanılır.
    //DAta Ayrintili bilgi, sayfanin en altinda. 3. bolumde

    private StorageReference storageReference;//6.bolumde ayrinti var. Örneğin, bir ürün resmi yüklemek istediğinizde, bu referansı kullanarak ilgili klasöre erişebilir ve resmi yükleyebilirsiniz. Bu sayede Firebase Storage'da dosyaları organize etmek ve yönetmek daha kolay hale gelir.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        fireBaseStorage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = fireBaseStorage.getReference();//suan bize bombos bir referan veriyor. Baz bir referans veriyor bize. Referans, biz ilgili oge yi
        //nereye koyacgimizi takip edecegimiz bir degiskendir.


    }

    public void uploadButtonClicked(View view) {
        //firebase de epolama ile veritabani farkli kavramlar olarak adlandiriliyor ve farkli islevleri var.
        //Depolama, genelde kullanicinin foto, video vb yani buyuk dosyalarinin kayit edildgi bir alan iken
        //Ver tabani ise, kullanicinin mail, sipasiin url i, sohbet uygulamasininda kullanicinin mesajlari vb.
        //yani daha dinamik olanlari veri tababnindas sakliyor firebase.
        //BUNUMLA ilgili notlara bu class in en altinda ulasabilirisnn.

        if (imageData != null) {

            //universal unique id
            UUID uuid = UUID.randomUUID();
            String imageName = "images/" + uuid + ".jpg";
            fireBaseStorage.getReference().child(imageName).putFile(imageData) //yukluyor ama uygulama, arka planda baska islmemler daha yapacagi icin
                    //asenkron islemler icin asagidaki bazi gorevleri kendisine aktarmamiz gerekiyor.
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() //buradaki UploadTask ile uygulama image yukleme gorevini yapar ama
                            //arka planda baska gorevleri de yapabilir. TaskSnapshot ise ilgili dosyanin url, adi vb. bilgilerini barindaran bir nesnedir.7. not
                    {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //download url
                            //simdi de storage e yuklemis oldugumuz dosyaninin url ini alacagz ve bunu veritabanina yukleyecegz.
                            StorageReference newReference = fireBaseStorage.getReference(imageName);
                            newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    String comment = binding.commentText.getText().toString();
                                    FirebaseUser user = auth.getCurrentUser();
                                    String email = user.getEmail();
                                    //artik bir image i storage'a kayit ettik ve bu nlarin tmamaini ben firebaseFirestore, veri tabanina kayit etmem gerekiyor.-

                                    //veritabanina kayit edilen degerler, anahtar ve degeri eslesmesi seklinde kayit ediliyordu.
                                    //dolayisiyla biz bunu HashMap nesnesine kayit etmemiz gerekiyor. Daha sonra bu hashmap nesnesini de firebasestore veritabanina atacagiz.

                                    HashMap<String, Object> postData = new HashMap<String, Object>();
                                    postData.put("useremail", email);
                                    postData.put("downloadurl", downloadUrl);
                                    postData.put("comment", comment);
                                    postData.put("date", FieldValue.serverTimestamp());

                                    firebaseFirestore.collection("posts").add(postData) //buraya, addOnSuccessListener eklenmediği durumda kod hata verebilir veya beklenmedik sonuçlar üretebilir. Bu durum, Firestore'a veri eklenmeden sonra başka işlemler yapılması gerektiğinde, bu işlemlerin tamamlanmasını beklemek için addOnSuccessListener kullanmanın önemini vurgular. Eğer addOnSuccessListener eklenmezse, veri eklenme işlemi tamamlanmadan önce diğer işlemler gerçekleşebilir ve bu durum istenmeyen sonuçlara neden olabilir. Bu nedenle, asenkron işlemlerde başarı durumunu dinlemek ve uygun işlemleri gerçekleştirmek için addOnSuccessListener gibi uygun listener'ları kullanmak önemlidir.
                                            //daha ayrintili bilgiler 8. notta.

                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() //yani ilgili post yuklenene kadar bekle ve basariyla yuklersen
                                                    //asagidaki islemi yap.
                                            {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Intent intent = new Intent(UploadActivity.this, FeedActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(intent);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

                        }
                    }); //burasi hakkinda detayli mantik bilgisi asagida Not 7'de.
        }


    }

    public void selectImage(View view) {
        //manifest e izin istedigimizi belirrten kodu ekledik
        //once izin var mi onu kontrol edecgz.
        //eger izin yoksa, asaguida oyzellikle opermissinlerin android. (nokta) dan secmek onemli.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                //eger izni gostermemenizin mantigini kullaniciya aktarmamiz gerekiyorsa ki bununAndroid sistemin kendisi karar veriyor.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Izin Gerekli, Permission needed! Snackbar", Snackbar.LENGTH_INDEFINITE) //yani kullanici tamam yani anladim diyecegi ana kadar goster demis olduk
                            .setAction("Izin Ver, Give Permission Butonu", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //simdi olusturdugmuz "Izin Ver, Give Permission" butonuna tikladi ve ne olacagini bu methodun icerisine yaziyoruz.
                                    // yani biz burada "ask question ile kullanicdan izni istiyoruz."
                                    //ask permission //izin iste
                                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);


                                }
                            }) // yani burada kullanici ya bir buton gosterecegz ve izin ver gibi bir buton. Bu butonu tiklayacak ve tikladiktan sonra ise ne olacagini "bir Listener"
                            //ile koda dokecegiz.
                            .show();
                } else //yani kullaniciya iznin gosterilmesinin "mantigi" yoksa, yine ask question ile kullanicdan izni istiyoruz.. Yani yukaridaki onClick methodun da da burada da izni iyteyecgz ama burdakinin farki
                //kullaniciya ifade gostermeyesimiz.
                //ask permission //izin iste
                {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            } else //yani kullanici oncesinden zaten izin vermisse diger bir deyisle "ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED" false donerse
            {
                // o zaman zaten kullanicinin direkt galerisine gidecez.
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //direkt gidemiyorduk bu intent ile ancak bur result launcer ile biz ne yapacgimizi aciklayacagiz oncelikle. bununicin parametre olaak Intent isteyen activityresultlauncer
                //kullanacagiz. ve burada neler yaocagimizi anlatacgiz kendirisine.
                //ve ne yapmak istedigimizi asagida anlattik o halde gidelim  ve methodumuzu cagirqalim.
                activityResultLauncher.launch(intentToGallery); //burda izin devreden ciktigi icin yani izin zaten verilmis oldugu icin, kullaniciyi direkt aktiviteye yonlendiriyouz.
                //yukarida ise, kullanicdan henuz izin alamadik.

            }

        }
        //yani Android Surumu 33 ve altinda ise asagii yap. Yukaridakinin aynisi ama tek far su:
        //İlk blok, Manifest.permission.READ_MEDIA_IMAGES iznini kontrol eder.
        //İkinci blok ise Manifest.permission.READ_EXTERNAL_STORAGE iznini kontrol eder.
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Izin Gerekli, Permission needed! Snackbar", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Izin Ver, Give Permission Butonu", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                                }
                            })
                            .show();
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }
    }


    public void registerLauncher() {

        //1. register sonuc ve ne yapmak istedigimiz.
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                //simdi kullanicinin gallery sine gittik, kullanici ne yapti ? Sonuc ne diyoruz? Birseyleri secti mi vaz mi gecti, sd kart mi cikti bunu da kontrol edecgz.
                if (result.getResultCode() == Activity.RESULT_OK)//hersey ok ise, yani kullanicinin gallerysine gitti isek
                {
                    Intent intentFromResult = result.getData(); //veriyi alirken bana donus bir intent olarak donecek.
                    //simdi bana birseyler dondu ama bu veri bos mu degil mi onu kontrrol ediyoruz.
                    if (intentFromResult != null) {
                        //simdi ben bir datayi aldim ama ben bu bunu bir uri ya, yani bir ilgili dosyanin  bulundugu yerin bilgisini Uri degiskenine kayit etmem gerekiyor
                        imageData = intentFromResult.getData();
                        //simdi kullanici ya image View'da almak istedigi goruntuyu gosterecegz
                        binding.imageView.setImageURI(imageData); //simdi imageView'e bunu attik.

                        //burada bize yani bu projede bize sadece uri yeterli oldugu icin Bitmap'i kullanmayacagz.
                        /*
                          //simdi ise, image de detayli manipulation islemi yapabilmek icin,  bu uri yi Bitmap'e cevirecegiz.
                        //sayfanin en altinda Uri ile Bitmap arasindaki fark anlatiliyor. Uri, bir dosyanin yolunu gosterirken Bitmap ise bunun piksel deger karsiligini verir ve
                        //image de manioulasyona izin verir.

                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                ImageDecoder.Source source=ImageDecoder.createSource(UploadActivity.this.getContentResolver(),imageData);
                                //aldigimiz bu source u yani kodu, Bitmap'e ceviriyoz
                                selectedImage =ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);
                            } else {
                                selectedImage =MediaStore.Images.Media.getBitmap(UploadActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }



                        } catch (Exception e){
                            e.printStackTrace();
                        }


                         */
                    }
                }
            }
        });//bana bir sonuc olacagi yani bir eylem yapacagim icin bana bir Activity baslat dedim; ama sonrada bunun sonucunda yani sonuc da ne olacagini da Callback ile anlatmam gerekiyor.


        //2. register sonuc ve ne yapmak istedigimiz.
        // aslinda kodu yazarken ikinci yi yazmaya ilkin baslarsak daha mantikli olabilri. Yni permissionLauncher ve sonrasinda activityResultLauncher
        //methoduna devam etmek daha mantikli gorunuyor.
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                //evet izin verildikten sonraki durumu anlatmak icin buradaki callback i kullandik.
                //buradaki paramtree baklildiginda bir result arguman olarak Result verilmi yani,. bize bir izin verildi mi ?
                //result=true demek bize izin verildigi anlamina geliyor.
                if (result) {
                    //yukarida da belittigim gibi. kullanicidan izin alirsak ne yapacagimizi Result Launcher larda anlattik. Yani galeriye gidecek, ordan bilgilweri alcak ve simdi de
                    //o methidu burada cagiracagz. Ki izin Launcher ile activityLauncher imizi irtibatlandirmis olalim-
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery); // ve yukarida activityResultLauncher'da ne yapmak isteidigmizi anlatmis ve buradad da bu intenti baslt<mis olduk.
                } else //yani izin verilmez ise?
                {
                    Toast.makeText(UploadActivity.this, "Permission needed! permissionLauncher", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}