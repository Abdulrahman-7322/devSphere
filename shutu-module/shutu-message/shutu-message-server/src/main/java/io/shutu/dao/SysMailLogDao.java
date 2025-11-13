package io.shutu.dao;
import com.shutu.commons.mybatis.dao.BaseDao;
import io.shutu.entity.SysMailLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件发送记录
 * 
 * @author jiujingz@126.com
 */
@Mapper
public interface SysMailLogDao extends BaseDao<SysMailLogEntity> {
	
}
