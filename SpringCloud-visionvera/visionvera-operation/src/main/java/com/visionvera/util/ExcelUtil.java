package com.visionvera.util;

import com.visionvera.bean.cms.DeviceTypeVO;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.web.controller.rest.DeviceController;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

  
public class ExcelUtil {  
      
    private final static String excel2003L =".xls";    //2003- 版本的excel  
    private final static String excel2007U =".xlsx";   //2007+ 版本的excel  
    /** 
     * 描述：获取IO流中的数据，组装成List<List<Object>>对象 
     * @param in,fileName 
     * @return 
     * @throws IOException  
     */  
    public static List<List<Object>> getListByExcelCommon(InputStream in,String fileName) throws Exception{  
        List<List<Object>> list = null;  
          
        //创建Excel工作薄  
        Workbook work = getWorkbook(in,fileName);  
        if(null == work){  
            throw new Exception("创建Excel工作薄为空！");  
        }  
        Sheet sheet = null;  
        Row row = null;  
        Cell cell = null;  
          
        list = new ArrayList<List<Object>>();  
        //遍历Excel中所有的sheet  
        for (int i = 0; i < work.getNumberOfSheets(); i++) {
            sheet = work.getSheetAt(i);  
            if(sheet==null){
            	continue;
            }  
              
            //遍历当前sheet中的所有行  
            for (int j = sheet.getFirstRowNum(); j < sheet.getLastRowNum(); j++) {  
                row = sheet.getRow(j);  
                if(row==null||row.getFirstCellNum()==j){continue;}  
                  
                //遍历所有的列  
                List<Object> li = new ArrayList<Object>();  
                for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {  
                    cell = row.getCell(y);  
                    li.add(getCellValue(cell));  
                }  
                list.add(li);  
            }  
        }  
        work.close();
        in.close();
        list.remove(0);//去除第一行（第一行为title)
        return list;  
    }  
    
