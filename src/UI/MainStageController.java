package UI;

import analysis.GraphAnalysis;
import analysis.SampleComparison;
import graph.MyEdge;
import graph.MyGraph;
import graph.MyVertex;
import javafx.application.Platform;
import javafx.beans.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import main.GlobalConstants;
import main.UserSettings;
import model.AnalysisData;
import model.LoadedData;
import model.Sample;
import model.TaxonNode;
import org.apache.commons.math3.linear.RealMatrix;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import sampleParser.BiomV1Parser;
import sampleParser.BiomV2Parser;
import sampleParser.ReadName2TaxIdCSVParser;
import sampleParser.TaxonId2CountCSVParser;
import util.SaveAndLoadOptions;
import view.MyEdgeView;
import view.MyGraphView;
import view.MyVertexView;
import view.ViewPane;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static main.Main.getPrimaryStage;
import static model.AnalysisData.*;

public class MainStageController implements Initializable {

    private static Stage optionsStage;
    private static Stage exportImagesStage;

    private ViewPane viewPane;

    private static final int MAX_WIDTH_OF_SIDEPANES = 220;

    public MainStageController() {
    }

    private enum FileType {taxonId2Count, readName2TaxonId, biomV1, biomV2}

    public static boolean isMainViewMaximized = false;

    // alerts
    private Alert fileNotFoundAlert, confirmQuitAlert, aboutAlert, fileAlreadyLoadedAlert, wrongFileAlert, insufficientDataAlert;

    // FXML elements
    @FXML
    private AnchorPane leftPane, rightPane;

    @FXML
    private Tab mainViewTab;

    @FXML
    private Label leftLabel;

    @FXML
    private TreeView<String> treeViewFiles;

    @FXML
    private Accordion preferencesAccordion;


    /**
     * BUTTON ELEMENTS
     */
    @FXML
    private RadioButton collapseAllButton;

    /**
     * FILTER OPTION ELEMENTS
     **/
    @FXML
    private ChoiceBox<String> rankChoiceBox;

    //List of possible choices of the choice box
    ObservableList<String> ranksList = FXCollections.observableArrayList("Domain", "Kingdom", "Phylum", "Class",
            "Order", "Family", "Genus", "Species");

    @FXML
    private RadioButton compareSelectedSamplesButton;

    @FXML
    private TextField minPosCorrelationText, maxPosCorrelationText, minNegCorrelationText, maxNegCorrelationText;

    @FXML
    private Slider maxPValueSlider;

    @FXML
    private TextField maxPValueText;

    @FXML
    private TextField minFrequencyText;

    @FXML
    private TextField maxFrequencyText;

    @FXML
    private RangeSlider posCorrelationRangeSlider, negCorrelationRangeSlider, frequencyRangeSlider;

    /**
     * STARTUP PANE ELEMENTS
     **/
    @FXML
    private Label startupLabel;

    @FXML
    private ProgressIndicator startupSpinner;


    /**
     * STATUS FOOTER ELEMENTS
     **/
    @FXML
    private Label statusRightLabel;


    /**
     * GRAPH VIEW SETTING ELEMENTS
     **/
    @FXML
    private Slider sliderNodeRadius;

    @FXML
    private Slider sliderEdgeWidth;

    @FXML
    private RangeSlider sliderEdgeLength;

    @FXML
    private ToggleButton buttonPauseAnimation;

    @FXML
    private CheckBox checkAdvancedGraphSettings;

    @FXML
    private Label labelNodeRepulsion;

    @FXML
    private Slider sliderNodeRepulsion;

    @FXML
    private Label labelStretchParameter;

    @FXML
    private Slider sliderStretchParameter;

    @FXML
    private Label labelAnimationSpeed;

    @FXML
    private Slider sliderAnimationSpeed;

    @FXML
    private Label labelEdgeForce;

    @FXML
    private Slider sliderEdgeForce;

    @FXML
    private Button buttonResetGraphDefaults;

    @FXML
    private CheckBox showLabelsCheckBox;

    /**
     * ANALYSIS PANE ELEMENTS
     */

    @FXML
    private AnchorPane analysisPane;

    @FXML
    private PieChart frequencyChart;

    @FXML
    private BarChart<String, Double> degreeDistributionChart;

    @FXML
    private TextArea graphStatText, dataStatText;
    /**
     * INFO PANE
     */
    @FXML
    private TextFlow infoTextFlow;

    @FXML
    private TextArea infoTextArea;

    /**
     * Initializes every needed service
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load FontAweSome
        GlyphFontRegistry.register(new FontAwesome(getClass().getResourceAsStream("/fonts/fontawesome-webfont.ttf")));

        startTreePreloadService();
        initializeAccordion();
        initializeCollapseAllButton();
        initializeRankChoiceBox();
        initializeGraphSettings();
        initializeAnalysisPane();
        initializeGraphAnalysis();
        initializeInfoPane();
        initializeBindings();
        //preload settings
        SaveAndLoadOptions.loadSettings();

        //Display the info text in the bottom left pane
        displayInfoText();
    }


    @FXML
    /**
     * Should be called when the user clicks a button to analyze the loaded samples and display the graphview
     * Creates correlation data, creates the internal graph, applies default filter, displays the graph
     */
    public void startAnalysis() {
        boolean isAnalysisSuccessful = AnalysisData.performCorrelationAnalysis(new ArrayList<>(LoadedData.getSamplesToAnalyze()));
        if (isAnalysisSuccessful) {
            LoadedData.createGraph();
            LoadedData.getTaxonGraph().filterEdges();
            LoadedData.getTaxonGraph().filterVertices();
            displayGraph(LoadedData.getTaxonGraph());
            displayAnalysisTextsAndGraphs();
            displayGraphAnalysis();
            displayInfoText();
        } else {//The analysis couldn't be done because of insufficient data
            showInsufficientDataAlert();
        }

    }

