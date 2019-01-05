import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import lombok.Data;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Function:
 *
 * @author: Huangqing
 * @Date: 2019/1/5
 * @since: JDK 1.8
 */
public class HttpMessageParse {

    @Data
    public static class Request{

        /**
         *  请求方法 GET/POST/PUT/DELETE ...
         */
        private String method;

        /**
         *  请求路径
         */
        private String uri;

        /**
         * HTTP版本
         */
        private String version;

        /**
         *  请求头内容
         */
        private Map<String, String> headers;

        /**
         * 请求内容
         */
        private String message;
    }

    @Data
    public static class Response {

        /**
         * HTTP版本
         */
        private String version;

        /**
         * 响应码
         */
        private int code;

        /**
         * 响应状态
         */
        private String status;

        /**
         * 响应头
         */
        private Map<String, String> headers;

        /**
         * 响应正文
         */
        private String message;
    }

    /**
     * 解析请求行
     * 请求行组成： 请求方法 + URI + HTTP协议版本
     * @param reader
     * @param request
     */
    public static void parseRequestLine(BufferedReader reader, Request request) throws IOException {
        String[] requestLine = reader.readLine().split(" ");
        request.setMethod(requestLine[0]);
        request.setUri(requestLine[1]);
        request.setVersion(requestLine[2]);
    }

    /**
     * 解析请求头
     *
     * @param reader
     * @param request
     * @throws IOException
     */
    public static void parseRequestHeaders(BufferedReader reader, Request request) throws IOException {
        Map header = new HashMap(16);
        String line = reader.readLine();
        String[] headerMessage;
        while (!"".equals(line)) {
            headerMessage = line.split(":");
            String key = headerMessage[0].trim();
            String value = headerMessage[1].trim();
            header.put(key, value);
            line = reader.readLine();
        }
        request.setHeaders(header);
    }

    /**
     * 解析正文
     * 存在可能header中没有Content-Length参数
     * @param reader
     * @param request
     * @throws IOException
     */
    public static void parseRequestMessage(BufferedReader reader, Request request) throws IOException {
        String contentLength = request.getHeaders().getOrDefault( "Content-Length", "0");
        // 有摄者Content-Length参数
        if (!"0".equals(contentLength)) {
            char[] message = new char[Integer.parseInt(contentLength)];
            reader.read(message);
            request.setMessage(new String(message));
            return;
        }
        String message = reader.readLine().trim();
        if (message != null && message.length() > 0) {
            request.setMessage(message);
        }
    }

    /**
     * 解析请求
     * @param is
     * @throws IOException
     */
    public static Request parse2Request(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
        Request request = new Request();
        parseRequestLine(br, request);
        parseRequestHeaders(br, request);
        parseRequestMessage(br, request);
        return request;
    }


    /**
     * 生成响应信息
     * @param request
     * @param result
     */
    public static String buildResponse(Request request, String result) {
        Response response = new Response();
        StringBuffer httpRes = new StringBuffer();
        response.setVersion(request.getVersion());
        response.setCode(200);
        response.setStatus("OK");
        Map headers = new HashMap(16);
        headers.put("Content-Type", "application/json");
        headers.put("Content-Length", result.getBytes().length);
        response.setHeaders(headers);
        response.setMessage(result);
        buildResponseLine(response, httpRes);
        buildResponseHeader(response, httpRes);
        buildResponseMessage(response, httpRes);
        return httpRes.toString();
    }

    /**
     * 生成响应正文
     * @param response
     * @param httpRes
     */
    private static void buildResponseMessage(Response response, StringBuffer httpRes) {
        httpRes.append(response.getMessage()).append("\n");
    }

    /**
     * 生成响应头
     * @param response
     * @param httpRes
     */
    private static void buildResponseHeader(Response response, StringBuffer httpRes) {
        for (Map.Entry entry : response.getHeaders().entrySet()) {
            httpRes.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
        }
        httpRes.append("\n");
    }

    /**
     * 生成响应行
     * 响应行组成： 状态码 + 状态 + HTTP版本
     * @param response
     * @param httpRes
     */
    private static void buildResponseLine(Response response, StringBuffer httpRes) {
        httpRes.append(response.getCode()).append(" ").append(response.getStatus()).append(" ")
                .append(response.getVersion()).append("\n");
    }

}
