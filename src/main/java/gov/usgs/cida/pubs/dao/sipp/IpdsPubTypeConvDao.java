package gov.usgs.cida.pubs.dao.sipp;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.usgs.cida.pubs.dao.intfc.IIpdsPubTypeConvDao;
import gov.usgs.cida.pubs.domain.sipp.IpdsPubTypeConv;

@Repository
public class IpdsPubTypeConvDao extends SqlSessionDaoSupport implements IIpdsPubTypeConvDao {

	@Autowired
	public IpdsPubTypeConvDao(SqlSessionFactory sqlSessionFactory) {
		setSqlSessionFactory(sqlSessionFactory);
	}
	
	private static final String NS = "ipdsPubTypeConv";

	@Transactional(readOnly = true)
	@Override
	public IpdsPubTypeConv getByIpdsValue(String ipdsValue) {
		return getSqlSession().selectOne(NS + ".getByIpdsValue", ipdsValue);
	}

}