    /**
     * chooses which text to display on the bottom left pane
     * TODO: This isn't called everytime it should be, add some more listeners!
     */
    private void displayInfoText() {
        String infoText="";
        if (LoadedData.getSamplesToAnalyze() == null || LoadedData.getSamples().size() < 3) {
            infoText = "Please import at least 3 samples to begin correlation analysis!";
        } else if (compareSelectedSamplesButton.isSelected() && LoadedData.getSelectedSamples().size() <= 3) {
            infoText = "If you want to analyse selected samples only, please select at least 3 samples!";
        } else if (rankChoiceBox.getValue() == null) {
            infoText = "Choose a rank to display the graph!";
        } else if (LoadedData.getGraphView() != null && LoadedData.getGraphView().getSelectionModel().getSelectedItems().size() > 1) {
            StringBuilder builder = new StringBuilder("Selected Taxa:\n");
            ObservableList selectedItems = LoadedData.getGraphView().getSelectionModel().getSelectedItems();
            for (Object selectedItem : selectedItems) {
                MyVertex vertex = (MyVertex) selectedItem;
                builder.append(vertex.getTaxonName());
                builder.append("\n");
            }
            infoText = builder.toString();
        } else if (LoadedData.getGraphView() != null && LoadedData.getGraphView().getSelectionModel().getSelectedItems().size() == 1) {
            MyVertex selectedVertex = (MyVertex) LoadedData.getGraphView().getSelectionModel().getSelectedItems().get(0);
            //TODO: Redoing the entire analysis - not good!
            GraphAnalysis analysis = new GraphAnalysis(LoadedData.getTaxonGraph());

            infoText = "Selected Taxon:\n" + selectedVertex.getTaxonName() + "\nID: " + selectedVertex.getTaxonNode().getTaxonId()
                    + "\nMax. Frequency: " + String.format("%.3f", AnalysisData.getMaximumRelativeFrequencies().get(selectedVertex.getTaxonNode()))
            + "\nNo. of visible edges: " + analysis.getNodeDegrees().get(selectedVertex.getTaxonNode());
        } else if(LoadedData.getGraphView() != null) {
            //TODO: Also redoing the entire analysis...
            GraphAnalysis analysis = new GraphAnalysis(LoadedData.getTaxonGraph());
            ;
            infoText = "Network Overview: \nNo. of visible taxa: " + analysis.getFilteredGraph().getVertices().size()
            + "\nNo. of visible edges: " + analysis.getFilteredGraph().getEdges().size()
            + "\nAverage Degree: " + String.format("%.2f", analysis.getMeanDegree());
        }

        infoTextArea.setText(infoText);
    }

    /**
     * shows the graph in the main view
     *
     * @param taxonGraph
     */
    private void displayGraph(MyGraph<MyVertex, MyEdge> taxonGraph) {
        MyGraphView graphView = new MyGraphView(taxonGraph);
        LoadedData.setGraphView(graphView);
        ViewPane viewPane = new ViewPane(graphView);
        viewPane = new ViewPane(graphView);
        // Bind node hover status text
        statusRightLabel.textProperty().bind(viewPane.hoverInfo);

        // Settings need to be initialized with graphView
        bindGraphSettings(graphView);
        mainViewTab.setContent(viewPane);


        //Bind the showLabels-Checkbox to the visibility properties of the MyVertexView labels
        for (Node node : LoadedData.getGraphView().getMyVertexViewGroup().getChildren()) {
            MyVertexView vertexView = (MyVertexView) node;
            vertexView.getVertexLabel().visibleProperty().bind(showLabelsCheckBox.selectedProperty());
        }

        //call displayInfoText whenever the selection changes
        LoadedData.getGraphView().getSelectionModel().getSelectedItems().addListener((InvalidationListener) e -> displayInfoText());


    }

