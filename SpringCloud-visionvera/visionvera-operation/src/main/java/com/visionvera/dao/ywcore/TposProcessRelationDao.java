package com.visionvera.dao.ywcore;

import java.util.List;

import com.visionvera.bean.slweoms.TposProcessRelation;

/**
 * 平台进程关系Dao
 * @author dql
 *
 */
public interface TposProcessRelationDao {
	
	/**
	 * 添加平台进程的关系
	 * @param tpRelationList
	 */
	void insertTposProcessRelationBatch(List<TposProcessRelation> tpRelationList);
	
	/**
	 * 根据进程id删除与平台的关系
	 * @param id
	 */
	void deleteTposProcessRelationByProcessId(Integer tposProcessId);

}
