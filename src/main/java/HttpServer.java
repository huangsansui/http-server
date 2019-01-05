import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Function:
 *
 * http服务器
 *
 * @author: Huangqing
 * @Date: 2019/1/5
 * @since: JDK 1.8
 */
public class HttpServer {

    private static ExecutorService bootstrapExecutor = new ThreadPoolExecutor(10, 20,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

    private static ExecutorService taskExecutor = Executors.newSingleThreadExecutor();

    private static int port = 1024;


    public static void startHttpServer(){
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                bootstrapExecutor.execute(new ServerThread(serverSocket));
            }
        } catch (IOException e) {
            try {
                //重试
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                bootstrapExecutor.shutdown();
            }
        }
    }

    private static class ServerThread implements Runnable {

        private ServerSocket serverSocket;

        public ServerThread(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            try {
                Socket socket = serverSocket.accept();
                taskExecutor.execute(new HttpTask(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
