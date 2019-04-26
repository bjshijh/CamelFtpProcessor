package com.example.router;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;

import org.springframework.core.io.ClassPathResource;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFileMessage;

@Slf4j
@PropertySource("classpath:/ftp.properties")
@ConfigurationProperties
@Component
public class FtpConsumer extends RouteBuilder {

    @Value("${ftp.Url}")
    public String ftpUrl;

    @Value("${ftp.local.work.directory}")
    public String localDirectory;

    @Value("${ftp.keystore.password}")
    public String jskPassword;

    @Value("${ftp.check.period}")
    int timerPeriod;

    @Override
    public void configure() throws Exception {
        System.out.println("Camel FTP Consumer");

        ClassPathResource jksResource = new ClassPathResource("wsClientKeystore.jks");
        File targetFile = new File("KeyStore.jks");
        FileUtils.copyInputStreamToFile(jksResource.getInputStream(), targetFile);
        
        // move=.dealtWith/${date:now:yyyy-MM-dd}/${header.CamelFileName}
        // <to uri="bean:processFile"/>
        from(ftpUrl + "&ftpClient.trustStore.file=KeyStore.jks&ftpClient.trustStore.password=" + jskPassword
                + "&move=home/ftp/ftpuser/bak&delay=" + timerPeriod)
                .to("file:" + localDirectory)
                .process(new Processor() {
                    public void process(Exchange msg) {
                        processMessage(msg);
                    }
                });
    }
    
    void processMessage( Exchange msg) {
        GenericFileMessage gfm= msg.getIn(GenericFileMessage.class);
        String filename= gfm.getGenericFile().getFileNameOnly();
        log.info("Processing {}", filename);
        File f= new File (localDirectory + "/" + filename);
        f.getAbsolutePath();
    }
}
