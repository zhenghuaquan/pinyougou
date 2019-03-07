package com.pinyougou.shop.controller;

import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传的控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-25<p>
 */
@RestController
public class UploadController {

    /** 定义服务器的访问地址 */
    @Value("${fileServerUrl}")
    private String fileServerUrl;

    /** 文件上传 */
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file")MultipartFile multipartFile){
        // {url : '', status : 200|500}
        Map<String,Object> data = new HashMap<>();
        data.put("status", 500);
        try{
            // 1. 获取上传的文件名称
            String filename = multipartFile.getOriginalFilename();

            // 2. 获取上传的文件字节数组
            byte[] bytes = multipartFile.getBytes();

            // 3. 上传文件到FastDFS文件服务器
            // 3.1 获取fastdfs-client.conf文件的路径
            String path = this.getClass().getResource("/fastdfs-client.conf").getPath();
            // 3.2 初始化全局的客户端对象
            ClientGlobal.init(path);
            // 3.3 创建存储客户端对象
            StorageClient storageClient = new StorageClient();

            // 3.4 上传文件到文件服务器
            String[] arr = storageClient.upload_file(bytes, FilenameUtils.getExtension(filename), null);
            // arr[0] : 组的名称
            // arr[1] : 远程文件名称
            // 图片的访问路径：http://192.168.12.131 / arr[0] / arr[1]
            // 定义StringBuilder拼接字符串
            StringBuilder sb = new StringBuilder(fileServerUrl);
            for (String str : arr) {
                sb.append("/" + str);
            }


            data.put("status", 200);
            data.put("url" , sb.toString());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return data;
    }
}
