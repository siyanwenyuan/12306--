package com.chen.train.business.controller.admin;

import com.chen.train.common.context.LoginMemberContext;
import com.chen.train.common.resp.CommonResp;
import com.chen.train.common.resp.PageResp;
import com.chen.train.business.req.DailyTrainQueryReq;
import com.chen.train.business.req.DailyTrainSaveReq;
import com.chen.train.business.resp.DailyTrainQueryResp;
import com.chen.train.business.service.DailyTrainService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/admin/daily-train")
public class DailyTrainAdminController {

    @Autowired

    private DailyTrainService dailyTrainService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody DailyTrainSaveReq dailyTrainSaveReq) {
        dailyTrainService.save(dailyTrainSaveReq);
        return new CommonResp<>("添加乘客成功！");
    }

    /**
     * RequestBody 将json格式转化为Java对象所以此处不需要使用
     * @param dailyTrainQueryReq
     * @return
     */
    @GetMapping("/query-list")
    public CommonResp<PageResp <DailyTrainQueryResp>> queryList(DailyTrainQueryReq dailyTrainQueryReq){
        //直接从本地线程变量中获取businessId
        PageResp<DailyTrainQueryResp> queryList = dailyTrainService.queryList(dailyTrainQueryReq);
        return new  CommonResp<>(queryList);
    }


    /**
     * 删除功能
     */
    @DeleteMapping("delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id)
    {

        dailyTrainService.delete(id);
        return new CommonResp<>("删除成功");
    }

    /**
     * 执行定时任务接口
     */
  /*  @GetMapping("gen-daily/{date}")
    public CommonResp<Object> genDaily(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date)
    {
        dailyTrainService.genDaily(date);
        return new CommonResp<>();

    }*/


    @GetMapping("/gen-daily/{date}")
    public CommonResp<Object> genDaily(@PathVariable(value = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        dailyTrainService.genDaily(date);
        return new CommonResp<>();
    }


    /**
     * 测试远程调用
     */
    @GetMapping("/hello")
    public String getHello(){

        return "chenwan Hello";

    }


}