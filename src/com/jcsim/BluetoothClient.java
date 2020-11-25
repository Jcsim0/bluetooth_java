package com.jcsim;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: Jcsim
 * @Date: 2020/11/25 15:14
 * @Description:蓝牙客户端类
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
public class BluetoothClient {
    private StreamConnection streamConnection;//流连接
    private OnDiscoverListener onDiscoverListener = null;//发现监听
    private OnClientListener onClientListener = null;//客户端监听

    /**
     * 客户端监听
     */
    public interface OnClientListener {
        void onConnected(InputStream inputStream, OutputStream outputStream);
        void onConnectionFailed();
        void onDisconnected();
        void onClose();
    }

    /**
     * 发现监听
     */
    public interface OnDiscoverListener {
        void onDiscover(RemoteDevice remoteDevice);
    }


    /**
     * 无参构造函数
     */
    public BluetoothClient() {
    }

    /**
     * 查找所有
     * @throws IOException
     * @throws InterruptedException
     */
    public void find() throws IOException, InterruptedException {
        //附近所有的蓝牙设备，必须先执行 runDiscovery
        Set<RemoteDevice> devicesDiscovered = RemoteDeviceDiscovery.getDevices();
        Iterator<RemoteDevice> itr = devicesDiscovered.iterator();
        //连接
        while (itr.hasNext()) {
            RemoteDevice remoteDevice = itr.next();

            onDiscoverListener.onDiscover(remoteDevice);
        }
    }

    /**
     * 启动连接
     * @param remoteDevice
     * @param serviceUUID
     * @throws IOException
     * @throws InterruptedException
     */
    public void startClient(RemoteDevice remoteDevice, String serviceUUID) throws IOException, InterruptedException {
        String url = RemoteDeviceDiscovery.searchService(remoteDevice, serviceUUID);

        streamConnection = (StreamConnection) Connector.open(url);
        if (this.onClientListener != null) {
            this.onClientListener.onConnected(streamConnection.openInputStream(), streamConnection.openOutputStream());
        }
    }

    public OnDiscoverListener getOnDiscoverListener() {
        return onDiscoverListener;
    }


    public void setOnDiscoverListener(OnDiscoverListener onDiscoverListener) {
        this.onDiscoverListener = onDiscoverListener;
    }


    public OnClientListener getClientListener() {
        return onClientListener;
    }


    public void setClientListener(OnClientListener onClientListener) {
        this.onClientListener = onClientListener;
    }


}