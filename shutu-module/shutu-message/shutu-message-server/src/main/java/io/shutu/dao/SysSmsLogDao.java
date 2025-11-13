package io.shutu.dao;
import com.shutu.commons.mybatis.dao.BaseDao;
import io.shutu.entity.SysSmsLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短信日志
 *
 * @author jiujingz@126.com
 */
@Mapper
public interface SysSmsLogDao extends BaseDao<SysSmsLogEntity> {
	
}