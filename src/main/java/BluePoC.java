
import com.intel.bluetooth.SelectServiceHandler;

import javax.bluetooth.*;
import javax.microedition.io.Connection;
import javax.microedition.io.StreamConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BluePoC {

    public static void main(String[] args) throws IOException, InterruptedException {

        if (!LocalDevice.isPowerOn()) {
            System.err.println("Please turn on bluetooth");
            System.exit(1);
        }

        LocalDevice localDevice = LocalDevice.getLocalDevice();
        Connection connection = new com.intel.bluetooth.btspp.Connection();
        System.out.println("Local adress" + localDevice.getBluetoothAddress());
        System.out.println("mode "+ localDevice.getDiscoverable());
        localDevice.setDiscoverable(10390323);//GIAC
        DiscoveryAgent discoveryAgent = localDevice.getDiscoveryAgent();
        DiscoveryListener discoveryListener = new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                System.out.println("Device "+ btDevice);
                System.out.println("code"+cod);
            }

            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                System.out.println("Device "+ transID);
                System.out.println("code"+servRecord);
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {

            }

            @Override
            public void inquiryCompleted(int discType) {

            }
        };
        discoveryAgent.startInquiry(DiscoveryAgent.GIAC,discoveryListener);
        Thread.sleep(5000);
        for ( RemoteDevice remoteDevice : discoveryAgent.retrieveDevices(DiscoveryAgent.CACHED)){
            System.out.println("Address: = "+ remoteDevice.getBluetoothAddress() + "name = " + remoteDevice.getFriendlyName(false) + "state " + remoteDevice.isTrustedDevice() );
        }
        /*
        if (!localDevice.setDiscoverable(DiscoveryAgent.GIAC)) {
            System.err.println("Couldnt set discoverable");
            System.exit(2);
        };
         */

        System.err.println("LOCAL: " + localDevice.getBluetoothAddress());
        System.err.flush();

        //new Thread(() -> startService()).run();
        //startService();

        DiscoveryAgent discoverAgent = localDevice.getDiscoveryAgent();

        // var devices = PoCDiscoverer.searchDevicesSynchronous(discoverAgent);
        /*
        if (devices.containsKey(localDevice.getBluetoothAddress())) {
            System.err.println("GOTUS");
        }

        PoCDiscoverer.searchServicesOnDeviceSynchronous(discoverAgent, devices.get("A0AFBD29A567"));
        */
        System.out.println("Attempting to connect...");
        var client = new PoCClient();
        final String srvAddr = "AC824751484C";
        final String srvUUID = PoCService.serviceUUID.toString();
        //client.openConnection("btspp://%s:%s".formatted(srvAddr, "4"));

        /*
        for (var device : devices.values()) {
            PoCDiscoverer.searchServicesOnDeviceSynchronous(discoverAgent, device);
        }
        */

    }


    public static void startService() throws IOException {
        var serviceFactory = new PoCService();
        byte[] text = "Blue World".getBytes(StandardCharsets.UTF_8);

        StreamConnection clientConn = serviceFactory.waitForConnection();
        System.out.println("Connection opened!");
        var clientDevice = RemoteDevice.getRemoteDevice(clientConn);
        if (!clientDevice.isAuthenticated()) {
            System.err.println("Client not authenticated");
        }

        new WriterThread(clientConn).start();
        new ReadThread(clientConn).start();

    }
}
