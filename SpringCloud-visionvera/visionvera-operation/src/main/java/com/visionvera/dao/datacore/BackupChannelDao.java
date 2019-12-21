package com.visionvera.dao.datacore;

import com.visionvera.bean.datacore.BackupChannel;

import java.util.Map;

import com.visionvera.basecrud.CrudDao;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhanghui
 * @since 2019-07-23
 */
public interface BackupChannelDao extends CrudDao<BackupChannel> {
	
	
	BackupChannel getBackupChannelBySelective(Map<String,Object> params);

}
