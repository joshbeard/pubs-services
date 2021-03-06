package gov.usgs.cida.pubs.busservice;

import gov.usgs.cida.pubs.domain.Affiliation;
import gov.usgs.cida.pubs.domain.Contributor;
import gov.usgs.cida.pubs.domain.OutsideContributor;
import gov.usgs.cida.pubs.domain.PersonContributor;
import gov.usgs.cida.pubs.domain.UsgsContributor;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonContributorBusService extends BusService<PersonContributor<?>> {

	@Autowired
	PersonContributorBusService(final Validator validator) {
		this.validator = validator;
	}

	@Override
	public PersonContributor<?> getObject(Integer objectId) {
		PersonContributor<?> result = null;
		if (null != objectId) {
			result = (PersonContributor<?>) PersonContributor.getDao().getById(objectId);
		}
		return result;
	}

	@Override
	@Transactional
	public PersonContributor<?> updateObject(PersonContributor<?> object) {
		PersonContributor<?> result = object;
		if (null != object && null != object.getId()) {
			Integer id = object.getId();
			if (object instanceof OutsideContributor) {
				Contributor<PersonContributor<OutsideContributor>> oc = (OutsideContributor) object;
				oc.setValidationErrors(validator.validate(oc));
			} else {
				Contributor<PersonContributor<UsgsContributor>> uc = (UsgsContributor) object;
				uc.setValidationErrors(validator.validate(uc));
			}
			if (object.isValid()) {
				updateAffiliations(id, object);
				PersonContributor.getDao().update(object);
				result = (PersonContributor<?>) PersonContributor.getDao().getById(id);
			}
		}
		return result;
	}

	@Override
	@Transactional
	public PersonContributor<?> createObject(PersonContributor<?> object) {
		PersonContributor<?> result = object;
		if (null != result) {
			if (result instanceof OutsideContributor) {
				Contributor<PersonContributor<OutsideContributor>> oc = (OutsideContributor) result;
				Set<ConstraintViolation<Contributor<PersonContributor<OutsideContributor>>>> results = validator.validate(oc);
				oc.setValidationErrors(results);
			} else {
				Contributor<PersonContributor<UsgsContributor>> uc = (UsgsContributor) result;
				Set<ConstraintViolation<Contributor<PersonContributor<UsgsContributor>>>> results = validator.validate(uc);
				uc.setValidationErrors(results);
			}
			if (result.isValid()) {
				Integer id = PersonContributor.getDao().add(result);
				updateAffiliations(id, result);
				result = (PersonContributor<?>) PersonContributor.getDao().getById(id);
			}
		}
		return result;
	}

	private void updateAffiliations(Integer contributorId, PersonContributor<?> object) {
		if (null != object) {
			PersonContributor.getDao().removeAffiliations(contributorId);
			if (null != object.getAffiliations()) {
				for (Affiliation<?> affiliation : object.getAffiliations()) {
					if (null != affiliation) {
						PersonContributor.getDao().addAffiliation(contributorId, affiliation.getId());
					}
				}
			}
		}
	}

}
