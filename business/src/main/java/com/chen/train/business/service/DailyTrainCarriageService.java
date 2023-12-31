package com.chen.train.business.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.chen.train.business.domain.*;
import com.chen.train.business.enums.SeatColEnum;
import com.chen.train.common.resp.PageResp;
import com.chen.train.common.util.SnowUtil;
import com.chen.train.business.mapper.DailyTrainCarriageMapper;
import com.chen.train.business.req.DailyTrainCarriageQueryReq;
import com.chen.train.business.req.DailyTrainCarriageSaveReq;
import com.chen.train.business.resp.DailyTrainCarriageQueryResp;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainCarriageService {


       private static final Logger LOG = LoggerFactory.getLogger(DailyTrainCarriageService.class);


       @Resource
    private  DailyTrainCarriageMapper dailyTrainCarriageMapper;
    @Autowired
    private TrainCarriageService trainCarriageService;



    public void save(DailyTrainCarriageSaveReq dailyTrainCarriageSaveReq) {
        DateTime now = new DateTime().now();

        // 自动计算出列数和总座位数
        List<SeatColEnum> seatColEnums = SeatColEnum.getColsByType(dailyTrainCarriageSaveReq.getSeatType());
        dailyTrainCarriageSaveReq.setColCount(seatColEnums.size());
        dailyTrainCarriageSaveReq.setSeatCount(dailyTrainCarriageSaveReq.getColCount() * dailyTrainCarriageSaveReq.getRowCount());
        DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(dailyTrainCarriageSaveReq, DailyTrainCarriage.class);
        //如果id为空，则说明是新增
        if(ObjectUtil.isNull(dailyTrainCarriage.getId()))
        {
            //获取本地线程变量中的businessId，而不再需要进行传参设置
            dailyTrainCarriage.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.insert(dailyTrainCarriage);

        }else{
            //否则，则是修改，此时需要加上修改时间
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.updateByPrimaryKey(dailyTrainCarriage);
        }

    }

    /**
     * 查询列表功能
     */

    public PageResp<DailyTrainCarriageQueryResp> queryList(DailyTrainCarriageQueryReq dailyTrainCarriageQueryReq) {

        DailyTrainCarriageExample dailyTrainCarriageExample=new DailyTrainCarriageExample();
        //添加一个降序排列,后面的反而显示在前面
        dailyTrainCarriageExample.setOrderByClause("date desc,train_code asc,'index' asc");

        DailyTrainCarriageExample.Criteria criteria = dailyTrainCarriageExample.createCriteria();
        //条件查询，必须使用createCriteria下面的方法,根据条件查询

        if (ObjUtil.isNotNull(dailyTrainCarriageQueryReq.getDate())) {
            criteria.andDateEqualTo(dailyTrainCarriageQueryReq.getDate());
        }
        if (ObjUtil.isNotEmpty(dailyTrainCarriageQueryReq.getTrainCode())) {
            criteria.andTrainCodeEqualTo(dailyTrainCarriageQueryReq.getTrainCode());
        }


        //直接使用pagehelper中的分页插件，其中查询一页中的两条数据，然后需要写在sql之前
        PageHelper.startPage(dailyTrainCarriageQueryReq.getPage(), dailyTrainCarriageQueryReq.getSize());
        List<DailyTrainCarriage> dailyTrainCarriages = dailyTrainCarriageMapper.selectByExample(dailyTrainCarriageExample);
        PageInfo<DailyTrainCarriage> pageInfo=new PageInfo<>(dailyTrainCarriages);

        List<DailyTrainCarriageQueryResp> respList = BeanUtil.copyToList(dailyTrainCarriages, DailyTrainCarriageQueryResp.class);
        PageResp<DailyTrainCarriageQueryResp> pageResp=new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(respList);
        return pageResp;

    }

    /**
     * 删除功能
     */

    public void delete(Long id){
        dailyTrainCarriageMapper.deleteByPrimaryKey(id);
    }


    /**
     * 生成每日车厢数据
     */


    public void genDaily(Date date, String trainCode) {
        LOG.info("生成日期【{}】车次【{}】的车厢信息开始", DateUtil.formatDate(date), trainCode);

        //首先需要删除已经存在的改车次的车站信息
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        dailyTrainCarriageExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        dailyTrainCarriageMapper.deleteByExample(dailyTrainCarriageExample);
        //查询某一个车次的所有车站信息
        List<TrainCarriage> trainCarriageList = trainCarriageService.selectByTrainCode(trainCode);


        if (CollUtil.isEmpty(trainCarriageList)) {
            LOG.info("该车次没有车厢基础数据，生成该车次的车厢信息结束");

            return;

        }
        for (TrainCarriage trainCarriage : trainCarriageList
        ) {
            DateTime now = new  DateTime().now();
            DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(trainCarriage, DailyTrainCarriage.class);
            dailyTrainCarriage.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setDate(date);
            dailyTrainCarriageMapper.insert(dailyTrainCarriage);


        }
        LOG.info("生成日期【{}】车次【{}】的车厢信息结束", DateUtil.formatDate(date), trainCode);



    }


    public List<DailyTrainCarriage> selectBySeatType(Date date,String trainCode,String seatType){

        DailyTrainCarriageExample dailyTrainCarriageExample=new DailyTrainCarriageExample();

        dailyTrainCarriageExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode)
                .andSeatTypeEqualTo(seatType);
      return   dailyTrainCarriageMapper.selectByExample(dailyTrainCarriageExample);
    }


}


