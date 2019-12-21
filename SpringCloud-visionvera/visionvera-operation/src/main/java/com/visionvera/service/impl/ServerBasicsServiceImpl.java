package com.visionvera.service.impl;

import com.github.pagehelper.PageHelper;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.datacore.TerminalInfoDao;
import com.visionvera.dao.ywcore.ServerBasicsDao;
import com.visionvera.dao.ywcore.SlweomsDao;
import com.visionvera.enums.OperationSystem;
import com.visionvera.enums.TransferType;
import com.visionvera.service.ServerBasicsService;
import com.visionvera.util.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.*;

/**
 * 服务器基本信息Service
 * @author dql714099655
 *
 */
@Service
@Transactional(value = "transactionManager_ywcore", rollbackFor = Exception.class)
public class ServerBasicsServiceImpl extends BaseReturn implements ServerBasicsService {
	private Logger logger = LoggerFactory.getLogger(ServerBasicsServiceImpl.class);
	
	@Autowired
	private ServerBasicsDao serverBasicsDao;
	
	@Autowired
	private SlweomsDao slweomsDao;

	@Autowired
	private TerminalInfoDao terminalInfoDao;
	
	@Override
	public ServerBasics getServerBasicsByServerUnique(String serverUnique) {
		ServerBasics serverBasics = serverBasicsDao.getServerBasicsByServerUnique(serverUnique);
		return serverBasics;
	}

	@Override
	public ReturnData updateServerBasics(ServerBasics serverBasics) throws Exception{
		//检测服务器名称是否重复
		List<ServerBasics> serverBasicsList = serverBasicsDao.checkServerBasicsByNameExcludeSelf(serverBasics.getServerName(),serverBasics.getId());
		if(serverBasicsList != null && serverBasicsList.size() > 0) {
			return super.returnError("已存在相同名称的服务器");
		}
		
		String transferType = serverBasics.getTransferType();
		if(TransferType.IP.getTransferType().equals(transferType)) {
			//检测服务器管理ip是否重复
			serverBasicsList = serverBasicsDao.checkServerBasicsByManageIpExcludeSelf(serverBasics.getServerManageIp(),serverBasics.getId());
			if(serverBasicsList != null && serverBasicsList.size() > 0) {
				return super.returnError("已存在相同服务器管理ip,请修改后重新添加");
			}
		}
		
		ServerBasics oldServerBasics = serverBasicsDao.getServerBasicsById(serverBasics.getId());
		String serverDistrict = serverBasics.getServerDistrict();
		String[] regionCodeArr = serverDistrict.split(",");
		serverBasics.setServerProvince(regionCodeArr[0]);
		serverBasics.setGradeid(regionCodeArr.length == 1? 1:2);
		serverBasics.setServerUnique(oldServerBasics.getServerUnique());
		
		serverBasicsDao.updateServerBasic(serverBasics);
		/*if(TransferType.IP.getTransferType().equals(transferType)) {
			String oldOperationIp = oldServerBasics.getOperationIp();
			String operationIp = serverBasics.getOperationIp();
			logger.info("operation服务IP地址是否改变"+operationIp.equals(oldOperationIp));
			if(!operationIp.equals(oldOperationIp) && !ProbeDisplayUtil.PROBE_STATE_NOT_DIPLAY.equals(oldServerBasics.getState())) {
				boolean retFlag = IPProbeReqUtil.synchrOperationIp(serverBasics);
				if(!retFlag) {
					logger.error("同步微服务operation服务IP到探针失败"); 
					throw new Exception("同步微服务operation服务IP到探针失败");
				}
			}

		}else if(TransferType.V2V.getTransferType().equals(transferType) && !ProbeDisplayUtil.PROBE_STATE_NOT_DIPLAY.equals(oldServerBasics.getState())) {
			boolean retFlag = false;
			if (StringUtil.isNotNull(userId) ) {
				retFlag = ProbeManagerMsgUtil.modifyConfig(serverBasics, userId);
				if(!retFlag) {
					logger.error("同步监测探针管理服务信息失败");
					throw new Exception("同步监测探针管理服务信息失败");
				}
			}*//*else{
				List<ServerBasics> list=new ArrayList<>();
				list.add(serverBasics);
				retFlag = ProbeManagerMsgUtil.sendConfig(list);
			}*//*

		}*/
		return super.returnSuccess("修改服务器信息成功");
	}
	