    /**
     * shows the table in the analysis view
     */
    @FXML
    private void displayCorrelationTable() {
        //Delete whatever's been in the table before
//        analysisTable.getItems().clear();
//        analysisTable.getColumns().clear();
        TableView<String[]> analysisTable = new TableView<>();

        //We want to display correlations and p-Values of every node combination
        double[][] correlationMatrix = AnalysisData.getCorrelationMatrix().getData();
        double[][] pValueMatrix = AnalysisData.getPValueMatrix().getData();
        LinkedList<TaxonNode> taxonList = SampleComparison.getUnifiedTaxonList(
                LoadedData.getSamplesToAnalyze(), AnalysisData.getLevel_of_analysis());

        //We also want to color the two cells with the highest positive/negative correlation
        int[] highestPositiveCorrelationCoordinates = AnalysisData.getHighestPositiveCorrelationCoordinates();
        int[] highestNegativeCorrelationCoordinates = AnalysisData.getHighestNegativeCorrelationCoordinates();

        //Table will consist of strings
        String[][] tableValues = new String[correlationMatrix.length][correlationMatrix[0].length + 1];

        //Add the values as formatted strings
        for (int i = 0; i < tableValues.length; i++) {
            tableValues[i][0] = taxonList.get(i).getName();
            for (int j = 1; j < tableValues[0].length; j++) {
                tableValues[i][j] = String.format("%.3f", correlationMatrix[i][j - 1])
                        + "\n(" + String.format("%.2f", pValueMatrix[i][j - 1]) + ")";
            }
        }

        for (int i = 0; i < tableValues[0].length; i++) {
            String columnTitle;
            if (i > 0) {
                columnTitle = taxonList.get(i - 1).getName();
            } else {
                columnTitle = "";
            }
            TableColumn<String[], String> column = new TableColumn<>(columnTitle);
            final int columnIndex = i;
            column.setCellValueFactory(cellData -> {
                String[] row = cellData.getValue();
                return new SimpleStringProperty(row[columnIndex]);
            });
            analysisTable.getColumns().add(column);

            //First column contains taxon names and should be italic
            if (i == 0)
                column.setStyle("-fx-font-style:italic;");
        }

        for (int i = 0; i < tableValues.length; i++) {
            analysisTable.getItems().add(tableValues[i]);
        }

        //Display table on a new pane
        Stage tableStage = new Stage();
        tableStage.setTitle("Correlation Table");
        Scene tableScene = new Scene(analysisTable);
        tableStage.setScene(tableScene);
        tableStage.show();


    }

    private void displayAnalysisTextsAndGraphs() {

        //Display node with highest frequency
        double highestFrequency = AnalysisData.getHighestFrequency();
        TaxonNode nodeWithHighestFrequency = AnalysisData.getNodeWithHighestFrequency();
        dataStatText.setText("Highest Frequency:\n"
                + nodeWithHighestFrequency.getName() + " (" + String.format("%.3f", highestFrequency) + ")\n");

        //Display nodes with highest positive/negative correlation
        RealMatrix correlationMatrix = AnalysisData.getCorrelationMatrix();
        int[] highestPositiveCorrelationCoordinates = AnalysisData.getHighestPositiveCorrelationCoordinates();
        int[] highestNegativeCorrelationCoordinates = AnalysisData.getHighestNegativeCorrelationCoordinates();
        LinkedList<TaxonNode> taxonList = SampleComparison.getUnifiedTaxonList(LoadedData.getSamplesToAnalyze(), AnalysisData.getLevel_of_analysis());
        TaxonNode hPCNode1 = taxonList.get(highestPositiveCorrelationCoordinates[0]);
        TaxonNode hPCNode2 = taxonList.get(highestPositiveCorrelationCoordinates[1]);
        TaxonNode hNCNode1 = taxonList.get(highestNegativeCorrelationCoordinates[0]);
        TaxonNode hNCNode2 = taxonList.get(highestNegativeCorrelationCoordinates[1]);

        dataStatText.setText(dataStatText.getText() + "\nHighest Positive Correlation:\n"
                + hPCNode1.getName() + " - " + hPCNode2.getName()
                + " (" + String.format("%.3f", correlationMatrix.getEntry(highestPositiveCorrelationCoordinates[0], highestPositiveCorrelationCoordinates[1]))
                + ")\n");
        dataStatText.setText(dataStatText.getText() + "\nHighest Negative Correlation:\n"
                + hNCNode1.getName() + " - " + hNCNode2.getName()
                + " (" + String.format("%.3f", correlationMatrix.getEntry(highestNegativeCorrelationCoordinates[0], highestNegativeCorrelationCoordinates[1]))
                + ")");

        //Generate Data for the pie chart
        frequencyChart.getData().clear();
        HashMap<TaxonNode, Double> averageCounts = SampleComparison.calcAverageCounts(LoadedData.getSamplesToAnalyze(), AnalysisData.getLevel_of_analysis());
        for (TaxonNode taxonNode : averageCounts.keySet()) {
            PieChart.Data data = new PieChart.Data(taxonNode.getName(), averageCounts.get(taxonNode));
            frequencyChart.getData().add(data);
        }


        analysisPane.setVisible(true);


    }

