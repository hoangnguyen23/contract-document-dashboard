package vn.contractgovernance.api.files;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Real clamd INSTREAM; a missing scanner can never yield CLEAN. */
@Component
public class ClamAvScanner {
    public record Result(boolean infected,String engineVersion,String databaseVersion) {}
    private final String host;private final int port;
    private final ScheduledExecutorService deadlines=Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().daemon(true).name("clamav-deadline").factory());
    public ClamAvScanner(@Value("${cg.scanner.host:localhost}")String host,@Value("${cg.scanner.port:3310}")int port){this.host=host;this.port=port;}
    public Result scan(byte[] bytes) throws IOException {
        var version=command("zVERSION\0",null);var parts=version.split("/",3);
        if(parts.length<2 || !parts[0].startsWith("ClamAV ") || parts[0].length()>80 || parts[1].isBlank() || parts[1].length()>80)throw new IOException("Scanner version response is invalid");
        var answer=command("zINSTREAM\0",bytes);
        if(answer.endsWith(": OK"))return new Result(false,parts[0],parts[1]);
        if(answer.endsWith(" FOUND"))return new Result(true,parts[0],parts[1]);
        throw new IOException("Scanner did not return a definitive result");
    }
    private String command(String command,byte[] bytes)throws IOException {
        try(var socket=new Socket()) {
            socket.connect(new InetSocketAddress(host,port),3000);socket.setSoTimeout(15000);
            var timeout=deadlines.schedule(()->{try{socket.close();}catch(IOException ignored){}},20,TimeUnit.SECONDS);
            try {
                var output=new DataOutputStream(socket.getOutputStream());output.write(command.getBytes(StandardCharsets.US_ASCII));
                if(bytes!=null){for(int offset=0;offset<bytes.length;offset+=8192){int count=Math.min(8192,bytes.length-offset);output.writeInt(count);output.write(bytes,offset,count);}output.writeInt(0);}output.flush();
                var result=new ByteArrayOutputStream();var input=socket.getInputStream();
                for(int value; (value=input.read())!=-1;){if(value==0 || value=='\n')return result.toString(StandardCharsets.US_ASCII).trim();if(result.size()>=4096)throw new IOException("Scanner response exceeds bounded size");result.write(value);}
                throw new IOException("Scanner closed connection without terminator");
            } finally {timeout.cancel(false);}
        }
    }
    @PreDestroy public void close(){deadlines.shutdownNow();}
}
