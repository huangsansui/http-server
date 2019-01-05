import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Function:
 *
 * @author: Huangqing
 * @Date: 2019/1/5
 * @since: JDK 1.8
 */
public class HttpTask implements Runnable{

    private Socket socket;

    public HttpTask(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 获取HTTP请求内容
            HttpMessageParse.Request request = HttpMessageParse.parse2Request(socket.getInputStream());
            // TODO: 2019/1/5 对请求的处理
            // 返回响应
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            String result = "hello";
            String httpRsp = HttpMessageParse.buildResponse(request, result);
            pw.print(httpRsp);
            pw.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