    public void displayGraphAnalysis() {
        //Generate Data for the BarChart
        GraphAnalysis analysis = new GraphAnalysis(LoadedData.getTaxonGraph());
        HashMap<Integer, Double> degreeDistribution = analysis.getDegreeDistribution();
        XYChart.Series<String, Double> degreeSeries = new XYChart.Series<>();

        for (Map.Entry<Integer, Double> entry : degreeDistribution.entrySet()) {
            degreeSeries.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
        }
        degreeDistributionChart.getData().clear();
        degreeDistributionChart.getData().add(degreeSeries);

        //Generate Graph Statistics to display in the TextArea
        HashMap<TaxonNode, Integer> hubs = analysis.getHubs();
        graphStatText.setText("List of Hubs:\n\n");

        //Sort hubs by descending values
        Map<TaxonNode, Integer> hubsSorted = hubs.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));


        for (Map.Entry<TaxonNode, Integer> entry : hubsSorted.entrySet()) {
            graphStatText.setText(graphStatText.getText() + entry.getKey().getName() + " (" + entry.getValue() + ")\n");
        }


    }


    //FILE methods
    @FXML
    /**
     * opens a file chooser and gives the user the possibility to select a file
     * file chooser default location is where save states are
     */ public void openRecentFile() {
        /*openFileWindow();*/
    }

    @FXML
    /**
     * Closes the current project and empties the tree view
     */ public void closeProject() {
        if (treeViewFiles.getRoot() != null) {
            LoadedData.closeProject(treeViewFiles);
            if (mainViewTab.getContent() != null) {
                mainViewTab.setContent(null);
                LoadedData.getGraphView().animationService.cancel();
            }
        }
    }

    @FXML
    public void openId2CountFiles() {
        openFiles(FileType.taxonId2Count);
    }

    @FXML
    public void openReadName2TaxonIdFiles() {
        openFiles(FileType.readName2TaxonId);
    }

    @FXML
    public void openBiomV1Files() {
        openFiles(FileType.biomV1);
    }

    @FXML
    public void openBiomV2Files() {
        openFiles(FileType.biomV1);
    }

    /**
     * Exits the program
     * quitButton
     */
    @FXML
    public void quit() {
        confirmQuit();
    }

    @FXML
    public void toggleMainView() {
        if (!isMainViewMaximized) {
            setPanesWidth(0);
            isMainViewMaximized = true;
        } else {
            setPanesWidth(MAX_WIDTH_OF_SIDEPANES);
            isMainViewMaximized = false;
        }
    }

    //SPECIALIZED METHODS

    private void setPanesWidth(int width) {
        leftPane.setMaxWidth(width);
        rightPane.setMaxWidth(width);
        leftPane.setMinWidth(width);
        rightPane.setMinWidth(width);
    }

    /**
     * sets the openFileChooser directory
     * opens a file to load from
     *
     * @param fileType
     */
    private void openFiles(FileType fileType) {
        FileChooser fileChooser = new FileChooser();
        String fileChooserTitle = "Load from ";

        if ((Boolean) UserSettings.userSettings.get("isDefaultFileChooserLocation")) {
            setDefaultOpenDirectory(fileChooser);
        } else {
            fileChooser.setInitialDirectory(new File((String) UserSettings.userSettings.get("defaultFileChooserLocation")));
        }

        switch (fileType) {
            case taxonId2Count:
                fileChooser.setTitle(fileChooserTitle + "taxonId2Count file");
                break;
            case readName2TaxonId:
                fileChooser.setTitle(fileChooserTitle + "readName2TaxonId file");
                break;
            case biomV1:
                fileChooser.setTitle(fileChooserTitle + "biomV1 file");
                break;
            case biomV2:
                fileChooser.setTitle(fileChooserTitle + "biomV2 file");
        }

        //Choose the file / files

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(getPrimaryStage());

        if (selectedFiles != null) {
            ArrayList<String> namesOfAlreadyLoadedFiles = new ArrayList<>();
            for (File file : selectedFiles) {
                String foundFileName = file.getName();
                if (LoadedData.getOpenFiles() != null && LoadedData.getOpenFiles().contains(foundFileName)) {
                    namesOfAlreadyLoadedFiles.add(foundFileName);
                } else {
                    switch (fileType) {
                        case taxonId2Count:
                            addId2CountFileToTreeView(file);
                            break;
                        case readName2TaxonId:
                            addReadName2TaxonIdFileToTreeView(file);
                            break;
                        case biomV1:
                            addBiomV1FileToTreeView(file);
                            break;
                        case biomV2:
                            addBiomV2FileToTreeView(file);
                            break;
                    }
                }
                //Maybe multiple at once?
                //verifyOpenedFile(selectedFile);
            }
            if (namesOfAlreadyLoadedFiles.size() != 0) {
                showFileAlreadyLoadedAlert(namesOfAlreadyLoadedFiles);
            }
        }
    }

    /**
     * sets the default directory for openings files
     *
     * @param fileChooser
     */
    private void setDefaultOpenDirectory(FileChooser fileChooser) {
        //Set to user directory or go to default if cannot access
        //TODO: osx?
        String userDirectoryString = System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        if (!userDirectory.canRead()) {
            userDirectory = new File("c:/");
            userDirectoryString = "c:/";
        }
        fileChooser.setInitialDirectory(userDirectory);
        UserSettings.userSettings.put("defaultFileChooserLocation", userDirectoryString);
    }

    /**
     * adds opening readNameToTaxId files to the treeview
     *
     * @param file
     */
    private void addReadName2TaxonIdFileToTreeView(File file) {
        ReadName2TaxIdCSVParser readName2TaxIdCSVParser = new ReadName2TaxIdCSVParser(TreePreloadService.taxonTree);

        ArrayList<Sample> samples;

        try {
            samples = readName2TaxIdCSVParser.parse(file.getAbsolutePath());
        } catch (IOException e) {
            showWrongFileAlert();
            return;
        }

        LoadedData.addSamplesToDatabase(samples, treeViewFiles, file.getName());
        activateButtonsOnTheRightPane();
    }

    /**
     * adds opening biom files to the treeview
     *
     * @param file
     */
    private void addBiomV1FileToTreeView(File file) {
        BiomV1Parser biomV1Parser = new BiomV1Parser(TreePreloadService.taxonTree);

        ArrayList<Sample> samples;

        samples = biomV1Parser.parse(file.getAbsolutePath());

        LoadedData.addSamplesToDatabase(samples, treeViewFiles, file.getName());
        activateButtonsOnTheRightPane();
    }

    /**
     * adds opening biom files to the treeview
     *
     * @param file
     */
    private void addBiomV2FileToTreeView(File file) {
        BiomV2Parser biomV2Parser = new BiomV2Parser(TreePreloadService.taxonTree);

        ArrayList<Sample> samples;

        try {
            samples = biomV2Parser.parse(file.getAbsolutePath());
        } catch (IOException e) {
            showWrongFileAlert();
            return;
        }

        LoadedData.addSamplesToDatabase(samples, treeViewFiles, file.getName());
        activateButtonsOnTheRightPane();
    }

    /**
     * adds openings taxIdToCountFiles to the tree view
     *
     * @param file
     */
    private void addId2CountFileToTreeView(File file) {
        TaxonId2CountCSVParser taxonId2CountCSVParser = new TaxonId2CountCSVParser(TreePreloadService.taxonTree);

        ArrayList<Sample> samples;

        try {
            samples = taxonId2CountCSVParser.parse(file.getAbsolutePath());
        } catch (IOException e) {
            showWrongFileAlert();
            return;
        }

        LoadedData.addSamplesToDatabase(samples, treeViewFiles, file.getName());
        activateButtonsOnTheRightPane();
    }


    /**
     * verifies the opened file
     * should be not null and possible to add to the taxonView
     *
     * @param selectedFile
     */
    private void verifyOpenedFile(File selectedFile) {
        boolean isFileFound = selectedFile != null;
        if (!isFileFound) {
            fileNotFoundAlertBox();
        }
        //leftLabel.setText(isFileFound ? selectedFile.getName() : "No such file found.");
        if (isFileFound) {
            //addFileToTreeView(selectedFile);
        }
    }


    @FXML
    /**
     * Collapses all nodes in the treeview element
     */
    public void collapseAll() {
        if (treeViewFiles.getRoot().getChildren().isEmpty()) {
            collapseAllButton.disarm();
            collapseAllButton.setSelected(false);
        } else {
            if (collapseAllButton.isSelected()) {
                for (TreeItem<String> treeItem : treeViewFiles.getRoot().getChildren()) {
                    treeItem.setExpanded(false);
                }
                collapseAllButton.disarm();
                collapseAllButton.setSelected(false);
            } else {
                for (TreeItem<String> treeItem : treeViewFiles.getRoot().getChildren()) {
                    treeItem.setExpanded(true);
                }
                collapseAllButton.setSelected(true);
                collapseAllButton.arm();
            }
        }
    }

    //INITIALIZATIONS

    /**
     * Starts the tree preload service
     */
    private void startTreePreloadService() {
        TreePreloadService treePreloadService = new TreePreloadService();
        treePreloadService.setOnSucceeded(e -> startupSpinner.setProgress(100));
        startupLabel.textProperty().bind(treePreloadService.messageProperty());
        treePreloadService.start();
    }

    /**
     * Initializes the accordion on the right pane
     */
    private void initializeAccordion() {
        preferencesAccordion.setExpandedPane(preferencesAccordion.getPanes().get(0));
    }

    /**
     * Activates the buttons on the right pane
     */
    private void activateButtonsOnTheRightPane() {
        rankChoiceBox.setDisable(false);
    }

    /**
     * Initializes the collapse all button on the left pane
     */
    private void initializeCollapseAllButton() {
        collapseAllButton.setSelected(false);
    }

    /**
     * Initializes the rank selection toggle group and adds a listener to the rank selection
     */
    private void initializeRankChoiceBox() {
        rankChoiceBox.setDisable(true);
        rankChoiceBox.setItems(ranksList);

    }

    /**
     * Hides all the components of the analysis pane, since they should only be displayed when data is loaded
     */
    private void initializeAnalysisPane() {
        analysisPane.setVisible(false);

    }

    /**
     * Sets up listeners that update graph everytime the graph changes
     */
    private void initializeGraphAnalysis() {
        degreeDistributionChart.getXAxis().setLabel("Degree");
        degreeDistributionChart.getYAxis().setLabel("Node Fraction");
        posCorrelationLowerFilterProperty().addListener(observable -> {
            if (LoadedData.getTaxonGraph() != null) {
                LoadedData.getTaxonGraph().filterEdges();
                displayGraphAnalysis();
                displayInfoText();
            }
        });
        posCorrelationUpperFilterProperty().addListener(observable -> {
            if (LoadedData.getTaxonGraph() != null) {
                LoadedData.getTaxonGraph().filterEdges();
                displayGraphAnalysis();
                displayInfoText();
            }
        });
        negCorrelationLowerFilterProperty().addListener(observable -> {
            if (LoadedData.getTaxonGraph() != null) {
                LoadedData.getTaxonGraph().filterEdges();
                displayGraphAnalysis();
                displayInfoText();
            }
        });
        negCorrelationUpperFilterProperty().addListener(observable -> {
            if (LoadedData.getTaxonGraph() != null) {
                LoadedData.getTaxonGraph().filterEdges();
                displayGraphAnalysis();
                displayInfoText();
            }
        });

        maxPValueProperty().addListener(observable -> {
            if (LoadedData.getTaxonGraph() != null) {
                LoadedData.getTaxonGraph().filterEdges();
                displayGraphAnalysis();
                displayInfoText();
            }
        });
        minFrequencyProperty().addListener(observable -> {
            if (LoadedData.getTaxonGraph() != null) {
                LoadedData.getTaxonGraph().filterVertices();
                displayGraphAnalysis();
                displayInfoText();
            }
        });
        maxFrequencyProperty().addListener(observable -> {
            if (LoadedData.getTaxonGraph() != null) {
                LoadedData.getTaxonGraph().filterVertices();
                displayGraphAnalysis();
                displayInfoText();
            }
        });

    }

    /**
     * Sets up listeners to call displayInfoText() whenever it is necessary
     */
    private void initializeInfoPane() {
//        LoadedData.getSamplesToAnalyze().addListener((InvalidationListener) e -> displayInfoText());
        rankChoiceBox.valueProperty().addListener(e -> displayInfoText());
    }

    private void initializeBindings() {
        //First, bind the LoadedData.analyzeAll boolean property to the radio buttons
        LoadedData.analyzeSelectedProperty().bind(compareSelectedSamplesButton.selectedProperty());

        //Since the slider value property is double and the text field property is a string, we need to convert them
        //Defining own class to avoid exceptions
        class MyNumberStringConverter extends NumberStringConverter {
            @Override
            public Number fromString(String value) {
                try {
                    return super.fromString(value);
                } catch (RuntimeException ex) {
                    return 0;
                }
            }
        }
        StringConverter<Number> converter = new MyNumberStringConverter();
        //Bind every slider to its corresponding text field and vice versa
        Bindings.bindBidirectional(minPosCorrelationText.textProperty(), posCorrelationRangeSlider.lowValueProperty(), converter);
        Bindings.bindBidirectional(maxPosCorrelationText.textProperty(), posCorrelationRangeSlider.highValueProperty(), converter);
        Bindings.bindBidirectional(minNegCorrelationText.textProperty(), negCorrelationRangeSlider.lowValueProperty(), converter);
        Bindings.bindBidirectional(maxNegCorrelationText.textProperty(), negCorrelationRangeSlider.highValueProperty(), converter);
        Bindings.bindBidirectional(maxPValueText.textProperty(), maxPValueSlider.valueProperty(), converter);
        Bindings.bindBidirectional(minFrequencyText.textProperty(), frequencyRangeSlider.lowValueProperty(), converter);
        Bindings.bindBidirectional(maxFrequencyText.textProperty(), frequencyRangeSlider.highValueProperty(), converter);

        //Bind the internal filter properties to the slider values
        AnalysisData.posCorrelationLowerFilterProperty().bind(posCorrelationRangeSlider.lowValueProperty());
        AnalysisData.posCorrelationUpperFilterProperty().bind(posCorrelationRangeSlider.highValueProperty());
        AnalysisData.negCorrelationLowerFilterProperty().bind(negCorrelationRangeSlider.lowValueProperty());
        AnalysisData.negCorrelationUpperFilterProperty().bind(negCorrelationRangeSlider.highValueProperty());
        AnalysisData.minFrequencyProperty().bind(frequencyRangeSlider.lowValueProperty());
        AnalysisData.maxFrequencyProperty().bind(frequencyRangeSlider.highValueProperty());
        AnalysisData.maxPValueProperty().bind(maxPValueSlider.valueProperty());

        //The lower value of the negative slider can't be set to -1 from FXML for reasons beyond understanding, so we set it manually
        negCorrelationRangeSlider.setLowValue(-1);

        //We want the graph to be redone if one of the following occurs:
        //1. Radio button switches between "Analyze All" and "Analyze Selected"
        compareSelectedSamplesButton.selectedProperty().addListener(observable -> {
            if ((!compareSelectedSamplesButton.isSelected() || LoadedData.getSelectedSamples().size() >= 3)
                    && rankChoiceBox.getValue() != null)
                startAnalysis();
        });
        //2. Rank selection changes
        rankChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                AnalysisData.setLevel_of_analysis(newValue.toLowerCase());
                startAnalysis();
            }
        });
        //3. Sample selection changes while "Analyze Selected" is selected AND at least three samples are selected
        LoadedData.getSelectedSamples().addListener((InvalidationListener) observable -> {
            if (compareSelectedSamplesButton.isSelected() && LoadedData.getSelectedSamples().size() >= 3) {
                startAnalysis();
            }
        });


    }

    /**
     * Initialize advanced setting elements to only be visible when checkbox is activated
     */
    public void initializeGraphSettings() {

        labelAnimationSpeed.visibleProperty().bind(checkAdvancedGraphSettings.selectedProperty());
        sliderAnimationSpeed.visibleProperty().bind(checkAdvancedGraphSettings.selectedProperty());
        labelEdgeForce.visibleProperty().bind(checkAdvancedGraphSettings.selectedProperty());
        sliderEdgeForce.visibleProperty().bind(checkAdvancedGraphSettings.selectedProperty());
        labelNodeRepulsion.visibleProperty().bind(checkAdvancedGraphSettings.selectedProperty());
        sliderNodeRepulsion.visibleProperty().bind(checkAdvancedGraphSettings.selectedProperty());
        labelStretchParameter.visibleProperty().bind(checkAdvancedGraphSettings.selectedProperty());
        sliderStretchParameter.visibleProperty().bind(checkAdvancedGraphSettings.selectedProperty());
        buttonResetGraphDefaults.setOnAction(e -> setGraphSettingsDefault());
        setGraphSettingsDefault();
    }

    /**
     * Bind graph setting controls to MyGraphView isntance.
     *
     * @param graphView instance to which controls are bound
     */
    public void bindGraphSettings(MyGraphView graphView) {
        // Bind Node Radius Slider to all Nodes in Graph
        for (Node node : graphView.getMyVertexViewGroup().getChildren()) {
            ((MyVertexView) node).getRadiusProperty().bind(sliderNodeRadius.valueProperty());
        }
        // Bind Edge Width Slider to all Edges in Graph
        for (Node node : graphView.getMyEdgeViewGroup().getChildren()) {
            ((MyEdgeView) node).getWidthProperty().bind(sliderEdgeWidth.valueProperty());
        }

        /**buttonPauseAnimation.setOnAction(e -> {
         boolean isRunning = graphView.animationService.isRunning();
         if (isRunning) graphView.animationService.cancel();
         if (!isRunning) graphView.animationService.restart();
         }); **/

        sliderEdgeLength.lowValueProperty().addListener((o, e, n) -> {
            graphView.animationService.setEdgeLengthLow(n.doubleValue());
        });

        sliderEdgeLength.highValueProperty().addListener((o, e, n) -> {
            graphView.animationService.setEdgeLengthHigh(n.doubleValue());
        });

        sliderNodeRepulsion.valueProperty().addListener((o, e, n) -> {
            graphView.animationService.setNodeRepulsion(n.intValue());
        });

        sliderStretchParameter.valueProperty().addListener((o, e, n) -> {
            graphView.animationService.setStretchForce(n.doubleValue());
        });

        sliderEdgeForce.valueProperty().addListener((o, e, n) -> {
            graphView.animationService.setForce(n.doubleValue());
        });


        sliderAnimationSpeed.valueProperty().addListener((o, e, n) -> {
            Double fr = sliderAnimationSpeed.getMax() - n.doubleValue();
            graphView.animationService.setFrameRate(fr.intValue());
        });

        buttonPauseAnimation.selectedProperty().bindBidirectional(graphView.pausedProperty);
    }

    @FXML
    public void resetFilterSettings() {
        posCorrelationRangeSlider.setLowValue(0);
        posCorrelationRangeSlider.setHighValue(1);
        negCorrelationRangeSlider.setLowValue(-1);
        negCorrelationRangeSlider.setHighValue(0);
        maxPValueSlider.setValue(1);
        frequencyRangeSlider.setLowValue(0);
        frequencyRangeSlider.setHighValue(1);
    }

    //ALERTS

    /**
     * creates the file not found alert box
     */
    private void fileNotFoundAlertBox() {
        fileNotFoundAlert = new Alert(Alert.AlertType.ERROR);
        fileNotFoundAlert.setTitle("File not found");
        fileNotFoundAlert.setHeaderText("File not found");
        fileNotFoundAlert.setContentText("Could not find the file you were looking for");

        //style the alert
        DialogPane dialogPane = fileNotFoundAlert.getDialogPane();
        dialogPane.getStylesheets().add("/UI/alertStyle.css");

        Exception fileNotFoundException = new FileNotFoundException("Could not find your selected file");

        //create expandable exception
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        fileNotFoundException.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was: ");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        fileNotFoundAlert.getDialogPane().setExpandableContent(expContent);

        fileNotFoundAlert.showAndWait();
    }

    @FXML
    /**
     *
     * Shows information about the software.
     */ private void showAboutAlert() {
        String information = String.format("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua." + " Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.");
        Text text = new Text(information);
        text.setWrappingWidth(500);
        aboutAlert = new Alert(Alert.AlertType.INFORMATION);
        aboutAlert.setTitle("About NetWork Analysis Tool");
        aboutAlert.setHeaderText("What is the Network Analysis Tool?");
        aboutAlert.getDialogPane().setContent(text);
        aboutAlert.show();
    }

    /**
     * Prompts an alert if the user tries to load a file that does not match the requirements.
     */
    //TODO: If multiple files are wrong, not every file should get its own alert.
    //Could also refactor and use the fileNotFoundAlert
    private void showWrongFileAlert() {
        wrongFileAlert = new Alert(Alert.AlertType.ERROR);
        wrongFileAlert.setTitle("File not loaded");
        wrongFileAlert.setHeaderText("Invalid file.");
        aboutAlert.show();
    }

    /**
     * Prompts an alert that the selected file is already part of the current project.
     */
    private void showFileAlreadyLoadedAlert(ArrayList<String> fileNames) {
        if (fileNames.size() > 1) {
            fileNames = fileNames
                    .stream()
                    .map(string -> "'" + string + "'")
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        String name = String.join(",\n", fileNames);

        String oneFileAlreadyLoaded = "The file '" + name + "' is already loaded in your project.";
        String multipleFilesAlreadyLoaded = "The files\n" + name + "\n are already loaded in your project.";
        fileAlreadyLoadedAlert = new Alert(Alert.AlertType.ERROR);
        fileAlreadyLoadedAlert.setTitle("File not loaded.");
        fileAlreadyLoadedAlert.setContentText(fileNames.size() == 1 ? oneFileAlreadyLoaded : multipleFilesAlreadyLoaded);
        fileAlreadyLoadedAlert.show();
    }

    /**
     * Prompts an alert telling the user that the chosen data is not sufficient for an analysis
     */
    private void showInsufficientDataAlert() {
        insufficientDataAlert = new Alert(Alert.AlertType.ERROR);
        insufficientDataAlert.setTitle("Insufficient data for Analysis");
        insufficientDataAlert.setHeaderText("Not enough data to perform the analysis.");
        insufficientDataAlert.setContentText("Try choosing a more specific rank!");
        insufficientDataAlert.show();
    }

    /**
     * Opens new PopUp Window with Image Export options.
     */
    @FXML
    private void exportImages() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("src/UI/exportImageGUI"));
            loader.setLocation(new URL("file:" + new File("").getCanonicalPath().concat("/src/UI/exportImageGUI.fxml")));
            //Parent root = loader.load();
            ExportImageController exportImageController = new ExportImageController(viewPane);
            //ExportImageController exportImageController = loader.getController();
            //exportImageController.setViewPane(viewPane);
            loader.setController(exportImageController);
            Parent root = loader.load();
            exportImagesStage = new Stage();
            exportImagesStage.setTitle("Export Image");
            Scene exportImageScene = new Scene(root, 300, 200);
            exportImagesStage.setScene(exportImageScene);
            exportImageScene.getStylesheets().add(GlobalConstants.DARKTHEME);
            exportImagesStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * method for the quit button
     * opens an alert box
     * asks whether to save/quit/continue running the program
     */
    private void confirmQuit() {
        confirmQuitAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmQuitAlert.setTitle("Remember to save your files!");
        confirmQuitAlert.setHeaderText("Quit?");
        confirmQuitAlert.setContentText("Do you really want to quit?");

        ButtonType quitButton = new ButtonType("Quit");
        ButtonType saveAndQuitButton = new ButtonType("Save and quit");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmQuitAlert.initModality(Modality.APPLICATION_MODAL);
        confirmQuitAlert.initOwner(getPrimaryStage());
        confirmQuitAlert.getButtonTypes().setAll(quitButton, saveAndQuitButton, cancelButton);

        Optional<ButtonType> result = confirmQuitAlert.showAndWait();

        if (result.get() == quitButton) {
            Platform.exit();
        } else if (result.get() == saveAndQuitButton) {
            confirmQuitAlert.close();
        } else {
            confirmQuitAlert.close();
        }
    }

    @FXML
    /**
     * runs when the optionsButton is clicked
     * opens the Options stage
     *
     */ private void optionsButtonClicked() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(new URL("file:" + new File("").getCanonicalPath().concat("/src/UI/optionsGui.fxml")));
            Parent root = fxmlLoader.load();
            this.optionsStage = new Stage();
            optionsStage.setTitle("Options");
            Scene optionsScene = new Scene(root, 1000, 700);
            optionsStage.setScene(optionsScene);
            optionsScene.getStylesheets().add(GlobalConstants.DARKTHEME);
            optionsStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * handler for Window Events
     * opens an alert which asks whether to quit or not
     * use when setting handlers of the X button
     */
    public EventHandler<WindowEvent> confirmCloseEventHandler = (WindowEvent event) -> {
        confirmQuitAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmQuitAlert.setTitle("Remember to save your files!");
        confirmQuitAlert.setHeaderText("Quit?");
        confirmQuitAlert.setContentText("Do you really want to quit?");

        ButtonType quitButton = new ButtonType("Quit");
        ButtonType saveAndQuitButton = new ButtonType("Save and quit");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmQuitAlert.initModality(Modality.APPLICATION_MODAL);
        confirmQuitAlert.initOwner(getPrimaryStage());
        confirmQuitAlert.getButtonTypes().setAll(quitButton, saveAndQuitButton, cancelButton);

        Optional<ButtonType> result = confirmQuitAlert.showAndWait();

        if (result.get() == quitButton) {
            Platform.exit();
        } else if (result.get() == saveAndQuitButton) {
            Platform.exit();
        } else {
            event.consume();
        }
    };

    /**
     * Sets all slider elements in the graph settings menu to default values
     */
    private void setGraphSettingsDefault() {

        sliderAnimationSpeed.setValue(25);
        sliderEdgeForce.setValue(1.5);
        sliderNodeRepulsion.setValue(10);
        sliderStretchParameter.setValue(0.9);
        sliderNodeRadius.setValue(15);
        sliderEdgeWidth.setValue(5);
        sliderEdgeLength.setLowValue(10);
        sliderEdgeLength.setHighValue(500);
        buttonPauseAnimation.setSelected(false);

    }

    public static Stage getOptionsStage() {
        return optionsStage;
    }
}
