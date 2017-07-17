package cx.ath.chuacw.GreedySpidersEnable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import cx.ath.chuacw.GreedySpidersEnable.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.FileObserver;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GreedySpidersEnableSolution extends Activity {
	// Application TAG
	private static final String TAG = "GreedySpiders";
	private static final String sRemoveLine = "hintDatetime"; // line to remove
	// Directory of application
	private static final String sDirectory = "/data/data/com.blyts.greedyspiders.free";
	// Name of preference
	private static final String sFilename = "GreedySpidersPrefs.xml";
// For a new application to hack, change the above constants, that's all...
	
	FileObserver mfs;
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		startObserver();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		startObserver();
	}

	private void createObserver() {
		Log.v(TAG, "starting Observer");
        mfs = new FileObserver(sDirectory, FileObserver.CLOSE_WRITE){
			@Override
			public void onEvent(int event, String path) {
				Log.v(TAG, "Event observed!");
				if ((path!=null)&&(path.contains(sFilename))) {
					mfs.stopWatching();
					removeLastAccess();
					mfs.startWatching();
				}
			}
        };
	}
	
	private void startObserver() {
		if (mfs==null) {
			createObserver();
		}
		mfs.startWatching();
	}
	
	private void stopObserver() {
		if (mfs!=null) mfs.stopWatching();
		mfs = null;
	}
	
	private final void checkFinishing() {
		if (isFinishing()) {
			Log.v(TAG, "stopping Observer");
			stopObserver();
		}
	}
	
	@Override
	protected void onPause() {
		checkFinishing();
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		stopObserver();
		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		checkFinishing();
		super.onStop();
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//        EnableObserver(); // Will be called in onResume
        Button btnEnableSolution = (Button)findViewById(R.id.btnEnableSolution);
        btnEnableSolution.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeLastAccess();
			}
		});
    }
    
    private static final String sChMod = "chmod 777 "; 
    
    private void removeLastAccess() {
    	Log.v(TAG, "Creating patch file...");
		ShellCommand sc = new ShellCommand();
// Change access to the directory
		String prefsDirectory = String.format("%s/shared_prefs/", sDirectory);
		String sCommand = String.format("%s%s", new Object[] {sChMod, prefsDirectory});
		sc.su.runWaitFor(sCommand);
		// Change access to the file
		sCommand = String.format("%s%s%s", new Object[] {sChMod, prefsDirectory, sFilename});
		sc.su.runWaitFor(sCommand);
		String sFullFilename = String.format("%s%s", new Object[] {prefsDirectory, sFilename});
		String sTempFilename = String.format("%stemp.xml", prefsDirectory);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(sFullFilename));
			String Line = null;
			BufferedWriter bw = new BufferedWriter(new FileWriter(sTempFilename));
			while ((Line=br.readLine())!=null) {
				if (!Line.contains(sRemoveLine)) {
				  bw.write(Line);
				  bw.write("\n");
				}
			}
			bw.close();
			br.close();
			br = null;
			bw = null;
			Log.v(TAG, "Created new patch file successfully!");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Exception "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sGiveFullAccess = String.format("%s%s", new Object[] {sChMod, sTempFilename});
		sc.su.runWaitFor(sGiveFullAccess);
		File f = new File(sFullFilename);
		f.delete();
		File f2 = new File(sTempFilename);
		f2.renameTo(f);
		f = null;
		f2 = null;
		sc = null;
		Log.v(TAG, "Patched file now in place!");
    }
}