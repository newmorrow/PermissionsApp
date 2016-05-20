package com.example.veretennikova.permissionsapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {

    private static final int READ_CONTACTS_REQUEST_CODE = 0;
    private static final int WRITE_CONTACTS_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int OVERLAY_REQUEST_CODE = 3;
    private static final int REQUEST_IMAGE_CAPTURE = 5;

    private boolean shallCheck = true;
    private Button permissionsButton;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView targetSdkVersion = (TextView) findViewById(R.id.target_sdk);
        int version = 0;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = packageInfo.applicationInfo.targetSdkVersion;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        targetSdkVersion.setText(getString(R.string.target_sdk, String.valueOf(version)));

        Button readContactsBtn = (Button) findViewById(R.id.read_contacts);
        readContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!shallCheck || !requestPermissionIfNeeded(Manifest.permission.READ_CONTACTS, READ_CONTACTS_REQUEST_CODE)) {
                    showContacts();
                }
            }
        });
        Button writeContactsBtn = (Button) findViewById(R.id.write_contacts);
        writeContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!shallCheck || !requestPermissionIfNeeded(Manifest.permission.WRITE_CONTACTS, WRITE_CONTACTS_REQUEST_CODE)) {
                    showContacts();
                }
            }
        });
        Button launchCameraBtn = (Button) findViewById(R.id.launch_camera);
        launchCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!shallCheck || !requestPermissionIfNeeded(Manifest.permission.CAMERA, CAMERA_REQUEST_CODE)) {
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

        permissionsButton = (Button) findViewById(R.id.switch_permissions);
        updatePermissionsButton();
        permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shallCheck = !shallCheck;
                updatePermissionsButton();
            }
        });
    }

    private void updatePermissionsButton() {
        if (shallCheck) {
            permissionsButton.setText(getString(R.string.on_permissions));
            permissionsButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            permissionsButton.setText(getString(R.string.off_permissions));
            permissionsButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_REQUEST_CODE) {
            //check whether permission was granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted
                } else {
                    toggleOverlayService();
                }
            }
        }
    }

    private void showContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null
        );
        new AlertDialog.Builder(this)
                .setAdapter(
                        new SimpleCursorAdapter(
                                this,
                                android.R.layout.simple_list_item_1,
                                cursor,
                                new String[]{ContactsContract.Contacts.DISPLAY_NAME}, new int[]{android.R.id.text1}, 0
                        ),
                        null
                )
                .show();
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
        if (shallCheck) {
            // First check whether the permission is granted
            // Only for API level 23
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                //construct an intent
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                //request the permission
                startActivityForResult(intent, OVERLAY_REQUEST_CODE);
                return;
            }
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
