package xavier.ricardo.softapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import xavier.ricardo.softapp.tasks.LoginTask;
import xavier.ricardo.softapp.tasks.NotificadorService;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// teste ca-app-pub-3940256099942544/6300978111
		// real  ca-app-pub-0381609228541841/9233128121
		// app   ca-app-pub-0381609228541841~8494761522
		// novo  ca-app-pub-0381609228541841/8083720641
		//MobileAds.initialize(this, "ca-app-pub-0381609228541841~8494761522");
		//AdView mAdView = (AdView) findViewById(R.id.adView);
		//AdRequest adRequest = new AdRequest.Builder().build();
		//mAdView.loadAd(adRequest);
		/*
		build
		    implementation 'com.google.android.gms:play-services-ads:18.3.0'
		manifest
        <meta-data
            android:name="com.google.android.gms.version"
            android:value="@integer/google_play_services_version" />
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-0381609228541841~8494761522" />
                    <activity
            android:name="com.google.android.gms.ads.AdActivity"
            android:configChanges="keyboard|keyboardHidden|orientation|screenLayout|uiMode|screenSize|smallestScreenSize" />
    <TextView
        android:layout_width="fill_parent"
        android:layout_height="0dip"
        android:layout_weight="1" />
    <com.google.android.gms.ads.AdView
        xmlns:ads="http://schemas.android.com/apk/res-auto"
        android:id="@+id/adView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_centerHorizontal="true"
        ads:adSize="BANNER"
        ads:adUnitId="ca-app-pub-0381609228541841/9233128121" >
    </com.google.android.gms.ads.AdView>
		 */
		
		SharedPreferences shared = getSharedPreferences("softapp", Context.MODE_PRIVATE);
		
		boolean salvar = shared.getBoolean("salvar", false);
		if (salvar) {
			CheckBox cbSalvar = (CheckBox) findViewById(R.id.cbSalvar);
			cbSalvar.setChecked(true);
			
			String usuario = shared.getString("usuario", "");
			if (!usuario.trim().equals("")) {
				EditText etUsuario = (EditText) findViewById(R.id.etUsuario);
				etUsuario.setText(usuario);
			}
			
			String senha = shared.getString("senha", "");
			if (!senha.trim().equals("")) {
				EditText tpSenha = (EditText) findViewById(R.id.tpSenha);
				tpSenha.setText(senha);
			}

		}

	}
	
	public void login(View v) {
		
		if (!NetworkUtils.verificaConexao(this)) {
			Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_LONG).show();
			return;
		}		
	
		EditText etUsuario = (EditText) findViewById(R.id.etUsuario);
		String usuario = etUsuario.getText().toString().trim();
		
		if (usuario.equals("")) {
			Toast.makeText(this, "Usuário não preenchido", Toast.LENGTH_LONG).show();
			etUsuario.requestFocus();
			return;
		}
		EditText tpSenha = (EditText) findViewById(R.id.tpSenha);
		String senha = tpSenha.getText().toString().trim();
		if (senha.equals("")) {
			Toast.makeText(this, "Senha não preenchida", Toast.LENGTH_LONG).show();
			tpSenha.requestFocus();
			return;
		}
		
		new LoginTask(this, usuario, senha).execute();
		
	}
	
	public void resultadoLogin(String mensagem) {

		if (mensagem == null) {
			Toast.makeText(this, "Erro na comunicação", Toast.LENGTH_LONG).show();
			return;
		}

		if (!mensagem.startsWith("ok")) {
			Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
			return;
		}
		
		boolean administrador = false;
		String[] args = mensagem.split(":");
		if (args.length > 1) {
			administrador = args[1].equalsIgnoreCase("S");
		}
		
		EditText etUsuario = (EditText) findViewById(R.id.etUsuario);
		String usuario = etUsuario.getText().toString().trim();
		
		EditText tpSenha = (EditText) findViewById(R.id.tpSenha);
		String senha = tpSenha.getText().toString().trim();
		
		CheckBox cbSalvar = (CheckBox) findViewById(R.id.cbSalvar);
		boolean salvar = cbSalvar.isChecked();		
		
		SharedPreferences shared = getSharedPreferences("softapp", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		editor.putString("usuario", usuario);
		editor.putString("senha", senha);
		editor.putBoolean("salvar", salvar);
		editor.commit();
		
		boolean rodando = false;
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (service.service.getClassName().contains("NotificadorService")) {
				rodando = true;
				break;
			}
		}
		
		if (!rodando) {
			Intent intentService = new Intent(this, NotificadorService.class);
			intentService.putExtra("usuario", usuario);
			startService(intentService);
		}

		Intent intent = new Intent(this, CalendarActivity.class);
		intent.putExtra("usuario", usuario);
		intent.putExtra("administrador", administrador);
		startActivity(intent);
		
	}

}
