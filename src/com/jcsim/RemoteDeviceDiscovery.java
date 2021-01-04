package com.jcsim;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: Jcsim
 * @Date: 2020/11/25 15:15
 * @Description:设备查找类
 */
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
public class RemoteDeviceDiscovery {

    public final static Set<RemoteDevice> devicesDiscovered = new HashSet<RemoteDevice>();

    public final static Vector<String> serviceFound = new Vector<String>();

    final static Object serviceSearchCompletedEvent = new Object();
    final static Object inquiryCompletedEvent = new Object();


    /**
     * 发现监听
     * 蓝牙发现代理在请求阶段的不同时候会分别调用DiscoveryListener（发现监听器）不同的回调方法：
     * .deviceDiscovered() 指出是否有设备被发现。
     * .inquiryCompleted() 指出是否请求已经成功、触发一个错误或已被取消。
     */
    private static DiscoveryListener listener = new DiscoveryListener() {
        public void inquiryCompleted(int discType) {
            System.out.println("#" + "搜索完成");
            synchronized (inquiryCompletedEvent) {
                //唤醒对象的等待池中的所有线程，进入锁池。
                inquiryCompletedEvent.notifyAll();
            }
        }

        /**
         * 发现设备
         * @param remoteDevice
         * @param deviceClass
         */
        @Override
        public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
            devicesDiscovered.add(remoteDevice);

            try {
                System.out.println("#发现设备" + remoteDevice.getFriendlyName(false)+"   设备地址："+remoteDevice.getBluetoothAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /**
         * 发现服务
         * @param transID id
         * @param servRecord 服务记录
         *servicesDiscovered() 表示是否服务已被发现。
         */
        @Override
        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            for (int i = 0; i < servRecord.length; i++) {
                String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                if (url == null) {
                    continue;
                }
                serviceFound.add(url);
                DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
                if (serviceName != null) {
                    System.out.println("service " + serviceName.getValue() + " found " + url);
                } else {
                    System.out.println("service found " + url);
                }
            }
            System.out.println("#" + "servicesDiscovered");
        }

        /**
         * 服务搜索已完成
         * @param arg0
         * @param arg1
         * serviceSearchCompleted()表示服务发现是否已经完成。
         */
        @Override
        public void serviceSearchCompleted(int arg0, int arg1) {
            System.out.println("#" + "serviceSearchCompleted");
            synchronized(serviceSearchCompletedEvent){
                serviceSearchCompletedEvent.notifyAll();
            }
        }
    };


    /**
     * 查找设备
     * @throws IOException
     * @throws InterruptedException
     */
    private static void findDevices() throws IOException, InterruptedException {
        // 移除集合中所有元素
        devicesDiscovered.clear();
        // 设置锁
        synchronized (inquiryCompletedEvent) {
            // 获取本地蓝牙设备
            LocalDevice ld = LocalDevice.getLocalDevice();

            System.out.println("#本机蓝牙名称:" + ld.getFriendlyName());

            //建立查詢  startInquiry() 启动发现附近设备
            //在请求进行时，蓝牙发现代理会在适当的时候调用回调方法DeviceDiscovered()和inquiryCompleted()。
            // GIAC 指定通用查询访问吗。没有限制地被设置为可发现模式，在设备中长期驻存； LIAC指定有限查询访问码，设备将在唯一的有限时间周期（典型为1分钟）里可被发现。过期，设备会自动返回无法发现的模式。
            boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC,listener);

            if (started) {
                System.out.println("#" + "等待搜索完成...");
                inquiryCompletedEvent.wait();
                // 搜索结束 cancelInquiry()取消当前进行的任何请求
                LocalDevice.getLocalDevice().getDiscoveryAgent().cancelInquiry(listener);
                System.out.println("#发现设备数量：" + devicesDiscovered.size());
            }
        }
    }

    /**
     * 获取设备
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static Set<RemoteDevice> getDevices() throws IOException, InterruptedException {
        findDevices();
        return devicesDiscovered;
    }

    /**
     * 查找服务
     * @param btDevice
     * @param serviceUUID
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static String searchService(RemoteDevice btDevice, String serviceUUID) throws IOException, InterruptedException {
        UUID[] searchUuidSet = new UUID[] { new UUID(serviceUUID, false) };

        int[] attrIDs =  new int[] {
                0x0100 // Service name
        };

        synchronized(serviceSearchCompletedEvent) {
            System.out.println("search services on " + btDevice.getBluetoothAddress() + " " + btDevice.getFriendlyName(false));
            LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, btDevice, listener);
            serviceSearchCompletedEvent.wait();
        }

        if (serviceFound.size() > 0) {
            return serviceFound.elementAt(0);
        } else {
            return "";
        }
    }
}