package com.visionvera.enums;

/**
 * Created by dql on 2018/5/9.
 * 操作系统类型枚举
 */
public enum OperationSystem {
    
    Linux("Linux"),    
    Windows("Windows");

	private String desc;
	
    private OperationSystem(String desc){
        this.desc = desc;
    }
    
    public String getDesc() {
    	return desc;
    }

    public String toString(){
        return desc;
    }

}
