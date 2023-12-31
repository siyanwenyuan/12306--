package com.chen.train.business.controller;


import com.chen.train.common.context.LoginMemberContext;
import com.chen.train.common.resp.CommonResp;
import com.chen.train.common.resp.PageResp;
import com.chen.train.business.req.DailyTrainStationQueryReq;
import com.chen.train.business.req.DailyTrainStationSaveReq;
import com.chen.train.business.resp.DailyTrainStationQueryResp;
import com.chen.train.business.service.DailyTrainStationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/daily-train-station")
public class DailyTrainStationAdminController {

    @Autowired

    private DailyTrainStationService dailyTrainStationService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody DailyTrainStationSaveReq dailyTrainStationSaveReq) {
        dailyTrainStationService.save(dailyTrainStationSaveReq);
        return new CommonResp<>("添加乘客成功！");
    }

    /**
     * RequestBody 将json格式转化为Java对象所以此处不需要使用
     * @param dailyTrainStationQueryReq
     * @return
     */
    @GetMapping("/query-list")
    public CommonResp<PageResp <DailyTrainStationQueryResp>> queryList(DailyTrainStationQueryReq dailyTrainStationQueryReq){
        //直接从本地线程变量中获取businessId
        PageResp<DailyTrainStationQueryResp> queryList = dailyTrainStationService.queryList(dailyTrainStationQueryReq);
        return new  CommonResp<>(queryList);
    }


    /**
     * 删除功能
     */
    @DeleteMapping("delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id)
    {

        dailyTrainStationService.delete(id);
        return new CommonResp<>("删除成功");
    }


}