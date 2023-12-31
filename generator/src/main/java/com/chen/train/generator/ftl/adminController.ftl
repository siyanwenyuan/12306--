package com.chen.train.${module}.controller;


import com.chen.train.common.context.LoginMemberContext;
import com.chen.train.common.resp.CommonResp;
import com.chen.train.common.resp.PageResp;
import com.chen.train.${module}.req.${Domain}QueryReq;
import com.chen.train.${module}.req.${Domain}SaveReq;
import com.chen.train.${module}.resp.${Domain}QueryResp;
import com.chen.train.${module}.service.${Domain}Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/${do_main}")
public class ${Domain}AdminController {

    @Autowired

    private ${Domain}Service ${domain}Service;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody ${Domain}SaveReq ${domain}SaveReq) {
        ${domain}Service.save(${domain}SaveReq);
        return new CommonResp<>("添加乘客成功！");
    }

    /**
     * RequestBody 将json格式转化为Java对象所以此处不需要使用
     * @param ${domain}QueryReq
     * @return
     */
    @GetMapping("/query-list")
    public CommonResp<PageResp <${Domain}QueryResp>> queryList(${Domain}QueryReq ${domain}QueryReq){
        //直接从本地线程变量中获取${module}Id
        PageResp<${Domain}QueryResp> queryList = ${domain}Service.queryList(${domain}QueryReq);
        return new  CommonResp<>(queryList);
    }


    /**
     * 删除功能
     */
    @DeleteMapping("delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id)
    {

        ${domain}Service.delete(id);
        return new CommonResp<>("删除成功");
    }


}