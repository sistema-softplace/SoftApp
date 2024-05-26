package xavier.ricardo.softapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import xavier.ricardo.softapp.tasks.ReagendarTask;

public class ReagendarActivity extends Activity {

    private DatePicker dpData;
    private TimePicker tpHora;
    private String usuario;
    private String data;
    private String previsao;

    private TextView tvDiaSemana;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reagendar);

        Calendar dataInicial = Calendar.getInstance();
        dataInicial.setTime(new Date());

        Intent intent = getIntent();
        usuario = intent.getStringExtra("usuario");
        data = intent.getStringExtra("data");
        previsao = intent.getStringExtra("previsao");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            dataInicial.setTime(df.parse(previsao));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tvDiaSemana = (TextView) findViewById(R.id.tvNovoDiaSemana);
        df = new SimpleDateFormat("EEE");
        tvDiaSemana.setText(df.format(dataInicial.getTime()));

        final ReagendarActivity contexto = this;

        dpData = (DatePicker) findViewById(R.id.dpNovaData);
        tpHora = (TimePicker) findViewById(R.id.tpNovaHora);

        tpHora.setCurrentHour(8);
        tpHora.setCurrentMinute(0);

        dpData.init(dataInicial.get(Calendar.YEAR), dataInicial.get(Calendar.MONTH), dataInicial.get(Calendar.DAY_OF_MONTH),

                new OnDateChangedListener() {

                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        Calendar cal = Calendar.getInstance();
                        cal.set(year, monthOfYear, dayOfMonth);

                        tvDiaSemana = (TextView) findViewById(R.id.tvNovoDiaSemana);
                        SimpleDateFormat df = new SimpleDateFormat("EEE");
                        tvDiaSemana.setText(df.format(cal.getTime()));

                    }
                });

    }

    public void confirmarReagendamento(View v) {
        String previsao = String.format("%04d-%02d-%02d %02d:%02d:00", dpData.getYear(), dpData.getMonth()+1, dpData.getDayOfMonth(),
                tpHora.getCurrentHour(), tpHora.getCurrentMinute());
        new ReagendarTask(this, usuario, data, previsao).execute();
    }

    public void resultadoReagendamento() {
        finish();
    }
}