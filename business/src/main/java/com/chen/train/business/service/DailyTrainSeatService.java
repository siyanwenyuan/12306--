package com.chen.train.business.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.chen.train.business.domain.*;
import com.chen.train.business.req.SeatSellReq;
import com.chen.train.business.resp.SeatSellResp;
import com.chen.train.common.resp.PageResp;
import com.chen.train.common.util.SnowUtil;
import com.chen.train.business.mapper.DailyTrainSeatMapper;
import com.chen.train.business.req.DailyTrainSeatQueryReq;
import com.chen.train.business.req.DailyTrainSeatSaveReq;
import com.chen.train.business.resp.DailyTrainSeatQueryResp;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainSeatService {


    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainStationService.class);

    @Autowired
    private  DailyTrainSeatMapper dailyTrainSeatMapper;

    @Autowired
    private TrainSeatService trainSeatService;

    @Autowired
    private TrainStationService trainStationService;



    public void save(DailyTrainSeatSaveReq dailyTrainSeatSaveReq) {
        DateTime now = new DateTime().now();
        DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(dailyTrainSeatSaveReq, DailyTrainSeat.class);
        //如果id为空，则说明是新增
        if(ObjectUtil.isNull(dailyTrainSeat.getId()))
        {
            //获取本地线程变量中的businessId，而不再需要进行传参设置
            dailyTrainSeat.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.insert(dailyTrainSeat);

        }else{
            //否则，则是修改，此时需要加上修改时间
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.updateByPrimaryKey(dailyTrainSeat);
        }

    }

    /**
     * 查询列表功能
     */

    public PageResp<DailyTrainSeatQueryResp> queryList(DailyTrainSeatQueryReq dailyTrainSeatQueryReq) {

        DailyTrainSeatExample dailyTrainSeatExample=new DailyTrainSeatExample();
        //添加一个降序排列,后面的反而显示在前面
        dailyTrainSeatExample.setOrderByClause("date desc, train_code asc, carriage_index asc, carriage_seat_index asc");

        DailyTrainSeatExample.Criteria criteria = dailyTrainSeatExample.createCriteria();
        //条件查询，必须使用createCriteria下面的方法,根据条件查询
        if (ObjectUtil.isNotEmpty(dailyTrainSeatQueryReq.getTrainCode())) {
            criteria.andTrainCodeEqualTo(dailyTrainSeatQueryReq.getTrainCode());
        }


        //直接使用pagehelper中的分页插件，其中查询一页中的两条数据，然后需要写在sql之前
        PageHelper.startPage(dailyTrainSeatQueryReq.getPage(), dailyTrainSeatQueryReq.getSize());
        List<DailyTrainSeat> dailyTrainSeats = dailyTrainSeatMapper.selectByExample(dailyTrainSeatExample);
        PageInfo<DailyTrainSeat> pageInfo=new PageInfo<>(dailyTrainSeats);

        List<DailyTrainSeatQueryResp> respList = BeanUtil.copyToList(dailyTrainSeats, DailyTrainSeatQueryResp.class);
        PageResp<DailyTrainSeatQueryResp> pageResp=new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(respList);
        return pageResp;

    }

    /**
     * 删除功能
     */

    public void delete(Long id){
        dailyTrainSeatMapper.deleteByPrimaryKey(id);
    }


    /**
     * 生成每日车次的车厢的车座信息
     */


    public void genDaily(Date date, String trainCode) {
        LOG.info("生成日期【{}】车次【{}】的座位信息开始", DateUtil.formatDate(date), trainCode);

        //首先需要删除已经存在的改车次的车站信息
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        dailyTrainSeatExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        dailyTrainSeatMapper.deleteByExample(dailyTrainSeatExample);
        //查询某一个车次的所有车站信息
        List<TrainSeat> trainSeatList = trainSeatService.selectByTrainCode(trainCode);

        List<TrainStation> stationList = trainStationService.selectByTrainCode(trainCode);
        String sell = StrUtil.fillBefore("", '0', stationList.size() - 1);


        if (CollUtil.isEmpty(trainSeatList)) {
            LOG.info("该车次没有座位基础数据，生成该车次的座位信息结束");
            return;

        }
        for (TrainSeat trainSeat : trainSeatList
        ) {
            DateTime now = new  DateTime().now();
            DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(trainSeat, DailyTrainSeat.class);
            dailyTrainSeat.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setDate(date);
            dailyTrainSeat.setSell(sell);
            dailyTrainSeatMapper.insert(dailyTrainSeat);


        }
        LOG.info("生成日期【{}】车次【{}】的座位信息结束", DateUtil.formatDate(date), trainCode);



    }
    public int countSeat(Date date, String trainCode) {
        return countSeat(date, trainCode, null);
    }

    public int countSeat(Date date, String trainCode, String seatType) {
        DailyTrainSeatExample example = new DailyTrainSeatExample();
        DailyTrainSeatExample.Criteria criteria = example.createCriteria();
        criteria.andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);
        if (StrUtil.isNotBlank(seatType)) {
            criteria.andSeatTypeEqualTo(seatType);
        }
        long l = dailyTrainSeatMapper.countByExample(example);
        if (l == 0L) {
            return -1;
        }
        return (int) l;
    }

    /**
     * 通过车厢查询座位
     */

    public List<DailyTrainSeat> selectByCarriage(Date date,String trainCode,Integer carriageIndex)
    {
        DailyTrainSeatExample dailyTrainSeatExample=new DailyTrainSeatExample();
        //添加一个排序条件，通过索引进行排序
        dailyTrainSeatExample.setOrderByClause("carriage_seat_index asc");
        dailyTrainSeatExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode)
                .andCarriageIndexEqualTo(carriageIndex);
        return  dailyTrainSeatMapper.selectByExample(dailyTrainSeatExample);

    }

    /**
     * 查询某日某车次的所有座位
     */
    public List<SeatSellResp> querySeatSell(SeatSellReq req) {
        Date date = req.getDate();
        String trainCode = req.getTrainCode();
        LOG.info("查询日期【{}】车次【{}】的座位销售信息", DateUtil.formatDate(date), trainCode);
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        dailyTrainSeatExample.setOrderByClause("`carriage_index` asc, carriage_seat_index asc");
        dailyTrainSeatExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);
        return BeanUtil.copyToList(dailyTrainSeatMapper.selectByExample(dailyTrainSeatExample), SeatSellResp.class);
    }

}