	@Override
	public List<ServerBasics> getAllServerBasics() {
		List<ServerBasics> serverBasicsList = serverBasicsDao.getAllServerBasics();
		return serverBasicsList;
	}

	@Override
	public ReturnData insertServerBasics(ServerBasics serverBasics) throws Exception {
		//检查服务器名称是否重复
		List<ServerBasics> serverBasicsList = serverBasicsDao.checkServerBasicsByName(serverBasics.getServerName());
		if(serverBasicsList != null && serverBasicsList.size() > 0) {
			return super.returnError("已存在相同名称的服务器");
		}
		
		String transferType = serverBasics.getTransferType();
		if(TransferType.IP.getTransferType().equals(transferType)) {
			//IP协议下，校验IP是否重复
			serverBasicsList = serverBasicsDao.checkServerBasicsByManageIp(serverBasics.getServerManageIp());
			if(serverBasicsList != null && serverBasicsList.size() > 0) {
				return super.returnError("已存在相同服务器管理ip,请修改后重新添加");
			}
		}

		/*if(TransferType.V2V.getTransferType().equals(transferType) && GlobalConstants.REUSE_XZ_TERNO_NO == serverBasics.getReuseXzNo()) {
			//V2V探针下，校验终端号是否重复，验证终端和mac是否有效
			Integer integer = terminalInfoDao.selectTerminalInfo(serverBasics.getV2vNetMac().replace(":",""), serverBasics.getTerminalCode());
			if(integer > 0) {
				//验证重复
				int count = serverBasicsDao.getServerCountByTerminalCode(serverBasics.getTerminalCode());
				if(count > 0) {
					return super.returnError("已存在相同的虚拟终端号码");
				}
			}else {
				return super.returnError("填写的虚拟终端号码和视联网虚拟mac无效");
			}

		}*/
		if(TransferType.V2V.getTransferType().equals(transferType)) {
			//验证终端号是否重复
			int count = serverBasicsDao.getServerCountByTerminalCode(serverBasics.getTerminalCode());
			if(count > 0) {
				return super.returnError("已存在相同的虚拟终端号码");
			} else if(GlobalConstants.REUSE_XZ_TERNO_NO == serverBasics.getReuseXzNo()) {
				Integer integer = terminalInfoDao.selectTerminalInfo(serverBasics.getV2vNetMac().replace(":",""), serverBasics.getTerminalCode());
				if(integer == 0) {
					return super.returnError("填写的虚拟终端号码和视联网虚拟mac无效");
				}
			}
		}
		
		//生成服务器唯一标识
		String uuid = UUID.randomUUID().toString();
		String serverUnique = "servers_"+uuid;
		serverBasics.setServerUnique(serverUnique);
		serverBasics.setState(ProbeDisplayUtil.PROBE_STATE_NOT_DIPLAY);
		String serverDistrict = serverBasics.getServerDistrict();
		String[] regionCodeArr = serverDistrict.split(",");
		serverBasics.setServerProvince(regionCodeArr[0]);
		serverBasics.setGradeid(regionCodeArr.length == 1 ? 1:2);
		serverBasicsDao.insertServerBasics(serverBasics);
		List<ServerBasics> serverList = new ArrayList<ServerBasics>();
		serverList.add(serverBasics);
		boolean retFlag = ProbeManagerMsgUtil.sendConfig(serverList);
		if(!retFlag) {
			throw new Exception("同步服务器到监测探针管理失败");
		}
		return super.returnSuccess("服务器信息添加成功");
	}

	@Override
	public ServerBasics getServerBasicsById(Integer id) {
		ServerBasics serverBasics = serverBasicsDao.getServerBasicsById(id);
		return serverBasics;
	}

	
	@Override
	public List<ServerBasics> getServerBasicByPage(Map<String, Object> paramMap) {
		Integer pageNum = (Integer)paramMap.get("pageNum");
		Integer pageSize = (Integer)paramMap.get("pageSize");
		List<ServerBasics> serverBasicsList = new ArrayList<ServerBasics>();
		if(pageNum != -1) {
			PageHelper.startPage(pageNum, pageSize);
			serverBasicsList = serverBasicsDao.getServerBasicsList(paramMap);
			for (ServerBasics serverBasics : serverBasicsList) {
				String serverDistrict = serverBasics.getServerDistrict();
				String[] districtCodeArr = serverDistrict.split(",");
				StringBuilder districtBuilder = new StringBuilder();
				String districtName = "";
				for (String districtCode : districtCodeArr) {
					RegionVO regionVO = slweomsDao.getRegionVOById(districtCode);
					if(regionVO!=null){
						districtBuilder.append(regionVO.getName()).append("/");
					}
				}
				if(districtBuilder.length() > 0) {
					districtName = districtBuilder.substring(0, districtBuilder.length()-1);
				}
				serverBasics.setServerDistrictName(districtName);
			}
		}
		return serverBasicsList;
	}
	
