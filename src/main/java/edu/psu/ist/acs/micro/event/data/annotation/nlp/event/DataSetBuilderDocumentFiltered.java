package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jetty.util.ConcurrentHashSet;

import edu.cmu.ml.rtw.generic.data.annotation.Datum;
import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.annotation.DocumentSetInMemoryLazy;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.DataSetBuilder;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.LabelMapping;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPMutable;
import edu.cmu.ml.rtw.generic.parse.AssignmentList;
import edu.cmu.ml.rtw.generic.parse.Obj;
import edu.cmu.ml.rtw.generic.util.ThreadMapper.Fn;

public abstract class DataSetBuilderDocumentFiltered<D extends Datum<L>, L> extends DataSetBuilder<D, L> {
	protected interface PairFn<S, T> {
		T apply(S o1, S o2);
	}
	
	public enum FilterType {
		INTERSECT,
		EXCLUDE,
		NONE
	}
	
	public enum LabelMode {
		ONLY_LABELED,
		ONLY_UNLABELED,
		ALL_AS_UNLABELED,
		ALL
	}
	
	public enum ClusterMode {
		POST,
		GLOBAL
	}
	
	protected String storage;
	protected String documents;
	protected Set<String> filter;
	protected FilterType filterType = FilterType.INTERSECT;
	protected LabelMode labelMode = LabelMode.ONLY_LABELED;
	protected ClusterMode clusterMode = ClusterMode.POST;
	protected LabelMapping<L> labelMapping;
	
	private String[] parameterNames = { "storage", "documents", "filter", "filterType", "labelMode", "labelMapping", "clusterMode" };
	
	private DocumentSetInMemoryLazy<DocumentNLP, DocumentNLPMutable> docs;
	
	
	public DataSetBuilderDocumentFiltered() {
		this(null);
	}
	
	public DataSetBuilderDocumentFiltered(DatumContext<D, L> context) {
		this.context = context;
	}
	
	@Override
	public String[] getParameterNames() {
		return this.parameterNames;
	}

	@Override
	public Obj getParameterValue(String parameter) {
		if (parameter.equals("storage"))
			return Obj.stringValue(this.storage);
		else if (parameter.equals("documents"))
			return Obj.stringValue(this.documents);
		else if (parameter.equals("filter"))
			return (this.filter != null) ? Obj.array(Arrays.asList(this.filter.toArray(new String[0]))) : null;
		else if (parameter.equals("filterType"))
			return Obj.stringValue(String.valueOf(this.filterType));
		else if (parameter.equals("labelMode"))
			return Obj.stringValue(String.valueOf(this.labelMode));
		else if (parameter.equals("labelMapping"))
			return this.labelMapping == null ? null : Obj.stringValue(this.labelMapping.toString());
		else if (parameter.equals("clusterMode"))
			return Obj.stringValue(this.clusterMode.toString());
		return null;
	}
	
	@Override
	protected boolean fromParseInternal(AssignmentList internalAssignments) {
		return true;
	}

	@Override
	protected AssignmentList toParseInternal() {
		return null;
	}

	@Override
	public boolean setParameterValue(String parameter, Obj parameterValue) {
		if (parameter.equals("storage"))
			this.storage = this.context.getMatchValue(parameterValue);
		else if (parameter.equals("documents"))
			this.documents = this.context.getMatchValue(parameterValue);
		else if (parameter.equals("filter")) {
			if (parameterValue == null) {
				this.filter = null;
			} else {
				this.filter = new ConcurrentHashSet<String>();
				this.filter.addAll(this.context.getMatchArray(parameterValue));
			}
		} else if (parameter.equals("filterType"))
			this.filterType = FilterType.valueOf(this.context.getMatchValue(parameterValue));
		else if (parameter.equals("labelMode"))
			this.labelMode = LabelMode.valueOf(this.context.getMatchValue(parameterValue));
		else if (parameter.equals("labelMapping"))
			this.labelMapping = (parameterValue == null) ? null : this.context.getDatumTools().getLabelMapping(this.context.getMatchValue(parameterValue));
		else if (parameter.equals("clusterMode"))
			this.clusterMode = ClusterMode.valueOf(this.context.getMatchValue(parameterValue));
		else
			return false;
		return true;
	}
	
	protected String getClusterName(String docName) {
		if (this.clusterMode == ClusterMode.GLOBAL)
			return "0";
		else
			return getDocumentNameWithoutPost(docName);
		
	}
	
	public static String getDocumentNameWithoutPost(String docName) {
		String clusterName = docName;
		if (docName.indexOf("_p") >= 0) {
			String[] docNameParts = docName.split("_p");
			if (docNameParts.length == 2) {
				try {
					Integer.parseInt(docNameParts[docNameParts.length - 1]);
					clusterName = docNameParts[0];
				} catch (NumberFormatException e) {
					
				}
			}
		}
		
		return clusterName;
	}
	
	protected boolean matchesFilter(DocumentNLP document) {
		return nameMatchesFilter(getDocumentNameWithoutPost(document.getName()));
	}
	
	protected DocumentSetInMemoryLazy<DocumentNLP, DocumentNLPMutable> getDocuments() {
		if (this.docs == null)
			this.docs = new DocumentSetInMemoryLazy<DocumentNLP, DocumentNLPMutable>(this.context.getDataTools().getStoredItemSetManager().getItemSet(this.storage, this.documents));
		return this.docs;
	}
	
	protected Map<String, Set<String>> getDocumentClusters() {
		if (this.docs == null)
			this.docs = new DocumentSetInMemoryLazy<DocumentNLP, DocumentNLPMutable>(this.context.getDataTools().getStoredItemSetManager().getItemSet(this.storage, this.documents));
		
		Map<String, Set<String>> clusters = new TreeMap<String, Set<String>>();
		
		docs.map(new Fn<DocumentNLP, Boolean>() {
			@Override
			public Boolean apply(DocumentNLP doc) {
				String docName = doc.getName();
				String clusterName = getClusterName(docName);
				
				if (!nameMatchesFilter(getDocumentNameWithoutPost(docName)))
					return true;
				
				synchronized (clusters) {
					if (!clusters.containsKey(clusterName))
						clusters.put(clusterName, new TreeSet<String>());
					clusters.get(clusterName).add(docName);
				}
				
				return true;
			}
			
		}, this.context.getMaxThreads(), this.context.getDataTools().getGlobalRandom());

		
		return clusters;
	}
	
	protected <S, T> List<T> runAllPairs(Collection<S> objs, PairFn<S, T> fn, List<T> results, boolean directed) {
		Set<S> done = new HashSet<S>();
		
		for (S obj1 : objs) {
			done.add(obj1);
			for (S obj2 : objs) {
				if (obj1.equals(obj2) || (!directed && done.contains(obj2)))
					continue;
				T result = fn.apply(obj1, obj2);
				if (result == null)
					continue;
				results.add(result);
			}
		}
		
		return results;
	}
	
	protected <S, T> List<T> runAllPairs(Collection<S> objs1, Collection<S> objs2, PairFn<S, T> fn, List<T> results) {		
		for (S obj1 : objs1) {
			for (S obj2 : objs2) {
				T result = fn.apply(obj1, obj2);
				if (result == null)
					continue;
				results.add(result);
			}
		}
		
		return results;
	}
	
	private boolean nameMatchesFilter(String documentName) {
		if (this.filterType == FilterType.NONE)
			return true;
		else if (this.filterType == FilterType.INTERSECT) {
			return this.filter.contains(documentName);
		} else {
			return !this.filter.contains(documentName);
		}
	}
}
