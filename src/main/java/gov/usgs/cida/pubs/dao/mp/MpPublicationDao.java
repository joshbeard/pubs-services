package gov.usgs.cida.pubs.dao.mp;

import gov.usgs.cida.pubs.aop.ISetDbContext;
import gov.usgs.cida.pubs.dao.intfc.IMpPublicationDao;
import gov.usgs.cida.pubs.domain.mp.MpPublication;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author drsteini
 *
 */
public class MpPublicationDao extends MpDao<MpPublication> implements IMpPublicationDao {

    private static final String NS = "mpPublication";
    public static final String GET_NEW_ID = ".getNewProdId";
    public static final String LOCK_PUB = ".locPub";
    public static final String RELEASE_LOCKS = ".releaseLocks";
    public static final String RELEASE_LOCKS_USER = RELEASE_LOCKS + "User";
    public static final String RELEASE_LOCKS_PUB = RELEASE_LOCKS + "Pub";

   /**
     * {@inheritDoc}
     * @see gov.usgs.cida.pubs.dao.BaseDao#add(java.lang.Object)
     */
    @Transactional
    @ISetDbContext
    @Override
    public Integer add(MpPublication domainObject) {
        getSqlSession().insert(NS + ADD, domainObject);
        return domainObject.getId();
    }

    /**
     * {@inheritDoc}
     * @see gov.usgs.cida.pubs.dao.BaseDao#getById(java.lang.Integer)
     */
    @Transactional(readOnly = true)
    @ISetDbContext
    @Override
    public MpPublication getById(Integer domainID) {
        return (MpPublication) getSqlSession().selectOne(NS + GET_BY_ID, domainID);
    }

    /**
     * {@inheritDoc}
     * @see gov.usgs.cida.pubs.dao.BaseDao#getByMap(java.util.Map)
     */
    @Transactional(readOnly = true)
    @ISetDbContext
    @Override
    public List<MpPublication> getByMap(Map<String, Object> filters) {
        return getSqlSession().selectList(NS + GET_BY_MAP, filters);
    }
    
	/** {@inheritDoc}
	 * @see gov.usgs.cida.pubs.core.dao.intfc.IDao#getObjectCount(java.util.Map)
	 */
	@Override
	@Transactional(readOnly = true)
	@ISetDbContext
	public Integer getObjectCount(Map<String, Object> filters) {
		return getSqlSession().selectOne(NS + GET_COUNT, filters);
	}

    /** {@inheritDoc}
     * @see gov.usgs.cida.pubs.dao.intfc.IDao#update(java.lang.Object)
     */
    @Transactional
    @ISetDbContext
    @Override
    public void update(MpPublication domainObject) {
        getSqlSession().update(NS + UPDATE, domainObject);
    }

    /** {@inheritDoc}
     * @see gov.usgs.cida.pubs.dao.intfc.IDao#delete(java.lang.Object)
     */
    @Transactional
    @ISetDbContext
    @Override
    public void delete(MpPublication domainObject) {
        deleteById(domainObject.getId());
    }

    /** {@inheritDoc}
     * @see gov.usgs.cida.pubs.dao.intfc.IDao#deleteById(java.lang.Integer)
     */
    @Transactional
    @ISetDbContext
    @Override
    public void deleteById(Integer domainID) {
        getSqlSession().delete(NS + DELETE, domainID);
    }

    /** {@inheritDoc}
     * @see gov.usgs.cida.pubs.dao.intfc.IMpDao#copyFromPw(java.lang.Integer)
     */
    @Transactional
    @ISetDbContext
    @Override
    public void copyFromPw(Integer domainID) {
        getSqlSession().insert(NS + COPY_FROM_PW, domainID);
    }

    /** {@inheritDoc}
     * @see gov.usgs.cida.pubs.dao.intfc.IMpDao#publishToPw(java.lang.Integer)
     */
    @Transactional
    @ISetDbContext
    @Override
    public void publishToPw(Integer domainID) {
        getSqlSession().update(NS + PUBLISH, domainID);
    }

    /**
     * {@inheritDoc}
     * @see gov.usgs.cida.pubs.dao.intfc.IMpPublicationDao#getNewProdId()
     */
    @Transactional
    @ISetDbContext
    @Override
    public Integer getNewProdId() {
        return getSqlSession().selectOne(NS + GET_NEW_ID);
    }

    @Transactional
    @ISetDbContext
	@Override
	public void lockPub(Integer domainId) {
    	getSqlSession().update(NS + LOCK_PUB, domainId);
	}

    @Transactional
    @ISetDbContext
	@Override
	public void releaseLocksUser(String lockUsername) {
    	getSqlSession().update(NS + RELEASE_LOCKS_USER, lockUsername);
	}

    @Transactional
    @ISetDbContext
	@Override
	public void releaseLocksPub(Integer domainID) {
    	getSqlSession().update(NS + RELEASE_LOCKS_PUB, domainID);
	}

}
