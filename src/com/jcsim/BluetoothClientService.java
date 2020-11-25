package com.jcsim;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: Jcsim
 * @Date: 2020/11/25 15:17
 * @Description:蓝牙客户端业务类
 */
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.ConnectionNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

public class BluetoothClientService {

    public static void main(String[] argv) {

        final String serverUUID = "1000110100001000800000805F9B34FB"; //需要与服务端相同

        BluetoothClient client = new BluetoothClient();

        Vector<RemoteDevice> remoteDevices = new Vector<>();

        client.setOnDiscoverListener(new BluetoothClient.OnDiscoverListener() {

            @Override
            public void onDiscover(RemoteDevice remoteDevice) {
                remoteDevices.add(remoteDevice);
            }

        });

        client.setClientListener(new BluetoothClient.OnClientListener() {

            @Override
            public void onConnected(InputStream inputStream, OutputStream outputStream) {
                System.out.printf("Connected");
                //添加通信代码

            }

            @Override
            public void onConnectionFailed() {
                System.out.printf("Connection failed");
            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onClose() {

            }

        });

        try {
            client.find();

            if (remoteDevices.size() > 0 ) {

                client.startClient(remoteDevices.firstElement(), serverUUID);
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }catch (ConnectionNotFoundException e){
            System.out.println("当前蓝牙不在线");
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}