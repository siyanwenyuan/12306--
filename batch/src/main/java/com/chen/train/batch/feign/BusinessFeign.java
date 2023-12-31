package com.chen.train.batch.feign;


import com.chen.train.common.resp.CommonResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;

/*
@FeignClient(name = "business",url = "http://localhost:8002/business")
*/
@FeignClient("business")
public interface BusinessFeign {
    @GetMapping("/hello")
     String hello();
    @GetMapping("/business/admin/daily-train/gen-daily/{date}")
   // CommonResp<Object> genDaily(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date);
    CommonResp<Object> genDaily(@PathVariable(value = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date);

}
