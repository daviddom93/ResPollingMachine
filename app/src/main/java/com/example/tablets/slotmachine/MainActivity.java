package com.example.tablets.slotmachine;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Random;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    private Button btnVotar, btnFinalizar, btnReset, btnSegunda, btnResultados;
    private TextView txtVotosTotales, txtResultado, txtFecha;
    private Spinner spinnerVotos;
    private int totales = 0;
    private int[] votos, imgs, empatados;
    private String[] datos;
    private ImageView imgWinner1, imgWinner2, imgWinner3, imgEaster;
    Winner winner1, winner2, winner3;
    boolean isStarted, segundaVuelta;
    RestauranteAdapter adapter;
    AlertDialog pin_dialog, ley_dialog, easter_dialog, chart_dialog;
    PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btnVotar = findViewById(R.id.btnVotar);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        btnResultados = findViewById(R.id.btnResultados);
        btnReset = findViewById(R.id.btnReset);
        btnSegunda = findViewById(R.id.btnSegunda);
        txtVotosTotales = findViewById(R.id.txtVotosTotales);
        txtResultado = findViewById(R.id.txtResultado);
        txtFecha = findViewById(R.id.txtFecha);
        spinnerVotos = findViewById(R.id.spinnerVotos);
        imgWinner1 = findViewById(R.id.imgWinner1);
        imgWinner2 = findViewById(R.id.imgWinner2);
        imgWinner3 = findViewById(R.id.imgWinner3);
        segundaVuelta = false;

        inicializarDatos();

        adapter = new RestauranteAdapter(this, datos, imgs);
        spinnerVotos.setAdapter(adapter);
        //Contamos el voto
        btnVotar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("VOTO", "Voto recibido a " + datos[spinnerVotos.getSelectedItemPosition()]);
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Voto recibido a " + datos[spinnerVotos.getSelectedItemPosition()], Toast.LENGTH_SHORT);

                toast.show();
                int voto = spinnerVotos.getSelectedItemPosition();
                votos[voto]++;
                totales++;
                txtVotosTotales.setText(getString(R.string.recibidos) + totales);
                spinnerVotos.setSelection(0);
            }
        });

        btnFinalizar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pinDialog(true);
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pinDialog(false);
            }
        });

        btnResultados.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chartDialog();
            }
        });

        //Segunda vuelta
        btnSegunda.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String[] datosAux = new String[empatados.length];
                int[] imgsAux = new int[empatados.length];
                for (int i = 0; i < empatados.length; i++) {
                    datosAux[i] = datos[empatados[i]];
                    imgsAux[i] = imgs[empatados[i]];
                }
                Log.i("VOTO", "Hay " + empatados.length + " restaurantes en la segunda vuelta");
                datos = datosAux;
                imgs = imgsAux;
                adapter.actualizarDatos(datos, imgs);
                adapter.notifyDataSetChanged();
                segundaVuelta = true;
                btnReset.performClick();
            }
        });
    }

    private void finalizar() {
        Log.i("VOTO", "Votación finalizada");
        if(!segundaVuelta) {
            // Aleatorizamos los votos aleatorios
            for (int i = 0; i < votos[0]; i++) {
                int indice = (int) (Math.random() * (votos.length - 1) + 1);
                votos[indice]++;
                Log.i("VOTO", "Aleatorio a: " + indice);
            }
            votos[0] = 0;
        }

        // Contamos votos
        //Votos totales
        totales=0;
        for (int i = 0; i < votos.length; i++)
            totales += votos[i];

        // Votos por restaurante
        int idGanador = -1;
        int maximo = -1;
        for (int i = 0; i < votos.length; i++) {
            if (votos[i] > maximo) {
                idGanador = i;
                maximo = votos[i];
            }
        }

        // Vemos el número de empatados
        int numEmpatados = 0;
        for (int i = 0; i < votos.length; i++) {
            if (votos[i] == maximo) {
                numEmpatados++;
            }
        }

        // Si hay empate
        if (numEmpatados > 1) {
            Log.i("VOTO", "Hay empate entre " + numEmpatados);
            empatados = new int[numEmpatados];
            int aux = 0;
            for (int i = 0; i < votos.length; i++) {
                if (votos[i] == maximo) {
                    empatados[aux] = i;
                    Log.i("VOTO", "" + empatados[aux]);
                    aux++;
                }
            }
            String textEmpatados = "";
            for (int i = 0; i < numEmpatados; i++) {
                if (i == 0 && (i != numEmpatados - 2)) { // Primer restaurante
                    textEmpatados = datos[empatados[i]] + ", ";
                } else if (i == numEmpatados - 2) { // Penúltimo restaurante
                    textEmpatados = textEmpatados + datos[empatados[i]] + " y ";
                } else if (i == numEmpatados - 1) { // Último restaurante
                    textEmpatados = textEmpatados + datos[empatados[i]] + ".";
                } else { // Restaurantes intermedios
                    textEmpatados = textEmpatados + datos[empatados[i]] + ", ";
                }
            }
            Log.i("VOTO", textEmpatados);

            if(!segundaVuelta) {
                txtResultado.setText("Hay empate entre " + numEmpatados + " restaurantes. Los empatados son: " + textEmpatados);
                txtResultado.setVisibility(VISIBLE);
                btnResultados.setVisibility(VISIBLE);
                btnSegunda.setVisibility(VISIBLE);
            } else {
                int[] imgsAux = new int[empatados.length];
                for (int i = 0; i < empatados.length; i++) {
                    imgsAux[i] = imgs[empatados[i]];
                }
                imgs = imgsAux;
                txtResultado.setText("Hay empate entre: " + textEmpatados +  "\nEL GANADOR ALEATORIO ES... ");
                txtResultado.setVisibility(VISIBLE);
                int numAleatorio = (int) Math.round(Math.random());
                int indice = (int) numAleatorio * (empatados.length - 1);
                Log.i("VOTO", "Aleatorio: " + numAleatorio);
                Log.i("VOTO", "Indice: " + indice);
                slotMachine(empatados[indice], imgs);
            }
        } else {
            txtResultado.setText("EL GANADOR DE LA SEMANA CON " + maximo + " VOTOS ES... ");
            txtResultado.setVisibility(VISIBLE);
            if(datos.length < 10)
                slotMachine(idGanador, imgs);
            else
                slotMachine(idGanador, imgs);
        }
        btnFinalizar.setEnabled(false);
        btnVotar.setEnabled(false);
    }

    private void reset () {
        totales = 0;
        for (int i = 0; i < votos.length; i++)
            votos[i] = 0;
        txtResultado.setVisibility(View.INVISIBLE);
        btnFinalizar.setEnabled(true);
        btnVotar.setEnabled(true);
        btnSegunda.setVisibility(GONE);
        btnResultados.setVisibility(GONE);
        txtVotosTotales.setText(getString(R.string.recibidos) + totales);
        imgWinner1.setImageResource(0);
        imgWinner2.setImageResource(0);
        imgWinner3.setImageResource(0);
        if(!segundaVuelta)
            inicializarDatos();
    }

    public void slotMachine(final int idGanador, int [] imgs) {
        winner1 = new Winner(new Winner.WinnerListener() {
            @Override
            public void newImage(final int img) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imgWinner1.setImageResource(img);
                    }
                });
            }
        }, 250, randomLong(0, 200), idGanador-1, imgs);

        winner1.start();

        winner2 = new Winner(new Winner.WinnerListener() {
            @Override
            public void newImage(final int img) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imgWinner2.setImageResource(img);
                    }
                });
            }
        }, 150, randomLong(150, 400), idGanador-1, imgs);

        winner2.start();

        winner3 = new Winner(new Winner.WinnerListener() {
            @Override
            public void newImage(final int img) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imgWinner3.setImageResource(img);
                    }
                });
            }
        }, 200, randomLong(150, 400), idGanador-1, imgs);

        winner3.start();

        isStarted = true;

        imgWinner1 = findViewById(R.id.imgWinner1);
        imgWinner2 = findViewById(R.id.imgWinner2);
        imgWinner3 = findViewById(R.id.imgWinner3);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isStarted) {
                    winner1.stopWinner();
                    winner2.stopWinner();
                    winner3.stopWinner();

                    isStarted = false;
                    btnResultados.setVisibility(VISIBLE);
                }
            }
        }, 4500);
    }

    public void pinDialog (final boolean finalizar) {
        Builder builder = new Builder(this);
        LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.pindialog, null);

        builder.setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pin_dialog.cancel();
                pin_dialog = null;
            }
        });

        builder.setPositiveButton(R.string.accept,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText et = (EditText) pin_dialog.findViewById(R.id.editPin);
                String pass = et.getText().toString();

                if (pass.equals("0000")) {
                    if(finalizar) {
                        finalizar();
                    } else {
                        reset();
                    }
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.passIncorrecta , Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        builder.setView(v);
        pin_dialog = builder.create();
        pin_dialog.show();
    }

    public void leyDialog () {
        Builder builder = new Builder(this);
        LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.leydialog, null);

        builder.setNeutralButton("OK",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setView(v);
        ley_dialog = builder.create();
        ley_dialog.show();

        imgEaster = ley_dialog.findViewById(R.id.imgEaster);
        imgEaster.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                easterDialog();
                return true;
            }
        });
    }

    public void easterDialog () {
        Builder builder = new Builder(this);
        LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.easterdialog, null);

        builder.setNeutralButton("OK",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setView(v);
        easter_dialog = builder.create();
        easter_dialog.show();

        ImageView imageView = (ImageView) easter_dialog.findViewById(R.id.imgEasterEgg);
        int randomGif=(int) (Math.random() * 5) + 1;
        int idGif = getResources().getIdentifier("gif"+Integer.toString(randomGif),"drawable",getPackageName());
        GlideApp.with(this).load(idGif).into(imageView);
    }

    public void chartDialog () {
        Builder builder = new Builder(this);
        LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.chartdialog, null);

        builder.setNeutralButton("OK",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setView(v);
        chart_dialog = builder.create();
        chart_dialog.show();

        pieChart = (PieChart) chart_dialog.findViewById(R.id.pieChart);

        //propiedades de grafico
        pieChart.setRotationEnabled(true);
        pieChart.animateXY(1000, 1000);
        pieChart.setHoleRadius(36f);

        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        ArrayList<PieEntry> yVals = new ArrayList<PieEntry>();

        //Añade datos a grafico
        for(int index=1;index<datos.length;index++) {
            if(votos[index]>0){
                float votos2 = (((float)votos[index])/totales)*100f;
                yVals.add(new PieEntry(votos2,datos[index]));
            }
        }

        PieDataSet dataSet = new PieDataSet(yVals, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter( new PercentFormatter() );
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.getDescription().setEnabled(false);
        pieChart.setData(data);
        pieChart.highlightValues(null);
        pieChart.invalidate();

    }

    public static final Random RANDOM = new Random();

    public static long randomLong(long lower, long upper) {
        return lower + (long) (RANDOM.nextDouble() * (upper - lower));
    }

    private void inicializarDatos() {
        datos = getResources().getStringArray(R.array.restaurantes);
        imgs = new int[datos.length];
        votos = new int[datos.length];
        txtFecha.setText(R.string.semanaVoto);
        TypedArray ta = getResources().obtainTypedArray(R.array.imagenes);
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                imgs[i] = id;
            }
        }
        ta.recycle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_acerca:
                Builder builder = new Builder(this);
                builder.setTitle(R.string.titulo_acercade)
                        .setMessage(R.string.acercade)
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                AlertDialog ad = builder.create();
                ad.show();
                return true;
            case R.id.action_ley:
                leyDialog();
                return true;
            case R.id.action_settings:
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
