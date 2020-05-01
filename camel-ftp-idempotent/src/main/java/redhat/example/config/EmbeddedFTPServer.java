package redhat.example.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Slf4j
@Configuration
public class EmbeddedFTPServer {

    //@EventListener(ApplicationReadyEvent.class)
    @Bean("ftpServer")
    public FtpServer createFtpServer() throws FtpException {

        String filePath = System.getProperty("user.dir") + "/ftp";
        log.info("FTP Working Project Directory : {}", filePath);

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager userManager = userManagerFactory.createUserManager();

        BaseUser user = new BaseUser();
        user.setAuthorities(Arrays.asList(new Authority[]{new ConcurrentLoginPermission(2, 2),
                new WritePermission()}));
        user.setName("username");
        user.setPassword("admin");
        user.setHomeDirectory(filePath);
        userManager.save(user);

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(21);

        FtpServerFactory factory = new FtpServerFactory();
        factory.setUserManager(userManager);
        factory.addListener("default", listenerFactory.createListener());

        FtpServer server = factory.createServer();
        server.start();
        return server;
    }
}