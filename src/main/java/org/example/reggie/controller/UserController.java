package org.example.reggie.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.CustomException;
import org.example.reggie.common.R;
import org.example.reggie.entity.User;
import org.example.reggie.service.UserService;
import org.example.reggie.utils.SMSUtils;
import org.example.reggie.utils.ValidateCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Value("${reggie.accessKeyId}")
    private String accessKeyId;
    @Value("${reggie.accessKeySecret}")
    private String accessKeySecret;

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession httpSession) {
        //1.获取手机号
        String phoneNumber = user.getPhone();

        //2.生成随机4位验证码
        if (null != phoneNumber) {
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("手机号为：{}，验证码为：{}", phoneNumber, code);

            //3.发送验证码
            try {
                SMSUtils.sendMessage(phoneNumber, code, accessKeyId, accessKeySecret);
            } catch (Exception e) {
                throw new CustomException("发送验证码失败");
            }

            //4.将验证码保存到Session
            httpSession.setAttribute(phoneNumber, code);

            return R.success("手机验证码发送成功");
        }

        return R.error("手机号不能为空");
    }
}
