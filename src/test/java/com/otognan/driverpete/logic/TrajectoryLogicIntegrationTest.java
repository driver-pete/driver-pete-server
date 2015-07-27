package com.otognan.driverpete.logic;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;
import org.apache.commons.codec.binary.Base64;

import com.otognan.driverpete.BaseStatelesSecurityITTest;


public class TrajectoryLogicIntegrationTest extends BaseStatelesSecurityITTest {
    
    private TrajectoryLogicApi server() throws Exception {
        String token = this.getTestToken();
        return this.serverAPI(token, TrajectoryLogicApi.class);
    }

//    @Test
//    public void determineDataLength() throws Exception {
//        String inputStr = "   &*()!@#$$%^&())((     ()/n/ndfgsd)(*)(@''''???";
//        byte[] encodedBytes = Base64.encodeBase64(inputStr.getBytes("UTF-8"));
//        TypedInput in = new TypedByteArray("application/octet-stream", encodedBytes);
//        
//        int dataLenth = this.server().compressedLength(in);
//        
//        assertThat(dataLenth, equalTo(inputStr.length()));
//    }
    
    @Test
    public void uploadToS3() throws Exception {
        String inputStr = "Hello. I'm going to be uploaded to S3";
        byte[] encodedBytes = Base64.encodeBase64(inputStr.getBytes());
        TypedInput in = new TypedByteArray("application/octet-stream", encodedBytes);
        this.server().compressed(in);
    }
    
}
