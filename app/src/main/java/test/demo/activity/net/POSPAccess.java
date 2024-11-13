package test.demo.activity.net;

import test.demo.activity.utils.DebugLogUtil;
import test.demo.activity.utils.PreferencesUtil;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class POSPAccess {
    private final String TAG = POSPAccess.class.getSimpleName();

    public static final int DEFAULT_CONN_TIMEOUT = 20 * 1000;//默认连接超时时间30s
    public static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;//默认读写超时时间

    private String serverIP;//服务IP
    private int serverPort;//服务器端口
    private int connectTimeout = DEFAULT_CONN_TIMEOUT;//连接超时时间,单位秒
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;//读超时时间,单位秒

    private Socket socket;//Socket对象
    private InputStream inputStream;//Socket输入流
    private OutputStream outputStream;//Socket输出流

    public POSPAccess() {
        initIPAndPort();
        checkIpAndPort();
    }

    /**
     * 初始化IP和端口
     */
    private void initIPAndPort() {
        serverIP = PreferencesUtil.getServerIP();
        serverPort = Integer.parseInt(PreferencesUtil.getServerPort());
    }

    /**
     * 检查IP和端口是否合法
     */
    private void checkIpAndPort() {
        if (!validIP(serverIP)) {
            throw new RuntimeException("illegal server ip!:" + serverIP);
        }
        if (serverPort <= 0) {
            throw new RuntimeException("illegal server port!:" + serverPort);
        }
    }

    void transaction(byte[] sendBuff, int sendLen, byte[] recBuff, int[] recLen) throws Exception {
        DebugLogUtil.i(TAG, "Connect to " + serverIP + " : " + serverPort);
        try {
            if (sendLen <= 0 || sendLen > 1024) {
                return;
            }
            socket = new Socket();
            socket.setSoTimeout(socketTimeout);
            socket.setSoLinger(true, 0);
            socket.connect(new InetSocketAddress(serverIP, serverPort), connectTimeout);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            outputStream.write(sendBuff, 0, sendLen);
            outputStream.flush();

            //等待server数据就绪
            int count = 0;
            while (inputStream.available() <= 0) {
                Thread.sleep(50);
                if (count++ >= 200) {//累计等待时间超过10s
                    throw new SocketTimeoutException();
                }
            }

            int len = 0;
            int index = 0;
            while ((len = inputStream.available()) > 0) {
                index += inputStream.read(recBuff, index, len);
                Thread.sleep(10);
            }

            recLen[0] = index;//接收的总字节数
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            throw new Exception("68");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("E4");
        } finally {
            closePOSP();
        }
    }


    public void test(String str) {
        try {
            //1.创建客户端Socket，指定服务器地址和端口
            Socket socket = new Socket("192.168.2.177", 8888);
            //2.获取输出流，向服务器端发送信息
            OutputStream os = socket.getOutputStream();//字节输出流
            PrintWriter pw = new PrintWriter(os);//将输出流包装为打印流
            pw.write(str);
            pw.flush();
            socket.shutdownOutput();//关闭输出流
            //3.获取输入流，并读取服务器端的响应信息
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String info = null;
            while ((info = br.readLine()) != null) {
                DebugLogUtil.e("lj", "我是客户端，服务器说：" + info);
            }
            //4.关闭资源
            br.close();
            is.close();
            pw.close();
            os.close();
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 断开连接，释放资源
     */
    private void closePOSP() {
        close(inputStream);
        close(outputStream);
        close(socket);

        inputStream = null;
        outputStream = null;
        socket = null;
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validIP(String ip) {
        if (ip == null || ip.isEmpty())
            return false;
        String newIp = ip.trim();
        if (newIp.length() < 6 & newIp.length() > 15)
            return false;

        try {
            String rule = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.)" +
                    "{3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
            Pattern pattern = Pattern.compile(rule);
            Matcher matcher = pattern.matcher(ip);
            return matcher.matches();
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }
}
