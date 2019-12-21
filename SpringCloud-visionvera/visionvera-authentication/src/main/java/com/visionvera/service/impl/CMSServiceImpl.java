package com.visionvera.service.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.cms.IndustryVO;
import com.visionvera.bean.cms.PhoneVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.RoleVO;
import com.visionvera.bean.cms.UserGroupVO;
import com.visionvera.bean.cms.UserLogin;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.authentication.CMSDao;
import com.visionvera.service.CMSService;
import com.visionvera.util.ResultMapUtil;
import com.visionvera.util.StringUtil;
import com.visionvera.util.TimeUtil;

/**
 * 会管的数据业务
 *
 */
/**
 * @author user
 *2018年6月13日
 */
/**
 * @author user
 *2018年6月13日
 */
/**
 * @author user
 *2018年6月13日
 */
@Service
@Transactional(value = "transactionManager_authentication", rollbackFor = Exception.class)
public class CMSServiceImpl implements CMSService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CMSDao cmsDao;
	
	
	/**
	 * 查询本周或者本月会议召开的数量
	 * @param dataType 0表示本周，1表示本月
	 * @return
	 */
	public Map<String, Object> getMeetingNum(Integer dataType) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		int meetingNum = 0;
		
		//今天
		String currentDay = TimeUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss");
		
		if (dataType.equals(0)) {//获取本周会议召开的数量
			//本周一
			String firstDayOfWeek = TimeUtil.firstDayOfWeek();
			
			paramsMap.put("startTime", firstDayOfWeek);
			paramsMap.put("endTime", currentDay);
			meetingNum = this.cmsDao.selectMeetingNum(paramsMap);//获取该时间段内会议的数量
			
		} else if (dataType.equals(1)) {//获取本月会议召开的数量
			//本月第一天
			String firstDayOfMonth = TimeUtil.firstDayOfMonth(0);
			
			paramsMap.put("startTime", firstDayOfMonth);
			paramsMap.put("endTime", currentDay);
			meetingNum = this.cmsDao.selectMeetingNum(paramsMap);
		}
		resultMap.put("meetingNum", meetingNum);
		
		return resultMap;
	}
	
	

	/**
	 * @Description: 统计最近num周、num月的会议数量
	 * @param num(0表示本周或本月)  dataType(0表示周，1表示月)
	 * @return Map<String,Object>  
	 * @author zhanghui
	 * @date 2018年6月9日
	 */
	@Override
	public Map<String, Object> getMeetingNum(Integer num, Integer dataType) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		int meetingNum = 0;
		String currentDay = TimeUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss");
		//获取本周或本月会议召开数量
		if(num==0){
			return getMeetingNum(dataType);
		}
		///获取num周会议召开的数量
		if(dataType==0){
			String  startTime = TimeUtil.lastWeek(num);
			paramsMap.put("startTime", startTime);
			paramsMap.put("endTime", currentDay);
			meetingNum = this.cmsDao.selectMeetingNum(paramsMap);//获取该时间段内会议的数量
			
		}else{
		//获取num月会议召开的数量	
			String  startTime = TimeUtil.lastMonths(num);
			paramsMap.put("startTime", startTime);
			paramsMap.put("endTime", currentDay);
			meetingNum = this.cmsDao.selectMeetingNum(paramsMap);//获取该时间段内会议的数量
		}  
		resultMap.put("meetingNum", meetingNum);
		return resultMap;
	}

	/**
	 * @Description: 统计最近num周、num月的会议数量百分比
	 * @param num(0当前的周或月) dataType(0表示周，1表示月) perType(1本周和上周 ,2不同年份同月 ,3本月和上月)百分比
	 * @return Map<String,Object>  
	 * @author zhanghui
	 * @date 2018年6月9日
	 */
	@Override
	public Map<String, Object> getMeetingNumPer(Integer num, Integer dataType, Integer perType) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		String currentDate =TimeUtil.dateToString(new Date());
		////本周和上周
		if(perType==1){
			//本周数量
			BigDecimal week = new BigDecimal((Integer)getMeetingNum(0).get("meetingNum"));
			//上周数量
			BigDecimal lastweek = new BigDecimal((Integer)getMeetingNum(-1).get("meetingNum"));
			BigDecimal per =week.subtract(lastweek).divide(lastweek,2).multiply(new BigDecimal(100));
			String increase = String.valueOf(per.doubleValue())+"%";
			resultMap.put("meetingNum", lastweek.intValue());
			resultMap.put("meetingPer", increase);
			return resultMap;
			
			
		}else if(perType==2){
			//本月数量
			BigDecimal currentMonthNum = new BigDecimal((Integer)getMeetingNum(1).get("meetingNum"));
			//查询同月上一年
			String lastMonth =TimeUtil.lastYears(-1);
			String firstDate =TimeUtil.getFirstDayOfMonth(TimeUtil.stringToDate(lastMonth));//上一年同月第一天
			String lastDate =TimeUtil.getLastDayOfMonth(TimeUtil.stringToDate(lastMonth));//上一年同月最后一天
			BigDecimal lastYearMonthNum = new BigDecimal((Integer)getMeetingNumByDate(firstDate,lastDate).get("meetingNum"));
			BigDecimal per =lastYearMonthNum.subtract(currentMonthNum).divide(lastYearMonthNum,2).multiply(new BigDecimal(100));
			String increase = String.valueOf(per.doubleValue())+"%";
			resultMap.put("meetingNum", currentMonthNum.intValue());
			resultMap.put("meetingPer", increase);
			return resultMap;
		}else if(perType==3){
			//本月数量
			BigDecimal currentMonthNum = new BigDecimal((Integer)getMeetingNum(1).get("meetingNum"));
			//查询上个月数据
			String lastMonth =TimeUtil.lastMonths(-1);
			String firstDate =TimeUtil.getFirstDayOfMonth(TimeUtil.stringToDate(lastMonth));//上月第一天
			String lastDate =TimeUtil.getLastDayOfMonth(TimeUtil.stringToDate(lastMonth));//上月月最后一天
			BigDecimal lastMonthNum = new BigDecimal((Integer)getMeetingNumByDate(firstDate,lastDate).get("meetingNum"));
			BigDecimal per =lastMonthNum.subtract(currentMonthNum).divide(lastMonthNum,2).multiply(new BigDecimal(100));
			String increase = String.valueOf(per.doubleValue())+"%";
			resultMap.put("meetingNum", currentMonthNum.intValue());
			resultMap.put("meetingPer", increase);
			return resultMap;
		}
		
		return resultMap;
	}
	
	/**
	 * @Description: 查询指定日期的会议数量
	 * @param beginTime endTime
	 * @return Map<String,Object>  
	 * @author zhanghui
	 * @date 2018年6月14日
	 */
	@Override
	public Map<String, Object> getMeetingNumByDate(String beginTime, String endTime) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		int meetingNum = 0;
		try {
			paramsMap.put("startTime", beginTime);
			paramsMap.put("endTime", endTime);
			meetingNum = this.cmsDao.selectMeetingNum(paramsMap);
			resultMap.put("meetingNum", meetingNum);
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "查询成功");
		} catch (Exception e) {
			logger.error("查询异常"+e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "查询失败");
		}
		
		return resultMap;
	}
	
	/**
	 * @Description: 查询月、周会议数量首页展示
	 * @param dataType numMonth
	 * @return Map<String,Object>  
	 * @author zhanghui
	 * @date 2018年6月14日
	 */
	@Override
	public Map<String, Object> getMeetingData(Integer dataType, Integer numMonth) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<String> xData = new ArrayList<String>();
		List<Integer> yData = new ArrayList<Integer>();
		Integer currentMonthNum=0;
		//月数据
		if(dataType==1){
			for(int i=-numMonth+1;i<=0;i++){
				//获取当前日期及前十个月的日期
				String date =TimeUtil.lastMonths(i);
				String mouth =TimeUtil.getOnlyMonth(TimeUtil.stringToDate(date));
				//获取月初日期
				String monthFirst =TimeUtil.getFirstDayOfMonth(TimeUtil.stringToDate(date));
				//获取月末日期
				String monthEnd =TimeUtil.getLastDayOfMonth(TimeUtil.stringToDate(date));
				Map<String, Object> result =getMeetingNumByDate(monthFirst, monthEnd);
				if((Integer)result.get("errcode")==1){
					return result;
				}
				Integer num = (Integer) result.get("meetingNum");
				xData.add(Integer.parseInt(mouth)+"月");
				yData.add(num);
				//获取当月数量
				if(i==0){
					currentMonthNum=num;
				}
				
			}
			resultMap.put("xData", xData);
			resultMap.put("yData", yData);
			resultMap.put("currentMonthNum", currentMonthNum);
		//周数据
		}else{
		 int totalWeek=	 TimeUtil.getTotalWeekOfMonth(new Date()); //获取当月有几周
		 int  weekOfMonth = TimeUtil.getWeekOfMonth(new Date());//获取当前是第几周
		 String currentMonth =TimeUtil.lastMonths(0);//当前日期
		 String firstDayOfWeek =TimeUtil.getFirstDayOfMonth(TimeUtil.stringToDate(currentMonth));//本月第一天
		 String endDayOfMonth = TimeUtil.getLastDayOfMonth(TimeUtil.stringToDate(currentMonth));//本月最后一天
		 for(int i=1;i<=totalWeek;i++){
				 String mondayOfWeek =TimeUtil.getFirstOfSpecifiedWeek(TimeUtil.stringToDate(firstDayOfWeek),1);//当前日期的下周一
			     String endDayOfWeek =TimeUtil.lastDay(TimeUtil.stringToDate(mondayOfWeek),-1);
			     if(!TimeUtil.isBefore(endDayOfWeek, endDayOfMonth)){
			    	 endDayOfWeek =endDayOfMonth;
			     }
			     
			     Map<String, Object> result =getMeetingNumByDate(firstDayOfWeek, endDayOfWeek);
			     if((Integer)result.get("errcode")==1){
						return result;
				 }
			    System.out.println("====="+firstDayOfWeek+"======="+endDayOfWeek);
			    firstDayOfWeek =mondayOfWeek;
				Integer num = (Integer) result.get("meetingNum");
				xData.add("第"+i+"周");
				yData.add(num);
				resultMap.put("xData", xData);
				resultMap.put("yData", yData);
				resultMap.put("currentWeek", weekOfMonth);
		 }
			
		}
		
		return resultMap;
	}
	
	
	/**
	 * 用户登录执行的逻辑
	 * @param loginType 登录类型。1表示用户名密码登录；2表示手机号密码登录；3表示手机号验证码登录
	 * @param userConditionParma 用户参数
	 * @return map
	 * @throws ParseException 
	 */
	public Map<String, Object> getUserLoginInfo(Integer loginType, UserLogin userConditionParma) throws ParseException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		
		UserVO user = null;//数据库查询的结果
		
		if (loginType.equals(1)) {//用户名密码登录
			paramsMap.put("loginName", userConditionParma.getLoginName());
			user = this.cmsDao.selectUserByUniqueKey(paramsMap);
			
			if (user == null) {//用户不存在
				resultMap = ResultMapUtil.getResultMapError("用户名不存在");
				return resultMap;
			}
			
			if (!user.getLoginPwd().equals(userConditionParma.getLoginPwd())) {//密码不对
				resultMap = ResultMapUtil.getResultMapError("用户名和密码不匹配");
				return resultMap;
			}
			
			if (StringUtil.isNull(user.getLoginModule())) {//用户没有可登陆的模块
				resultMap = ResultMapUtil.getResultMapError("用户无权限登陆");
				return resultMap;
			}
		}
		
		if (loginType.equals(2)) {//手机号密码登录
			paramsMap.put("phone", userConditionParma.getPhone());
			user = this.cmsDao.selectUserByUniqueKey(paramsMap);
			
			if (user == null) {//手机号不存在
				resultMap = ResultMapUtil.getResultMapError("手机号不存在");
				return resultMap;
			}
			
			if (!user.getLoginPwd().equals(userConditionParma.getLoginPwd())) {//密码不对
				resultMap = ResultMapUtil.getResultMapError("用户名和密码不匹配");
				return resultMap;
			}
			
			if (StringUtil.isNull(user.getLoginModule())) {//用户没有可登陆的模块
				resultMap = ResultMapUtil.getResultMapError("用户无权限登陆");
				return resultMap;
			}
		}
		
		if (loginType.equals(3)) {//手机号验证码登录
			resultMap = this.chkVerifiCode(userConditionParma.getPhone(), userConditionParma.getVerifiCode());
			
			if (resultMap.get("errcode").equals(1)) {//校验验证码失败
				return resultMap;
			}
			
			//校验成功，查询用户信息，返回Controller
			paramsMap.put("phone", userConditionParma.getPhone());
			user = this.cmsDao.selectUserByUniqueKey(paramsMap);
			
			if (user == null) {//手机号不存在
				resultMap = ResultMapUtil.getResultMapError("手机号不存在");
				return resultMap;
			}
			
			if (StringUtil.isNull(user.getLoginModule())) {//用户没有可登陆的模块
				resultMap = ResultMapUtil.getResultMapError("用户无权限登陆");
				return resultMap;
			}
			
		}
		
		resultMap.put("errcode", 0);
		resultMap.put("errmsg", "登录成功");
		resultMap.put("data", user);
		return resultMap;
	}
	
	/**
	 * 获取Session失效时间
	 * @return
	 */
	public String getSessionTimeout() {
		return this.cmsDao.selectSessionTimeout();
	}
	
	/**
	 * 添加用户信息
	 * @param user
	 * @return
	 */
	public Map<String, Object> addUser(UserVO user) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		
		int userCount = 0;//用户数量
		
		resultMap = ResultMapUtil.getResultMapError("添加失败");
		
		//检查用户名是否重复
		paramsMap.put("loginName", user.getLoginName());
		userCount = this.cmsDao.selectUserCountByUniqueKey(paramsMap);
		if (userCount > 0) {
			resultMap = ResultMapUtil.getResultMapError("[" + user.getLoginName() + "]账号已存在");
			return resultMap;
		}
		
		//检查手机号是否重复
		paramsMap.clear();
		paramsMap.put("phone", user.getPhone());
		userCount = this.cmsDao.selectUserCountByUniqueKey(paramsMap);
		if (userCount > 0) {
			resultMap = ResultMapUtil.getResultMapError("[" + user.getPhone() + "]手机号已存在");
			return resultMap;
		}
		
		//执行插入数据库
		int count = this.cmsDao.insertUser(user);
		if (count > 0) {
			/** 添加用户与行业归属的关系 Start */
			paramsMap.clear();
			paramsMap.put("userId", user.getUuid());
			paramsMap.put("industryIds", user.getIndustryId().split(","));
			this.cmsDao.insertUserIndustry(paramsMap);
			/** 添加用户与行业归属的关系 End */
			
			/** 添加用户与角色的关系 Start */
			
			/** 添加用户与角色的关系 End */
			
			resultMap = ResultMapUtil.getResultMap(0, "添加成功");
			return resultMap;
			
		}
		
		resultMap = ResultMapUtil.getResultMapError("插入用户失败");
		return resultMap;
		
		/*//补充数据
		user.setIsvalid(1);//通过web添加的用户默认直接生效，不需要审批
		user.setAllowHkzs(0);//禁止登录
		//插入用户
		int insertUser = this.cmsDao.insertUser(user);
		if (insertUser > 0) {
			//将新增的用户信息插入到OA通讯录中
			UserVO oaUser = this.cmsDao.selectOAUserByName(user);
			if (oaUser == null) {//不存在则插入OA通讯录
				this.cmsDao.insertOAUser(user);
			}
			
			//新增用户角色关联
			if (StringUtil.isNotNull(user.getRole())) {
				paramsMap.clear();
				paramsMap.put("userId", user.getUuid());
				paramsMap.put("roleId", user.getRole());
				this.cmsDao.insertUserRole(paramsMap);
			}
			
			//新增用户分组信息、用于用组关联、用户组与设备组关联
			UserGroupVO userGroupVO = new UserGroupVO();
			userGroupVO.setName(user.getLoginName());
			if (userGroupVO.getDescription() == null) {
				userGroupVO.setDescription(" ");
			}
			userGroupVO.setUuid(user.getGroupId());
			userGroupVO.setUsers(user.getUuid());
			this.addUserGroup(userGroupVO);//新增用户组
			
			resultMap = ResultMapUtil.getResultMap(0, "添加成功");
			return resultMap;
		}
		return resultMap;*/
	}
	
	/**
	 * 更新用户相关信息
	 * @param user
	 * @return
	 */
	@Override
	public Map<String, Object> updateUser(UserVO user) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HashMap<String, Object> paramsMap = new HashMap<String, Object>();
		
		int count = this.cmsDao.updateUser(user);//更新用户基本信息
		
		if (count > 0) {
			/** 更新用户与可登陆平台的关系 Start */
			paramsMap.clear();
			paramsMap.put("userId", user.getUuid());
			this.cmsDao.deleteUserPlatform(paramsMap);//先根据用户ID删除
			
			paramsMap.clear();
			paramsMap.put("userId", user.getUuid());
			paramsMap.put("platformIds", user.getPlatformId().split(","));
			this.cmsDao.insertUserPlatform(paramsMap);//然后添加用户登录平台的关系
			/** 更新用户与可登陆平台的关系 End */
			
			/** 更新用户与行业归属的关系 Start */
			paramsMap.clear();
			paramsMap.put("userId", user.getUuid());
			this.cmsDao.deleteUserIndustry(paramsMap);//先根据用户ID删除用于与行业归属的关系
			
			paramsMap.clear();
			paramsMap.put("userId", user.getUuid());
			paramsMap.put("industryIds", user.getIndustryId().split(","));
			this.cmsDao.insertUserIndustry(paramsMap);//然后添加用户与行业归属的关系
			/** 更新用户与行业归属的关系End */
			
			resultMap = ResultMapUtil.getResultMap(0, "更新用户信息成功");
			return resultMap;
		}
		
		//禁止登陆会控助手
		/*if (user.getAllowHkzs() != null && user.getAllowHkzs().equals(0)) {
			paramsMap.put("userId", user.getUuid());//用户ID
			paramsMap.put("clientType", 4);//4代表会控助手
			this.cmsDao.deleteAccessToken(paramsMap);//禁止登录会控助手
		}
		
		//更新用户基础数据
		this.cmsDao.updateUser(user);
		
		//更新用户和角色关联
		if (StringUtil.isNotNull(user.getRole())) {
			paramsMap.clear();
			paramsMap.put("roleId", user.getRole());
			paramsMap.put("userId", user.getUuid());
			this.cmsDao.deleteUserRoleRelByUserId(paramsMap);
			this.cmsDao.insertUserRole(paramsMap);
		}*/
		
		resultMap = ResultMapUtil.getResultMapError("更新用户信息失败");
		return resultMap;
	}
	
	/**
	 * 删除用户信息
	 * @param uuid 用户主键ID
	 * @return
	 */
	@Override
	public Map<String, Object> deleteUser(String uuid) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		
		String[] uuids = uuid.split(",");
		paramsMap.put("uuids", uuids);
		paramsMap.put("adminName", GlobalConstants.ADMIN_LOGIN_NAME);//超级管理员的登录名（admin)
		int isAdmin = this.cmsDao.selectAdminCount(paramsMap);
		if (isAdmin > 0) {//判断是否包含admin用户
			resultMap = ResultMapUtil.getResultMapError("admin不允许删除");
			return resultMap;
		}
		
		UserVO userOnLine = this.cmsDao.selectUserOnLine(paramsMap);//判断用户是否处于登录状态
		if (userOnLine != null) {
			resultMap = ResultMapUtil.getResultMapError("用户在线, 不允许删除");
			return resultMap;
		}
		
		//删除用户
		this.cmsDao.deleteUser(paramsMap);
		resultMap = ResultMapUtil.getResultMap(0, "删除成功");
		
		return resultMap;
	}
	
	/**
	 * 分页获取用户列表
	 * @param isPage 是否分页
	 * @param user 用户查询信息
	 * @param pageNum 页码。isPage为false值为null
	 * @param pageSize 每页显示多少条。isPage为false值为null
	 * @return
	 */
	@Override
	public PageInfo<UserVO> getUserList(boolean isPage, UserVO user, Integer pageNum, Integer pageSize) {
		if (isPage) {//需要分页处理
			PageHelper.startPage(pageNum, pageSize);//开始分页
		}
		
		List<UserVO> userList = this.cmsDao.selectUserByCondition(user);
		PageInfo<UserVO> userInfo = new PageInfo<UserVO>(userList);
		return userInfo;
	}
	
	/**
	 * 获取所有角色列表
	 * @param isPage 是否分页。true表示分页，false表示不分页
	 * @param pageNum 页码.isPage为false可以为null
	 * @param pageSize 每页显示多少条.isPage为false可以为null
	 * @return
	 */
	@Override
	public PageInfo<RoleVO> getAllRoles(boolean isPage, Integer pageNum, Integer pageSize) {
		if (isPage) {
			PageHelper.startPage(pageNum, pageSize);
		}
		
		List<RoleVO> roleList = this.cmsDao.selectAllRoles();
		PageInfo<RoleVO> roleInfo = new PageInfo<RoleVO>(roleList);
		
		return roleInfo;
	}
	
	/**
	 * 获取现有用户的所有行政区域
	 * @return
	 */
	@Override
	public List<RegionVO> getUserRegionList() {
		return this.cmsDao.selectUserRegionList();
	}
	
	/**
	 * 获取行政区域列表
	 * @return
	 */
	@Override
	public List<RegionVO> getRegions(Map<String, Object> paramsMap) {
		return this.cmsDao.selectRegions(paramsMap);
	}
	
	/**
	 * 根据用户的主键UUID查询用户信息。关联出其角色和组的信息
	 * @param uuid
	 * @return
	 */
	@Override
	public UserVO getUser(String uuid) {
		return this.cmsDao.selectUserByUUID(uuid);
	}
	
	/**
	 * 获取行业归属信息
	 * @param isPage 是否分页。false表示不分页，获取所有；true表示分页
	 * @param pageNum 页码
	 * @param pageSize 每页显示多少条
	 * @return
	 */
	@Override
	public PageInfo<IndustryVO> getIndustry(boolean isPage, Integer pageNum, Integer pageSize, IndustryVO industry) {
		if (isPage) {
			PageHelper.startPage(pageNum, pageSize);
		}
		
		List<IndustryVO> industryList = this.cmsDao.selectIndustryByCondition(industry);//查询数据
		PageInfo<IndustryVO> industryInfo = new PageInfo<IndustryVO>(industryList);
		
		return industryInfo;
	}
	
	/**
	 * 校验验证码是否正确
	 * 
	 * @return
	 * @throws ParseException 
	 */
	private Map<String, Object> chkVerifiCode(String phone, String verifiCode) throws ParseException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("phone", phone);
		paramsMap.put("verifiCode", verifiCode);
		
		List<PhoneVO> phoneList = this.cmsDao.selectPhoneByPhoneAndVriCode(paramsMap);
		
		if (phoneList.size() != 1 || !phoneList.get(0).getVerifiCode().equals(verifiCode)) {
			resultMap = ResultMapUtil.getResultMapError("验证码不正确");
			return resultMap;
		}
		Date date_new = new Date(System.currentTimeMillis());
		Long newTime = date_new.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		Date date_old = sdf.parse(phoneList.get(0).getCreateTime());
		Long oldTime = date_old.getTime();
		if((newTime - oldTime) > 300000){//5分钟失效
			resultMap = ResultMapUtil.getResultMapError("验证码已失效，请重新获取");
			return resultMap;
		}
		
		resultMap = ResultMapUtil.getResultMap(0, "验证成功");
		return resultMap;
	}
	
	/**
	 * 新增用户分组信息
	 * @param userGroup
	 * @return
	 */
	@SuppressWarnings("unused")
	private int addUserGroup(UserGroupVO userGroup) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		
		int result = 0;
		
		result = this.cmsDao.insertUserGroup(userGroup);//新增用户分组
		
		//新增用户与用户分组关联
		paramsMap.put("groupUUID", userGroup.getUuid());
		if (StringUtil.isNotNull(userGroup.getUsers())) {
			String[] uuids = userGroup.getUsers().split(",");
			if (uuids != null && uuids.length > 0) {
				paramsMap.put("userUUIDs", uuids);
				result = this.cmsDao.insertUserGroupRel(paramsMap);
			}
		}
		
		//新增用户组和设备组的关联
		if(StringUtil.isNotNull(userGroup.getDeviceGroups())) {
			String[] uuids = userGroup.getDeviceGroups().split(",");
			if (uuids != null && uuids.length > 0) {
				paramsMap.put("deviceGroupIds", uuids);
				result = this.cmsDao.insertGroupDevGroupRel(paramsMap);
			}
		}
		
		return result;
	}
}
