package com.example.booktrack;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.booktrack.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    //view binding
    private ActivityPdfAddBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progres dialog
    private ProgressDialog progressDialog;

    //kategorileri tutması için arraylist
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    //seçilen pdf'in Uri(Birleşik Kaynak Tanımlayıcı) değeri
    private Uri pdfUri = null;

    private static final int PDF_PICK_CODE = 1000;

    //TAG for debug
    private static final String TAG = "ADD_PDF_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Lütfen Bekleyin");
        progressDialog.setCanceledOnTouchOutside(false);



        //tıklanmada geri döner
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //tıklanmada pdf ekler
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

        //tıklanmada kategori seçer
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });

        //tıklandığında pdf yüklenir
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //veri doğrulama
                validateData();
            }
        });
    }

    String title = "", description = "";
    private void validateData() {
        Log.d(TAG, "validateData: Veri Onaylanıyor...");
        //veriyi çekme
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        //veriyi doğrulama
        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "Başlık Girin...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Açıklama Girin...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(selectedCategoryTitle)){
            Toast.makeText(this, "Kategori Seçin...", Toast.LENGTH_SHORT).show();
        } else if (pdfUri==null) {
            Toast.makeText(this, "Pdf seçin...", Toast.LENGTH_SHORT).show();
        }
        else{
            //bütün veriler doğrulandı, yükleme yapılabilir
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: Veritabanına kaydediliyor...");

        //ilerlemeyi gösterir
        progressDialog.setMessage("Pdf yükleniyor...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        String filePathAndName = "Kitaplar" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Pdf veritabanına yüklendi...");
                        Log.d(TAG, "onSuccess: pdf url'si alınıyor");

                        //pdf url'si alma
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();

                        //veritabanına yükler
                        uploadPdfInfoToDb(uploadedPdfUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Pdf yüklemesi başarısız çünkü "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Pdf yüklemesi başarısız çünkü "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        Log.d(TAG, "uploadPdfInfoToDb: Firebase veritabanına kaydediliyor...");

        progressDialog.setMessage("pdf bilgisi kaydediliyor...");

        String uid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);
        hashMap.put("url", ""+uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);

        //veritabanı > Kitaplar
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Kitaplar");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Başarıyla yüklendi...");
                        Toast.makeText(PdfAddActivity.this, "Başarıyla yüklendi...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Başarısız oldu çünkü "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Başarısız oldu çünkü "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Kategoriler yükleniyor...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        //veritabanı > Kategoriler
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Kategoriler");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("id").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedCategoryId, selectedCategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: kategori seçme diyaloğu gösteriliyor");

        //arraylist'ten kategorileri çekme
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kategori Seçin")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategoryTitle = categoryTitleArrayList.get(which);
                        selectedCategoryId = categoryIdArrayList.get(which);

                        binding.categoryTv.setText(selectedCategoryTitle);

                        Log.d(TAG, "onClick: Seçilen Kategori:"+selectedCategoryId+" "+selectedCategoryTitle);
                    }
                })
                .show();

        }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pdf seçin"), PDF_PICK_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == PDF_PICK_CODE){
                Log.d(TAG, "onActivityResult: PDF Seçildi");

                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: URI:"+pdfUri);
            }
        }
        else {
            Log.d(TAG, "onActivityResult: pdf seçme iptal edildi");
            Toast.makeText(this, "pdf seçme iptal edildi", Toast.LENGTH_SHORT).show();

        }
    }
}

