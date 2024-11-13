package test.demo.activity.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import test.demo.MyApplication;



public class TCPUtils {

    private static final String TAG = "TCPUtils";

    public static void justSend(String ipaddr, String portNum, String content) {
        Socket socket = null;
        OutputStream outputStream = null;

        if (null == content || content.isEmpty()) {
            return;
        }

        try {
            socket = new Socket(ipaddr, Integer.valueOf(portNum));
            socket.setSoTimeout(1000 * 30);
            socket.setSoLinger(true, 0);
            socket.setKeepAlive(true);

            byte[] sendData = Utils.hexStr2Bytes(content);
            outputStream = socket.getOutputStream();
            outputStream.write(sendData);
            outputStream.flush();

            outputStream.close();
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != outputStream) {
                    outputStream.close();
                    outputStream = null;
                }
                if (null != socket) {
                    socket.close();
                    socket = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static String sendReceive(String content, String dataType, String keyword, List<String> list) throws IOException, InterruptedException {
        StringBuilder retString = new StringBuilder();
        Socket socket = null;
        int flag = 0;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        if (null == content || content.isEmpty()) {
            return null;
        }

        try {
            socket = new Socket(PreferencesUtil.getServerIP(), Integer.valueOf(PreferencesUtil.getServerPort()));
            socket.setSoTimeout(1000 * 30);
            socket.setSoLinger(true, 0);
            socket.setKeepAlive(true);

            do {
                DebugLogUtil.i(TAG, "Client send content = " + content);
                byte[] sendData = Utils.hexStr2Bytes(content);
                outputStream = socket.getOutputStream();
                outputStream.write(sendData);
                outputStream.flush();

                inputStream = socket.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                while (inputStream.available() <= 0) {
                    Thread.sleep(50);
                }
                byte[] cbuf = new byte[2048];
                int offset = 0;
                int len = 0;
                retString.delete(0, retString.length());
                while ((len = inputStream.available()) > 0) {
                    inputStream.read(cbuf, offset, len);
                    if (len < cbuf.length) {
                        byte[] tmpbuf = new byte[len];
                        System.arraycopy(cbuf, 0, tmpbuf, 0, len);
                        retString.append(Utils.byte2HexStr(tmpbuf));
                    } else {
                        retString.append(Utils.byte2HexStr(cbuf));
                    }
                    if (!retString.substring(2, 4).equalsIgnoreCase(dataType)) {
                        retString.delete(0, retString.length());
                    }

                    Thread.sleep(10);
                }
                if (null != list) {
                    if (retString.toString().length() > 8 ) {
                        list.add(retString.substring(8, retString.length()));
                    }
                }
                if (MyApplication.app.autoTest && retString.toString().contains("DF8106")){
                    flag = 1;
                    break;
                }

            }
            while (retString != null && (keyword != null && keyword.length() > 0) && retString.toString().contains(keyword));
            if (null != list && (flag == 0)) {
                list.remove(list.size() - 1); //删除Server端返回的结束报文
            }
        }  finally {
            try {
                if (null != outputStream) {
                    outputStream.close();
                    outputStream = null;
                }
                if (null != inputStream) {
                    inputStream.close();
                    inputStream = null;
                }
                if (null != bufferedReader) {
                    bufferedReader.close();
                    bufferedReader = null;
                }
                if (null != socket) {
                    socket.close();
                    socket = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int length = retString.length();
        if(length>8 && list==null){
            return retString.substring(8,length);
        }
        return retString.toString();
    }

}
