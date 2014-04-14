import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DestSrcSampleGraph {
	public HashMap<Integer, Integer> subGraph = new HashMap<Integer, Integer>();
	int depth = 1;
	int[] srces;
	int[] dests;
	public int proteinsCount;
	public SampleGraph sampleGraph;

	public DestSrcSampleGraph(SampleGraph sampleGraph) {

		proteinsCount = sampleGraph.proteinsCount;
		this.sampleGraph = sampleGraph;
	}


	public void addNeighbors() {
		Object[] subGraphSet = subGraph.keySet().toArray();
		Set<Integer> neighbors = new HashSet<Integer>();
		for (int i = 0; i < subGraphSet.length; i++) {
			// Here we get the neighbors of ith node, in the database
			// We add all the candidate neighbors to the set of all neighbors
			for (Entry<Integer, Double> neighbor : neighborsOf(
					(Integer) subGraphSet[i]).entrySet()) {
				if (!subGraph.containsKey(neighbor.getKey()))
					subGraph.put(neighbor.getKey(), depth);
			}
		}

		if (subGraph.keySet().size() != subGraphSet.length) {
			depth += 1;
		}
	}

	public void expand(int numberOfNewNodes) {
		// subGraphSet is the list of nodes in the current network (visible
		// nodes).
		Object[] subGraphSet = subGraph.keySet().toArray();
		Map<Integer, Double> neighborsEdgeSum = new HashMap<Integer, Double>();
		Double tempSum;
		for (int i = 0; i < subGraphSet.length; i++) {
			// Here we get the neighbors of ith node, in the database
			// We add all the candidate neighbors to the set of all neighbors
			for (Entry<Integer, Double> neighbor : neighborsOf(
					(Integer) subGraphSet[i]).entrySet()) {
				if (!subGraph.containsKey(neighbor.getKey())) {
					tempSum = neighborsEdgeSum.get(neighbor.getKey());
					if (tempSum == null) {
						neighborsEdgeSum.put(neighbor.getKey(),
								neighbor.getValue());
					} else {
						neighborsEdgeSum.put(neighbor.getKey(),
								neighbor.getValue() + tempSum);
					}
				}
			}
		}
		// Iterate over all candidate neighbors and for each of them find all
		// the neighbors
		// amongst those neighbors sum over those that are in the subGraph and
		// save this summation

		// in neighborsEdgeSum we have the sum of edges from neighbors to the
		// subGraph for each neighbor
		// we just need to sort them and take the top 10
		ArrayList<Entry<Integer, Double>> neighborsFinalList = new ArrayList<Entry<Integer, Double>>(
				neighborsEdgeSum.entrySet());
		Collections.sort(neighborsFinalList,
				new Comparator<Entry<Integer, Double>>() {

					@Override
					public int compare(Entry<Integer, Double> arg0,
							Entry<Integer, Double> arg1) {
						if (arg0.getValue() > arg1.getValue())
							return -1;
						else if (arg0.getValue() < arg1.getValue())
							return 1;
						else
							return 0;
					}

				});
		if (neighborsFinalList.size() > 0)
			depth += 1;
		int l = Math.min(numberOfNewNodes, neighborsFinalList.size());
		for (int i = 0; i < l; i++)
			if (!subGraph.containsKey(neighborsFinalList.get(i).getKey()))
				subGraph.put(neighborsFinalList.get(i).getKey(), depth);

	}

	public Map<Integer, Double> neighborsOf(int index) {
		Map<Integer, Double> arcs = new HashMap<Integer, Double>();
		switch (index) {
		case 0:
			for (int i = 0; i < srces.length; i++) {
				arcs.put(srces[i], sampleGraph.logD);
			}
			break;
		default:

			int end = sampleGraph.newproteins[index + 1];
			int start = sampleGraph.newproteins[index];

			for (int i = start; i < end; i++)
				arcs.put(sampleGraph.neighbors[0][i],
						sampleGraph.neighbors[1][i] / 1000.0);

		}

		return arcs;
	}

	public Map<Integer, Double> edgesFrom(int index) {
		Map<Integer, Double> arcs = new HashMap<Integer, Double>();
		switch (index) {
		case 0:
			for (int i = 0; i < srces.length; i++) {
				arcs.put(srces[i], sampleGraph.logD);
			}
			break;
		default:

			int end = sampleGraph.newproteins[index + 1];
			int start = sampleGraph.newproteins[index];

			for (int i = start; i < end; i++)
				arcs.put(
						sampleGraph.neighbors[0][i],
						sampleGraph.logD
								+ Math.log(1000.0 / sampleGraph.neighbors[1][i]));

		}

		return arcs;
	}

}
