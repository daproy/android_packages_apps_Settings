/*
 * Copyright (C) 2013 The ChameleonOS Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.chameleonos.labs.identicons;

import android.annotation.ChaosLab;
import android.annotation.ChaosLab.Classification;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;

import com.android.internal.util.chaos.identicons.Identicon;
import com.android.internal.util.chaos.identicons.IdenticonFactory;

import java.util.ArrayList;

@ChaosLab(name="QuickStats", classification=Classification.NEW_CLASS)
public class IdenticonCreationService extends IntentService {
    private static final String TAG = "IdenticonCreationService";
    private static final int SERVICE_NOTIFICATION_ID = 8675309;

    ArrayList<ContentProviderOperation> mOps = new ArrayList<ContentProviderOperation>();

    public IdenticonCreationService() {
        super("IdenticonCreationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        startForeground(SERVICE_NOTIFICATION_ID, createNotification());
        processContacts();
        stopForeground(true);
    }

    private void processContacts() {
        Cursor cursor = getContacts();
        while(cursor.moveToNext()) {
            final long rawContactId = cursor.getLong(0);
            final String name = cursor.getString(1);
            final long photoId = cursor.getLong(2);
            if (!TextUtils.isEmpty(name)) {
                final byte[] photo = getContactPhotoBlob(photoId);
                if (photoId <= 0 || photo == null || IdenticonUtils.isIdenticon(photo)) {
                    generateIdenticon(rawContactId, name);
                }
            }
        }
        cursor.close();
        if (!mOps.isEmpty()) {
            updateNotification(getString(R.string.identicons_creation_service_running_title),
                    getString(R.string.identicons_creation_service_contact_summary_finishing));
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, mOps);
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to apply batch", e);
            } catch (OperationApplicationException e) {
                Log.e(TAG, "Unable to apply batch", e);
            }
        }
    }

    private Cursor getContacts() {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] {
                ContactsContract.Contacts.NAME_RAW_CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID
        };
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'";
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
                + " COLLATE LOCALIZED ASC";

        return getContentResolver().query(uri, projection, selection, null, sortOrder);
    }

    private byte[] getContactPhotoBlob(long photoId) {
        String[] projection = new String[] { ContactsContract.Data.DATA15 };
        String where = ContactsContract.Data._ID + " == "
                + String.valueOf(photoId) + " AND " + ContactsContract.Data.MIMETYPE + "=='"
                + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";
        Cursor cursor = getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                where,
                null,
                null);
        byte[] blob = null;
        if(cursor.moveToFirst()){
            blob = cursor.getBlob(0);
        }
        cursor.close();

        return blob;
    }

    private void generateIdenticon(long contactId, String name) {
        if (!TextUtils.isEmpty(name)) {
            updateNotification(getString(R.string.identicons_creation_service_running_title),
                    String.format(getString(R.string.identicons_creation_service_contact_summary),
                            name));
            final Identicon identicon = IdenticonFactory.makeIdenticon(this);
            final byte[] identiconImage = identicon.generateIdenticonByteArray(name);
            if (identicon == null) {
                Log.e(TAG, "generateIdenticon() - identicon for " + name + " is null!");
            } else {
                setContactPhoto(getContentResolver(), identiconImage, contactId);
            }
        }
    }

    private void setContactPhoto(ContentResolver resolver, byte[] bytes, long personId) {
        ContentValues values = new ContentValues();
        int photoRow = -1;
        String where = ContactsContract.Data.RAW_CONTACT_ID + " == "
                + String.valueOf(personId) + " AND " + ContactsContract.Data.MIMETYPE + "=='"
                + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";
        Cursor cursor = resolver.query(
                ContactsContract.Data.CONTENT_URI,
                null,
                where,
                null,
                null);
        int idIdx = cursor.getColumnIndexOrThrow(ContactsContract.Data._ID);
        if(cursor.moveToFirst()){
            photoRow = cursor.getInt(idIdx);
        }
        cursor.close();

        if (photoRow >= 0) {
            final String selection = ContactsContract.Data.RAW_CONTACT_ID
                    + " = ? AND "
                    + ContactsContract.Data.MIMETYPE
                    + " = ?";
            final String[] selectionArgs = new String[]{
                    String.valueOf(personId),
                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE};
            mOps.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, personId)
                    .withValue(ContactsContract.Data.IS_PRIMARY, 1)
                    .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, bytes)
                    .withValue("skip_processing", "skip_processing")
                    .withSelection(selection, selectionArgs)
                    .build());
        } else {
            mOps.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, personId)
                    .withValue(ContactsContract.Data.IS_PRIMARY, 1)
                    .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, bytes)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue("skip_processing", "skip_processing")
                    .build());
        }
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, Settings.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        @SuppressWarnings("deprecation")
        Notification notice = new Notification.Builder(this)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle(getString(R.string.identicons_creation_service_running_title))
                .setContentText(getString(R.string.identicons_creation_service_running_summary))
                .setSmallIcon(R.drawable.ic_settings_identicons)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .getNotification();
        return notice;
    }

    private void updateNotification(String title, String text) {
        Intent intent = new Intent(this, Settings.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationManager nm =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        @SuppressWarnings("deprecation")
        Notification notice = new Notification.Builder(this)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_settings_identicons)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .getNotification();
        nm.notify(SERVICE_NOTIFICATION_ID, notice);
    }
}
