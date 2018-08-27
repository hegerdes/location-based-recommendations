package de.nuttercode.androidprojectss2018.csi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.nuttercode.androidprojectss2018.csi.query.Query;
import de.nuttercode.androidprojectss2018.csi.query.QueryResultState;
import de.nuttercode.androidprojectss2018.csi.query.QueryResultSummary;
import de.nuttercode.androidprojectss2018.lbrserver.LBRServer;

/**
 * stores elements locally. use {@link #addStoreListener(StoreListener)} to
 * listener to new or removed elements.
 * 
 * @author Johannes B. Latzel
 *
 * @param <T>
 *            some {@link Serializable}
 */
public class Store<T extends Serializable> {

	private final Set<T> tSet;
	private final Query<T> query;
	private final List<StoreListener<T>> storeListenerList;

	/**
	 * @param query
	 * @throws IllegalArgumentException
	 *             if query is null
	 */
	protected Store(Query<T> query) {
		Assurance.assureNotNull(query);
		tSet = new HashSet<>();
		this.query = query;
		storeListenerList = new ArrayList<>();
	}

	public void addStoreListener(StoreListener<T> storeNewElementListener) {
		storeListenerList.add(storeNewElementListener);
	}

	public Set<T> getAll() {
		return new HashSet<>(tSet);
	}

	/**
	 * retrieves all applicable elements from the {@link LBRServer}. if the
	 * underlying queries does not have an {@link QueryResultState#OK} then no data
	 * will be saved in this {@link Store}. calls all added {@link StoreListener}s
	 * as added by {@link #addStoreListener(StoreListener)}.
	 * 
	 * @return the state of the underlying query
	 */
	public QueryResultState refresh() {
		QueryResultSummary<T> resultSummary = query.run();
		Collection<T> receivedElements;
		if (resultSummary.getQueryResultState() == QueryResultState.OK) {
			receivedElements = resultSummary.getQueryResult().getAll();
			for (T newElement : receivedElements)
				if (!tSet.contains(newElement))
					for (StoreListener<T> listener : storeListenerList)
						try {
							listener.onElementAdded(newElement);
						} catch (RuntimeException e) {
							e.printStackTrace();
						}

			for (T element : tSet)
				if (!receivedElements.contains(element))
					for (StoreListener<T> listener : storeListenerList)
						try {
							listener.onElementRemoved(element);
						} catch (RuntimeException e) {
							e.printStackTrace();
						}
			tSet.clear();
			tSet.addAll(receivedElements);
		}
		return resultSummary.getQueryResultState();
	}

	@Override
	public String toString() {
		return "Store [tSet=" + Arrays.toString(tSet.toArray()) + "]";
	}

}
