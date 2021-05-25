package com.example.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyBluetoothService {
    private static final String TAG = "ANTS_PLAYER_CLASS";
    private Handler handler;

    private interface MassageConstants {
        public static final int MASSAGE_READ = 0;
        public static final int MASSAGE_WRITE = 1;
        public static final int MASSAGE_TOAST = 2;
    }

   class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer;

        public ConnectedThread(BluetoothSocket socket){
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Input Error", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Output Error", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            mmBuffer = new byte[1024];
            int numBytes;

            while(true) {
                try {
                    numBytes = mmInStream.read(mmBuffer);

                    Message readMsg = handler.obtainMessage(MassageConstants.MASSAGE_READ, numBytes, -1, mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void Write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                Message writeMsg = handler.obtainMessage(MassageConstants.MASSAGE_WRITE, -1, -1, mmBuffer);
                writeMsg.sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();

                Message writeErrorMsg =
                        handler.obtainMessage(MassageConstants.MASSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
