package gov.usgs.cida.pubs.validation;

import static org.junit.Assert.assertTrue;
import gov.usgs.cida.pubs.domain.PublicationSeries;
import gov.usgs.cida.pubs.domain.PublicationSubtype;
import gov.usgs.cida.pubs.domain.PublicationType;
import gov.usgs.cida.pubs.domain.mp.MpPublication;

import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PublicationValidationTest extends BaseValidatorTest {

	@Autowired
	public Validator validator;
	protected MpPublication pub;
	protected PublicationType pubType;
	protected PublicationSubtype pubSubtype;
	protected PublicationSeries pubSeries;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		pub = new MpPublication();
		pubType = new PublicationType();
		pubSubtype = new PublicationSubtype();
		pubSeries = new PublicationSeries();
		pub.setPublicationType(pubType);
		pub.setPublicationSubtype(pubSubtype);
		pub.setSeriesTitle(pubSeries);
	}

	@Test
	public void noYearFalsePublicationYearEmptyTest() {
		pub.setValidationErrors(validator.validate(pub));
		assertTrue(pub.getValidationErrors().isEmpty());
	}

	@Test
	public void noYearTruePublicationYearNotEmptyTest() {
		pub.setNoYear(true);
		pub.setPublicationYear("1234");
		pub.setValidationErrors(validator.validate(pub));
		assertTrue(pub.getValidationErrors().isEmpty());
	}
}