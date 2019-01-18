package com.example.tablets.slotmachine;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RestauranteAdapter extends BaseAdapter {
    String [] datos;
    int[] imgs;
    Context context;
    LayoutInflater inflater;

    public RestauranteAdapter(Context context, String[] datos, int[] imgs) {
        this.datos = datos;
        this.imgs = imgs;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return datos.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.spinneritem, null);

        TextView restaurante = convertView.findViewById(R.id.txtSpinner);
        restaurante.setText(datos[position]);
        ImageView image = convertView.findViewById(R.id.imageView);
        image.setImageResource(imgs[position]);
        return(convertView);
    }

    public void actualizarDatos(String[] datos, int[] imgs) {
        this.datos = datos;
        this.imgs = imgs;
    }
}
