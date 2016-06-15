package pathfinding;

import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.UndirectedWeightedSubgraph;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.sql.Array;
import java.util.*;

/**
 * Wrapper for JGraphT library. Creating graphs, adding vertices and edges, searching for
 * the shortest paths and so on.
 */
public class JGraphTWrapper {
    public enum GraphElementType {
        DEFAULT, ELEVATOR, STAIRS
    }

    private SimpleWeightedGraph<LatLngGraphVertex, LatLngGraphEdge> graph;
    private int currentVertexId;


    public JGraphTWrapper() {
        graph = new SimpleWeightedGraph<>(LatLngGraphEdge.class);
        currentVertexId = 0;
    }

    /**
     * Adds new vertex.
     *
     * @param v - vertex to add
     * @return id of vertex added
     */
    public int addVertex(LatLng v, GraphElementType graphVertexType) {
        addVertexWithId(v, currentVertexId, graphVertexType);
        return currentVertexId++;
    }

    private void addVertexWithId(LatLng v, int id, GraphElementType graphVertexType) {
        LatLngGraphVertex vTemp = new LatLngGraphVertex(v, id, graphVertexType);
        graph.addVertex(vTemp);
    }

    /**
     * Adds new edge of given type.
     *
     * @param v1               - vertex edge begins
     * @param v2               - vertex edge ends
     * @param v1Index          - v1 vertex index
     * @param v2Index          - v2 vertex index
     * @param graphElementType - edge type (see LatLngGraphEdge.GraphElementType)
     */
    public void addEdge(LatLng v1, LatLng v2, int v1Index, int v2Index, GraphElementType graphElementType) {
        LatLngGraphVertex gv1 = new LatLngGraphVertex(v1, v1Index, graphElementType);
        LatLngGraphVertex gv2 = new LatLngGraphVertex(v2, v2Index, graphElementType);

        graph.addEdge(gv1, gv2, new LatLngGraphEdge(graphElementType));
        LatLngGraphEdge e = graph.getEdge(gv1, gv2);
        double penaltyWeight = (graphElementType == GraphElementType.DEFAULT) ? 0.0 : 1.0;
        graph.setEdgeWeight(e, haversine(gv1.getVertex().getLatitude(), gv1.getVertex().getLongitude(),
                gv2.getVertex().getLatitude(), gv2.getVertex().getLongitude()) + penaltyWeight);
    }

    /**
     * Shortest path using all edges.
     *
     * @param v1 - start LatLng
     * @param v2 - end LatLng
     * @return sequential list of LatLng objects
     */
    public ArrayList<LatLngGraphVertex> shortestPath(LatLngFlr v1, LatLng v2) {
        return shortestPathForGraph(v1, v2, graph);
    }

    private ArrayList<LatLngGraphVertex> shortestPathForGraph(LatLngFlr v1, LatLng v2, Graph<LatLngGraphVertex, LatLngGraphEdge> g) {
        ArrayList<LatLngGraphVertex> pointsList = new ArrayList<>();

        LatLngGraphVertex vTemp1 = new LatLngGraphVertex(v1.getLatLng(), 0, GraphElementType.DEFAULT);
        LatLngGraphVertex vTemp2 = new LatLngGraphVertex(v2, 0, GraphElementType.DEFAULT);
        if(!g.containsVertex(vTemp1)) {
            pointsList.add(vTemp1);
            vTemp1 = new LatLngGraphVertex(findClosestCoordinateToGiven(v1).getLatLng(), 0, GraphElementType.DEFAULT);
        }

        DijkstraShortestPath<LatLngGraphVertex, LatLngGraphEdge> dijkstraPathFinder = new DijkstraShortestPath<>(g, vTemp1, vTemp2);
        List<LatLngGraphEdge> foundPath = dijkstraPathFinder.getPathEdgeList();
        if (foundPath == null || foundPath.size() == 0) {
            return null;
        }

        LatLngGraphVertex testVertexFrom = foundPath.get(0).getV1();
        LatLngGraphVertex testVertexTo = foundPath.get(0).getV2();
        pointsList.add(testVertexFrom.equals(vTemp1) ? testVertexFrom : testVertexTo);
        for (int i = 0; i < foundPath.size(); ++i) {
            testVertexFrom = foundPath.get(i).getV1();
            testVertexTo = foundPath.get(i).getV2();
            pointsList.add(pointsList.get(pointsList.size() - 1).equals(testVertexFrom) ? testVertexTo : testVertexFrom);
        }
        return pointsList;
    }

    /**
     * Shortest path with only default edges.
     *
     * @param v1 - start LatLng
     * @param v2 - end LatLng
     * @return sequential list of LatLng objects
     */
    public ArrayList<LatLngGraphVertex> defaultShortestPath(LatLngFlr v1, LatLng v2) {
        Set<LatLngGraphEdge> oldEdges = graph.edgeSet();
        Set<LatLngGraphEdge> defaultEdges = new HashSet<>();
        for (LatLngGraphEdge edge : oldEdges) {
            if (edge.getGraphEdgeType() == GraphElementType.DEFAULT) {
                defaultEdges.add(edge);
            }
        }
        UndirectedWeightedSubgraph<LatLngGraphVertex, LatLngGraphEdge> defaultEdgesGraph =
                new UndirectedWeightedSubgraph<>(graph, null, defaultEdges);
        return shortestPathForGraph(v1, v2, defaultEdgesGraph);
    }

