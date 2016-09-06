package com.example.prueba;

import java.net.URLDecoder;

import com.example.prueba.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    private static final String TEL_PREFIX = "tel:";
    private static final String SMS_PREFIX = "sms:";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        
        String url ="http://www.medicoslaserena.cl/app";
        WebView view = (WebView) this.findViewById(R.id.webView1);       
        
        
        WebSettings webSettings = view.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
      
        
 
     
        
        class CustomWebViewClient extends WebViewClient {
    		
    		@Override
    		public boolean shouldOverrideUrlLoading(WebView wv, String url) {
    		    if(url.startsWith(TEL_PREFIX)) {
    		        Intent intent = new Intent(Intent.ACTION_DIAL);  // Intent intent = new Intent(Intent.ACTION_VIEW,
    		        intent.setData(Uri.parse(url));
    		        startActivity(intent);
    		        return true;
    		    }
    		    else if(url.startsWith(SMS_PREFIX)) {
    		    	
    	            String[] body=url.split("\\?body=");    	                	                   	                 	          
    		    	 Intent smsIntent = new Intent(Intent.ACTION_VIEW);
    		         smsIntent.setType("vnd.android-dir/mms-sms");
    		        // smsIntent=new Intent(Intent.ACTION_VIEW, Uri.parse(body[0]));
 	                 smsIntent.putExtra("sms_body", URLDecoder.decode(body[1]));
    		         smsIntent.putExtra("address", "");
    		         //smsIntent.putExtra("sms_body","Te recomiendo este doctor");    		         
    		         startActivity(smsIntent);          
    		         return true;
    		    }else if (url.startsWith("geo:")) { 
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url)); 
                    startActivity(intent); 
                    }
    		    return false;
    		}
    		
    		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    		    Log.e(String.valueOf(errorCode), description);

    		    // API level 5: WebViewClient.ERROR_HOST_LOOKUP
    		    if (errorCode == -2) {
    		   //   String summary = "<html><body style='background: black;'><p style='color: red;'>Unable to load information. Please check if your network connection is working properly or try again later.</p></body></html>";       view.loadData(summary, "text/html", null);
    		      return;
    		    }

    		    // Default behaviour
    		    super.onReceivedError(view, errorCode, description, failingUrl);
    		  }
    	}
        
        view.setWebViewClient(new CustomWebViewClient());
 
        view.loadUrl(url);
        
             

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        
        
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    
    
}
