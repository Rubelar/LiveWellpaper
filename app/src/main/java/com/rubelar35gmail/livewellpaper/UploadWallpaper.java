package com.rubelar35gmail.livewellpaper;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rubelar35gmail.livewellpaper.Common.Common;
import com.rubelar35gmail.livewellpaper.Models.AnalyzeModel.ComputerVision;
import com.rubelar35gmail.livewellpaper.Models.AnalyzeModel.URLUpload;
import com.rubelar35gmail.livewellpaper.Models.CategoryItem;
import com.rubelar35gmail.livewellpaper.Models.WallpaperItem;
import com.rubelar35gmail.livewellpaper.Remote.IComputerVision;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadWallpaper extends AppCompatActivity {

    ImageView image_preview;
    Button btn_upload, btn_browser, btn_submit;
    MaterialSpinner spinner;
    //Material Spinner Data
    Map<String, String> spinnerDate = new HashMap<>();

    private Uri filePath;

    String categoryIdSelect = "", directUrl = "", nameOfFile = "";

    //FireStorage
    FirebaseStorage storage;
    StorageReference storageReference;

    IComputerVision mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_wallpaper);

        mService = Common.getComputerVisionAPI();

        //Firebase Storage Init
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        //View
        image_preview = (ImageView) findViewById(R.id.image_preview);
        btn_browser = (Button) findViewById(R.id.btn_browser);
        btn_upload = (Button) findViewById(R.id.btn_upload);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        spinner = (MaterialSpinner) findViewById(R.id.spinner);

        //Load spinner data
        loadCategoryToSpinner();

        //Button Event
        btn_browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinner.getSelectedIndex() == 0) //Hint not choose anymore
                    Toast.makeText(UploadWallpaper.this, "Please choose category", Toast.LENGTH_SHORT).show();
                else
                    upload();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectAdultContent(directUrl);
            }
        });
    }

    private void detectAdultContent(final String directUrl) {
        if (directUrl.isEmpty())
            Toast.makeText(this, "Picture not uploaded", Toast.LENGTH_SHORT).show();
        else {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Analyzing Image...");
            progressDialog.show();

            mService.analyzeImage(Common.getAPIAdultEndPoint(), new URLUpload(directUrl))
                    .enqueue(new Callback<ComputerVision>() {
                        @Override
                        public void onResponse(Call<ComputerVision> call, Response<ComputerVision> response) {
                            if (response.isSuccessful()) {
                                if (!response.body().getAdult().isAdultContent())
                                {
                                    //If picture is not contain adult content
                                    //We will save it to our background gallery
                                    progressDialog.dismiss();
                                    saveUrlToCategory(categoryIdSelect, directUrl);
                                    Toast.makeText(UploadWallpaper.this, "Uploaded!!!", Toast.LENGTH_SHORT).show();

                                }
                                else
                                {
                                    //If url is adult content , we will detele it from url Firebase Storage
                                    deleteFileFromStorage(nameOfFile);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ComputerVision> call, Throwable t) {
                            Toast.makeText(UploadWallpaper.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void deleteFileFromStorage(String nameOfFile) {
        storageReference.child("image/"+nameOfFile)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UploadWallpaper.this, "Your image is adult content and will be deleted", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void upload() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            nameOfFile = UUID.randomUUID().toString();
            StorageReference ref = storageReference.child(new StringBuilder("images/").append(nameOfFile)
                    .toString());

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();

                            taskSnapshot.getStorage()
                                    .getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            directUrl = uri.toString();
                                            btn_submit.setEnabled(true);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(UploadWallpaper.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UploadWallpaper.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 + taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded : " + (int) progress + "%");
                        }
                    });
        }
    }

    private void saveUrlToCategory(String categoryIdSelect, String imageLink) {
        FirebaseDatabase.getInstance()
                .getReference(Common.STR_WALLPAPER)
                .push()
                .setValue(new WallpaperItem(imageLink, categoryIdSelect))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UploadWallpaper.this, "Success!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    //Ctrl+o


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                image_preview.setImageBitmap(bitmap);
                btn_upload.setEnabled(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture: "), Common.PICK_IMAGE_REQUEST);
    }

    private void loadCategoryToSpinner() {
        FirebaseDatabase.getInstance()
                .getReference(Common.STR_CATEGORY_BACKGROUND)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSmapShot : dataSnapshot.getChildren()) {
                            CategoryItem item = postSmapShot.getValue(CategoryItem.class);
                            String key = postSmapShot.getKey();

                            spinnerDate.put(key, item.getName());
                        }

                        //Because Material Spinner will not redeive hint so we need custom hint
                        //This is my tip
                        Object[] valueArray = spinnerDate.values().toArray();
                        List<Object> valueList = new ArrayList<>();
                        valueList.add(0, "Category"); //we will add first item is Hint
                        valueList.addAll(Arrays.asList(valueArray)); //And add all remain category name
                        spinner.setItems(valueList); //Set source data for spinner
                        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                                //When user choose caategory, we will get categoryId (key)
                                Object[] keyArray = spinnerDate.keySet().toArray();
                                List<Object> keyList = new ArrayList<>();
                                keyList.add(0, "Category_key");
                                keyList.addAll(Arrays.asList(keyArray));
                                categoryIdSelect = keyList.get(position).toString();//Assign key when user choose category

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        deleteFileFromStorage(nameOfFile);
        super.onBackPressed();
    }
}