    /**
     * Stores graph into the file using GraphML format.
     *
     * @param filename - exported file name
     */
    public void exportGraphML(String filename) {
        //TODO: implement (since JGraphT export sucks)
    }

    /**
     * Imports graph from the file of GraphML format. Doesn't return anything but if import was
     * successful internal graph object will be replaced by the imported one.
     *
     * @param inputStream - stream to read.
     */
    public void importGraphML(InputStream inputStream) throws XmlPullParserException, FileNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(br);
        int eventType = xpp.getEventType();

        graph = new SimpleWeightedGraph<>(LatLngGraphEdge.class);
        currentVertexId = 0;
        HashMap<Integer, LatLng> verticesMap = new HashMap<>();
        int id = -1;
        GraphElementType vertexType = GraphElementType.DEFAULT;
        boolean nodeDataFound = false;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tagName = xpp.getName();
                switch (tagName) {
                    case "node":
                        id = Integer.valueOf(xpp.getAttributeValue(null, "id"));
                        String vType = xpp.getAttributeValue(null, "type");
                        if (vType == null) vertexType = GraphElementType.DEFAULT;
                        else if (vType.equals("stairs")) vertexType = GraphElementType.STAIRS;
                        else if (vType.equals("elevator")) vertexType = GraphElementType.ELEVATOR;
                        else vertexType = GraphElementType.DEFAULT;
                        break;
                    case "edge":
                        int from = Integer.valueOf(xpp.getAttributeValue(null, "source"));
                        int to = Integer.valueOf(xpp.getAttributeValue(null, "target"));
                        String eType = xpp.getAttributeValue(null, "id");
                        GraphElementType graphEdgeType = GraphElementType.DEFAULT;
                        switch (eType) {
                            case "ELEVATOR":
                                graphEdgeType = GraphElementType.ELEVATOR;
                                break;
                            case "STAIRS":
                                graphEdgeType = GraphElementType.STAIRS;
                                break;
                            case "DEFAULT":
                                graphEdgeType = GraphElementType.DEFAULT;
                                break;
                        }
                        addEdge(verticesMap.get(from), verticesMap.get(to), from, to, graphEdgeType);
                        break;
                    case "data":
                        if (id != -1) {
                            nodeDataFound = true;
                        }
                        break;
                }
            } else if (eventType == XmlPullParser.TEXT) {
                if (id != -1 && nodeDataFound) {
                    String[] coords = xpp.getText().split(" ");
                    LatLng latLng = new LatLng(Double.valueOf(coords[0]), Double.valueOf(coords[1]));
                    addVertexWithId(latLng, id, vertexType);
                    verticesMap.put(id, latLng);
                    id = -1;
                    vertexType = GraphElementType.DEFAULT;
                    nodeDataFound = false;
                }
            }

            try {
                eventType = xpp.next();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns all graph vertices as array (warning - this method complexity is O(n))
     *
     * @return array of graph vertices
     */
    public LatLngGraphVertex[] getVertices() {
        LatLngGraphVertex[] v = new LatLngGraphVertex[graph.vertexSet().size()];
        v = graph.vertexSet().toArray(v);
        return v;
    }

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return 6372.8 * c;
    }

    public LatLngFlr findClosestCoordinateToGiven(LatLngFlr v) {
        LatLngGraphVertex[] verticesList = new LatLngGraphVertex[graph.vertexSet().size()];
        verticesList = graph.vertexSet().toArray(verticesList);
        LatLngFlr closestCoordinate = null;
        double shortestDistance = Double.MAX_VALUE;
        for(int i=0; i<verticesList.length; i++) {
            LatLng candidateCoordinate = verticesList[i].getVertex();
            double candidateDistance = calculateDistance(v, candidateCoordinate);
            if(candidateDistance < shortestDistance && v.getFloor() == (int) Math.floor(verticesList[i].getVertexId()/1000)) {
                closestCoordinate = new LatLngFlr(candidateCoordinate.getLatitude(), candidateCoordinate.getLongitude(), v.getFloor());
                shortestDistance = candidateDistance;
            }
        }

        return closestCoordinate;
    }

    private double calculateDistance(LatLng v1, LatLng v2) {
        return Math.sqrt(Math.pow(v1.getLatitude() - v2.getLatitude() ,2) + Math.pow(v1.getLongitude() - v2.getLongitude(),2));
    }

    public boolean graphContainsVertexWithCoordinates(LatLngFlr c) {
        LatLngGraphVertex vTemp = new LatLngGraphVertex(c.getLatLng(), 0, GraphElementType.DEFAULT);
        if(graph.containsVertex(vTemp))
            return true;
        else
            return false;
    }
}