	@Override
	public int updateServerThreshold(ServerBasics serverBasics) {
		serverBasics.setState(ProbeDisplayUtil.PROBE_STATE_CONFIGURED);
		int num = serverBasicsDao.updateServerThreshold(serverBasics);
		return num;
	}

	@Override
	public ServerBasics getServerBasicsByTposRegisterid(String tposRegisterid) {
		ServerBasics serverBasics = serverBasicsDao.getServerBasicsByRegisterid(tposRegisterid);
		return serverBasics;
	}

	@Override
	public ServerBasics getServerThreshold(String serverUnique) {
		ServerBasics serverBasics = serverBasicsDao.getServerThreshold(serverUnique);
		return serverBasics;
	}

	@Override
	public ReturnData addServerBasicsOfExcel(Workbook workbook) throws Exception{
		ReturnData returnData = new ReturnData();
		Sheet sheet = workbook.getSheetAt(0);
		List<ServerBasics> serverBasicsList = new ArrayList<ServerBasics>();
		
		String transferType = "";
		
		List<String> serverNameList = new ArrayList<String>();
		List<String> managerIpList = new ArrayList<String>();
		List<String> terminalCodeList = new ArrayList<>();
		int rowNum = ExcelUtil.getValidRowNum(sheet);
		for (int i = sheet.getFirstRowNum()+1; i <= rowNum-1; i++) {
			Row row = sheet.getRow(i);
			
			Cell cell  = row.getCell(0);
			if(checkCellNull(cell)) {
				return super.returnError("导入失败，第"+(i+1)+"行,服务器名称不能为空");
			}			
			String serverName = cell.getStringCellValue().trim();
			//校验服务器名称是否存在
			List<ServerBasics> servers = serverBasicsDao.checkServerBasicsByName(serverName);
			if(servers != null && servers.size() > 0) {
				return super.returnError("导入失败，第"+(i+1)+"行,服务器名称已经存在");
			}
			serverNameList.add(serverName);
			
			cell = row.getCell(1);
			if(checkCellNull(cell)) {
				return super.returnError("导入失败，第"+(i+1)+"行,主机名不能为空");
			}
			String serverHostname = cell.getStringCellValue().trim();
			String serverDistrictName = "";
			cell = row.getCell(2);
			if(checkCellNull(cell)) {
				return super.returnError("导入失败，第"+(i+1)+"行,服务器省份不能为空");
			}			
			serverDistrictName = cell.getStringCellValue().trim()+",";
			
			cell = row.getCell(3);
			if(!checkCellNull(cell)) {
				String cityName = cell.getStringCellValue().trim();
				if("北京市辖区".equals(cityName) || "天津市辖区".equals(cityName) || "上海市辖区".equals(cityName) || "重庆市辖区".equals(cityName)) {
					cityName = "市辖区";
				}
				serverDistrictName += cityName +",";
				cell = row.getCell(4);
				if(!checkCellNull(cell)) {
					serverDistrictName += cell.getStringCellValue().trim() +",";
				}
			}
			serverDistrictName = serverDistrictName.substring(0, serverDistrictName.length()-1);
			
			cell = row.getCell(5);
			String serverSite = checkCellNull(cell)? "":cell.getStringCellValue();
			
			cell = row.getCell(6);
			if(checkCellNull(cell)) {
				return super.returnError("导入失败，第"+(i+1)+"行,负责人不能为空");
			}
			String serverPrincipal = cell.getStringCellValue().trim();
			
			cell = row.getCell(7);
			if(checkCellNull(cell)) {
				return super.returnError("导入失败，第"+(i+1)+"行,联系电话不能为空");
			}			
			String serverPhone = cell.getStringCellValue().trim();
			if(!CheckFormUtil.checkMobileNumber(serverPhone)){
				return super.returnError("导入失败，第"+(i+1)+"行,联系电话格式有误");
			}
			
			cell = row.getCell(8);
			if(checkCellNull(cell)) {
				return super.returnError("导入失败，第"+(i+1)+"行,联系邮箱不能为空");
			}
			String serverEmail = cell.getStringCellValue().trim();
			if(!CheckFormUtil.checkEmail(serverEmail)){
				return super.returnError("导入失败，第"+(i+1)+"行,联系邮箱格式有误");
			}
			
			cell = row.getCell(9);
			if(checkCellNull(cell)) {
				return super.returnError("导入失败，第"+(i+1)+"行,操作系统不能为空");
			}			
			String serverOs = cell.getStringCellValue().trim();
			if(!serverOs.equals(OperationSystem.Linux.getDesc()) && !serverOs.equals(OperationSystem.Windows.getDesc())) {
				return super.returnError("导入失败，第"+(i+1)+"行，操作系统填写有误");
			}
			cell = row.getCell(10);
			String serverTheirRoom = checkCellNull(cell)? "":cell.getStringCellValue();
			cell = row.getCell(11);
			String serverManufacturer = checkCellNull(cell)? "":cell.getStringCellValue();
			
			if(StringUtil.isNull(transferType)) {
				cell = row.getCell(12);
				if(checkCellNull(cell)) {
					return super.returnError("导入失败，第"+ (i+1) +"行，传输协议不能为空");
				}
				transferType = cell.getStringCellValue().trim();
			}
			
			
			String serverManageIp = "";
			Integer port = ProbeManagerMsgUtil.probeManagePort;
			String serverElseIp = "";
			String user = "";
			String password = "";
			String operationIp = "";

			Integer reuseXzNo = 0;
			String terminalCode = "";
			String netMac = "";
			String v2vNetMac = "";
			
			
			if(TransferType.IP.getTransferType().equals(transferType)) {
				//获取IP协议需要的字段
				cell = row.getCell(13);
				if(checkCellNull(cell)) {
					return super.returnError("导入失败，第"+(i+1)+"行，管理IP不能为空");
				}
				serverManageIp = cell.getStringCellValue().trim();
				if(!CheckFormUtil.checkIp(serverManageIp)){
					return super.returnError("导入失败，第"+(i+1)+"行,管理IP格式有误");
				}
				servers = serverBasicsDao.checkServerBasicsByManageIp(serverManageIp);
				if(servers != null && servers.size() > 0) {
					return super.returnError("导入失败，第"+(i+1)+"行,管理IP已经存在");
				}
				managerIpList.add(serverManageIp);
				
				/*cell = row.getCell(14);
				if(checkCellNull(cell)) {
					return super.returnError("导入失败，第"+(i+1)+"行,监测探针端口不能为空");
				}
				try {
					port  = Integer.valueOf(cell.getStringCellValue().trim());
				} catch(Exception e) {
					return super.returnError("导入失败，第"+(i+1)+"行,监测探针端口请填写数字");
				}*/
				
				cell = row.getCell(14);
				serverElseIp = checkCellNull(cell)? "":cell.getStringCellValue();
				if(StringUtil.isNotNull(serverElseIp)&&!CheckFormUtil.checkIp(serverElseIp)){
					return super.returnError("导入失败，第"+(i+1)+"行,其他IP格式有误");
				}
				
				cell = row.getCell(15);
				if(checkCellNull(cell) && OperationSystem.Linux.getDesc().equals(serverOs)) {
					return super.returnError("导入失败，第"+(i+1)+"行,SSH用户不能为空");
				}			
				user = checkCellNull(cell)? "":cell.getStringCellValue().trim();
				
				cell = row.getCell(16);
				if(checkCellNull(cell) && OperationSystem.Linux.getDesc().equals(serverOs)) {
					return super.returnError("导入失败，第"+(i+1)+"行,SSH密码不能为空");
				}			
				password = checkCellNull(cell)? "":cell.getStringCellValue().trim();
				
				operationIp = ProbeManagerMsgUtil.probeManageIp;//探针管理IP从配置文件获取
				
			}else if(TransferType.V2V.getTransferType().equals(transferType)){
                cell = row.getCell(14);
                if(checkCellNull(cell)) {
                    return super.returnError("导入失败，第"+(i+1)+"行，探针终端虚拟号码不能为空");
                }
                terminalCode = cell.getStringCellValue().trim();
                terminalCodeList.add(terminalCode);

                Integer integer = serverBasicsDao.getServerCountByTerminalCode(terminalCode);
                if(integer > 0) {
                    return super.returnError("导入失败，第"+(i+1)+"行，虚拟终端号码重复");
                }

				cell = row.getCell(13);
				if(checkCellNull(cell)) {
					return super.returnError("导入失败，第"+(i+1)+"行,是否复用协转终端号码不能为空");
				}

				String cellStr = cell.getStringCellValue().trim();
				if("是".equals(cellStr)) {
					reuseXzNo = GlobalConstants.REUSE_XZ_TERNO_YES;
				}else {
					reuseXzNo = GlobalConstants.REUSE_XZ_TERNO_NO;

					cell = row.getCell(15);
					if(checkCellNull(cell)) {
						return super.returnError("导入失败，第"+(i+1)+"行，视联网通信网卡Mac不能为空");
					}
					netMac = cell.getStringCellValue().trim();

					cell = row.getCell(16);
					if(checkCellNull(cell)) {
						return super.returnError("导入失败，第"+(i+1)+"行，探针终端虚拟Mac不能为空");
					}
					v2vNetMac = cell.getStringCellValue().trim();

					integer = terminalInfoDao.selectTerminalInfo(v2vNetMac.replace(":",""),terminalCode);
					if(integer == 0) {
						return super.returnError("导入失败，第"+(i+1)+"行，虚拟终端号码和视联网虚拟mac无效");
					}

				}

			}else {
				return super.returnError("导入失败，第"+(i+1)+"行，传输协议填写有误");
			}
			
			ServerBasics serverBasics = new ServerBasics();
			serverBasics.setServerName(serverName);
			serverBasics.setServerHostname(serverHostname);
			serverBasics.setServerOs(serverOs);
			String[] districtNameArr = serverDistrictName.split(",");
			StringBuilder regionIds = new StringBuilder();
			String serverProvince = "";
			
			String pid = "000000000000";
			for(int index=0; index < districtNameArr.length; index++) {
				String regionName =districtNameArr[index];
				RegionVO regionVO = slweomsDao.getRegionVOByName(regionName,pid);
				if(regionVO == null) {
					returnData.setErrcode(1);
					returnData.setErrmsg("导入失败，第"+(i+1)+"行,行政区域"+serverDistrictName.replace(",", "")+"不存在，请修改后重试");
					return returnData;
				}
				String regionId = regionVO.getId();
				regionIds.append(regionId).append(",");
				if(index == 0) {
					serverProvince = regionId;
				}
				
				pid = regionId;
			}
			if(regionIds.length() > 0) {
				serverBasics.setServerDistrict(regionIds.substring(0, regionIds.length() -1));
			}else {
				serverBasics.setServerDistrict(regionIds.toString());
			}
			
			serverBasics.setServerProvince(serverProvince);
			
			if(districtNameArr.length >=2) {
				serverBasics.setGradeid(2);
			}else if(districtNameArr.length == 1) {
				serverBasics.setGradeid(1);
			}
			serverBasics.setServerSite(serverSite);
			serverBasics.setServerManageIp(serverManageIp);
			serverBasics.setPort(port);
			serverBasics.setServerPrincipal(serverPrincipal);
			serverBasics.setServerPhone(serverPhone);
			serverBasics.setUser(user);
			serverBasics.setPassword(AesUtils.encrypt(password));
			serverBasics.setServerEmail(serverEmail);
			serverBasics.setServerTheirRoom(serverTheirRoom);
			serverBasics.setServerElseIp(serverElseIp);
			serverBasics.setServerManufacturer(serverManufacturer);
			serverBasics.setTransferType(transferType);
			serverBasics.setOperationIp(operationIp);
			serverBasics.setReuseXzNo(reuseXzNo);
			serverBasics.setTerminalCode(terminalCode);
			serverBasics.setNetMac(netMac);
			serverBasics.setV2vNetMac(v2vNetMac);
			
			serverBasics.setState(ProbeDisplayUtil.PROBE_STATE_NOT_DIPLAY);
			
			String uuid = UUID.randomUUID().toString();
			String serverUnique = "servers_"+uuid;
			serverBasics.setServerUnique(serverUnique);
			
			serverBasicsList.add(serverBasics);
		}
		
		boolean duplicateFlag = checkDuplicateElement(serverNameList);
		if(duplicateFlag) {
			return super.returnError("导入失败，上传文件中服务器名称包含重复值，请修改后导入");
		}
		
		if(TransferType.IP.getTransferType().equals(transferType)) {
			duplicateFlag = checkDuplicateElement(managerIpList);
			if(duplicateFlag) {
				return super.returnError("导入失败，上传文件中IP地址包含重复值，请修改后导入");
			}
		}

		if(TransferType.V2V.getTransferType().equals(transferType)) {
		    duplicateFlag = checkDuplicateElement(terminalCodeList);
		    if(duplicateFlag) {
		        return super.returnError("导入失败，上传文件中包含重复的终端号码，请修改后导入");
            }
        }
		if(serverBasicsList.size()==0){
			 return super.returnError("导入失败，上传文件中服务器信息不能为空");
		}
		serverBasicsDao.insertServerBasicsBatch(serverBasicsList);
		if(TransferType.V2V.getTransferType().equals(transferType)) {
			boolean retFlag = ProbeManagerMsgUtil.sendConfig(serverBasicsList);
			if(!retFlag) {
				logger.error("服务器信息同步给监测探针管理服务失败");
				throw new Exception("服务器信息同步给探针管理服务失败");
			}
		}
		return super.returnSuccess("导入添加服务器信息成功");
	}
	
