package com.example.tablets.slotmachine;

import android.util.Log;

public class Winner extends Thread {

    interface WinnerListener {
        void newImage(int img);
    }

    public int i;
    private WinnerListener winnerListener;
    private long duration;
    private long startTime;
    private boolean isStarted;
    private int idGanador;
    private static int[] imgs;

    public Winner(WinnerListener winnerListener, long duration, long startTime, int idGanador, int [] imgs) {
        this.winnerListener = winnerListener;
        this.duration = duration;
        this.startTime = startTime;
        i = 0;
        isStarted = true;
        this.idGanador = idGanador;
        this.imgs = imgs;
    }

    public void nextImage() {
        i++;
        if (i == imgs.length) {
            i = 0;
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(startTime);
        } catch (InterruptedException e) {
            Log.e("DEBUG","Tenemos un problema con el hilo de la ruleta.");
        }

        while(isStarted) {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                Log.e("DEBUG","Tenemos otro problema con el hilo de la ruleta.");
            }

            nextImage();

            if (winnerListener != null) {
                winnerListener.newImage(imgs[i]);
            }
        }
        winnerListener.newImage(imgs[idGanador+1]);
    }

    public void stopWinner() {
        isStarted = false;
    }
}

