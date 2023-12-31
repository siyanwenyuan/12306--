package com.chen.train.member.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.experimental.PackagePrivate;

public class MemberLoginReq {

    /**
     *添加spring中的校验框架
     * 此注解表示不能为空
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$",message = "手机号码格式错误")
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    private String code;




    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public MemberLoginReq(String mobile) {
        this.mobile = mobile;
    }

    public MemberLoginReq() {
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MemberLoginReq{");
        sb.append("mobile='").append(mobile).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
