package ch.cern.cerncertinstaller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.security.cert.X509Certificate;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.security.KeyChain;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private Button installButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		installButton = (Button) findViewById(R.id.installButton);
		installButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                	installCERTS(R.raw.cern_root_ca, "cern root ca");
                	installCERTS(R.raw.cern_root_ca2, "cern root ca 2");
                	installCERTS(R.raw.cern_trusted_ca, "cern trusted ca");
                	installCERTS(R.raw.cern_grid_ca, "cern grid ca");
            }
        });
	}

	protected void installCERTS(int id, String certName) {
		Intent intent = KeyChain.createInstallIntent();
		try {
		    InputStream is = getApplicationContext().getResources().openRawResource(id);
    		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    		int nRead;
    		byte[] data = new byte[16384];

    		while ((nRead = is.read(data, 0, data.length)) != -1) {
    		  buffer.write(data, 0, nRead);
    		}

    		buffer.flush();

    		byte [] cert = buffer.toByteArray();
		    buffer.close();
		    is.close();
		    X509Certificate x509 = X509Certificate.getInstance(cert);
		    intent.putExtra(KeyChain.EXTRA_CERTIFICATE, x509.getEncoded()); 
		    intent.putExtra(KeyChain.EXTRA_NAME, certName);
		    startActivityForResult(intent, 0);  // this works but shows UI
		} catch (Exception e) { e.printStackTrace(); }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
