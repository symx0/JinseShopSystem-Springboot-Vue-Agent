package com.jinse.controller.user;

import com.jinse.utils.AliOssUtil;
import com.jinse.constant.MessageConstant;
import com.jinse.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/user/common")
@Api(tags = "C端通用接口")
@Slf4j
public class UserCommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        if (file == null) {
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
        log.info("C端文件上传：{}", file.getOriginalFilename());
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = UUID.randomUUID() + extension;
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("上传失败:{}", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
