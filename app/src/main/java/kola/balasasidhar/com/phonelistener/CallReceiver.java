package kola.balasasidhar.com.phonelistener;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by SASi on 14-Dec-15.
 */
public class CallReceiver extends BroadcastReceiver {
    private Context mContext;
    private static int prevState;
    private static MyPhoneListener phoneListener = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (phoneListener == null) {
            phoneListener = new MyPhoneListener();
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(mContext.TELEPHONY_SERVICE);
            telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    private class MyPhoneListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (incomingNumber != null && incomingNumber.length() > 0) {
                Log.d("Phone state", "" + state);
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.d("PHONE STATE : ", "RINGING");
                        prevState = state;
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.d("PHONE STATE : ", "OFF HOOK");
                        prevState = state;
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.d("PHONE STATE : ", "IDLE");
                        if (prevState == TelephonyManager.CALL_STATE_OFFHOOK) {
                            prevState = state;
                            fetchDetails();
                        } else if (prevState == TelephonyManager.CALL_STATE_RINGING) {
                            prevState = state;
                            fetchDetails();
                        }
                        break;
                }
            }
        }
    }

    private void fetchDetails() {

        Uri contacts = CallLog.Calls.CONTENT_URI;

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Cursor cursor = mContext.getContentResolver().query(contacts, null, null, null, null);
        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int name = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);

        if (cursor.moveToFirst()) {
            String phoneNumber = cursor.getString(number);
            String dateT = cursor.getString(date);
            String dur = cursor.getString(duration);
            int tp = Integer.parseInt(cursor.getString(type));
            String nm = cursor.getString(name);
            String callType = "";
            switch (tp) {
                case CallLog.Calls.INCOMING_TYPE:
                    callType = "Incoming Call";
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    callType = "Outgoing Call";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    callType = "Missed Call";
                    break;
            }

            Toast.makeText(mContext, "Number : " + phoneNumber + "\n" + "Name: " + nm + "\n" + "Date : " + dateT + "\n" + "Duration : " + dur + "\n" + "Type: " + callType, Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }
}
