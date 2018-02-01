package flak.backend.jdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import flak.Form;
import flak.Query;
import flak.Request;
import flak.Response;
import flak.util.IO;

public class JdkRequest implements Request, Response {

  private static final String[] EMPTY = {};

  private Context ctx;

  private final HttpExchange exchange;

  public final String[] split;

  private final String qs;

  private Form form;

  public JdkRequest(Context ctx, HttpExchange r) {
    this.ctx = ctx;
    this.exchange = r;
    this.qs = r.getRequestURI().getQuery();
    String path = ctx.makeRelativePath(r.getRequestURI().getPath());
    split = (path.isEmpty() || path.equals("/")) ? EMPTY
                                                 : trimLeftSlash(path).split("/");
  }

  public String getSplit(int tokenIndex) {
    return split[tokenIndex];
  }

  public String getSplat(int tokenIndex) {
    // TODO directly return a substring of the path
    StringBuilder b = new StringBuilder(64);
    for (int i = tokenIndex; i < split.length; i++) {
      if (b.length() > 0)
        b.append('/');
      b.append(split[i]);
    }
    return b.toString();
  }

  private static String trimLeftSlash(String uri) {
    if (uri.startsWith("/"))
      return uri.substring(1);
    else
      return uri;
  }

  public String getPath() {
    return ctx.app.relativePath(exchange.getRequestURI().getPath());
  }

  public String getQueryString() {
    return qs;
  }

  public HttpExchange getExchange() {
    return exchange;
  }

  public String getMethod() {
    return exchange.getRequestMethod();
  }

  @Override
  public Query getQuery() {
    return new FormImpl(getQueryString());
  }

  @Override
  public Form getForm() {
    try {
      if (form == null)
        form = new FormImpl(readData());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return form;
  }

  private String readData() throws IOException {
    return new String(IO.readFully(getInputStream()));
  }

  public InputStream getInputStream() {
    return exchange.getRequestBody();
  }

  @Override
  public String getHttpMethod() {
    return exchange.getRequestMethod();
  }

  // /////////// Response methods

  public void addHeader(String header, String value) {
    exchange.getResponseHeaders().add(header, value);
  }

  public void setStatus(int status) {
    try {
      exchange.sendResponseHeaders(status, 0);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public OutputStream getOutputStream() {
    return exchange.getResponseBody();
  }
}
