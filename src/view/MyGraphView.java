package view;


import com.google.common.base.Function;
import graph.MyEdge;
import graph.MyGraph;
import graph.MyVertex;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import model.VertexSelectionModel;


import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * <h1>The class is the general class for the graph view</h1>
 * <p>
 * The class contains methods for drawing the vertices and it also hosts the listeners for any changes of them.
 * Furthermore it hosts methods for pausing the animations and coloring the vertices.
 * </p>
 *
 * @see MyVertex
 * @see MyVertexView
 */
public class MyGraphView extends Group {

    private Group myVertexViewGroup;
    private Group myEdgeViewGroup;
    private MyGraph<MyVertex, MyEdge> graph;
    public SpringAnimationService animationService;
    private VertexSelectionModel selectionModel;

    // Properties changed by GUI, influencing the graph Display
    public BooleanProperty pausedProperty;

    protected Function<MyEdge, Integer> myEdgeLengthFunction;


    public MyGraphView(MyGraph<MyVertex, MyEdge> graph) {
        this.graph = graph;
        this.myVertexViewGroup = new Group();
        this.myEdgeViewGroup = new Group();
        this.animationService = new SpringAnimationService(graph);

        this.pausedProperty = new SimpleBooleanProperty(false);

        drawNodes();
        drawEdges();

        getChildren().add(myEdgeViewGroup);
        getChildren().add(myVertexViewGroup);

        // Add all Vertex to the selection Model and add Listener
        selectionModel = new VertexSelectionModel(graph.getVertices().toArray());
        addSelectionListener();
        addPausedListener();

        startLayout();
    }


    public void drawEdges() {
        graph.getEdges().forEach((edge) -> {
            myEdgeViewGroup.getChildren().add(new MyEdgeView(edge));
        });
    }


    public void drawNodes() {
        graph.getVertices().forEach((node) -> {
            myVertexViewGroup.getChildren().add(new MyVertexView(node));
        });
    }


    public void addSelectionListener() {
        selectionModel.getSelectedItems().addListener((ListChangeListener) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Object o : c.getAddedSubList()) {
                        MyVertex vertex = (MyVertex) o;
                        vertex.isSelectedProperty().setValue(true);
                    }
                }
                if (c.wasRemoved()) {
                    for (Object o : c.getRemoved()) {
                        MyVertex vertex = (MyVertex) o;
                        vertex.isSelectedProperty().setValue(false);
                    }

                }
            }
        });
    }

    public void addLayoutDimensionBinding() {


    }

    /**
     * Sets Colour Palette in the MyVertexView classes when called on User Input
     * @param palette Pakette item used for the colouring
     */
    public void setNodeColour(Palette palette) {
        for (Node n : myVertexViewGroup.getChildren()) {

            MyVertexView vW = (MyVertexView) n;
            vW.colourProperty.setValue(palette);

        }
    }

    /**
     * Sets Attribute in the MyVertexView classes when called on User Input
     * @param attribute in String Format, should be in AttributeMap of the MyVertex class

     */
    public void setNodeAttribute(String attribute) {
        for (Node n : myVertexViewGroup.getChildren()) {

            MyVertexView vW = (MyVertexView) n;
            vW.colourAttribute.setValue(attribute);

        }

    }

    /**
     * Sets Colour Palette in the MyEdgeView classes when called on User Input
     * @param palette Pakette item used for the colouring
     */
    public void setEdgeColour(Palette palette) {
        for (Node n : myEdgeViewGroup.getChildren()) {

            MyEdgeView eW = (MyEdgeView) n;
            eW.colourProperty.setValue(palette);

        }
    }

    /**
     * Sets Attribute in the MyEdgeView classes when called on User Input
     * @param attribute in String Format, should be in AttributeMap of the MyVertex class

     */
    public void setEdgeAttribute(String attribute) {
        for (Node n : myEdgeViewGroup.getChildren()) {

            MyEdgeView eW = (MyEdgeView) n;
            eW.colourAttribute.setValue(attribute);

        }

    }

    public void addPausedListener() {
        pausedProperty.addListener(e -> {
            if (pausedProperty.get()) {
                pauseAnimation();
            } else resumeAnimation();
        });
    }


    public void startLayout() {
        animationService.start();
        // If paused: cancel directly afterwards
        if (pausedProperty.get()) pauseAnimation();
    }

    public void updateNodePosition(MyVertex vertex) {
        animationService.updateNode(vertex);
    }

    public void pauseAnimation(){
        animationService.cancel();
    }

    public void resumeAnimation(){
        if (!pausedProperty.get()) animationService.restart();

    }

    public Group getMyVertexViewGroup() {
        return myVertexViewGroup;
    }

    public Group getMyEdgeViewGroup() {
        return myEdgeViewGroup;
    }

    public MyGraph<MyVertex, MyEdge> getGraph() {
        return graph;
    }

    public VertexSelectionModel getSelectionModel() {
        return selectionModel;
    }
}
