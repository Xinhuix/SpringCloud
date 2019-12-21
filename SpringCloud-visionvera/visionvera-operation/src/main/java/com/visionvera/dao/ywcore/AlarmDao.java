package com.visionvera.dao.ywcore;

import com.visionvera.bean.slweoms.AlarmInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报警Dao
 * @author dql714099655
 *
 */
public interface AlarmDao {
	
	/**
	 * 查询服务器的离线报警ID
	 * @param serverUnique
	 * @return
	 */
	List<Integer> getServerOfflineAlarmIds(String serverUnique);
	
	/**
	 * 修改报警为已处理
	 * @param alarmIds
	 */
	void updateAlarmTreatmentStates(List<Integer> alarmIds);
	
	/**
	 * 插入报警
	 * @param alarm
	 */
	void insertAlarm(AlarmInfo alarm);
	
	/**
	 * 批量插入报警
	 * @param alarmInfoList
	 */
	int insertAlarmBatch(@Param("alarmInfoList") List<AlarmInfo> alarmInfoList);	
	
	/**
	 * 查询过期时间的报警
	 * @param expireDay
	 * @param size
	 * @return
	 */
	List<Long> getAlarmInfoEarlist(@Param("expireDay")int expireDay, @Param("size")Integer size);

	/**
	 * 根据报警信息ID列表删除报警信息
	 * @param alarmIdList
	 * @return
	 */
	int deleteAlarmInfoList(@Param("alarmIdList") List<Long> alarmIdList);
}
