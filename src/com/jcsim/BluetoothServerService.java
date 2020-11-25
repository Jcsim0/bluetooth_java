package com.jcsim;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: Jcsim
 * @Date: 2020/11/25 15:16
 * @Description:蓝牙服务端通信业务
 */
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothServerService {

    public static void main(String[] argv) {
        final String serverName = "Bluetooth Server Test";
        final String serverUUID = "1000110100001000800000805F9B34FB";  //根据需要自定义

        BluetoothServer server = new BluetoothServer(serverUUID, serverName);
        server.setServerListener(new BluetoothServer.OnServerListener() {

            @Override
            public void onConnected(InputStream inputStream, OutputStream outputStream) {
                System.out.printf("Connected");
                //添加通信代码
                System.out.println(server.serverName);
                System.out.println(server.serverUUID);

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onClose() {

            }

        });

        server.start();
    }
}