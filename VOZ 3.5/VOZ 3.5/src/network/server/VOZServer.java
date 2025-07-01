/*     */ package network.server;

/*     */
 /*     */ import network.session.ISession;
/*     */ import network.session.Session;
/*     */ import network.session.SessionFactory;
/*     */ import java.io.IOException;
/*     */ import java.net.ServerSocket;
/*     */ import java.net.Socket;
import java.util.HashMap;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;

/*     */
 /*     */
 /*     */ public class VOZServer
        /*     */ implements InVOZServer /*     */ {

    /*     */ private static VOZServer I;
    /*     */    private int port;
    /*     */    private ServerSocket serverListen;
    /*     */    private Class sessionClone;

    /*     *//*     */
 /*     */ public static VOZServer gI() {
        /*  22 */ if (I == null) {
            /*  23 */ I = new VOZServer();
            /*     */        }
        /*  25 */ return I;
        /*     */    }
    /*     */    private boolean start;
    private boolean randomKey;
    private IServerClose serverClose;
    /*     */    private ISessionAcceptHandler acceptHandler;
    /*     */    private Thread loopServer;

    /*     *//*     */
 /*     */ private VOZServer() {
        /*  32 */ this.port = -1;
        /*     */ this.sessionClone = Session.class;
        /*     */    }
    /*     */
 /*     */
 /*     */ public static HashMap<String, Integer> firewall = new HashMap<>();
    public static HashMap<String, Integer> firewallDownDataGame = new HashMap<>();

    /*     */
 /*     */
 /*     */
 /*     */
 /*     *//*     */
 /*     */
 /*     */
 /*     */
 /*     */
 /*     */ public InVOZServer init() {
        /*  44 */ this.loopServer = new Thread(this);
        /*  45 */ return this;
        /*     */    }

    /*     */
 /*     *//*     */
 /*     */
 /*     */ public InVOZServer start(int port) throws Exception {
        /*  50 */ if (port < 0) {
            /*  51 */ throw new Exception("Vui lòng khởi tạo port server!");
            /*     */        }
        /*  53 */ if (this.acceptHandler == null) {
            /*  54 */ throw new Exception("AcceptHandler chưa được khởi tạo!");
            /*     */        }
        /*  56 */ if (!ISession.class.isAssignableFrom(this.sessionClone)) {
            /*  57 */ throw new Exception("Type session clone không hợp lệ!");
            /*     */        }
        /*     */ try {
            /*  60 */ this.port = port;
            /*  61 */ this.serverListen = new ServerSocket(port);
            /*  62 */        } catch (IOException ex) {
            /*  63 */ System.out.println("Lỗi khởi tạo server tại port " + port);
            /*  64 */ System.exit(0);
            /*     */        }
        /*  66 */ this.start = true;
        /*  67 */ this.loopServer.start();
        /*  68 */ System.out.println("Server Girlkun đang chạy tại port " + this.port);
        /*  69 */ return this;
        /*     */    }

    /*     */
 /*     *//*     */
 /*     */
 /*     */ public InVOZServer close() {
        /*  74 */ this.start = false;
        /*  75 */ if (this.serverListen != null) {
            /*     */ try {
                /*  77 */ this.serverListen.close();
                /*  78 */            } catch (IOException ex) {
                /*  79 */ ex.printStackTrace();
                /*     */            }
            /*     */        }
        /*  82 */ if (this.serverClose != null) {
            /*  83 */ this.serverClose.serverClose();
            /*     */        }
        /*  85 */ System.out.println("Server Girlkun đã đóng!");
        /*  86 */ return this;
        /*     */    }

    /*     */
 /*     *//*     */
 /*     */
 /*     */ public InVOZServer dispose() {
        /*  91 */ this.acceptHandler = null;
        /*  92 */ this.loopServer = null;
        /*  93 */ this.serverListen = null;
        /*  94 */ return this;
        /*     */    }

    /*     */
 /*     *//*     */
 /*     */
 /*     */ public InVOZServer setAcceptHandler(ISessionAcceptHandler handler) {
        /*  99 */ this.acceptHandler = handler;
        /* 100 */ return this;
        /*     */    }

    /*     */
 /*     */
   public void run() {
    while (this.start) {
        try {
           
            Socket socket = this.serverListen.accept();
            
            // Có thể ghi log đơn giản nếu cần
            System.out.println("New client connected!");

            
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            Logger.getLogger(VOZServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


    /*     */ public InVOZServer setDoSomeThingWhenClose(IServerClose serverClose) {
        /* 121 */ this.serverClose = serverClose;
        /* 122 */ return this;
        /*     */    }

    /*     */
 /*     *//*     */
 /*     */
 /*     */ public InVOZServer randomKey(boolean isRandom) {
        /* 127 */ this.randomKey = isRandom;
        /* 128 */ return this;
        /*     */    }

    /*     */
 /*     */
 /*     */ public boolean isRandomKey() {
        /* 133 */ return this.randomKey;
        /*     */    }

    /*     */
 /*     *//*     */
 /*     */
 /*     */ public InVOZServer setTypeSessioClone(Class clazz) throws Exception {
        /* 138 */ this.sessionClone = clazz;
        /* 139 */ return this;
        /*     */    }

    /*     */
 /*     */
 /*     */ public ISessionAcceptHandler getAcceptHandler() throws Exception {
        /* 144 */ if (this.acceptHandler == null) {
            /* 145 */ throw new Exception("AcceptHandler chưa được khởi tạo!");
            /*     */        }
        /* 147 */ return this.acceptHandler;
        /*     */    }

    /*     */
 /*     */
 /*     */ public void stopConnect() {
        /* 152 */ this.start = false;
        /*     */    }
    /*     */ }


/* Location:              C:\Users\VoHoangKiet\Downloads\TEA_V5\lib\GirlkunNetwork.jar!\com\girlkun\network\server\EMTIServer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */
