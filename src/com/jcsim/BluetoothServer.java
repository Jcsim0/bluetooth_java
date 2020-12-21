package com.jcsim;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: Jcsim
 * @Date: 2020/11/25 11:17
 * @Description:蓝牙服务端
 */
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BluetoothServer implements Runnable{

    //本机蓝牙设备
    private LocalDevice local = null;
    // 流连接
    private StreamConnection streamConnection = null;

    // 输入流
    private InputStream inputStream;
    private OutputStream outputStream;
    //接入通知
    private StreamConnectionNotifier notifier;
    //基于缓存的线程池
    private final static ExecutorService service = Executors.newCachedThreadPool();

    public String serverName;
    public String serverUUID;

    private OnServerListener mServerListener;

    /**
     * 服务监听接口
     */
    public interface OnServerListener {
        void onConnected(InputStream inputStream, OutputStream outputStream);
        void onDisconnected();
        void onClose();
    }

    /**
     * 蓝牙服务有参构造函数
     * @param serverUUID id
     * @param serverName 名称
     */
    public BluetoothServer(String serverUUID, String serverName) {
        this.serverUUID = serverUUID;
        this.serverName = serverName;
    }

    /**
     * 启动
     */
    public void start() {
        try {
            local = LocalDevice.getLocalDevice();
            if (!local.setDiscoverable(DiscoveryAgent.GIAC))
                System.out.println("请将蓝牙设置为可被发现");
            //作为服务端，被请求
            String url = "btspp://localhost:" +  serverUUID+ ";name="+serverName;
            notifier = (StreamConnectionNotifier) Connector.open(url);
            System.out.println("serverName="+serverName );
            service.submit(this);
        } catch (IOException e) {
            System.out.println(e.getMessage());;
        }
    }


    /**
     * 重写run（）
     */
    @Override
    public void run() {
        try {
            streamConnection = notifier.acceptAndOpen();                //阻塞的，等待设备连接
            inputStream = streamConnection.openInputStream();
            outputStream = streamConnection.openOutputStream();

            if (mServerListener != null) {
                mServerListener.onConnected(inputStream, outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }


    public OnServerListener getServerListener() {
        return mServerListener;
    }

    public void setServerListener(OnServerListener mServerListener) {
        this.mServerListener = mServerListener;
    }
}
