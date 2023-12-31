package com.chen.train.business.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.chen.train.common.exception.BusinessException;
import com.chen.train.common.exception.BusinessExceptionEnum;
import com.chen.train.common.resp.PageResp;
import com.chen.train.common.util.SnowUtil;
import com.chen.train.business.domain.Station;
import com.chen.train.business.domain.StationExample;
import com.chen.train.business.mapper.StationMapper;
import com.chen.train.business.req.StationQueryReq;
import com.chen.train.business.req.StationSaveReq;
import com.chen.train.business.resp.StationQueryResp;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {


    @Autowired
    private  StationMapper stationMapper;

    public void save(StationSaveReq stationSaveReq) {
        DateTime now = new DateTime().now();
        Station station = BeanUtil.copyProperties(stationSaveReq, Station.class);
        //如果id为空，则说明是新增
        if(ObjectUtil.isNull(station.getId()))
        {
            //保存之前需要校验唯一键是否存在
            Station stationDB = selectByUnique(stationSaveReq.getName());

            if(ObjectUtil.isNotEmpty(stationDB)){
                //如果不是空，则不需要添加，则需要向前端抛出一个异常
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_STATION_NAME_UNIQUE_ERROR);
            }

            //获取本地线程变量中的businessId，而不再需要进行传参设置
            station.setId(SnowUtil.getSnowflakeNextId());
            station.setCreateTime(now);
            station.setUpdateTime(now);
            stationMapper.insert(station);

        }else{
            //否则，则是修改，此时需要加上修改时间
            station.setUpdateTime(now);
            stationMapper.updateByPrimaryKey(station);
        }

    }

    private Station  selectByUnique(String name) {
        StationExample stationExample=new StationExample();
        stationExample.createCriteria().andNameEqualTo(name);
        List<Station> stationList = stationMapper.selectByExample(stationExample);
        if(CollUtil.isNotEmpty(stationList)){
            return stationList.get(0);
        }else{
            return null;
        }
    }

    /**
     * 查询列表功能
     */

    public PageResp<StationQueryResp> queryList(StationQueryReq stationQueryReq) {

        StationExample stationExample=new StationExample();
        //添加一个降序排列,后面的反而显示在前面
        stationExample.setOrderByClause("id desc");

        StationExample.Criteria criteria = stationExample.createCriteria();
        //条件查询，必须使用createCriteria下面的方法,根据条件查询


        //直接使用pagehelper中的分页插件，其中查询一页中的两条数据，然后需要写在sql之前
        PageHelper.startPage(stationQueryReq.getPage(), stationQueryReq.getSize());
        List<Station> stations = stationMapper.selectByExample(stationExample);
        PageInfo<Station> pageInfo=new PageInfo<>(stations);

        List<StationQueryResp> respList = BeanUtil.copyToList(stations, StationQueryResp.class);
        PageResp<StationQueryResp> pageResp=new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(respList);
        return pageResp;

    }

    /**
     * 删除功能
     */

    public void delete(Long id){
        stationMapper.deleteByPrimaryKey(id);
    }


    /**
     * 查询所有火车
     *
     */

    public List<StationQueryResp> queryAllStation(){

        StationExample stationExample=new StationExample();
        stationExample.setOrderByClause("name_pinyin asc");
        List<Station> stationList = stationMapper.selectByExample(stationExample);
        return BeanUtil.copyToList(stationList,StationQueryResp.class);

    }


}


