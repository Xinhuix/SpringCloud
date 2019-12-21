package com.visionvera.service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.cms.IndustryVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.RoleVO;
import com.visionvera.bean.cms.UserLogin;
import com.visionvera.bean.cms.UserVO;

/**
 * 会管数据的业务
 *
 */
public interface CMSService {
	/**
	 * 查询本周或者本月会议召开的数量
	 * @param dataType 0表示本周，1表示本月
	 * @return
	 */
	public Map<String, Object> getMeetingNum(Integer dataType);
	/**
	 * 查询前几周或者前几月会议召开的数量
	 * @param dataType 0表示周，1表示月
	 * @return
	 */
	public Map<String, Object> getMeetingNum(Integer num,Integer dataType);
	/**
	 * 查询指定日期会议召开的数量
	 * @param dataType 0表示周，1表示月
	 * @return
	 */
	public Map<String, Object> getMeetingNumByDate(String beginTime,String endTime);
	
	/**
	 * 查询百分比
	 * @param num(0当前的周或月) dataType(0表示周，1表示月) perType(1不同年份同周,2本周和上周 ,3不同年份同月 ,4本月和上月)百分比
	 * @return
	 */
	public Map<String, Object> getMeetingNumPer(Integer num,Integer dataType,Integer perType);
	/**
	 * 首页会议数量接口
	 * @param dataType(0表示周，1表示月) numMonth展示月数
	 * @return
	 */
	public Map<String, Object> getMeetingData(Integer dataType,Integer numMonth);
	
	/**
	 * 用户登录执行的逻辑
	 * @param loginType 登录类型。1表示用户名密码登录；2表示手机号密码登录；3表示手机号码登录
	 * @param user 用户登录信息
	 * @return map
	 * @throws ParseException 
	 */
	public Map<String, Object> getUserLoginInfo(Integer loginType, UserLogin user) throws ParseException;
	
	/**
	 * 获取Session失效时间
	 * @return
	 */
	public String getSessionTimeout();
	
	/**
	 * 添加用户信息
	 * @param user
	 * @return
	 */
	public Map<String, Object> addUser(UserVO user);
	
	/**
	 * 更新用户相关信息
	 * @param user
	 * @return
	 */
	public Map<String, Object> updateUser(UserVO user);
	
	/**
	 * 删除用户信息
	 * @param uuid 用户主键ID
	 * @return
	 */
	public Map<String, Object> deleteUser(String uuid);
	
	/**
	 * 分页获取用户列表
	 * @param isPage 是否分页
	 * @param user 用户查询信息
	 * @param pageNum 页码。isPage为false值为null
	 * @param pageSize 每页显示多少条。isPage为false值为null
	 * @return
	 */
	public PageInfo<UserVO> getUserList(boolean isPage, UserVO user, Integer pageNum, Integer pageSize);
	
	/**
	 * 获取所有角色列表
	 * @param isPage 是否分页。true表示分页，false表示不分页
	 * @param pageNum 页码.isPage为false可以为null
	 * @param pageSize 每页显示多少条.isPage为false可以为null
	 * @return
	 */
	public PageInfo<RoleVO> getAllRoles(boolean isPage, Integer pageNum, Integer pageSize);
	
	/**
	 * 获取现有用户的所有行政区域
	 * @return
	 */
	public List<RegionVO> getUserRegionList();
	
	/**
	 * 获取行政区域列表
	 * @return
	 */
	public List<RegionVO> getRegions(Map<String, Object> paramsMap);
	
	/**
	 * 根据用户主键UUID查询用户信息。关联出其角色和组的信息
	 * @param uuid
	 * @return
	 */
	public UserVO getUser(String uuid);
	
	/**
	 * 获取行业归属信息
	 * @param isPage 是否分页。false表示不分页，获取所有；true表示分页
	 * @param pageNum 页码
	 * @param pageSize 每页显示多少条
	 * @return
	 */
	public PageInfo<IndustryVO> getIndustry(boolean isPage, Integer pageNum, Integer pageSize, IndustryVO industry);
}
