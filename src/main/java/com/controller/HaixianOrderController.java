
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 商品订单
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/haixianOrder")
public class HaixianOrderController {
    private static final Logger logger = LoggerFactory.getLogger(HaixianOrderController.class);

    @Autowired
    private HaixianOrderService haixianOrderService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private AddressService addressService;
    @Autowired
    private HaixianService haixianService;
    @Autowired
    private YonghuService yonghuService;
@Autowired
private CartService cartService;
@Autowired
private HaixianCommentbackService haixianCommentbackService;



    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = haixianOrderService.queryPage(params);

        //字典表数据转换
        List<HaixianOrderView> list =(List<HaixianOrderView>)page.getList();
        for(HaixianOrderView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        HaixianOrderEntity haixianOrder = haixianOrderService.selectById(id);
        if(haixianOrder !=null){
            //entity转view
            HaixianOrderView view = new HaixianOrderView();
            BeanUtils.copyProperties( haixianOrder , view );//把实体数据重构到view中

                //级联表
                AddressEntity address = addressService.selectById(haixianOrder.getAddressId());
                if(address != null){
                    BeanUtils.copyProperties( address , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setAddressId(address.getId());
                    view.setAddressYonghuId(address.getYonghuId());
                }
                //级联表
                HaixianEntity haixian = haixianService.selectById(haixianOrder.getHaixianId());
                if(haixian != null){
                    BeanUtils.copyProperties( haixian , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setHaixianId(haixian.getId());
                }
                //级联表
                YonghuEntity yonghu = yonghuService.selectById(haixianOrder.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody HaixianOrderEntity haixianOrder, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,haixianOrder:{}",this.getClass().getName(),haixianOrder.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("用户".equals(role))
            haixianOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        haixianOrder.setInsertTime(new Date());
        haixianOrder.setCreateTime(new Date());
        haixianOrderService.insert(haixianOrder);
        return R.ok();
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody HaixianOrderEntity haixianOrder, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,haixianOrder:{}",this.getClass().getName(),haixianOrder.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("用户".equals(role))
//            haixianOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<HaixianOrderEntity> queryWrapper = new EntityWrapper<HaixianOrderEntity>()
            .eq("id",0)
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        HaixianOrderEntity haixianOrderEntity = haixianOrderService.selectOne(queryWrapper);
        if(haixianOrderEntity==null){
            haixianOrderService.updateById(haixianOrder);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        haixianOrderService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<HaixianOrderEntity> haixianOrderList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            HaixianOrderEntity haixianOrderEntity = new HaixianOrderEntity();
//                            haixianOrderEntity.setHaixianOrderUuidNumber(data.get(0));                    //订单号 要改的
//                            haixianOrderEntity.setAddressId(Integer.valueOf(data.get(0)));   //收货地址 要改的
//                            haixianOrderEntity.setHaixianId(Integer.valueOf(data.get(0)));   //商品 要改的
//                            haixianOrderEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            haixianOrderEntity.setBuyNumber(Integer.valueOf(data.get(0)));   //购买数量 要改的
//                            haixianOrderEntity.setHaixianOrderTruePrice(data.get(0));                    //实付价格 要改的
//                            haixianOrderEntity.setHaixianOrderTypes(Integer.valueOf(data.get(0)));   //订单类型 要改的
//                            haixianOrderEntity.setHaixianOrderCourierName(data.get(0));                    //快递公司 要改的
//                            haixianOrderEntity.setHaixianOrderCourierNumber(data.get(0));                    //快递单号 要改的
//                            haixianOrderEntity.setHaixianOrderPaymentTypes(Integer.valueOf(data.get(0)));   //支付类型 要改的
//                            haixianOrderEntity.setInsertTime(date);//时间
//                            haixianOrderEntity.setCreateTime(date);//时间
                            haixianOrderList.add(haixianOrderEntity);


                            //把要查询是否重复的字段放入map中
                                //订单号
                                if(seachFields.containsKey("haixianOrderUuidNumber")){
                                    List<String> haixianOrderUuidNumber = seachFields.get("haixianOrderUuidNumber");
                                    haixianOrderUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> haixianOrderUuidNumber = new ArrayList<>();
                                    haixianOrderUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("haixianOrderUuidNumber",haixianOrderUuidNumber);
                                }
                        }

                        //查询是否重复
                         //订单号
                        List<HaixianOrderEntity> haixianOrderEntities_haixianOrderUuidNumber = haixianOrderService.selectList(new EntityWrapper<HaixianOrderEntity>().in("haixian_order_uuid_number", seachFields.get("haixianOrderUuidNumber")));
                        if(haixianOrderEntities_haixianOrderUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(HaixianOrderEntity s:haixianOrderEntities_haixianOrderUuidNumber){
                                repeatFields.add(s.getHaixianOrderUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [订单号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        haixianOrderService.insertBatch(haixianOrderList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }





    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        // 没有指定排序字段就默认id倒序
        if(StringUtil.isEmpty(String.valueOf(params.get("orderBy")))){
            params.put("orderBy","id");
        }
        PageUtils page = haixianOrderService.queryPage(params);

        //字典表数据转换
        List<HaixianOrderView> list =(List<HaixianOrderView>)page.getList();
        for(HaixianOrderView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        HaixianOrderEntity haixianOrder = haixianOrderService.selectById(id);
            if(haixianOrder !=null){


                //entity转view
                HaixianOrderView view = new HaixianOrderView();
                BeanUtils.copyProperties( haixianOrder , view );//把实体数据重构到view中

                //级联表
                    AddressEntity address = addressService.selectById(haixianOrder.getAddressId());
                if(address != null){
                    BeanUtils.copyProperties( address , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setAddressId(address.getId());
                }
                //级联表
                    HaixianEntity haixian = haixianService.selectById(haixianOrder.getHaixianId());
                if(haixian != null){
                    BeanUtils.copyProperties( haixian , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setHaixianId(haixian.getId());
                }
                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(haixianOrder.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody HaixianOrderEntity haixianOrder, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,haixianOrder:{}",this.getClass().getName(),haixianOrder.toString());
            HaixianEntity haixianEntity = haixianService.selectById(haixianOrder.getHaixianId());
            if(haixianEntity == null){
                return R.error(511,"查不到该商品");
            }
            // Double haixianNewMoney = haixianEntity.getHaixianNewMoney();

            if(false){
            }
            else if((haixianEntity.getHaixianKucunNumber() -haixianOrder.getBuyNumber())<0){
                return R.error(511,"购买数量不能大于库存数量");
            }
            else if(haixianEntity.getHaixianNewMoney() == null){
                return R.error(511,"商品价格不能为空");
            }

            //计算所获得积分
            Double buyJifen =0.0;
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            YonghuEntity yonghuEntity = yonghuService.selectById(userId);
            if(yonghuEntity == null)
                return R.error(511,"用户不能为空");
            if(yonghuEntity.getNewMoney() == null)
                return R.error(511,"用户金额不能为空");
            double balance = yonghuEntity.getNewMoney() - haixianEntity.getHaixianNewMoney()*haixianOrder.getBuyNumber();//余额
            if(balance<0)
                return R.error(511,"余额不够支付");
            haixianOrder.setHaixianOrderTypes(1); //设置订单状态为已支付
            haixianOrder.setHaixianOrderTruePrice(haixianEntity.getHaixianNewMoney()*haixianOrder.getBuyNumber()); //设置实付价格
            haixianOrder.setYonghuId(userId); //设置订单支付人id
            haixianOrder.setHaixianOrderUuidNumber(String.valueOf(new Date().getTime()));
            haixianOrder.setHaixianOrderPaymentTypes(1);
            haixianOrder.setInsertTime(new Date());
            haixianOrder.setCreateTime(new Date());
                haixianEntity.setHaixianKucunNumber( haixianEntity.getHaixianKucunNumber() -haixianOrder.getBuyNumber());
                haixianService.updateById(haixianEntity);
                haixianOrderService.insert(haixianOrder);//新增订单
            yonghuEntity.setNewMoney(balance);//设置金额
            yonghuService.updateById(yonghuEntity);
            return R.ok();
    }
    /**
     * 添加订单
     */
    @RequestMapping("/order")
    public R add(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("order方法:,,Controller:{},,params:{}",this.getClass().getName(),params.toString());
        String haixianOrderUuidNumber = String.valueOf(new Date().getTime());

        //获取当前登录用户的id
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        Integer addressId = Integer.valueOf(String.valueOf(params.get("addressId")));

        Integer haixianOrderPaymentTypes = Integer.valueOf(String.valueOf(params.get("haixianOrderPaymentTypes")));//支付类型

        String data = String.valueOf(params.get("haixians"));
        JSONArray jsonArray = JSON.parseArray(data);
        List<Map> haixians = JSON.parseObject(jsonArray.toString(), List.class);

        //获取当前登录用户的个人信息
        YonghuEntity yonghuEntity = yonghuService.selectById(userId);

        //当前订单表
        List<HaixianOrderEntity> haixianOrderList = new ArrayList<>();
        //商品表
        List<HaixianEntity> haixianList = new ArrayList<>();
        //购物车ids
        List<Integer> cartIds = new ArrayList<>();

        BigDecimal zhekou = new BigDecimal(1.0);

        //循环取出需要的数据
        for (Map<String, Object> map : haixians) {
           //取值
            Integer haixianId = Integer.valueOf(String.valueOf(map.get("haixianId")));//商品id
            Integer buyNumber = Integer.valueOf(String.valueOf(map.get("buyNumber")));//购买数量
            HaixianEntity haixianEntity = haixianService.selectById(haixianId);//购买的商品
            String id = String.valueOf(map.get("id"));
            if(StringUtil.isNotEmpty(id))
                cartIds.add(Integer.valueOf(id));

            //判断商品的库存是否足够
            if(haixianEntity.getHaixianKucunNumber() < buyNumber){
                //商品库存不足直接返回
                return R.error(haixianEntity.getHaixianName()+"的库存不足");
            }else{
                //商品库存充足就减库存
                haixianEntity.setHaixianKucunNumber(haixianEntity.getHaixianKucunNumber() - buyNumber);
            }

            //订单信息表增加数据
            HaixianOrderEntity haixianOrderEntity = new HaixianOrderEntity<>();

            //赋值订单信息
            haixianOrderEntity.setHaixianOrderUuidNumber(haixianOrderUuidNumber);//订单号
            haixianOrderEntity.setAddressId(addressId);//收货地址
            haixianOrderEntity.setHaixianId(haixianId);//商品
            haixianOrderEntity.setYonghuId(userId);//用户
            haixianOrderEntity.setBuyNumber(buyNumber);//购买数量 ？？？？？？
            haixianOrderEntity.setHaixianOrderTypes(1);//订单类型
            haixianOrderEntity.setHaixianOrderPaymentTypes(haixianOrderPaymentTypes);//支付类型
            haixianOrderEntity.setInsertTime(new Date());//订单创建时间
            haixianOrderEntity.setCreateTime(new Date());//创建时间

            //判断是什么支付方式 1代表余额 2代表积分
            if(haixianOrderPaymentTypes == 1){//余额支付
                //计算金额
                Double money = new BigDecimal(haixianEntity.getHaixianNewMoney()).multiply(new BigDecimal(buyNumber)).multiply(zhekou).doubleValue();

                if(yonghuEntity.getNewMoney() - money <0 ){
                    return R.error("余额不足,请充值！！！");
                }else{
                    //计算所获得积分
                    Double buyJifen =0.0;
                    yonghuEntity.setNewMoney(yonghuEntity.getNewMoney() - money); //设置金额


                    haixianOrderEntity.setHaixianOrderTruePrice(money);

                }
            }
            haixianOrderList.add(haixianOrderEntity);
            haixianList.add(haixianEntity);

        }
        haixianOrderService.insertBatch(haixianOrderList);
        haixianService.updateBatchById(haixianList);
        yonghuService.updateById(yonghuEntity);
        if(cartIds != null && cartIds.size()>0)
            cartService.deleteBatchIds(cartIds);
        return R.ok();
    }

    /**
    * 退款
    */
    @RequestMapping("/refund")
    public R refund(Integer id, HttpServletRequest request){
        logger.debug("refund方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        String role = String.valueOf(request.getSession().getAttribute("role"));

            HaixianOrderEntity haixianOrder = haixianOrderService.selectById(id);
            Integer buyNumber = haixianOrder.getBuyNumber();
            Integer haixianOrderPaymentTypes = haixianOrder.getHaixianOrderPaymentTypes();
            Integer haixianId = haixianOrder.getHaixianId();
            if(haixianId == null)
                return R.error(511,"查不到该商品");
            HaixianEntity haixianEntity = haixianService.selectById(haixianId);
            if(haixianEntity == null)
                return R.error(511,"查不到该商品");
            Double haixianNewMoney = haixianEntity.getHaixianNewMoney();
            if(haixianNewMoney == null)
                return R.error(511,"商品价格不能为空");

            Integer userId = (Integer) request.getSession().getAttribute("userId");
            YonghuEntity yonghuEntity = yonghuService.selectById(userId);
            if(yonghuEntity == null)
                return R.error(511,"用户不能为空");
            if(yonghuEntity.getNewMoney() == null)
                return R.error(511,"用户金额不能为空");

            Double zhekou = 1.0;


            //判断是什么支付方式 1代表余额 2代表积分
            if(haixianOrderPaymentTypes == 1){//余额支付
                //计算金额
                Double money = haixianEntity.getHaixianNewMoney() * buyNumber  * zhekou;
                //计算所获得积分
                Double buyJifen = 0.0;
                yonghuEntity.setNewMoney(yonghuEntity.getNewMoney() + money); //设置金额


            }

            haixianEntity.setHaixianKucunNumber(haixianEntity.getHaixianKucunNumber() + buyNumber);



            haixianOrder.setHaixianOrderTypes(2);//设置订单状态为退款
            haixianOrderService.updateById(haixianOrder);//根据id更新
            yonghuService.updateById(yonghuEntity);//更新用户信息
            haixianService.updateById(haixianEntity);//更新订单中商品的信息
            return R.ok();
    }


    /**
     * 发货
     */
    @RequestMapping("/deliver")
    public R deliver(Integer id ,String haixianOrderCourierNumber, String haixianOrderCourierName){
        logger.debug("refund:,,Controller:{},,ids:{}",this.getClass().getName(),id.toString());
        HaixianOrderEntity  haixianOrderEntity = new  HaixianOrderEntity();;
        haixianOrderEntity.setId(id);
        haixianOrderEntity.setHaixianOrderTypes(3);
        haixianOrderEntity.setHaixianOrderCourierNumber(haixianOrderCourierNumber);
        haixianOrderEntity.setHaixianOrderCourierName(haixianOrderCourierName);
        boolean b =  haixianOrderService.updateById( haixianOrderEntity);
        if(!b){
            return R.error("发货出错");
        }
        return R.ok();
    }














    /**
     * 收货
     */
    @RequestMapping("/receiving")
    public R receiving(Integer id){
        logger.debug("refund:,,Controller:{},,ids:{}",this.getClass().getName(),id.toString());
        HaixianOrderEntity  haixianOrderEntity = new  HaixianOrderEntity();
        haixianOrderEntity.setId(id);
        haixianOrderEntity.setHaixianOrderTypes(4);
        boolean b =  haixianOrderService.updateById( haixianOrderEntity);
        if(!b){
            return R.error("收货出错");
        }
        return R.ok();
    }



    /**
    * 评价
    */
    @RequestMapping("/commentback")
    public R commentback(Integer id, String commentbackText, Integer haixianCommentbackPingfenNumber, HttpServletRequest request){
        logger.debug("commentback方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
            HaixianOrderEntity haixianOrder = haixianOrderService.selectById(id);
        if(haixianOrder == null)
            return R.error(511,"查不到该订单");
        if(haixianOrder.getHaixianOrderTypes() != 4)
            return R.error(511,"您不能评价");
        Integer haixianId = haixianOrder.getHaixianId();
        if(haixianId == null)
            return R.error(511,"查不到该商品");

        HaixianCommentbackEntity haixianCommentbackEntity = new HaixianCommentbackEntity();
            haixianCommentbackEntity.setId(id);
            haixianCommentbackEntity.setHaixianId(haixianId);
            haixianCommentbackEntity.setYonghuId((Integer) request.getSession().getAttribute("userId"));
            haixianCommentbackEntity.setHaixianCommentbackText(commentbackText);
            haixianCommentbackEntity.setInsertTime(new Date());
            haixianCommentbackEntity.setReplyText(null);
            haixianCommentbackEntity.setUpdateTime(null);
            haixianCommentbackEntity.setCreateTime(new Date());
            haixianCommentbackService.insert(haixianCommentbackEntity);

            haixianOrder.setHaixianOrderTypes(5);//设置订单状态为已评价
            haixianOrderService.updateById(haixianOrder);//根据id更新
            return R.ok();
    }












}
