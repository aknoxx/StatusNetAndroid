package at.tuwien.dsg.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Status;
import twitter4j.TwitterException;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import at.tuwien.dsg.common.*;

import at.tuwien.dsg.R;

public class Home extends Activity {
	
	private static UserManagement userManagement;
	private static final String TAG = "Home";
	
	private static final int ACTIVITY_EXPORT = 1337;
	private static final int ACTIVITY_IMPORT = 1338;
	
	private static final int LOGOUT_ID = 0;
	private static final int EXPORT_ID = 1;
	
	private Menu menu;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //ListView tweetsList = (ListView) findViewById(R.id.tweet_list); 
        //tweetsList.setAdapter(new ArrayAdapter<String>(this,R.layout.list_item,COUNTRIES)); 
        
       /* setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, COUNTRIES));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
              // When clicked, show a toast with the TextView text
              Toast.makeText(getApplicationContext(), "Test",
                  Toast.LENGTH_SHORT).show();
            }
          });*/
        
        userManagement = UserManagement.getInstance();
        
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
		String accessToken = settings.getString("oAuthAccessToken", null);
		String accessTokenSecret = settings.getString("oAuthAccessTokenSecret", null);
		
		if(accessToken != null && accessTokenSecret != null) {
			UserManagement.getInstance().loginAuto(accessToken, accessTokenSecret);
			displayTimeline();
		}        
		else {
        	setLoginView();
        }
    }
	
	private void setLoginView() {
		setContentView(R.layout.home_login);
    	final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        //actionBar.setHomeAction(new IntentAction(this, createIntent(this), R.drawable.ic_title_home_demo));
        actionBar.setTitle("Home");

        final Action shareAction = new IntentAction(this, createShareIntent(), R.drawable.ic_title_share_default);
        actionBar.addAction(shareAction);
        final Action otherAction = new IntentAction(this, new Intent(this, OtherActivity.class), R.drawable.ic_title_export_default);
        actionBar.addAction(otherAction);
        
        Log.d(TAG, "home");
    	
    	
    	Button startProgress = (Button) findViewById(R.id.btn_login);
        startProgress.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	//String authUrl = tweeter.getAuthURL();
                /*Intent i = new Intent(Home.this, Login.class);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(i,5);*/
            	
            	//login();
            	
            	String authUrl = null;
				try {
					authUrl = UserManagement.getInstance().getAuthenticationURL();
				} catch (TwitterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
    			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    			startActivity(i);
            	
            	/*try {
        			String authUrl = tweeter.getAuthURL();
        			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
        		} catch (Exception e) {
        			//Toast.makeText(this.e.getMessage(), Toast.LENGTH_LONG).show();
        		}*/
            }
        });
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        menu.add(0, LOGOUT_ID, 0, "Change user"/*R.string.menu_import*/);
        //menu.add(0, EXPORT_ID, 1, R.string.menu_export);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case EXPORT_ID:            
        	//exportMeals();        	
            return true;
        case LOGOUT_ID:        
        	logout();        	
	        return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
	
    private void logout() {
    	SharedPreferences settings = getPreferences(0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString("oAuthAccessToken", null);
	    editor.putString("oAuthAccessTokenSecret", null);
	    editor.commit();
	    
	    setLoginView();
	    //this.menu.removeItem(LOGOUT_ID);
    }
    
	/*
	private void login() {
		LoginHelper.login(this);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Toast.makeText(this, "result", Toast.LENGTH_LONG).show();
		
		switch(requestCode){
        /*case ACTIVITY_EXPORT:
        	if (resultCode == FileManager.SUCCESS_RETURN_CODE)
        		showToast("Meals exported successfuly!");
        	else
        		showToast("Error while exporting Meals!");
	        break;
        case ACTIVITY_IMPORT:  	
        	if(resultCode == FileManager.SUCCESS_RETURN_CODE)
        		showToast("Meals imported successfuly!");
        	else
        		showToast("Error while imoprting Meals!");
		}
	}*/
	
	private void displayTimeline() { 
		
		setContentView(R.layout.home_tweets);
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        //actionBar.setHomeAction(new IntentAction(this, createIntent(this), R.drawable.ic_title_home_demo));
        actionBar.setTitle("Home");

        final Action shareAction = new IntentAction(this, createShareIntent(), R.drawable.ic_title_share_default);
        actionBar.addAction(shareAction);
        final Action otherAction = new IntentAction(this, new Intent(this, OtherActivity.class), R.drawable.ic_title_export_default);
        actionBar.addAction(otherAction);
        
        List<Status> userTimeline = null;
		try {
			userTimeline = UserManagement.getInstance().getTwitter().getUserTimeline();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        List<String> msgs = new ArrayList<String>();
        
        // \\d	A digit: [0-9]
        // \\w	A word character: [a-zA-Z_0-9]
        // \\s	A whitespace character: [ \t\n\x0B\f\r]
        
        // +	One or more.
        // \\.+	Anything
        // *	Zero or more.
        
        String regexLG = "LG\\s+\\w+\\.\\w+\\?date=\\d{2}\\.\\d{2}\\.\\d{4}&duration=\\d{4}\\s+#\\w+";
        
        // SR proofread.Blog http://www.ikangai.com #blog 
        //String regexSR = "SR\\s+\\w+\\.\\w+\\?date=\\d{2}\\.\\d{2}\\.\\d{4}&duration=\\d{4}\\s+#\\w+";
        
        String regex2 = "#.{1,}";
        
        Pattern p = Pattern.compile(regexLG);
        
        for (Status status : userTimeline) {
        	if(p.matcher(status.getText()).matches()) {
        		msgs.add("PATTERN MATCH " + status.getText());
        	}			
        	msgs.add(status.getText());
		}
        
        ListView tweetsList = (ListView) findViewById(R.id.tweet_list); 
        tweetsList.setAdapter(new ArrayAdapter<String>(this,R.layout.list_item,msgs)); 
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		Toast.makeText(this, "new Intent", Toast.LENGTH_LONG).show();
		
		super.onNewIntent(intent);
		
		Uri uri = intent.getData();
		if(uri != null) {
			try {
				UserManagement.getInstance().finalizeOAuthentication(uri);
				
				SharedPreferences settings = getPreferences(0);
			    SharedPreferences.Editor editor = settings.edit();
			    editor.putString("oAuthAccessToken", UserManagement.getInstance().getReqToken());
			    editor.putString("oAuthAccessTokenSecret", UserManagement.getInstance().getSecretToken());
			    editor.commit();
				
				displayTimeline();
				
			} catch (TwitterException e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		
		
		
		/*try {
			UserManagement.getInstance().sendTweeterMessage("hello ");
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		/*Uri uri = intent.getData();
		if(tweeter.authenticate(uri)) {
			Toast.makeText(this, "authenticated", Toast.LENGTH_LONG).show();
		}
		else {
			Toast.makeText(this, "NOT authenticated", Toast.LENGTH_LONG).show();
		}*/
		
		/*Uri uri = intent.getData();
		if(uri != null) {
			try {
				tweeter.authenticate(uri);
			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		
		
		setContentView(R.layout.list_item);*/
		
	}
	
	public static Intent createIntent(Context context) {
        Intent i = new Intent(context, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }

    private Intent createShareIntent() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Shared from the ActionBar widget.");
        return Intent.createChooser(intent, "Share");
    }
    
    static final String[] COUNTRIES = new String[] {
        "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra",
        "Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina",
        "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan",
        "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium",
        "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia",
        "Bosnia and Herzegovina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory",
        "British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso", "Burundi",
        "Cote d'Ivoire", "Cambodia", "Cameroon", "Canada", "Cape Verde",
        "Cayman Islands", "Central African Republic", "Chad", "Chile", "China",
        "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo",
        "Cook Islands", "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czech Republic",
        "Democratic Republic of the Congo", "Denmark", "Djibouti", "Dominica", "Dominican Republic",
        "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea",
        "Estonia", "Ethiopia", "Faeroe Islands", "Falkland Islands", "Fiji", "Finland",
        "Former Yugoslav Republic of Macedonia", "France", "French Guiana", "French Polynesia",
        "French Southern Territories", "Gabon", "Georgia", "Germany", "Ghana", "Gibraltar",
        "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guinea", "Guinea-Bissau",
        "Guyana", "Haiti", "Heard Island and McDonald Islands", "Honduras", "Hong Kong", "Hungary",
        "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica",
        "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan", "Laos",
        "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg",
        "Macau", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands",
        "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia", "Moldova",
        "Monaco", "Mongolia", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia",
        "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand",
        "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "North Korea", "Northern Marianas",
        "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru",
        "Philippines", "Pitcairn Islands", "Poland", "Portugal", "Puerto Rico", "Qatar",
        "Reunion", "Romania", "Russia", "Rwanda", "Sqo Tome and Principe", "Saint Helena",
        "Saint Kitts and Nevis", "Saint Lucia", "Saint Pierre and Miquelon",
        "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Saudi Arabia", "Senegal",
        "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands",
        "Somalia", "South Africa", "South Georgia and the South Sandwich Islands", "South Korea",
        "Spain", "Sri Lanka", "Sudan", "Suriname", "Svalbard and Jan Mayen", "Swaziland", "Sweden",
        "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "The Bahamas",
        "The Gambia", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey",
        "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Virgin Islands", "Uganda",
        "Ukraine", "United Arab Emirates", "United Kingdom",
        "United States", "United States Minor Outlying Islands", "Uruguay", "Uzbekistan",
        "Vanuatu", "Vatican City", "Venezuela", "Vietnam", "Wallis and Futuna", "Western Sahara",
        "Yemen", "Yugoslavia", "Zambia", "Zimbabwe"
      };
}
