package at.tuwien.dsg.common;

import twitter4j.TwitterException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class LoginHelper {

	public static void login(Context context) {
		try {
			Toast.makeText(context, "LoginHelper", Toast.LENGTH_LONG).show();
			String authUrl = UserManager.getInstance().getAuthenticationURL();
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			context.startActivity(i);
		} catch (TwitterException e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	public static void onNewIntent(Intent intent, Context context) {
		Toast.makeText(context, "new Intent", Toast.LENGTH_LONG).show();
		Uri uri = intent.getData();
		if(uri != null) {
			try {
				UserManager.getInstance().finalizeOAuthentication(uri);
			} catch (TwitterException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
}
