package org.example.reggie.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传下载
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    //获取application.yml中的转存目录
    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * 参数名必须与前端对应--file
     * 不然无法接收
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //file是临时文件，若不转存会被删除
        log.info("上传文件信息--->file: {}", file);

        //若转存文件路径不存在则创建指定目录
        java.io.File dir = new java.io.File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        //获取原始文件名
        String orignalName = file.getOriginalFilename();
        //截取原始文件名后缀，这个方法的截取包含.
        String suffix = orignalName.substring(orignalName.lastIndexOf("."));
        //获取随机UUID并拼接原始后缀
        String newFileName = UUID.randomUUID().toString() + suffix;

        //将文件转存到指定目录
        try {
            file.transferTo(new java.io.File(basePath + newFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //返回转存后的文件名
        return R.success(newFileName);
    }

    /**
     * 文件下载
     * @param response
     * @param name
     */
    @GetMapping("/download")
    public void download(HttpServletResponse response, String name){


        try {
            //通过输入流读取文件内容
            FileInputStream inputStream = new FileInputStream(basePath + name);
            //通过输出流将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            //设置响应的 Content-Type 声明返回类型
            response.setContentType("image/jpeg");

            /* read(byte [] b )
            从(来源)输入流中(读取内容)读取的一定数量字节数,并将它们存储到
            (去处)缓冲区数组b中,返回值为实际读取的字节数,
            运行一次读取一定的数量的字节数.
            java会尽可能的读取b个字节,但也有可能读取少于b的字节数.
            至少读取一个字节第一个字节存储读入元素b[0],下一个b[1],等等。
            读取的字节数是最多等于b的长度.如果没有可用的字节,
            因为已经到达流的末尾,
            -1返回的值 ,如果b.length==0,则返回0.*/

            int len = 0;  // 记录每次读取的字节数
            byte[] bytes = new byte[1024];
            while ( (len = inputStream.read(bytes)) != -1 ) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

