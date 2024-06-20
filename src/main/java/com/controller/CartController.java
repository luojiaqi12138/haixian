
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
 * 购物车
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/cart")
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private HaixianService haixianService;
    @Autowired
    private YonghuService yonghuService;



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
        PageUtils page = cartService.queryPage(params);

        //字典表数据转换
        List<CartView> list =(List<CartView>)page.getList();
        for(CartView c:list){
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
        CartEntity cart = cartService.selectById(id);
        if(cart !=null){
            //entity转view
            CartView view = new CartView();
            BeanUtils.copyProperties( cart , view );//把实体数据重构到view中

                //级联表
                HaixianEntity haixian = haixianService.selectById(cart.getHaixianId());
                if(haixian != null){
                    BeanUtils.copyProperties( haixian , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setHaixianId(haixian.getId());
                }
                //级联表
                YonghuEntity yonghu = yonghuService.selectById(cart.getYonghuId());
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
    public R save(@RequestBody CartEntity cart, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,cart:{}",this.getClass().getName(),cart.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("用户".equals(role))
            cart.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        Wrapper<CartEntity> queryWrapper = new EntityWrapper<CartEntity>()
            .eq("yonghu_id", cart.getYonghuId())
            .eq("haixian_id", cart.getHaixianId())
            .eq("buy_number", cart.getBuyNumber())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        CartEntity cartEntity = cartService.selectOne(queryWrapper);
        if(cartEntity==null){
            cart.setCreateTime(new Date());
            cart.setInsertTime(new Date());
            cartService.insert(cart);
            return R.ok();
        }else {
            return R.error(511,"商品已添加到购物车");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody CartEntity cart, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,cart:{}",this.getClass().getName(),cart.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("用户".equals(role))
            cart.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<CartEntity> queryWrapper = new EntityWrapper<CartEntity>()
            .notIn("id",cart.getId())
            .andNew()
            .eq("yonghu_id", cart.getYonghuId())
            .eq("haixian_id", cart.getHaixianId())
            .eq("buy_number", cart.getBuyNumber())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        CartEntity cartEntity = cartService.selectOne(queryWrapper);
        cart.setUpdateTime(new Date());
        if(cartEntity==null){
            cartService.updateById(cart);//根据id更新
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
        cartService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
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
        PageUtils page = cartService.queryPage(params);

        //字典表数据转换
        List<CartView> list =(List<CartView>)page.getList();
        for(CartView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        CartEntity cart = cartService.selectById(id);
            if(cart !=null){


                //entity转view
                CartView view = new CartView();
                BeanUtils.copyProperties( cart , view );//把实体数据重构到view中

                //级联表
                    HaixianEntity haixian = haixianService.selectById(cart.getHaixianId());
                if(haixian != null){
                    BeanUtils.copyProperties( haixian , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setHaixianId(haixian.getId());
                }
                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(cart.getYonghuId());
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
    * 添加购物车
    */
    @RequestMapping("/add")
    public R add(@RequestBody CartEntity cart, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,cart:{}",this.getClass().getName(),cart.toString());
        Wrapper<CartEntity> queryWrapper = new EntityWrapper<CartEntity>()
            .eq("yonghu_id", cart.getYonghuId())
            .eq("haixian_id", cart.getHaixianId())
            .eq("buy_number", cart.getBuyNumber());

        CartEntity cartEntity = cartService.selectOne(queryWrapper);

        if(cartEntity==null){
            cart.setCreateTime(new Date());
            cart.setInsertTime(new Date());
        cartService.insert(cart);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }



}
