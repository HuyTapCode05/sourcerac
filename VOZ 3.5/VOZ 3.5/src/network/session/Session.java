package network.session;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import network.handler.IKeySessionHandler;
import network.handler.IMessageHandler;
import network.handler.IMessageSendCollect;
import network.io.Collector;
import network.io.Message;
import network.io.Sender;
import network.server.VOZSessionManager;
import utils.StringUtil;

public abstract class Session implements ISession {

    private static ISession instance;
    private static int ID_INIT;
    public TypeSession typeSession;
    public byte timeWait = 50;
    public static final int BUFFER_SIZE = 1048576; // 1 MB

    public static ISession gI() throws Exception {
        if (instance == null) {
            throw new Exception("Instance chưa được khởi tạo!");
        }
        return instance;
    }

    public static synchronized ISession initInstance(String host, int port) throws Exception {
        if (instance != null) {
            throw new Exception("Instance đã được khởi tạo!");
        }
        instance = new Session(host, port) {
            @Override
            public void sendKey() throws Exception {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        return instance;
    }

    private byte[] KEYS = "Girlkun75".getBytes();
    private boolean sentKey;
    public int id;
    private Socket socket;
    private boolean connected;
    private boolean reconnect;
    private Sender sender;
    private Collector collector;
    private Thread tSender;
    private Thread tCollector;
    private IKeySessionHandler keyHandler;
    private String ip;
    private String host;
    private int port;

    // Constructor dành cho client
    public Session(String host, int port) throws IOException {
        this.id = 752002;
        this.host = host;
        this.port = port;
        this.socket = new Socket(host, port);
        initializeSocket(this.socket);
        this.typeSession = TypeSession.CLIENT;
        this.connected = true;
        initThreadSession();
    }

    // Constructor dành cho server
    public Session(Socket socket) {
        this.id = ID_INIT++;
        this.typeSession = TypeSession.SERVER;
        this.socket = socket;
        initializeSocket(this.socket);
        this.connected = true;
        this.ip = ((InetSocketAddress) socket.getRemoteSocketAddress())
                        .getAddress().toString().replace("/", "");
        initThreadSession();
    }
    
    // Thiết lập cấu hình cho socket
    private void initializeSocket(Socket socket) {
        try {
            socket.setSendBufferSize(BUFFER_SIZE);
            socket.setReceiveBufferSize(BUFFER_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void sendMessage(Message msg) {
        if (sender != null && isConnected() && sender.getNumMessages() < 1000) {
            sender.sendMessage(msg);
        }
    }

    @Override
    public ISession setSendCollect(IMessageSendCollect collect) {
        sender.setSend(collect);
        collector.setCollect(collect);
        return this;
    }

    @Override
    public ISession setMessageHandler(IMessageHandler handler) {
        collector.setMessageHandler(handler);
        return this;
    }

    @Override
    public ISession setKeyHandler(IKeySessionHandler handler) {
        this.keyHandler = handler;
        return this;
    }

    @Override
    public ISession startSend() {
        if (tSender != null && !tSender.isAlive()) {
            tSender.start();
        }
        return this;
    }

    @Override
    public ISession startCollect() {
        if (tCollector != null && !tCollector.isAlive()) {
            tCollector.start();
        }
        return this;
    }

    @Override
    public String getIP() {
        return this.ip;
    }

    @Override
    public long getID() {
        return this.id;
    }

    @Override
    public void disconnect() {
        connected = false;
        sentKey = false;
        if (sender != null) {
            sender.close();
        }
        if (collector != null) {
            collector.close();
        }
        if (reconnect) {
            reconnect();
            return;
        }
        dispose();
    }

    @Override
    public void dispose() {
        if (sender != null) {
            sender.dispose();
        }
        if (collector != null) {
            collector.dispose();
        }
        socket = null;
        sender = null;
        collector = null;
        tSender = null;
        tCollector = null;
        ip = null;
        VOZSessionManager.gI().removeSession(this);
    }

    @Override
    public void setKey(Message message) throws Exception {
        if (keyHandler == null) {
            throw new Exception("Key handler chưa được khởi tạo!");
        }
        keyHandler.setKey(this, message);
    }

    @Override
    public void setKey(byte[] key) {
        this.KEYS = key;
    }

    @Override
    public boolean sentKey() {
        return this.sentKey;
    }

    @Override
    public void setSentKey(boolean sent) {
        this.sentKey = sent;
    }

    @Override
    public void doSendMessage(Message msg) throws Exception {
        sender.doSendMessage(msg);
    }

    @Override
    public ISession start() {
        startSend();
        startCollect();
        return this;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public byte[] getKey() {
        return this.KEYS;
    }

    @Override
    public TypeSession getTypeSession() {
        return this.typeSession;
    }

    @Override
    public ISession setReconnect(boolean b) {
        this.reconnect = b;
        return this;
    }

    @Override
    public int getNumMessages() {
        if (isConnected()) {
            return sender.getNumMessages();
        }
        return -1;
    }

    @Override
    public void reconnect() {
        if (typeSession == TypeSession.CLIENT && !isConnected()) {
            while (!isConnected()) {
                try {
                    socket = new Socket(host, port);
                    initializeSocket(socket);
                    connected = true;
                    initThreadSession();
                    start();
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void initThreadSession() {
        // Cập nhật sender
        if (sender == null) {
            sender = new Sender(this, socket);
        } else {
            sender.setSocket(socket);
        }
        tSender = new Thread(sender, "Thread tsender");
        
        // Cập nhật collector
        if (collector == null) {
            collector = new Collector(this, socket);
        } else {
            collector.setSocket(socket);
        }
        tCollector = new Thread(collector, "Thread collector");
    }
}
