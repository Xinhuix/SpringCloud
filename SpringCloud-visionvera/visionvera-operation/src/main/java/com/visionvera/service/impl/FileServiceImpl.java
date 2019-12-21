package com.visionvera.service.impl;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visionvera.bean.cms.FileGradeVO;
import com.visionvera.bean.cms.FileVO;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.operation.FileDao;
import com.visionvera.service.FileService;
import com.visionvera.util.Util;

@Service
@Transactional(value = "transactionManager_operation", rollbackFor = Exception.class)
public class FileServiceImpl implements FileService {

	
	@Resource
	private FileDao fileDao;
	
	/**
	 * @param paramsMap 
	 * 
	 * @Title: getFileList
	 * @Description: TODO 获取文件列表
	 * @param @return  参数说明  
	 * @return Map<String,Object>    返回类型 
	 * @throws
	 */
	public List<FileVO> getFileList(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return fileDao.getFileList(map, new RowBounds());
		}
		return fileDao.getFileList(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * @Title: getFileListCount 
	 * @Description: 获取文件列表总条目 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	public int getFileListCount(Map<String, Object> map) {
		return fileDao.getFileListCount(map);
	}

	/**
	 * @param paramsMap 
	 * 
	 * @Title: addFile
	 * @Description: TODO 新增文件-记录文件信息
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	public int addFile(FileVO file) {
		return fileDao.addFile(file);
	}

	/**
	 * @param paramsMap 
	 * 
	 * @Title: updateFile
	 * @Description: TODO 更新文件信息
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	public int updateFile(FileVO file) {
		return fileDao.updateFile(file);
	}

	/**
	 * @param paramsMap 
	 * 
	 * @Title: deleteFile
	 * @Description: TODO 删除文件
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	public void deleteFile(FileVO f) {
		try {
			fileDao.deleteFile(f);
			File file_root = new File(Util.getServerPath() + GlobalConstants.FILE_DIR_ROOT);  
	        //判断目录是否存在  
	        if(file_root.exists()) {
            	File afterfile = new File(file_root + File.separator + f.getName());
            	//先删除文件（如果已经存在）
            	if(afterfile.exists()){
            		afterfile.delete();
            	}
	        }
		} catch (Exception e) {
			throw new RuntimeException("运行时出错！");// 为了使事务回滚
		}
	}
	 
	/**
	 * @param paramsMap 
	 * 
	 * @Title: checkName
	 * @Description: TODO 检验文件名是否存在
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	public int checkName(FileVO file) {
		return fileDao.checkName(file);
	}
	/**
	 * @param paramsMap 
	 * 
	 * @Title: getAppInfoByAppPackage
	 * @Description: TODO 
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	public List<FileVO> getAppInfoByAppPackage(Map<String, Object> paramsMap) {
		return fileDao.getAppInfoByAppPackage(paramsMap);
	}
	/**
	 * @param paramsMap 
	 * 
	 * @Title: updateForceByPackage
	 * @Description: TODO 
	 * @param @return  参数说明  
	 * @return int    返回类型 
	 * @throws
	 */
	public int updateForceByPackage(Map<String, Object> paramsMap) {
		return fileDao.updateForceByPackage(paramsMap);
	}
	/** <pre>fileQRCode(这里用一句话描述这个方法的作用)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月16日 下午2:18:54    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月16日 下午2:18:54    
	 * 修改备注： 
	 * @param file
	 * @return</pre>    
	 */
	public List<FileVO> getAppInfoByAppQRCode() {
		return fileDao.getAppInfoByAppQRCode();
	}
	/** <pre>checkSpecialVersion(校验当前版本是否为特殊版本)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月16日 下午2:18:54    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月16日 下午2:18:54    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	public Integer checkSpecialVersion(Map<String, Object> paramsMap) {
		return fileDao.checkSpecialVersion(paramsMap);
	}

	/** <pre>getUserPath(获取用户行政区域path值)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月16日 下午2:18:54    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月16日 下午2:18:54    
	 * 修改备注： 
	 * @param loginName
	 * @return</pre>    
	 */
	public String getUserPath(String loginName) {
		return fileDao.getUserPath(loginName);
	}
	/** <pre>getFileDetail(这里用一句话描述这个方法的作用)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月25日 上午10:45:30    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月25日 上午10:45:30    
	 * 修改备注： 
	 * @param name
	 * @return</pre>    
	 */
	public FileVO getFileDetail(Map<String, Object> paramsMap) {
		return fileDao.getFileDetail(paramsMap);
	}
	/** <pre>qrUpdate(获取会控App当前可使用版本)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月25日 上午10:45:30    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月25日 上午10:45:30    
	 * 修改备注： 
	 * @return</pre>    
	 */
	public List<FileVO> qrUpdate() {
		return fileDao.qrUpdate();
	}
	/** <pre>addFileGrade(往数据库新增需升级文件信息)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年3月1日 上午10:45:30    
	 * 修改人：周逸芳        
	 * 修改时间：2018年3月1日 上午10:45:30    
	 * 修改备注： 
	 * @return</pre>    
	 */
	public int addFileGrade(Map<String, Object> resultMap) {
		return fileDao.addFileGrade(resultMap);
	}
	/** <pre>selectFile(查询数据库文件类型)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年3月1日 上午10:45:30    
	 * 修改人：周逸芳        
	 * 修改时间：2018年3月1日 上午10:45:30    
	 * 修改备注： 
	 * @return</pre>    
	 */
	public FileGradeVO selectFile() {
		return fileDao.selectFile();
	}

	/** <pre>updateGrade(修改文件状态)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年3月1日 上午10:45:30    
	 * 修改人：周逸芳        
	 * 修改时间：2018年3月1日 上午10:45:30    
	 * 修改备注： 
	 * @return</pre>    
	 */
	public int updateGrade(Map<String, Object> paramMap) {
		return fileDao.updateGrade(paramMap);
	}
	/** <pre>deleteFileGrade(删除数据库文件)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年3月1日 上午10:45:30    
	 * 修改人：周逸芳        
	 * 修改时间：2018年3月1日 上午10:45:30    
	 * 修改备注： 
	 * @return</pre>    
	 */
	public int deleteFileGrade() {
		return fileDao.deleteFileGrade();
	}
}