    /** 
     * 描述：获取IO流中的数据，组装成List<List<Object>>对象 
     * @param in,fileName 
     * @return 
     * @throws IOException  
     */  
    public static Map<String, Object> getListByExcel(InputStream in,String fileName) throws Exception{  
    	Map<String, Integer> devTypeMap = getDevTypeMap();
        List<DeviceVO> list = null;  
        List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();//导入错误的设备列表
        Map<String, Object> errMap = null;//导入错误的设备信息
        //创建Excel工作薄  
        Workbook work = getWorkbook(in,fileName);  
        if(null == work){  
            throw new Exception("创建Excel工作薄为空！");  
        }  
        Sheet sheet = null;  
        Row row = null;  
          
        list = new ArrayList<DeviceVO>();  
        //遍历Excel中所有的sheet  
        for (int i = 0; i < work.getNumberOfSheets(); i++) {
            sheet = work.getSheetAt(i);  
            if(sheet==null){
            	continue;
            }
              
            //遍历当前sheet中的所有行  
            Object type = null;
            for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {  
                row = sheet.getRow(j);  
                if(row==null||row.getFirstCellNum()==j){continue;}  
                  
                //遍历所有的列  
                DeviceVO device = new DeviceVO();  
                int start = row.getFirstCellNum();
                try{
                	if(!String.valueOf(getCellValue(row.getCell(start))).matches("^[0-9]*[1-9][0-9]*$") || String.valueOf(getCellValue(row.getCell(start))).length() > 5 ){//设备号码非法
                		errMap = new HashMap<String, Object>();
                		errMap.put("id", getCellValue(row.getCell(start)));
                		errMap.put("name", getCellValue(row.getCell(start+1)));
                		errMap.put("msg", "设备号码非法");
                		errList.add(errMap);
                		continue;
                	}
                	device.setName(getCellValue(row.getCell(start+1))==null ? "":getCellValue(row.getCell(start+1)).toString().trim());
                	if(StringUtils.isEmpty(device.getName())){//设备名称为空的用设备号代替
                		device.setName(String.valueOf(device.getId()));
                	}
                	if(!getCellValue(row.getCell(start+1)).toString().matches("^[\uFF00-\uFFFF\u4e00-\u9fa5A-Za-z0-9_\\-:]{1,600}$")
                			&&!device.getName().matches("^[\uFF00-\uFFFF\u4e00-\u9fa5A-Za-z0-9_\\-:]{1,600}$")){//设备名称非法
                		errMap = new HashMap<String, Object>();
                		errMap.put("id", getCellValue(row.getCell(start)));
                		errMap.put("name", getCellValue(row.getCell(start+1)));
                		errMap.put("msg", "设备名称非法");
                		errList.add(errMap);
                		continue;
                	}
                	if(StringUtils.isEmpty(getCellValue(row.getCell(start+5)).toString())){//设备类型为空
                		errMap = new HashMap<String, Object>();
                		errMap.put("id", getCellValue(row.getCell(start)));
                		errMap.put("name", getCellValue(row.getCell(start+1)));
                		errMap.put("msg", "设备类型为空");
                		errList.add(errMap);
                		continue;
                	}
	                type = getCellValue(row.getCell(start+5));
	                device.setId(getCellValue(row.getCell(start))==null ? "-1":getCellValue(row.getCell(start)).toString().trim());
	                if(type != null){
	                	device.setType(devTypeMap.get(type.toString().trim()).toString());
	                }
	                device.setMac(getCellValue(row.getCell(start+2))==null ? "":getCellValue(row.getCell(start+2)).toString().trim());
	                if(device.getMac().indexOf(".") > 0){
	                	device.setMac(device.getMac().substring(0, device.getMac().indexOf(".")));
	                }
	                device.setIp(getCellValue(row.getCell(start+3))==null ? "":getCellValue(row.getCell(start+3)).toString().trim());
	                device.setDescription(getCellValue(row.getCell(start+4))==null ? "":getCellValue(row.getCell(start+4)).toString().trim());
	                list.add(device);  
	            } catch(Exception e){
	        		continue;
	            }
            }
        }  
        work.close();
        in.close();
        if(list.size() > 0 && list.get(0).getId().equals("号码") && list.get(0).getType().equals("类型")){
        	list.remove(0);//去除第一行（第一行为title)
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("list", list);//通过验证的设备列表
        result.put("errList", errList);//验证失败的设备列表
        return result;  
    }  
      
    /** 
     * 描述：根据文件后缀，自适应上传文件的版本  
     * @param inStr,fileName 
     * @return 
     * @throws Exception 
     */  
    public static  Workbook getWorkbook(InputStream inStr,String fileName) throws Exception{  
        Workbook wb = null;  
        String fileType = fileName.substring(fileName.lastIndexOf("."));  
        if(excel2003L.equals(fileType)){  
            wb = new HSSFWorkbook(inStr);  //2003-  
        }else if(excel2007U.equals(fileType)){  
            wb = new XSSFWorkbook(inStr);  //2007+  
        }else{  
            throw new Exception("解析的文件格式有误！");  
        }  
        return wb;  
    }  
  
    /** 
     * 描述：对表格中数值进行格式化 
     * @param cell 
     * @return 
     */  
    public static Object getCellValue(Cell cell){
    	if(cell == null){
    		return null;
    	}
        Object value = null;  
//        DecimalFormat df = new DecimalFormat("0");  //格式化number String字符  
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");  //日期格式化  
        DecimalFormat df2 = new DecimalFormat("0.00");  //格式化数字  
          
        switch (cell.getCellType()) {  
        case Cell.CELL_TYPE_STRING:  
            value = cell.getRichStringCellValue().getString();  
            break;  
        case Cell.CELL_TYPE_NUMERIC:  
            if("General".equals(cell.getCellStyle().getDataFormatString())){  
            	cell.setCellType(1);
//                value = df.format(cell.getNumericCellValue());  
            	value = cell.getRichStringCellValue().getString();  
            }else if("m/d/yy".equals(cell.getCellStyle().getDataFormatString())){  
                value = sdf.format(cell.getDateCellValue());  
            }else{  
                value = df2.format(cell.getNumericCellValue());  
            } 
            break;  
        case Cell.CELL_TYPE_BOOLEAN:  
            value = cell.getBooleanCellValue();  
            break;  
        case Cell.CELL_TYPE_BLANK:  
            value = "";  
            break;  
        default:  
            break;  
        }  
        return value;  
    }

    /**
     * 创建excel文档，
     * @param list 数据
     * @param keys list中map的key数组集合
     * @param columnNames excel的列名
     * */
    public static Workbook createWorkBook(List<Map<String, Object>> list,String []keys,String columnNames[]) {
//    	String str="#FF7F00";
//    	int[] color=new int[3];
//    	color[0]=Integer.parseInt(str.substring(1, 3), 16);
//    	color[1]=Integer.parseInt(str.substring(3, 5), 16);
//    	color[2]=Integer.parseInt(str.substring(5, 7), 16);
    	// 创建excel工作簿
        Workbook wb = new HSSFWorkbook();
        // 创建第一个sheet（页），并命名
        Sheet sheet = wb.createSheet(list.get(0).get("sheetName").toString());
        // 手动设置列宽。第一个参数表示要为第几列设；，第二个参数表示列的宽度，n为列高的像素数。
        for(int i=0;i<keys.length;i++){
            sheet.setColumnWidth((short) i, (short) (35.7 * 150));
        }

        // 创建第一行
        Row row = sheet.createRow((short) 0);

        // 创建两种单元格格式
        CellStyle cs = wb.createCellStyle();
        CellStyle cs2 = wb.createCellStyle();
        CellStyle cs3 = wb.createCellStyle();//超时 单元格样式
        CellStyle cs4 = wb.createCellStyle();//超时 单元格样式
        CellStyle cs5 = wb.createCellStyle();//超时 单元格样式

        // 创建两种字体
        Font f = wb.createFont();
        Font f2 = wb.createFont();
        Font f3 = wb.createFont();
        Font f4 = wb.createFont();
        Font f5 = wb.createFont();

        // 创建第一种字体样式（用于列名）
        f.setFontHeightInPoints((short) 10);
        f.setColor(IndexedColors.BLACK.getIndex());
        f.setBoldweight(Font.BOLDWEIGHT_BOLD);

        // 创建第二种字体样式（用于值）
        f2.setFontHeightInPoints((short) 10);
        f2.setColor(IndexedColors.BLACK.getIndex());
        
        f3.setFontHeightInPoints((short) 10);
        f3.setColor(IndexedColors.RED.getIndex());
//      Font f3=wb.createFont();
//      f3.setFontHeightInPoints((short) 10);
//      f3.setColor(IndexedColors.RED.getIndex());
        
        f4.setFontHeightInPoints((short) 10);
        f4.setColor(IndexedColors.GREEN.getIndex());
        
        f5.setFontHeightInPoints((short) 10);
        f5.setColor(IndexedColors.GOLD.getIndex());
        // 设置第一种单元格的样式（用于列名）
        cs.setFont(f);
        cs.setBorderLeft(CellStyle.BORDER_THIN);
        cs.setBorderRight(CellStyle.BORDER_THIN);
        cs.setBorderTop(CellStyle.BORDER_THIN);
        cs.setBorderBottom(CellStyle.BORDER_THIN);
        cs.setAlignment(CellStyle.ALIGN_CENTER);

        // 设置第二种单元格的样式（用于值）
        cs2.setFont(f2);
        cs2.setBorderLeft(CellStyle.BORDER_THIN);
        cs2.setBorderRight(CellStyle.BORDER_THIN);
        cs2.setBorderTop(CellStyle.BORDER_THIN);
        cs2.setBorderBottom(CellStyle.BORDER_THIN);
        cs2.setAlignment(CellStyle.ALIGN_CENTER);
        
        //设置超时单元格样式
        cs3.setFont(f3);
        cs3.setFillForegroundColor(HSSFColor.RED.index);//setFillBackgroundColor(HSSFColor.RED.index);
        cs3.setBorderLeft(CellStyle.BORDER_THIN);
        cs3.setBorderRight(CellStyle.BORDER_THIN);
        cs3.setBorderTop(CellStyle.BORDER_THIN);
        cs3.setBorderBottom(CellStyle.BORDER_THIN);
        cs3.setAlignment(CellStyle.ALIGN_CENTER);//
        
        //设置超时单元格样式
        cs4.setFont(f4);
        cs4.setFillForegroundColor(HSSFColor.RED.index);//setFillBackgroundColor(HSSFColor.RED.index);
        cs4.setBorderLeft(CellStyle.BORDER_THIN);
        cs4.setBorderRight(CellStyle.BORDER_THIN);
        cs4.setBorderTop(CellStyle.BORDER_THIN);
        cs4.setBorderBottom(CellStyle.BORDER_THIN);
        cs4.setAlignment(CellStyle.ALIGN_CENTER);//
        
        cs5.setFont(f5);
        cs5.setFillForegroundColor(HSSFColor.RED.index);//setFillBackgroundColor(HSSFColor.RED.index);
        cs5.setBorderLeft(CellStyle.BORDER_THIN);
        cs5.setBorderRight(CellStyle.BORDER_THIN);
        cs5.setBorderTop(CellStyle.BORDER_THIN);
        cs5.setBorderBottom(CellStyle.BORDER_THIN);
        cs5.setAlignment(CellStyle.ALIGN_CENTER);//

        //设置列名
        for(int i=0;i<columnNames.length;i++){
            Cell cell = row.createCell(i);
            cell.setCellValue(columnNames[i]);
            cell.setCellStyle(cs);
        }
        //设置每行每列的值
        for (short i = 1; i < list.size(); i++) {
            // Row 行,Cell 方格 , Row 和 Cell 都是从0开始计数的
            // 创建一行，在页sheet上
            Row row1 = sheet.createRow((short) i);
            // 在row行上创建一个方格
            for(short j=0;j<keys.length;j++){
                Cell cell = row1.createCell(j);
                cell.setCellValue(list.get(i).get(keys[j]) == null?" ": list.get(i).get(keys[j]).toString());
                if (j<keys.length && list.get(i).get(keys[j])!= null && list.get(i).get(keys[j])!= "") {
                	if(j<keys.length && (list.get(i).get(keys[j])).toString().contains("大于") 
                     		&& !(list.get(i).get(keys[j])).toString().contains("小于")){
                     	cell.setCellStyle(cs3);
                     	row1.getCell(j-1).setCellStyle(cs3);
                    }else if (j<keys.length && (list.get(i).get(keys[j])).toString().contains("大于")
                     		&& (list.get(i).get(keys[j])).toString().contains("小于")) {
                     	cell.setCellStyle(cs5);
                     	row1.getCell(j-1).setCellStyle(cs5);
     				}else if (j<keys.length && !(list.get(i).get(keys[j])).toString().contains("大于")
                     		&& (list.get(i).get(keys[j])).toString().contains("小于")) {
     					cell.setCellStyle(cs4);
     					row1.getCell(j-1).setCellStyle(cs4);
					}else{//未审批，无值
                     	cell.setCellStyle(cs2);
                    }
				}else{
					cell.setCellStyle(cs2);
				}
                
            }
        }
        return wb;
    }

	
	private static Map<String, Integer> getDevTypeMap(){
		Map<String, Integer> devTypeMap = new HashMap<String, Integer>();
		DeviceController dc = (DeviceController) SpringContextUtil.getBean("deviceController"); 
		List<DeviceTypeVO> devTypeList = dc.getDeviceTypeList();
		if(devTypeList != null){
			for(DeviceTypeVO devType: devTypeList){
				devTypeMap.put(devType.getName(), devType.getId());
			}
		}
		return devTypeMap;
	}
	@SuppressWarnings("unchecked")
	public static Workbook createMasterDetailWorkBook(ArrayList<Object> paramList) {
		Workbook wb = new HSSFWorkbook();
		HashMap<String, String> headerFooter = (HashMap<String, String>)paramList.get(0);
		List<Map<String, String>> listin = (ArrayList<Map<String, String>>)paramList.get(2);
		//创建多个sheet
		//分页算法计算需要多少个sheet
    	int rowsCount=listin.size();  
 		int pageCount=65000;  
 		int sheetCount=(rowsCount-1)/pageCount+1;
        List<Sheet> sheets= new ArrayList<Sheet>();
        for(int i=1;i<=sheetCount;i++){
            sheets.add(wb.createSheet(headerFooter.get("footer")+(i)));
        }
        if(sheets.size()==0){
        	return wb;
        }
        CellStyle  csb = wb.createCellStyle();
        Font fb = wb.createFont();
        fb.setFontHeightInPoints((short)15);
        fb.setBoldweight(Font.BOLDWEIGHT_BOLD);
        csb.setFont(fb);
        csb.setAlignment(CellStyle.ALIGN_CENTER);
       
        Sheet sheet = sheets.get(0);
    	Row row = sheet.createRow((short) 0);
        CellRangeAddress cra =new CellRangeAddress(0, 0, 0, 3);
        sheet.addMergedRegion(cra); 
        
        Cell cell1 = row.createCell(0);
        cell1.setCellValue(headerFooter.get("header"));
        cell1.setCellStyle(csb);
		for(int z=0;z<4;z++){
	        for (int j = 0; j < sheets.size(); j++) {
	        	sheets.get(j).setColumnWidth((short) z, (short) (35.7 * 200));
	   		}
 	    }
       
        String[] keysMap =(String[])paramList.get(3);
		List<Map<String, String>> listmapcell = (ArrayList<Map<String, String>>)paramList.get(1);
		
		CellStyle  cs = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBoldweight(Font.BOLDWEIGHT_BOLD);
        cs.setFont(f);
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        
        CellStyle  csv = wb.createCellStyle();
        Font fv = wb.createFont();
        csv.setFont(fv);
        csv.setAlignment(CellStyle.ALIGN_CENTER);
        
        CellStyle  cs1 = wb.createCellStyle();
        Font f1 = wb.createFont();
        f1 = wb.createFont();
        f1.setBoldweight(Font.BOLDWEIGHT_BOLD);
        f1.setColor(IndexedColors.RED.getIndex());
        cs1.setFont(f1);
        cs1.setAlignment(CellStyle.ALIGN_CENTER);
        cs1.setFont(f1);
        cs1.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        cs1.setFillPattern(CellStyle.SOLID_FOREGROUND);
        
        CellStyle  cs2 = wb.createCellStyle();
        Font f2 = wb.createFont();
        f2.setColor(IndexedColors.RED.getIndex());
        cs2.setFont(f2);
        cs2.setAlignment(CellStyle.ALIGN_CENTER);
        cs2.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        cs2.setFillPattern(CellStyle.SOLID_FOREGROUND);
        
        Row rows = null;
        Cell cell =null;
        for (int x= 1; x<= sheetCount; x++) {
        	 // 计算出每个sheet起始行
        	 int start = (x-1)*pageCount;
			 int end = x*pageCount;
			 if(x==sheetCount){
				 int maxCount =sheetCount*pageCount;
				 int cha=pageCount-(maxCount-rowsCount); 
				 end=start+cha;
			 }
			 if(x==1){
				 for(int i=0;i<listmapcell.size();i++){
					if (sheet != null) {
						rows = sheet.createRow((short) i+1);
			        	for(int j=0;j<listmapcell.size()-1;j++){
			        	   cell = rows.createCell(j);
				           cell.setCellValue(listmapcell.get(i).get(keysMap[j]));
				           if(j%2==0){
				        	   cell.setCellStyle(cs);  
				           }else{
				        	   cell.setCellStyle(csv); 
				           }
				           if(i==4){
					           cell.setCellStyle(cs1);
				            }
				            if(i==listmapcell.size()-1&&j==3&&rowsCount>0){
				              for(int z=0;z<end;z++){
				            	rows = sheet.createRow((short) i+2+z);
				            	for(int m=0;m<keysMap.length;m++){
				            		cell = rows.createCell(m);
						            String value =listin.get(z).get(keysMap[m]);
				            		if(m==0){
				            			value="";
						            }
				            		cell.setCellValue(value);
						            cell.setCellStyle(cs2);
				            	}
							  }
				           }
			        	}
					}
		        }
				}else{
				 int index=0;
				 for(@SuppressWarnings("unused")
				 int j2 = start; start < end; start++){
					rows = sheets.get(x-1).createRow((short) index);
			       	for(int j=1;j<keysMap.length;j++){
			       	   cell = rows.createCell(j-1);
			           cell.setCellValue(listin.get(start).get(keysMap[j]));
			           cell.setCellStyle(cs2);
			       	} 
		       	    index++;
			     }
			}
        }
     return wb;
	}
	@SuppressWarnings("unchecked")
	public static Workbook createMasterWorkBook(List<Object> paramList) {
		HashMap<String, String> headerFooter = (HashMap<String, String>) paramList.get(0);
		List<Map<String, String>> listmapcell = (ArrayList<Map<String, String>>)paramList.get(1);
		String[] keysMap =(String[])paramList.get(2);
		Workbook wb = new HSSFWorkbook();
        //创建多个sheet
	    //分页算法计算需要多少个sheet
	    int rowsCount=listmapcell.size();  
	    int pageCount=65000;  
	    int sheetCount=(rowsCount-1)/pageCount+1;
        List<Sheet> sheets= new ArrayList<Sheet>();
        for(int i=0;i<sheetCount;i++){
    		sheets.add(wb.createSheet(headerFooter.get("footer")+(i+1)));
    	}
        if(sheets.size()==0){
        	return wb;
        }
        for(int i=0;i<keysMap.length;i++){
        	for (int j = 0; j < sheets.size(); j++) {
        		if(i==0||i==3||i==6||i==5||i==7){
            		sheets.get(j).setColumnWidth((short) i, (short) (35.7 * 102));
            	}else if(i==1||i==4){
            		sheets.get(j).setColumnWidth((short) i, (short) (35.7 * 180));
            	}else{
            		sheets.get(j).setColumnWidth((short) i, (short) (35.7 * 240));
            	}
        	}
        }
        CellRangeAddress cra =new CellRangeAddress(0, 0, 0, keysMap.length-1);
        sheets.get(0).addMergedRegion(cra);
        
        CellStyle  csb = wb.createCellStyle();
        Font fb = wb.createFont();
        fb.setFontHeightInPoints((short)15);
        fb.setBoldweight(Font.BOLDWEIGHT_BOLD);
        csb.setFont(fb);
        csb.setAlignment(CellStyle.ALIGN_CENTER);
       
        CellStyle  csv = wb.createCellStyle();
        Font fv = wb.createFont();
        fv.setBoldweight(Font.BOLDWEIGHT_BOLD);
        csv.setFont(fv);
        csv.setAlignment(CellStyle.ALIGN_CENTER);
        
        CellStyle  csv2 = wb.createCellStyle();
        csv2.setAlignment(CellStyle.ALIGN_CENTER);
        
        Row row = sheets.get(0).createRow((short) 0);
        Cell cell1 = row.createCell(0);
        cell1.setCellValue(headerFooter.get("header"));
        cell1.setCellStyle(csb);
		
        Row rows = null;
        Cell cell =null;
        for (int i = 1; i <= sheetCount; i++) {
        	 ////按照分页理论 计算出每个sheet起始行
        	 int start = (i-1)*pageCount;
			 int end = i*pageCount;
			 if(i==sheetCount){
				 int maxCount =sheetCount*pageCount;
				 int cha=pageCount-(maxCount-rowsCount); 
				 end=start+cha;
			 }
			 //根据每个sheet的起始行,把数据插入对应的sheet
			 int index=0;
			 for(@SuppressWarnings("unused")
			int j2 = start; start < end; start++){
			 	//由于第一个sheet有标题，所以第一个sheet特殊处理   
	        	if(i==1){
	         	  rows = sheets.get(i-1).createRow((short) index+1);
	         	}else{
	         	  rows = sheets.get(i-1).createRow((short) index);
	         	}
	        	for(int j=0;j<keysMap.length;j++){
	        	   cell = rows.createCell(j);
		           cell.setCellValue(listmapcell.get(start).get(keysMap[j]));
		           if(start==0){
		        	   cell.setCellStyle(csv);
		           }else{
		        	  if(j!=1&&j!=2){
		        		  cell.setCellStyle(csv2);  
		        	  } 
		           }
	        	}
	        	index++;
		     }
		}
        return wb;
	}


	public static int getValidRowNum(Sheet sheet) {
        CellReference cellReference = new CellReference("A4");
        boolean flag = false;
        /*cellReference.getRow()*/
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum();) {
            Row r = sheet.getRow(i);
            if(r == null){
                // 如果是空行（即没有任何数据、格式），直接把它以下的数据往上移动
                sheet.shiftRows(i+1, sheet.getLastRowNum(),-1);
                continue;
            }
            flag = false;
            for(Cell c:r){
                if(c.getCellType() != Cell.CELL_TYPE_BLANK){
                    flag = true;
                    break;
                }
            }
            if(flag){
                i++;
                continue;
            }
            else{//如果是空白行（即可能没有数据，但是有一定格式）
                if(i == sheet.getLastRowNum())//如果到了最后一行，直接将那一行remove掉
                    sheet.removeRow(r);
                else//如果还没到最后一行，则数据往上移一行
                    sheet.shiftRows(i+1, sheet.getLastRowNum(),-1);
            }
        }
        return sheet.getLastRowNum() +1;
    }
}
	
	
	
	
	
	
	
	
	


















