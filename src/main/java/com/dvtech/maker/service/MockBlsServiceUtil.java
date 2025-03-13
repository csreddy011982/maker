package com.dvtech.maker.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MockBlsServiceUtil {

    @Value("${bls-file-feed.system-environment.host}")
    private String sftpServerName;

    @Value("${bls-file-feed.system-environment.user}")
    private String sftpUserName;

    @Value("${bls-file-feed.system-environment.key}")
    private String sftpKey;

    @Value("${bls-file-feed.input-data.aba-file-path}")
    private String abaFilePath;

    private static final String DATA_FILE_EXTENSION = ".dat";
    private static final String MARKER_FILE_EXTENSION = ".mrk";

    public String getAba() {
List<String> aba;
try(BufferedReader reader = new BufferedReader(new FileReader(Paths.get(new DefaultResourceLoader().
        getResource(abaFilePath).getFile().getAbsolutePath()).toString()))){
    aba = reader.lines().collect(Collectors.toList());
}catch(Exception e){
    throw new RuntimeException(e);
}
        return aba.get(new Random().nextInt(aba.size()));
    }
    public String getIndividualId() {
        List<String> individualId;
        try(BufferedReader reader = new BufferedReader(new FileReader(Paths.get(new DefaultResourceLoader().
                getResource(abaFilePath).getFile().getAbsolutePath()).toString()))){
            individualId = reader.lines().collect(Collectors.toList());
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return individualId.get(new Random().nextInt(individualId.size()));
    }

//    public void upload(String feedName, String payload, String remoteFileLocation) throws SftpException {
//        Session session = null;
//        ChannelSftp channel = null;
//        String remoteFeedName = remoteFileLocation + feedName;
//
//        try {
//            JSch jsch = new JSch();
//            session = jsch.getSession(sftpUserName, sftpServerName, 22);
//            session.setPassword(sftpKey);
//
//            Properties config = new Properties();
//            config.put("StrictHostKeyChecking", "no");
//            config.put("PreferredAuthentications", "password");
//            session.setConfig(config);
//            session.connect();
//
//            channel = (ChannelSftp) session.openChannel("sftp");
//            channel.connect();
//
//            channel.put(new ByteArrayInputStream(new byte[0]), remoteFeedName + MARKER_FILE_EXTENSION);
//            channel.put(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)),
//                    remoteFeedName + DATA_FILE_EXTENSION);
//
//        } catch (JSchException e) {
//            log.error("SFTP error", e);
//        } finally {
//            try {
//                channel.disconnect();
//            } catch (Exception ignored) {}
//
//            try {
//                session.disconnect();
//            } catch (Exception ignored) {
//
//            }
//        }
//    }

}
