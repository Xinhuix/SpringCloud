package com.visionvera.bean.cms;

/**
 * @ClassName: DeviceTypeVO
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xiechs
 * @date 2016年9月14日
 * 
 */
public class DeviceTypeVO {

	public DeviceTypeVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	private Integer id;					//设备类型编号。1-启明；2-极光
	private String name; 				//设备类型名称
	private String description;			//备注

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
