package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.util.List;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.time.NormalizedTimeValue;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.util.StoredJSONSerializable;

/**
 * 
 * Parses out normalized TimeML times into ranges of dates representing
 * grounded time intervals. 
 * 
 * Dates based on weeks in time expressions are currently only roughly estimated, 
 * although it's possible
 * to represent them precisely.  Possibly fix this in the future.
 * 
 * @author Bill McDowell
 * 
 */
public class LinkableNormalizedTimeValue extends NormalizedTimeValue implements Argumentable {
	public LinkableNormalizedTimeValue(DataTools dataTools) {
		super(dataTools);
	}
	
	public LinkableNormalizedTimeValue(DataTools dataTools, StoreReference reference) {
		super(dataTools, reference);
	}
	
	public LinkableNormalizedTimeValue(DataTools dataTools, StoreReference reference, String id, String value, List<StoreReference> someExpressionReferences) {
		super(dataTools, reference, id, value, someExpressionReferences);
	}
	
	public LinkableNormalizedTimeValue toDate() {			
  		if (this.value == null)
  			return this; // FIXME?
		if(this.value.matches("^\\d\\d\\d\\d-\\d\\d-\\d\\d.+") ) {
  			return new LinkableNormalizedTimeValue(this.dataTools, this.reference, this.getId(), this.value.substring(0, 10), this.someExpressionReferences);
  		} else {
  			return null;
  		}
	}

	@Override
	public Argumentable.Type getArgumentableType() {
		return Argumentable.Type.TIME;
	}
	
	public LinkableTimeExpression getSomeExpression(int index) {
		return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.someExpressionReferences.get(index), true);
	}
	
	@Override
	public StoredJSONSerializable makeInstance(StoreReference reference) {
		return new LinkableNormalizedTimeValue(this.dataTools, reference);
	}
}
