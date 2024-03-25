# 0. Controller类中获取载荷的各种方法

Controller接收参数的常用方式总体可以分为三类。第一类是Get请求通过拼接url进行传递，第二类是Post请求通过请求体进行传递，第三类是通过请求头部进行参数传递。

 **1 @PathVariable接收参数**

请求方式：localhost:7001/param/123

请求示例：

![img](https://ask.qcloudimg.com/raw/yehe-4fac7fe57135/6wgi0ghv4k.png)

```java
@GetMapping("{id}")
public String getPathVariable(@PathVariable String id){
    return "id="+id;
}
```



**2 @RequestParam接收参数**

使用这个注解需要注意两个点，一是加了这个参数后则请求中必须传递这个参数，二是@**RequestParam**这个注解可以指定名字，请求参数必须和指定的这个名字相同，如果不指定，则默认为具体参数名。

请求方式：localhost:7001/param/getParam?myId=18

请求示例：

![](https://ask.qcloudimg.com/raw/yehe-4fac7fe57135/0mze02t4m0.png)

```java
@GetMapping("getParam")
public String getRequestParam(@RequestParam("myId") String id){
    return "id="+id;
}
```



**3 无注解传参**

这种方式和2对比，最大的区别就是这个参数不是必传的，请求路径上可以不传递。

请求方式：localhost:7001/param/getString?id=18

请求示例：

![img](https://ask.qcloudimg.com/raw/yehe-4fac7fe57135/117eohu7zr.png)

```java
@GetMapping("getString")
public String getString(String id){
    return "id="+id;
}
```



**4 HttpServletRequest接收参数**

请求方式：localhost:7001/param/getRequest?id=18

请求示例：

![img](https://ask.qcloudimg.com/raw/yehe-4fac7fe57135/7g5aiymrs3.png)

```java
@GetMapping("getRequest")
public String getRequest(HttpServletRequest request){
    String id = request.getParameter("id");
    return "id="+id;
}
```



**5 @RequestBody接收请求体参数**

这种方式一般用来传递实体对象，加了这个注解后，参数也是必传的。

请求方式：{"id":18}

请求示例：

![img](https://ask.qcloudimg.com/raw/yehe-4fac7fe57135/1strm1mic1.png)

```java
@PostMapping("getBody")
public String getBody(@RequestBody String id){
    return "id="+id;
}
```



**6 @RequestHeader接收请求头参数**

请求示例：

![img](https://ask.qcloudimg.com/raw/yehe-4fac7fe57135/a7hh8ta0w1.png)

```java
@PostMapping("getHeader")
public String getHeader(@RequestHeader String id){
    return "id="+id;
}
```

# 0.1 技术栈、功能架构

![image-20240312093853422](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240312093853422.png?token=AWRFT4N2MKE6P2K6ZKOKJCLF7KEQU)

![image-20240312094002950](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240312094002950.png?token=AWRFT4PAOZFRWCQWTUQOJV3F7KEQW)

![image-20240312094135409](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240312094135409.png?token=AWRFT4OLW7AWROJQWTE7HSDF7KEQY)

# 1. 2024/3/12 --> 新建项目

## 1.1 数据库创建

![image-20240312095737099](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240312095737099.png?token=AWRFT4KAVVZ5GVEIQUNLFGTF7KEQ2)

![image-20240312101045603](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240312101045603.png?token=AWRFT4PNBEGCT3DDVDZ7H5TF7KEQ4)



## 1.2 登录功能

![image-20240313093805162](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240313093805162.png?token=AWRFT4L35U35BEWVVPHNMSDF7KEQ6)

```java
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的密码进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5Hex(password);

        //2.根据页面提交的username查询数据库
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee employee1 = employeeService.getOne(lambdaQueryWrapper);

        //3.查询是否存在此用户名
        if(employee1 != null) {
            return R.error("无此用户名，登录失败");
        }

        //4.比对密码
        if(!employee1.getPassword().equals(password)){
            return R.error("密码不正确，登录失败");
        }

        //5.查看员工状态，若禁用则返回员工禁用结果
        if(employee1.getStatus() == 0){
            return R.error("员工已禁用，登录失败");
        }

        //6.登录成功，将员工信息返回给前端
        request.getSession().setAttribute("employ",employee1.getId());
        return R.success(employee1);

    }
}
```

因为前端传回的访问方式是Post所以用 @PostMapping

传回的为json格式，所以要用@RequestBody进行封装



## 1.3 退出功能

![image-20240313104041196](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240313104041196.png?token=AWRFT4PFZMU6I3NJ2EIZZ3TF7KERA)



# 2. 2024/3/13 and 14 --> 员工管理

需求分析：

![image-20240313110556676](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240313110556676.png?token=AWRFT4KY3LVUKKRCUQ65PNLF7KERC)

![image-20240313110629945](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240313110629945.png?token=AWRFT4LD56IHLU4CJRPVIKLF7KERE)

![image-20240313110959394](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240313110959394.png?token=AWRFT4K33GDARXDLPDEFZN3F7KERG)

## 2.1 登录功能增加过滤器

![image-20240313111202973](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240313111202973.png?token=AWRFT4ISZW6AW3YFIMBVEADF7KERI)

![image-20240313111800941](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240313111800941.png?token=AWRFT4IGWTVDRFTZMLK4ZXLF7KERK)

## 2.2 新增员工

![image-20240313143405189](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240313143405189.png?token=AWRFT4NVAEIWQHBIH4XJGZLF7KERM)

![image-20240313143501712](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240313143501712.png?token=AWRFT4NHTBJUPCWUKPQDZHTF7KERO)

![image-20240313143935399](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240313143935399.png?token=AWRFT4O73SJT34DUHS2TRM3F7KERQ)

![image-20240313161827585](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240313161827585.png?token=AWRFT4IWLR23QNFMELKX7XTF7KERS)

采用全局异常捕获



## 2.3 员工信息分页查询

![image-20240313164910491](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240313164910491.png?token=AWRFT4NMTGO3GODFWONZAHLF7KERU)

![image-20240313165230133](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240313165230133.png?token=AWRFT4OGCVWDWP57CO43F7LF7KERY)

可以使用mp的分页插件简化开发

```java
/**
 * 配置分页插件
 */
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }
}
```



## 2.4 启用/禁用员工账号

前端实现：只有admin用户可以看到禁用启用按钮

![image-20240314102457665](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314102457665.png?token=AWRFT4J7NLMTEBVQOD3SYK3F7KER2)

![image-20240314102906846](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314102906846.png?token=AWRFT4NJO7J6AM4KX4KAYY3F7KER4)



![image-20240314103422172](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314103422172.png?token=AWRFT4K5ZD5QC3PIZWHL3ODF7KER6)

![image-20240314104916207](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314104916207.png?token=AWRFT4NW4FIGHUG6GTTNR4TF7KESA)

![image-20240314105236867](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314105236867.png?token=AWRFT4NKB4DM4ARFQPMAX6LF7KESC)

![image-20240314105236867](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314105312873.png?token=AWRFT4M3AMKMW6XW5L4XUJDF7KESE)



## 2.5 员工信息编辑

![image-20240314135108926](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314135108926.png?token=AWRFT4OKA2CV5RMLHOALOSTF7KESG)

![image-20240314135117622](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314135117622.png?token=AWRFT4NXKKALSZZEUXC7T2DF7KESK)



# 3. 2024/3/14 --> 分类管理（菜品、套餐）

![image-20240314141105057](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314141105057.png?token=AWRFT4JTT7TXKI52LEMAFG3F7KESM)

## 3.1 公共字段自动填充

![image-20240314141307515](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314141307515.png?token=AWRFT4JZZAZEINHSJBW5CSTF7KESO)

![image-20240314141502472](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314141502472.png?token=AWRFT4MUUDUQI2JRRHX5XRTF7KESQ)

![image-20240314141840140](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314141840140.png?token=AWRFT4OEFBOAZAZXQZZO7G3F7KESS)

![image-20240314141900926](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314141900926.png?token=AWRFT4KLMDYZ3OVAXCB5BITF7KESW)

![image-20240314143140544](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314143140544.png?token=AWRFT4NZMI7UVHRUZ2JRLV3F7KESY)

![image-20240314143220567](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314143220567.png?token=AWRFT4NG56TLA3JURTJFI53F7KES2)

![image-20240314143930422](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314143930422.png?token=AWRFT4PZBT7YTFAQ54ZHXC3F7KES6)

![image-20240314144250976](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314144250976.png?token=AWRFT4IJ62MCUY6ABTYIESLF7KETA)



## 3.2 新增分类

![image-20240314151537366](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314151537366.png?token=AWRFT4OYI344Y4ZQZ5S6H2LF7KETC)

![image-20240314151627988](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314151627988.png?token=AWRFT4JWSMO2GHOZW6QP7ZTF7KETE)

![image-20240314151858440](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314151858440.png?token=AWRFT4JEHBRMKWX6TLSC2CDF7KETG)

![image-20240314151930944](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314151930944.png?token=AWRFT4I2HVG73SRRJ3QYIODF7KETI)

![image-20240314152812297](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314152812297.png?token=AWRFT4JPEZSTA54MVCIY6HTF7KETM)



## 3.3删除分类

![image-20240314155902718](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314155902718.png?token=AWRFT4MYAAHRW5QETLSLMBDF7KETO)

![image-20240314161215520](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314161215520.png?token=AWRFT4LAV4OAMH6NGADDMWDF7KETQ)



## 3.4 修改分类

![image-20240314165537368](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314165537368.png?token=AWRFT4OKHWD4ABBSRGXR6ZTF7KETS)



# 4. 2024/3/15 --> 菜品管理

![image-20240314171601548](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314171601548.png?token=AWRFT4M75ZTTIXAUPNKJXL3F7KETW)

![image-20240314171613873](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314171613873.png?token=AWRFT4JJEWFHSQUEOEWU33DF7KETY)

![image-20240314171628273](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240314171628273.png?token=AWRFT4IWFW7RDJEUXRV3OVLF7KET2)



## 4.0 起售、停售、删除



## 4.1 文件上传下载

![image-20240315090437203](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315090437203.png?token=AWRFT4MFGGHUCFCJKY35BALF7KET4)

![image-20240315090613232](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315090613232.png?token=AWRFT4LSBVMAQOKV7LCRSHLF7KET6)

![image-20240315090720497](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315090720497.png?token=AWRFT4JXVKC4M5WBEDBV2FDF7KEUA)

![image-20240315090848205](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315090848205.png?token=AWRFT4MLP3SJKSD3FZVXZ63F7KEUC)

![image-20240315095441144](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315095441144.png?token=AWRFT4P2IECIP7OZ4GFJ2DLF7KEUE)



## 4.2 新增菜品

![image-20240315101912377](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315101912377.png?token=AWRFT4P3JXWTUANPELEOAMDF7KEUG)

![image-20240315101935170](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315101935170.png?token=AWRFT4KRKA4SEZ2COSH3P6DF7KEUI)

![image-20240315102437901](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315102437901.png?token=AWRFT4PMUNXKU7ANI7HOOM3F7KEUK)

![image-20240315103333962](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315103333962.png?token=AWRFT4I7VHVXVAPHJ5E34ADF7KEUO)

![image-20240315105245982](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315105245982.png?token=AWRFT4KVZ7DZAXQGQ4MXTRLF7KEUQ)



## 4.3 分页查询

![image-20240315141430222](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315141430222.png?token=AWRFT4LHPHBPNU3TSBNSE6LF7KEUS)

![image-20240315141615650](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315141615650.png?token=AWRFT4OHQ3VX7PDCYJGOJSDF7KEUU)



## 4.4 修改菜品

![image-20240315153128449](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315153128449.png?token=AWRFT4JDXFH3PHJMTRNXVMLF7KEUW)

![image-20240315153151017](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240315153151017.png?token=AWRFT4MNYQYHIPE5SN6ZVCDF7KEU2)



# 5. 2024/3/19 --> 套餐管理

## 5.0 起售、停售

## 5.1 新增套餐

![image-20240319092119205](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240319092119205.png?token=AWRFT4KPX2WDIKORNPPV6KTF7KEU4)



## 5.2 套餐分页查询

![image-20240319095709274](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240319095709274.png?token=AWRFT4KJHRGH5HCGWAI2HD3F7KEU6)

![image-20240319095724838](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240319095724838.png?token=AWRFT4NONBI3JSZHUI6R3BLF7KEVA)



## 5.3 套餐删除

![image-20240319103146854](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240319103146854.png?token=AWRFT4KR22HVBRZQOCBE5L3F7KEVC)

![image-20240319103211950](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240319103211950.png?token=AWRFT4I427XAYIQDQNLDCLTF7KEVE)



## 5.4 套餐修改

同4.4



# 6. 2024/3/19 --> 用户地址簿管理

![image-20240319144856781](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240319144856781.png?token=AWRFT4OTB2WLRL66Z5GOSWTF7KEVG)

![image-20240319144935916](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240319144935916.png?token=AWRFT4OUIC2JPURBNZYUWTLF7KEVI)



# 7. 2024/3/20 --> 手机验证码登录

![image-20240319141951147](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240319141951147.png?token=AWRFT4K2QXTY2YXIQDIM4JDF7KEVK)



## 7.1 短信发送

![image-20240319142422588](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240319142422588.png?token=AWRFT4MQAW2SP5U2WLQUEKDF7KEVO)

![image-20240320092245277](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240320092245277.png?token=AWRFT4JD2ERBW24G2WE72MLF7KEVQ)

![image-20240320092326196](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240320092326196.png?token=AWRFT4PPTJW3RNKZ5IQFXI3F7KEVS)

![image-20240320092815626](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240320092815626.png?token=AWRFT4NJ3Q3A47A77QLME3TF7KEVU)

![image-20240320092821549](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240320092821549.png?token=AWRFT4PJIF3WR2LEOJ6IY4TF7KEVW)



# 8. 2024/3/20 & 21 --> 移动端菜品展示、购物车、下单

![image-20240319144702700](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240319144702700.png?token=AWRFT4NTQJHKX6DJRNWHSV3F7KEVY)

![image-20240319144846017](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240319144846017.png?token=AWRFT4K3PXO6GSRP2HBUNLTF7KEV2)



## 8.1 菜品展示

![image-20240320145830136](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240320145830136.png?token=AWRFT4KBSID4JQKJDSEGHBDF7KE56)

![image-20240320150539123](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240320150539123.png?token=AWRFT4OTMLZY7HC52XI23HLF7KFQQ)



## 8.2 购物车添加、查看

![image-20240320170225922](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240320170225922.png?token=AWRFT4KT4LXEEDM5QP7JBIDF7KTGY)

![image-20240320170301698](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240320170301698.png?token=AWRFT4NOUFYKFESG2OGXTFLF7KTVO)

![image-20240320170605343](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240320170605343.png?token=AWRFT4PYBZHA5PL5P3H3R4LF7KTVS)

![image-20240320170647772](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240320170647772.png?token=AWRFT4PNRA7HY7QTDWC5LXLF7KVD2)



## 8.3 用户下单

![image-20240321103726712](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240321103726712.png)

![image-20240321103840097](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240321103840097.png)

![image-20240321103850799](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240321103850799.png)

![image-20240321104251796](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240321104251796.png)

![image-20240321104713179](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240321104713179.png)

![image-20240321104804065](https://raw.githubusercontent.com/normalSp/imgSave/master/image-20240321104804065.png)























