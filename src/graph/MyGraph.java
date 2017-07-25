package graph;

import edu.uci.ics.jung.graph.AbstractTypedGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import model.AnalysisData;
import model.Sample;
import model.TaxonNode;

import java.util.*;

import static model.AnalysisData.*;

/**
 * Created by julian on 10.06.17.
 * <p>
 * Heavily based on the JUNG UndirectedSparseGraph class,
 * most methods are copied.
 */
public class MyGraph<V, E> extends AbstractTypedGraph<V, E>
        implements UndirectedGraph<V, E> {


    private HashMap<TaxonNode, MyVertex> taxonNodeToVertexMap;
    private HashMap<Integer, HashMap<Integer, MyEdge>> nodeIdsToEdgesMap;
    protected Map<V, Map<V, E>> vertices; // Map of vertices to adjacency maps of vertices to incident edges
    protected Map<E, Pair<V>> edges;    // Map of edges to connected vertex sets

    /**
     * Creates an instance.
     */
    public MyGraph() {
        super(EdgeType.UNDIRECTED);
        vertices = new HashMap<>();
        edges = new HashMap<>();
        taxonNodeToVertexMap = new HashMap<>();
        nodeIdsToEdgesMap = new HashMap<>();
        setupFilterListeners();
    }

    private void setupFilterListeners() {
        minCorrelationProperty().addListener(observable -> filterEdges());
        maxCorrelationProperty().addListener(observable -> filterEdges());
        maxPValueProperty().addListener(observable -> filterEdges());
        minFrequencyProperty().addListener(observable -> {
            filterVertices();
        });
        maxFrequencyProperty().addListener(observable -> {
            filterVertices();
        });

//        minCorrelationProperty().addListener((observable, oldValue, newValue) -> {
//            for (E e : edges.keySet()) {
//                MyEdge edge = (MyEdge) e; //E is always MyEdge
//                if (edge.getCorrelation() < newValue.doubleValue()) {
//                    edge.hideEdge();
//                } else {
//                    edge.showEdge();
//
//                }
//            }
//        });
//
//        maxCorrelationProperty().addListener((observable, oldValue, newValue) -> {
//            for (E e : edges.keySet()) {
//                MyEdge edge = (MyEdge) e;
//                if (edge.getCorrelation() > newValue.doubleValue()) {
//                    edge.hideEdge();
//
//                } else {
//                    edge.showEdge();
//
//                }
//            }
//        });
//
//        maxPValueProperty().addListener((observable, oldValue, newValue) -> {
//            for (E e : edges.keySet()) {
//                MyEdge edge = (MyEdge) e;
//                if (edge.getPValue() > newValue.doubleValue()) {
//                    edge.hideEdge();
//
//                } else {
//                    edge.showEdge();
//
//                }
//            }
//        });
//
//        minFrequencyProperty().addListener((observable, oldValue, newValue) -> {
//            for (V v : vertices.keySet()) {
//                MyVertex vertex = (MyVertex) v;
//                if (AnalysisData.getMaximumRelativeFrequencies().get(vertex.getTaxonNode()) < newValue.doubleValue()){
//                    for (MyEdge myEdge : vertex.getEdgesList()) {
//                        myEdge.hideEdge();
//                    }
//                }else{
//                    for (MyEdge myEdge : vertex.getEdgesList()) {
//                        myEdge.showEdge();
//                    }
//                }
//
//            }
//        });
//
//        maxFrequencyProperty().addListener((observable, oldValue, newValue) -> {
//            for (V v : vertices.keySet()) {
//                MyVertex vertex = (MyVertex) v;
//                if (AnalysisData.getMaximumRelativeFrequencies().get(vertex.getTaxonNode()) > newValue.doubleValue()){
//                    for (MyEdge myEdge : vertex.getEdgesList()) {
//                        myEdge.hideEdge();
//                    }
//                }else{
//                    for (MyEdge myEdge : vertex.getEdgesList()) {
//                        myEdge.showEdge();
//                    }
//                }
//            }
//        });
    }

    @Override
    public boolean addEdge(E edge, Pair<? extends V> endpoints, EdgeType edgeType) {
        this.validateEdgeType(edgeType);
        Pair<V> new_endpoints = getValidatedEndpoints(edge, endpoints);
        if (new_endpoints == null)
            return false;

        V v1 = new_endpoints.getFirst();
        V v2 = new_endpoints.getSecond();

        if (findEdge(v1, v2) != null)
            return false;

        edges.put(edge, new_endpoints);

        if (!vertices.containsKey(v1))
            this.addVertex(v1);

        if (!vertices.containsKey(v2))
            this.addVertex(v2);

        // map v1 to <v2, edge> and vice versa
        vertices.get(v1).put(v2, edge);
        vertices.get(v2).put(v1, edge);

        return true;
    }


    public Collection<E> getInEdges(V vertex) {
        return this.getIncidentEdges(vertex);
    }

    public Collection<E> getOutEdges(V vertex) {
        return this.getIncidentEdges(vertex);
    }

    public Collection<V> getPredecessors(V vertex) {
        return this.getNeighbors(vertex);
    }

    public Collection<V> getSuccessors(V vertex) {
        return this.getNeighbors(vertex);
    }

    @Override
    public E findEdge(V v1, V v2) {
        if (!containsVertex(v1) || !containsVertex(v2))
            return null;
        return vertices.get(v1).get(v2);
    }

    @Override
    public Collection<E> findEdgeSet(V v1, V v2) {
        if (!containsVertex(v1) || !containsVertex(v2))
            return null;
        ArrayList<E> edge_collection = new ArrayList<E>(1);
//        if (!containsVertex(v1) || !containsVertex(v2))
//            return edge_collection;
        E e = findEdge(v1, v2);
        if (e == null)
            return edge_collection;
        edge_collection.add(e);
        return edge_collection;
    }

    public Pair<V> getEndpoints(E edge) {
        return edges.get(edge);
    }

    public V getSource(E directed_edge) {
        return null;
    }

    public V getDest(E directed_edge) {
        return null;
    }

    public boolean isSource(V vertex, E edge) {
        return false;
    }

    public boolean isDest(V vertex, E edge) {
        return false;
    }

    public Collection<E> getEdges() {
        return Collections.unmodifiableCollection(edges.keySet());
    }

    public Collection<V> getVertices() {
        return Collections.unmodifiableCollection(vertices.keySet());
    }

    public boolean containsVertex(V vertex) {
        return vertices.containsKey(vertex);
    }

    public boolean containsEdge(E edge) {
        return edges.containsKey(edge);
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public Collection<V> getNeighbors(V vertex) {
        if (!containsVertex(vertex))
            return null;
        return Collections.unmodifiableCollection(vertices.get(vertex).keySet());
    }

    public Collection<E> getIncidentEdges(V vertex) {
        if (!containsVertex(vertex))
            return null;
        return Collections.unmodifiableCollection(vertices.get(vertex).values());
    }

    public boolean addVertex(V vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("vertex may not be null");
        }
        if (!containsVertex(vertex)) {
            vertices.put(vertex, new HashMap<V, E>());
            return true;
        } else {
            return false;
        }
    }

    public boolean removeVertex(V vertex) {
        if (!containsVertex(vertex))
            return false;

        // iterate over copy of incident edge collection
        for (E edge : new ArrayList<E>(vertices.get(vertex).values()))
            removeEdge(edge);

        vertices.remove(vertex);
        return true;
    }

    public boolean removeEdge(E edge) {
        if (!containsEdge(edge))
            return false;

        Pair<V> endpoints = getEndpoints(edge);
        V v1 = endpoints.getFirst();
        V v2 = endpoints.getSecond();

        // remove incident vertices from each others' adjacency maps
        vertices.get(v1).remove(v2);
        vertices.get(v2).remove(v1);

        edges.remove(edge);
        return true;
    }

    public void filterEdges(){
        for (E e: edges.keySet()){
            MyEdge edge = (MyEdge) e; //E is always of type MyEdge
            if(edge.getCorrelation() < getMinCorrelation() || edge.getCorrelation() > getMaxCorrelation()
                    || edge.getPValue() > getMaxPValue()){
                edge.hideEdge();
            }else{
                edge.showEdge();
            }
        }
    }

    public  void filterVertices(){
        for (V v : vertices.keySet()){
            MyVertex vertex = (MyVertex) v;
            double vertexMaxRelativeFrequency = AnalysisData.getMaximumRelativeFrequencies().get(vertex.getTaxonNode());
            if(vertexMaxRelativeFrequency < getMinFrequency() || vertexMaxRelativeFrequency > getMaxFrequency()){
                for (MyEdge myEdge : vertex.getEdgesList()) {
                    myEdge.hideEdge();
                }
            }else{
                for (MyEdge myEdge : vertex.getEdgesList()) {
                    myEdge.showEdge();
                }
            }
        }
    }

    /**
     * Filters the taxa contained in the castListToGeneric of samples. Returns a list of taxa that lie below/above the given
     * lower/upper correlation & frequency thresholds and below the given p-Value threshold
     *
     * @param samples
     */
    public void filterTaxa(List<Sample> samples) {

//        //Get the unfiltered List of all taxons contained in either sample1 or sample2 and sort it by node id
//        LinkedList<TaxonNode> taxonList = SampleComparison.getUnifiedTaxonList(samples, getLevel_of_analysis());
//
//        //Counts the visible edges of each node - is initially set to n-1 for every node, decremented when edge is hidden
//        int[] visibleEdgeCounts = new int[taxonList.size()];
//        Arrays.fill(visibleEdgeCounts, taxonList.size() - 1);
//
//        //Get correlation matrix and p-value matrix
//        RealMatrix correlationMatrix = SampleComparison.getCorrelationMatrixOfSamples(samples, getLevel_of_analysis());
//        RealMatrix correlationPValues = SampleComparison.getCorrelationPValuesOfSamples(samples, getLevel_of_analysis());
//
//        //Compare every node with every other node
//        for (int i = 0; i < taxonList.size(); i++) {
//            for (int j = 0; j < i; j++) {
//                //Access MyEdge object via the Hashmap (we need the node ids for this)
//                int idOfFirstNode = taxonList.get(i).getTaxonId();
//                int idOfSecondNode = taxonList.get(j).getTaxonId();
//                MyEdge currentEdge = getNodeIdsToEdgesMap().get(idOfFirstNode).get(idOfSecondNode);
//                //Test if edge between the nodes should be hidden
//                if (correlationMatrix.getEntry(i, j) < getMinCorrelation() ||
//                        correlationMatrix.getEntry(i, j) > getMaxCorrelation() ||
//                        correlationPValues.getEntry(i, j) > getMaxPValue()) {
//                    //Hide it
//                    currentEdge.hideEdge();
//                    //Decrement edgeCount of both nodes
//                    visibleEdgeCounts[i]--;
//                    visibleEdgeCounts[j]--;
//                } else {
//                    //Show it
//                    currentEdge.showEdge();
//                }
//            }
//        }
//        //Check if the maximum relative frequency of every taxon lies in between the upper and lower threshold
//        final HashMap<TaxonNode, Double> maximumRelativeFrequencies = getMaximumRelativeFrequencies();
//        //Additionally, hide all vertices that don't have visible edges anymore, show the rest
//        for (int i = 0; i < visibleEdgeCounts.length; i++) {
//            TaxonNode currentNode = taxonList.get(i);
//            if (visibleEdgeCounts[i] == 0
//                    || maximumRelativeFrequencies.get(currentNode) < getMinFrequency()
//                    || maximumRelativeFrequencies.get(currentNode) > getMaxFrequency())
//                taxonNodeToVertexMap.get(currentNode).hideVertex();
//            else
//                taxonNodeToVertexMap.get(currentNode).showVertex();
//        }
    }


    public HashMap<Integer, HashMap<Integer, MyEdge>> getNodeIdsToEdgesMap() {
        return nodeIdsToEdgesMap;
    }

    public HashMap<TaxonNode, MyVertex> getTaxonNodeToVertexMap() {
        return taxonNodeToVertexMap;
    }

    //    /*
//  The following 3 methods are basically overloaded filters with default params for 2 of the three doubles
//   */
//    public void filterSamplesByPValue(List<Sample> samples, double pValueThreshold, String rank) {
//        filterTaxa(samples, 1, -1, pValueThreshold, rank);
//    }
//
//    public void filterSamplesByMinimalCorrelation(List<Sample> samples, double upperCorrelationThreshold, String rank) {
//        filterTaxa(samples, 1, upperCorrelationThreshold, 1, rank);
//    }
//
//    public void filterSamplesByMaximalCorrelation(List<Sample> samples, double lowerCorrelationThreshold, String rank) {
//        filterTaxa(samples, lowerCorrelationThreshold, -1, 1, rank);
//    }


}
