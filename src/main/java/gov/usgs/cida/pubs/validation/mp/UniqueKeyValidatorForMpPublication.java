package gov.usgs.cida.pubs.validation.mp;

import gov.usgs.cida.pubs.domain.Publication;
import gov.usgs.cida.pubs.utility.PubsUtilities;
import gov.usgs.cida.pubs.validation.constraint.UniqueKey;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author drsteini
 *
 */
public class UniqueKeyValidatorForMpPublication implements ConstraintValidator<UniqueKey, Publication<?>> {

    /** {@inheritDoc}
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize(UniqueKey constraintAnnotation) {
     // Nothing for us to do here at this time.
    }

    /** {@inheritDoc}
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid(Publication<?> value, ConstraintValidatorContext context) {
        boolean rtn = true;
        if (null == value.getIndexId()) {
            rtn = true;
        } else {
            Map<String, Object> filters = new HashMap<String,Object>();
            filters.put("indexId", ((Publication<?>) value).getIndexId());
            List<Publication<?>> pubs = Publication.getPublicationDao().getByMap(filters);
            for (Publication<?> pub : pubs) {
                if (null == value.getId() || 0 != pub.getId().compareTo(value.getId())) {
                    rtn = false;
                    Object[] messageArguments = Arrays.asList(new String[]{"indexId " + value.getIndexId(), pub.getId().toString()}).toArray();
                    String errorMsg = PubsUtilities.buildErrorMsg(context.getDefaultConstraintMessageTemplate(), messageArguments); 
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(errorMsg).addPropertyNode("indexId").addConstraintViolation();
                }
            }
        }
        if (null == value.getIpdsId()) {
            rtn = true;
        } else {
            Map<String, Object> filters = new HashMap<String,Object>();
            filters.put("ipdsId", ((Publication<?>) value).getIpdsId());
            List<Publication<?>> pubs = Publication.getPublicationDao().getByMap(filters);
            for (Publication<?> pub : pubs) {
                if (null == value.getId() || 0 != pub.getId().compareTo(value.getId())) {
                    rtn = false;
                    Object[] messageArguments = Arrays.asList(new String[]{"ipdsId " + value.getIpdsId(), pub.getId().toString()}).toArray();
                    String errorMsg = PubsUtilities.buildErrorMsg(context.getDefaultConstraintMessageTemplate(), messageArguments); 
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(errorMsg).addPropertyNode("ipdsId").addConstraintViolation();
                }
            }
        }

        return rtn;
    }

}