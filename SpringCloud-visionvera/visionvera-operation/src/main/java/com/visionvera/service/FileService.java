package com.visionvera.service;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.cms.FileGradeVO;
import com.visionvera.bean.cms.FileVO;

public interface FileService {
	
	/**
	 * @param paramsMap 
	 * 
	 * @Title: getFileList
	 * @Description: TODO 获取文件列表
	 * @param @return  参数说明  
	 * @return Map<String,Object>    返回类型 
	 * @throws
	 */
	List<FileVO> getFileList(Map<String, Object> file);
	
	/**
	 * 
	 * @Title: getFileListCount 
	 * @Description: 获取文件列表总条目 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getFileListCount(Map<String, Object> map);

	/**
	 * @param paramsMap 
	 * 
	 * @Title: addFile
	 * @Description: TODO 新增文件-记录文件信息
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	int addFile(FileVO file);

	/**
	 * @param paramsMap 
	 * 
	 * @Title: updateFile
	 * @Description: TODO 更新文件信息
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	int updateFile(FileVO file);

	/**
	 * @param paramsMap 
	 * 
	 * @Title: deleteFile
	 * @Description: TODO 删除文件
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	void deleteFile(FileVO f);
	
	/**
	 * @param paramsMap 
	 * 
	 * @Title: checkName
	 * @Description: TODO 检验文件名是否存在
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	int checkName(FileVO file);
	/**
	 * @param paramsMap 
	 * 
	 * @Title: getAppInfoByAppPackage
	 * @Description: TODO 
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	List<FileVO> getAppInfoByAppPackage(Map<String, Object> paramsMap);
	/**
	 * @param paramsMap 
	 * 
	 * @Title: updateForceByPackage
	 * @Description: TODO 
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	int updateForceByPackage(Map<String, Object> paramsMap);
	/** <pre>fileQRCode(这里用一句话描述这个方法的作用)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月16日 下午2:18:54    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月16日 下午2:18:54    
	 * 修改备注： 
	 * @param file
	 * @return</pre>    
	 */
	List<FileVO> getAppInfoByAppQRCode();
	/** <pre>checkSpecialVersion(校验当前版本是否为特殊版本)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月16日 下午2:18:54    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月16日 下午2:18:54    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	Integer checkSpecialVersion(Map<String, Object> paramsMap);

	/** <pre>getUserPath(获取用户行政区域path值)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月16日 下午2:18:54    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月16日 下午2:18:54    
	 * 修改备注： 
	 * @param loginName
	 * @return</pre>    
	 */
	String getUserPath(String loginName);
	/** <pre>getFileDetail(这里用一句话描述这个方法的作用)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月25日 上午10:45:30    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月25日 上午10:45:30    
	 * 修改备注： 
	 * @param name
	 * @return</pre>    
	 */
	FileVO getFileDetail(Map<String, Object> paramsMap);
	/** <pre>qrUpdate(获取会控App当前可使用版本)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月25日 上午10:45:30    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月25日 上午10:45:30    
	 * 修改备注： 
	 * @return</pre>    
	 */
	List<FileVO> qrUpdate();
	/** <pre>addFileGrade(往数据库新增需升级文件信息)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年3月1日 上午10:45:30    
	 * 修改人：周逸芳        
	 * 修改时间：2018年3月1日 上午10:45:30    
	 * 修改备注： 
	 * @return</pre>    
	 */
	int addFileGrade(Map<String, Object> resultMap);
	/** <pre>selectFile(查询数据库文件类型)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年3月1日 上午10:45:30    
	 * 修改人：周逸芳        
	 * 修改时间：2018年3月1日 上午10:45:30    
	 * 修改备注： 
	 * @return</pre>    
	 */
	FileGradeVO selectFile();
	/** <pre>updateGrade(修改文件状态)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年3月1日 上午10:45:30    
	 * 修改人：周逸芳        
	 * 修改时间：2018年3月1日 上午10:45:30    
	 * 修改备注： 
	 * @param userId 
	 * @return</pre>    
	 */
	int updateGrade(Map<String, Object> paramMap);
	/** <pre>deleteFileGrade(删除数据库文件)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年3月1日 上午10:45:30    
	 * 修改人：周逸芳        
	 * 修改时间：2018年3月1日 上午10:45:30    
	 * 修改备注： 
	 * @return</pre>    
	 */
	int deleteFileGrade();
}
