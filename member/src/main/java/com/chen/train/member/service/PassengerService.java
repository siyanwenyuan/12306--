package com.chen.train.member.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.chen.train.common.context.LoginMemberContext;
import com.chen.train.common.resp.PageResp;
import com.chen.train.common.util.SnowUtil;
import com.chen.train.member.domain.Passenger;
import com.chen.train.member.domain.PassengerExample;
import com.chen.train.member.mapper.PassengerMapper;
import com.chen.train.member.req.PassengerQueryReq;
import com.chen.train.member.req.PassengerSaveReq;
import com.chen.train.member.resp.PassengerQueryResp;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerService {


    @Autowired
    private  PassengerMapper passengerMapper;

    public void save(PassengerSaveReq passengerSaveReq) {
        DateTime now = new DateTime().now();
        Passenger passenger = BeanUtil.copyProperties(passengerSaveReq, Passenger.class);
        //如果id为空，则说明是新增
        if(ObjectUtil.isNull(passenger.getId()))
        {
            //获取本地线程变量中的memberId，而不再需要进行传参设置
            passenger.setId(SnowUtil.getSnowflakeNextId());
            passenger.setCreateTime(now);
            passenger.setUpdateTime(now);
            passengerMapper.insert(passenger);

        }else{
            //否则，则是修改，此时需要加上修改时间
            passenger.setUpdateTime(now);
            passengerMapper.updateByPrimaryKey(passenger);
        }

    }

    /**
     * 查询列表功能
     */

    public PageResp<PassengerQueryResp> queryList(PassengerQueryReq passengerQueryReq) {

        PassengerExample passengerExample=new PassengerExample();
        //添加一个降序排列,后面的反而显示在前面
        passengerExample.setOrderByClause("id desc");

        PassengerExample.Criteria criteria = passengerExample.createCriteria();
        //条件查询，必须使用createCriteria下面的方法,根据条件查询


        //直接使用pagehelper中的分页插件，其中查询一页中的两条数据，然后需要写在sql之前
        PageHelper.startPage(passengerQueryReq.getPage(), passengerQueryReq.getSize());
        List<Passenger> passengers = passengerMapper.selectByExample(passengerExample);
        PageInfo<Passenger> pageInfo=new PageInfo<>(passengers);

        List<PassengerQueryResp> respList = BeanUtil.copyToList(passengers, PassengerQueryResp.class);
        PageResp<PassengerQueryResp> pageResp=new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(respList);
        return pageResp;

    }

    /**
     * 删除功能
     */

    public void delete(Long id){
        passengerMapper.deleteByPrimaryKey(id);
    }


    /**
     * 查询我的所有乘客
     */

    public List<PassengerQueryResp> queryMine(){
        PassengerExample passengerExample=new PassengerExample();

        passengerExample.setOrderByClause("name asc");
        passengerExample.createCriteria().andMemberIdEqualTo(LoginMemberContext.getId());

        List<Passenger> passengers = passengerMapper.selectByExample(passengerExample);
        return BeanUtil.copyToList(passengers,PassengerQueryResp.class);

    }


}


