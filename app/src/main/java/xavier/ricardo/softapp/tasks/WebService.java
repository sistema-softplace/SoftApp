package xavier.ricardo.softapp.tasks;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;

public class WebService {

    private static final String SERVIDOR = "softplacemoveis.dyndns.org:8080";
    private static final String SERVICO = "softws";

    public static String getServidor() {
        return SERVIDOR;
    }

    public static String getServico() {
        return SERVICO;
    }
}
