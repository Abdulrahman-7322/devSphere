package io.shutu.dao;
import com.shutu.commons.mybatis.dao.BaseDao;
import io.shutu.entity.SysSmsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短信
 * 
 * @author jiujingz@126.com
 */
@Mapper
public interface SysSmsDao extends BaseDao<SysSmsEntity> {
	
}
