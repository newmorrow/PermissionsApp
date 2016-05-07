package com.example.veretennikova.permissionsapp;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {

    private static final int READ_CONTACTS_REQUEST_CODE = 0;
    private static final int WRITE_CONTACTS_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int OVERLAY_REQUEST_CODE = 3;
    private static final int REQUEST_IMAGE_CAPTURE = 5;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button readContactsBtn = (Button) findViewById(R.id.read_contacts);
        readContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!requestPermissionIfNeeded(Manifest.permission.READ_CONTACTS, READ_CONTACTS_REQUEST_CODE)) {
                    showContacts();
                }
            }
        });
        Button writeContactsBtn = (Button) findViewById(R.id.write_contacts);
        writeContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!requestPermissionIfNeeded(Manifest.permission.WRITE_CONTACTS, WRITE_CONTACTS_REQUEST_CODE)) {
                    showContacts();
                }
            }
        });
        Button launchCameraBtn = (Button) findViewById(R.id.launch_camera);
        launchCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!requestPermissionIfNeeded(Manifest.permission.CAMERA, CAMERA_REQUEST_CODE)) {
                    launchCamera();
                }
            }
        });
        Button launchOverlayBtn = (Button) findViewById(R.id.launch_overlay);
        launchOverlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestManageOverlayPermission();
            }
        });
    }

    private boolean requestPermissionIfNeeded(final String permission, final int readContactsRequestCode) {
        if (PermissionChecker.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // Show an explanation to the user.
                new AlertDialog.Builder(this)
                        .setTitle("Permission Description")
                        .setMessage(permission + " is really needed, I swear!")
                        .setNeutralButton("CLOSE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Request the permission after the explanation
                                requestPermission(permission, readContactsRequestCode);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                requestPermission(permission, readContactsRequestCode);
            }
            return true;
        }
        return false;
    }

    private void requestPermission(String permission, int readContactsRequestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, readContactsRequestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_CONTACTS_REQUEST_CODE:
                if (permissionGranted(grantResults)) {
                    showContacts();
                } else {
                    //Permission denied. Disable the functionality that depends on this permission.
                }
                break;
            case WRITE_CONTACTS_REQUEST_CODE:
                if (permissionGranted(grantResults)) {
                    showContacts();
                }
                break;
            case CAMERA_REQUEST_CODE:
                if (permissionGranted(grantResults)) {
                    launchCamera();
                }
                break;
        }
    }

    private void showContacts() {
        //todo
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private boolean permissionGranted(int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    public void requestManageOverlayPermission() {
        // First check whether the permission is granted
        // Only for API level 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //construct an intent
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            //request the permission
            startActivity(intent);
            return;
        }
        toggleOverlayService();
    }

    private void toggleOverlayService() {
        Intent intent = new Intent(this, OverlayService.class);
        // Try to stop the service if it is already running
        // Otherwise start the service
        if (!stopService(intent)) {
            startService(intent);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, OverlayService.class);
        if (!stopService(intent)) {
            super.onBackPressed();
        }
    }
}
