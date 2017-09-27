package com.android.kheirouben.gpsgsm;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.android.kheirouben.gpsgsm.MainActivity;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by KheirouBen on 23/05/2017.
 */

public class SmsListener extends BroadcastReceiver {
    public static String msgBody;
    public static String msg_from;
    public Intent intent;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            MainActivity main = new MainActivity();


            if (bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        msgBody = msgs[i].getMessageBody();

                        if (containsGpsData(msgBody)) {
                            //main.toast("object created");
                            intent = new Intent(context, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("msgBody", msgBody);
                            context.startActivity(intent);
//                            MainActivity.toast("intent sent");

                        } else {
                            MainActivity.toast("No GPS data");
                        }

                    }
                } catch (Exception e) {

                    //       Log.d("Exception caught",e.getMessage());
                }
            }
        }
    }

    private boolean containsGpsData(String msgBody) {

        if (msgBody == null)
            return false;
        if (msgBody.startsWith("gps")) {
            return true;
        } else return false;
    }
/*
        int	pos = msgBody.indexOf ("geo:");

        if (pos < 0)
            return false;

        msgBody = msgBody.substring (pos);
        if ((pos = msgBody.indexOf (' ')) > 0)
            msgBody = msgBody.substring (0, pos);

        return (GeoUri.stringToLocation(msgBody) != null);
  */  

/*
    saveAndNotify (Context	context,SmsMessage	msg
    ) {
        Database	db = new Database (context);
        long		id = -1;

        if (db.open ()) {
            id = db.addRecord (msg);
            db.close ();
        }

        if (id >= 0) {
            Intent	intent = new Intent (context, GetMeThere.class);

            intent.putExtra (WinRak.RECEIVED_SMS_ID, id);

            PendingIntent pi = PendingIntent.getActivity (context, 0,
                    intent, 0);
            String	from = Utility.getContactNameFromNumber (context,
                    msg.getOriginatingAddress ());
            String	ticker = "GPS SMS from " + from;
            Notification n = new Notification (
                    R.drawable.notify_geo_sms_icon,
                    ticker, System.currentTimeMillis ());

            n.setLatestEventInfo (context, from, msg.getMessageBody(), pi);
            n.flags |= Notification.FLAG_AUTO_CANCEL;

            // Don't do any vibration or beeps - the regular SMS receiver
            // will do that.
            NotificationManager nm;

            nm = (NotificationManager) context.getSystemService (
                    Context.NOTIFICATION_SERVICE);
            nm.notify (1, n);
        }
    }
*/
}