	//判断单元格内容是否为空
	private boolean checkCellNull(Cell cell) {
		if(cell != null) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
			if(StringUtil.isNull(cell.getStringCellValue().trim())) {
				return true;
			}
			return false;
		} 
		return true;
	}

	/**
	 * 判断集合中是否有重复元素
	 * @param elementList
	 * @return true:有重复  false:无重复
	 */
	private boolean checkDuplicateElement(List<String> elementList) {
		Set<String> otherCollection = new HashSet<String>();
		for (String element : elementList) {
			otherCollection.add(element);
		}
		if(elementList.size() - otherCollection.size() == 0) {
			return false;
		}
		return true;
	}

	@Override
	public List<ServerBasics> getServerBasicsListByIds(List<Integer> serverIds) {
		if(serverIds != null && serverIds.size() == 0) {
			return Collections.emptyList();
		}
		List<ServerBasics> serverBasicsList = serverBasicsDao.getServerBasicsByIds(serverIds);
		return serverBasicsList;
	}

	
	@Override
	public void exportServerBasics(Map<String,Object> paramMap,HttpServletResponse response) throws Exception {
		//List<ServerBasics> serverBasicsList = serverBasicsDao.getServerBasicsByIds(serverIds);
		List<ServerBasics> serverBasicsList = serverBasicsDao.getServerBasicsList(paramMap);
		String transferType = (String)paramMap.get("transferType");
		
		String[] titleName = null;
		String[] keys = null;
		if(TransferType.IP.getTransferType().equals(transferType)) {
			titleName = new String[]{"服务器名称","主机名称","所在区域","详细地址","负责人",
					"联系电话","联系邮箱","操作系统","所属机房","设备厂商","传输协议","管理IP","探针服务端口","其他IP","SSH用户","SSH密码",
					"探针管理IP"};
			keys = new String[]{"serverName","serverHostname","serverDistrictName","serverSite","serverPrincipal",
					"serverPhone","serverEmail","serverOs","serverTheirRoom","serverManufacturer","transferType","serverManageIp","port","serverElseIp","user","password",
					"operationIp"};
		}else if(TransferType.V2V.getTransferType().equals(transferType)) {
			titleName = new String[]{"服务器名称","主机名称","所在区域","详细地址","负责人",
					"联系电话","联系邮箱","操作系统","所属机房","设备厂商","传输协议","是否复用协转虚拟号码","探针终端虚拟号码","视联网通信网卡Mac","探针终端虚拟Mac"};
			keys = new String[]{"serverName","serverHostname","serverDistrictName","serverSite","serverPrincipal",
					"serverPhone","serverEmail","serverOs","serverTheirRoom","serverManufacturer","transferType","reuseXzNo","terminalCode","netMac","v2vNetMac"};
		}		
		List<Map<String, Object>> excelDataList = new ArrayList<Map<String,Object>>();
		Map<String,Object> sheetNameMap = new HashMap<String, Object>();
		sheetNameMap.put("sheetName", "sheet");
		excelDataList.add(sheetNameMap);
		for (ServerBasics serverBasics : serverBasicsList) {
			Map<String,Object> dataMap = new HashMap<String, Object>();			
			//dataMap.put("id", serverBasics.getId());
			dataMap.put("serverName", serverBasics.getServerName());
			dataMap.put("serverHostname", serverBasics.getServerHostname());
			String serverDistrict = serverBasics.getServerDistrict();
			String[] districtArr = serverDistrict.split(",");
			StringBuilder districtNameBuilder = new StringBuilder();
			for (String district : districtArr) {
				RegionVO regionVO = slweomsDao.getRegionVOById(district);
				districtNameBuilder = districtNameBuilder.append(regionVO.getName()).append(",");
			}
			String districtName = "";
			if(districtNameBuilder.length() > 0) {
				districtName = districtNameBuilder.substring(0, districtNameBuilder.length()-1);
			}
			dataMap.put("serverDistrictName", districtName);
			dataMap.put("serverSite", serverBasics.getServerSite());
			dataMap.put("serverPrincipal", serverBasics.getServerPrincipal());
			dataMap.put("serverPhone", serverBasics.getServerPhone());
			dataMap.put("serverEmail", serverBasics.getServerEmail());
			dataMap.put("serverOs", serverBasics.getServerOs());
			dataMap.put("serverTheirRoom", serverBasics.getServerTheirRoom());
			dataMap.put("serverManufacturer", serverBasics.getServerManufacturer());
			dataMap.put("transferType", transferType);
			if(TransferType.IP.getTransferType().equals(transferType)) {
				dataMap.put("serverManageIp", serverBasics.getServerManageIp());
				dataMap.put("port", serverBasics.getPort());
				dataMap.put("serverElseIp", serverBasics.getServerElseIp());
				dataMap.put("user", serverBasics.getUser());
				dataMap.put("password", AesUtils.decrypt(serverBasics.getPassword()));
				dataMap.put("operationIp", serverBasics.getOperationIp());
			}else if(TransferType.V2V.getTransferType().equals(transferType)) {
				Integer reuseXzNo = serverBasics.getReuseXzNo();
				if(GlobalConstants.REUSE_XZ_TERNO_YES == reuseXzNo) {
					dataMap.put("reuseXzNo","是");
				}else {
					dataMap.put("reuseXzNo","否");
					dataMap.put("netMac", serverBasics.getNetMac());
					dataMap.put("v2vNetMac", serverBasics.getV2vNetMac());
				}
				dataMap.put("terminalCode", serverBasics.getTerminalCode());
			}
			
			excelDataList.add(dataMap);
		}
		
		Workbook workBook = ExcelUtil.createWorkBook(excelDataList, keys, titleName);
		// 告诉浏览器用什么软件可以打开此文件
        response.setHeader("content-Type", "application/vnd.ms-excel;charset=UTF-8");
        // 下载文件的默认名称
        response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode("服务器信息列表.xls", "utf-8"));
        try {
        	workBook.write(response.getOutputStream());
        }finally {
        	workBook.close();
        }
	}
	
	@Override
	public void updateServerOnLine(ServerBasics serverBasics) {
		serverBasicsDao.updateServerOnLineState(serverBasics);
	}
	
	@Override
	public ServerBasics getServerBasicsByProcessId(Integer processId) {
		ServerBasics serverBasics = serverBasicsDao.getServerBasicsByProcessId(processId);
		return serverBasics;
	}

	@Override
	public void updateServerProbeVersion(ServerBasics serverBasics) {
		serverBasicsDao.updateServerProbeVersion(serverBasics);
	}

	@Override
	public int getServerCountByTerminalCodeExcludeSelf(String terminalCode, Integer id) {
		return serverBasicsDao.getServerCountByTerminalCodeExcludeSelf(terminalCode,id);
	}

	@Override
	public List<Map<String, Object>> getConfigCheckList(String time) {
		
		return serverBasicsDao.getConfigCheckList(time);
	}

}
