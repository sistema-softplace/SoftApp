package xavier.ricardo.softapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import xavier.ricardo.softapp.R;
import xavier.ricardo.softapp.tasks.AgendaTask;
import xavier.ricardo.softapp.tasks.EncerramentoTask;
import xavier.ricardo.softapp.tasks.FotoTask;

public class MainActivity extends Activity {

    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int ALTERAR_DATA = 3;
    private String imageFilePath;
    private String timeStamp;
    private TextView tvDiaSemana;
    private DatePicker dpData;
    private String usuario;
    private Agenda agenda;
    private String chaveFoto;
    private View parentFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.title_activity_main);

        Calendar dataInicial = Calendar.getInstance();
        dataInicial.setTime(new Date());

        Intent intent = getIntent();
        usuario = intent.getStringExtra("usuario");
        String data = intent.getStringExtra("data");
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        try {
            dataInicial.setTime(df.parse(data));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tvDiaSemana = (TextView) findViewById(R.id.tvDiaSemana);
        df = new SimpleDateFormat("EEE");
        tvDiaSemana.setText(df.format(dataInicial.getTime()));

        final MainActivity contexto = this;

        dpData = (DatePicker) findViewById(R.id.dpData);

        dpData.init(dataInicial.get(Calendar.YEAR), dataInicial.get(Calendar.MONTH),
                dataInicial.get(Calendar.DAY_OF_MONTH), new OnDateChangedListener() {

                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        Calendar cal = Calendar.getInstance();
                        cal.set(year, monthOfYear, dayOfMonth);

                        tvDiaSemana = (TextView) findViewById(R.id.tvDiaSemana);
                        SimpleDateFormat df = new SimpleDateFormat("EEE");
                        tvDiaSemana.setText(df.format(cal.getTime()));

                        new AgendaTask(contexto, usuario, cal).execute();
                    }
                });

        new AgendaTask(this, usuario, dataInicial).execute();

    }

    public void resultadoAgenda(Agenda agenda) {

        this.agenda = agenda;

        ListView lvAgenda = (ListView) findViewById(R.id.lvCompromissos);
        AgendaAdapter adapter = new AgendaAdapter(this, agenda.getCompromissos());
        lvAgenda.setAdapter(adapter);

    }

    public void endereco(View v) {

        Compromisso compromisso = null;
        for (Compromisso c : agenda.getCompromissos()) {
            if (c.getHora().equals((String) v.getTag())) {
                compromisso = c;
                break;
            }
        }

        Intent intent = new Intent(this, EnderecoActivity.class);
        intent.putExtra("rua", compromisso.getRua() + "," + compromisso.getNro() + " " + compromisso.getComplemento());
        intent.putExtra("bairro", compromisso.getBairro() + "," + compromisso.getCidade());
        startActivity(intent);

    }

    public void ligar(View v) {

        View p = (View) v.getParent();
        TextView tvFones = (TextView) p.findViewById(R.id.tvFones);
        String[] fones = tvFones.getText().toString().split(" ");

        if (fones.length == 0) {
            return;
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }

        if (fones.length == 1) {
            String fone = fones[0];
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + tiraFormatacao(fone)));
            startActivity(intent);
            return;
        }

        PopupMenu popup = new PopupMenu(MainActivity.this, v);
        popup.getMenuInflater().inflate(R.menu.fones, popup.getMenu());

        popup.getMenu().getItem(0).setTitle(fones[0]);
        popup.getMenu().getItem(1).setTitle(fones[1]);
        if (fones.length > 2) {
            popup.getMenu().getItem(2).setVisible(true);
            popup.getMenu().getItem(2).setTitle(fones[1]);
        } else {
            popup.getMenu().getItem(2).setVisible(false);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("MissingPermission")
            public boolean onMenuItemClick(MenuItem item) {
                String fone = item.getTitle().toString();
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + tiraFormatacao(fone)));
                startActivity(intent);
                return true;
            }
        });

        popup.show();

    }

    public void abrir(View v) {

        View p = (View) v.getParent();
        TextView tvPedido = (TextView) p.findViewById(R.id.tvPedido);
        String pdf = tvPedido.getText().toString();
        Uri uri = Uri.parse("http://ricardoxavier.no-ip.org/soft/pedidos/" + pdf);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);

    }

    public void abrirOrcamento(View v) {

        View p = (View) v.getParent();
        TextView tvOrcamento = (TextView) p.findViewById(R.id.tvOrcamento);
        String pdf = tvOrcamento.getText().toString();
        Uri uri = Uri.parse("http://ricardoxavier.no-ip.org/soft/pedidos/" + pdf);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);

    }

    public void detalhes(View v) {

        Button btn = (Button) v;
        try {
            Intent intent = new Intent(this, DetalhesActivity.class);
            String[] partes = btn.getText().toString().split(" ");
            intent.putExtra("fornecedor", partes[0]);
            intent.putExtra("data", partes[1]);
            intent.putExtra("orcamento", partes[2].split("-")[0]);
            intent.putExtra("pedido", partes[2].split("-")[1]);
            startActivity(intent);

        } catch (Exception e) {
        }
    }

    public void anexos(View v) {

        try {
            Intent intent = new Intent(this, AnexoActivity.class);
            String tag = (String) v.getTag();
            String[] partes = tag.split(" ");
            intent.putExtra("fornecedor", partes[0]);
            intent.putExtra("data", partes[1]);
            intent.putExtra("orcamento", partes[2].split("-")[0]);
            startActivity(intent);

        } catch (Exception e) {
        }

    }

    private String tiraFormatacao(String fone) {
        String ddd = fone.substring(1, 3);
        String prefixo = null;
        String sufixo = null;
        if (fone.length() == 14) {
            // (31)98874-9526
            // 012345678901234
            prefixo = fone.substring(4, 9);
            sufixo = fone.substring(10, 14);
        } else {
            // (31)3378-9526
            // 01234567890123
            prefixo = fone.substring(4, 8);
            sufixo = fone.substring(9, 13);
        }
        if (ddd.equals("00")) {
            ddd = "";
        }
        if (ddd.length() == 2) {
            ddd = "0" + ddd;
        }
        return ddd + prefixo + sufixo;
    }

    public void encerrar(View v) {

        String chave = (String) v.getTag();

        String[] partes = chave.split(";");
        String usuario = partes[0].trim();
        String dt = partes[1].trim();
        ImageView im = new ImageView(this);
        for (Compromisso compromisso : agenda.getCompromissos()) {
            if (compromisso.getUsuario().trim().equals(usuario)
                    && compromisso.getData().trim().equals(dt)) {
                Intent intent = new Intent(this, EncerrarActivity.class);
                intent.putExtra("chave", chave);
                startActivityForResult(intent, 1);
                break;
            }
        }
    }

    public void reagendar(View v) {

        View p = (View) v.getParent();
        Button encerrar = p.findViewById(R.id.btEncerrar);
        String chave = (String) encerrar.getTag();
        String previsao = String.format("%04d-%02d-%02d", dpData.getYear(), dpData.getMonth()+1, dpData.getDayOfMonth());
        String[] partes = chave.split(";");
        String usuario = partes[0].trim();
        String data = partes[1].trim();

        Intent intent = new Intent(this, ReagendarActivity.class);
        intent.putExtra("usuario", usuario);
        intent.putExtra("data", data);
        intent.putExtra("previsao", previsao);
        startActivityForResult(intent, ALTERAR_DATA);
    }

    private File createImageFile() throws IOException {
        timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, ".jpg", storageDir);
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    public void foto(View v) {
        chaveFoto = v.getTag().toString();
        parentFoto = ((View) v.getParent().getParent()).findViewById(R.id.llFoto);
        //try {
            //File photoFile = createImageFile();
            //Uri photoURI = FileProvider.getUriForFile(this, "xavier.ricardo.softapp.provider", photoFile);
            //Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Intent takePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        //} catch (IOException e) {
            //e.printStackTrace();
        //}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ALTERAR_DATA) {
            finish();
            return;
        }

        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                try {

                    ImageView imageView;
                    if (parentFoto != null) {
                        imageView = (ImageView) parentFoto.findViewById(R.id.ivFoto);
                    } else {
                        imageView = (ImageView) findViewById(R.id.ivFoto);
                    }
                    parentFoto = null;
                    imageView.setImageURI(data.getData());
                    /*
                    imageFilePath = data.getData().getPath();
                    File file = new File(imageFilePath);
                    byte[] bArray = new byte[(int) file.length()];
                    FileInputStream stream = new FileInputStream(file);
                    stream.read(bArray);
                    stream.close();
                    String base64 = Base64.encodeToString(bArray, Base64.DEFAULT);
                    file.delete();

                     */
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bArray = baos.toByteArray();
                    String base64 = Base64.encodeToString(bArray, Base64.DEFAULT);

                    Button btn = (Button) findViewById(R.id.btDetalhes);
                    String[] partes = btn.getText().toString().split(" ");
                    if (chaveFoto != null) {
                        partes = chaveFoto.split(" ");
                    }

                    if (partes.length >= 3) {
                        String fornecedor = partes[0].trim();
                        String[] dmy = partes[1].split("/");
                        if (dmy.length == 3) {
                            String dt = String.format("%04d-%02d-%02d", Integer.parseInt(dmy[2]), Integer.parseInt(dmy[1]), Integer.parseInt(dmy[0]));
                            String orcamento = partes[2].split("-")[0];
                            timeStamp =
                                    new SimpleDateFormat("yyyyMMdd_HHmmss",
                                            Locale.getDefault()).format(new Date());
                            new FotoTask(this, fornecedor, dt, orcamento, base64, timeStamp).execute();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            String chave = data.getStringExtra("chave");
            String[] partes = chave.split(";");
            String usuario = partes[0].trim();
            String dt = partes[1].trim();
            String observacao = partes.length > 2 ? partes[2] : "";
            for (Compromisso compromisso : agenda.getCompromissos()) {
                if (compromisso.getUsuario().trim().equals(usuario)
                        && compromisso.getData().trim().equals(dt)) {
                    compromisso.setEncerramento(observacao);
                    break;
                }
            }
        }
    }

    public void onTaskResult(String result) {
        if (result.startsWith("ok")) {
            result = "Foto anexada ao orçamento";
        }
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }


}
