package com.example.flask_test;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.flask_test.retrofit.ApiService;
import com.example.flask_test.retrofit.RetrofitClient;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.POST;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.bumptech.glide.Glide;
public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView imageView;
    private ImageView resultImageView;

    private Response<ResponseBody> response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        resultImageView = findViewById(R.id.resultImageView);

        View selectImageButton = findViewById(R.id.selectedImageButton);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageChooser();
            }
        });

        Button uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

//        // 이미지뷰를 꾹 누를 때 호출되는 메서드
//        resultImageView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                registerForContextMenu(resultImageView);
//                openContextMenu(resultImageView);
//                unregisterForContextMenu(resultImageView);
//                return true;
//            }
//        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
//            imageView.setImageURI(imageUri);
            resultImageView.setImageURI(imageUri);
        }
    }
/*
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.menu_download) {
            return true;
        }
        else {
                return super.onContextItemSelected(item);
        }
    }

    private void downloadImage() {
        // 이미지를 다운로드하는 로직을 추가
        // 여기에는 이미지를 저장하는 코드를 넣으면 됩니다.
        // 예를 들어, 내부 저장소에 저장하는 경우:
        try {
            saveImageToInternalStorage(response.body().byteStream(), "downloaded_image.jpg");
            Toast.makeText(MainActivity.this, "이미지 다운로드 완료", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "이미지 다운로드 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToInternalStorage(InputStream inputStream, String fileName) throws IOException {
        File directory = getFilesDir();
        File file = new File(directory, fileName);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }

        fos.close();
        inputStream.close();
    }
*/
    private void uploadImage() {
        if (imageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);

                // 이미지 파일의 이름은 "image.jpg"로 고정
                MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", "image.jpg",
                        RequestBody.create(MediaType.parse("image/*"), bytes));

                // Retrofit을 사용하여 서버에 이미지 업로드
                uploadImageToServer(imagePart);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToServer(MultipartBody.Part imagePart) {
        Retrofit retrofit = RetrofitClient.getRetrofit();
        ApiService apiService = retrofit.create(ApiService.class);
        Call<ResponseBody> call = apiService.runModel(imagePart);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // 서버에서의 응답을 이미지로 표시하기 위해 Glide을 사용
                        Glide.with(MainActivity.this)
                                .load(response.body().byteStream()) // InputStream을 사용하여 이미지 로딩
                                .load(response.body().bytes())
                                .into(resultImageView); // ImageView에 이미지 표시
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to receive response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}