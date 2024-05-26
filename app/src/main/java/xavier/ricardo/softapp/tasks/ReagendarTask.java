package xavier.ricardo.softapp.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import xavier.ricardo.softapp.ReagendarActivity;

public class ReagendarTask extends AsyncTask<String, Void, Void> {

	private ReagendarActivity contexto;
	private ProgressDialog progress;
	private String usuario;
	private String data;
	private String previsao;

	public ReagendarTask(ReagendarActivity contexto, String usuario, String data, String previsao) {
		this.contexto = contexto;
		this.usuario = usuario;
		this.data = data;
		this.previsao = previsao;
	}
	
	@Override
	protected void onPreExecute() {
		progress = new ProgressDialog((Context) contexto);
		progress.setMessage("Aguarde...");
		progress.show();		
		super.onPreExecute();		
	}

	@Override
	protected void onPostExecute(Void unused) {
		super.onPostExecute(unused);
		progress.dismiss();
		contexto.resultadoReagendamento();
	}
	
	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
	}

	@Override
	protected Void doInBackground(String... params) {
		
		try {
			
			String url = String.format("http://%s/%s/softws/reagendar/%s/%s/%s",
				WebService.getServidor(), WebService.getServico(), usuario.toLowerCase(), data, previsao);
			url = url.replace(" ", "%20");
			Log.i("SOFTAPP", url);
			
			HttpClient httpClient = new DefaultHttpClient();
			
			HttpPut httpPut = new HttpPut(url);
			HttpResponse httpResponse = httpClient.execute(httpPut);
			
			InputStream inputStream = httpResponse.getEntity().getContent();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line = "";
			StringBuilder resultStr = new StringBuilder();
			while ((line = bufferedReader.readLine()) != null) {
				resultStr.append(line);
			}
			inputStream.close();	
			//Log.i("SOFTAPP", resultStr.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
