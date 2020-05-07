package com.roncoo.es.score.first;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;


/**
 * @author chaishuai
 * @date 2018/9/25
 */
public class PDF2ElasticSearch {

    public static void main(String[] args) throws Exception {
        // 先构建client
        Settings settings = Settings.builder()
                .put("cluster.name", "elasticsearch")
                .build();

        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

        //System.out.println(encodeBase64File("C:\\Users\\chaishuai\\Desktop\\add\\iVMS.pdf"));

        pdf2ES(client);
        client.close();
    }


    private static void pdf2ES(TransportClient client) throws Exception {
        IndexResponse response = client.prepareIndex("mypdf", "_doc", "5")
                .setPipeline("attachment")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        //.field("data", encodeBase64File("C:\\Users\\chaishuai\\Desktop\\add\\iVMS.pdf"))
                        //.field("data", encodeBase64File("C:\\Users\\chaishuai\\Desktop\\add\\sql语句优化总结.docx"))
                        .field("data", encodeBase64File("C:\\Users\\chaishuai\\Desktop\\add\\postgresql-9.1-A4.pdf"))
                        .endObject())
                .get();
        System.out.println(response.getResult());
    }

    public static String encodeBase64File(String path) throws Exception {
        File file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);  inputFile.close();
        return new String(new Base64().encode(buffer),"utf-8");
    }

    public static byte[] encodeBase64File2Byte(String path) throws Exception {
        File file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);  inputFile.close();
        return new Base64().encode(buffer);
    }




}
