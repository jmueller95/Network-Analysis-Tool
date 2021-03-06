package UI;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.UserSettings;
import org.apache.commons.io.FilenameUtils;
import view.ViewPane;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


/**
 * <h1>This is the controller class for exporting the graph as an image.</h1>
 * <p>
 * The class implements methods for exporting the graph/view pane into the following currently supported formats:
 * gif
 * jpg
 * png
 * </p>
 *
 * @see ViewPane
 *
 * @version Image boundaries should be fixed. Possibly a legend should be added.
 */
public class ExportImageController implements Initializable {

    @FXML
    private TextField imgTextPath;

    @FXML
    private Button imgButtonBrowse;

    @FXML
    private RadioButton imgRadioJpg;

    @FXML
    private RadioButton imgRadioPng;

    @FXML
    private RadioButton imgRadioGif;

    @FXML
    private RadioButton imgRadioAll;

    @FXML
    private RadioButton imgRadioVisible;

    @FXML
    private Button imgButtonQuit;

    @FXML
    private Button imgButtonSave;


    private StringProperty fullPathString = new SimpleStringProperty();
    private String fileName;
    private File imgFolder;
    private File imgFile;
    private ViewPane vP;

    /**
     * Constructor, takes the ViewPane which can later be saved
     * @param vp
     */
    public ExportImageController(ViewPane vp) {
        this.vP = vp;

    }

    /**
     * GUI controller, sets up basic stuff
     */
    private void initializeElements() {
        fullPathString.bindBidirectional(imgTextPath.textProperty());

        try {
            imgFile = new File((String)UserSettings.userSettings.get("imageExportFile"));
        } catch (Exception e) {
            System.err.println("imageExportFile not found in UserSettings");
            imgFile = new File(System.getProperty("user.home") + File.separator + "correlation_graph.png");
        }

        System.out.println("IMGFILE: " + imgFile.getAbsolutePath());

        fullPathString.setValue(imgFile.getPath());
        fullPathString.addListener( (e, o, n) -> {
            try{
                imgFile = new File(n);
                setFileType(imgFile.getName());
            } catch (Exception ex) {
                System.err.println("Incorrect file path");
            }
        });

        imgButtonBrowse.setOnAction(e -> openFileChooser());
        setFileType(imgFile.getName());

        // Activating Radiobuttons changes File extension
        imgRadioGif.setOnAction(e -> setPathExtension(".gif"));
        imgRadioJpg.setOnAction(e -> setPathExtension(".jpg"));
        imgRadioPng.setOnAction(e -> setPathExtension(".png"));

        imgButtonSave.setOnAction(e -> exportImage());
    }

    /**
     * Writes the image to the Output Stream
     */
    public void exportImage() {
        WritableImage wim = new WritableImage((int) vP.getWidth(),(int) vP.getHeight());

        vP.snapshot(null, wim);
        try {
            ImageIO.write( SwingFXUtils.fromFXImage( wim, null ), FilenameUtils.getExtension(fullPathString.get()), imgFile );
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Created output image");

    }

    /**
     * Opens the FileChooser (hopefully) at the previous saved locations - else at home directory
     */
    private void openFileChooser() {
        try {
            imgFile = new File((String)UserSettings.userSettings.get("imageExportFile"));
        } catch (Exception e) {
            System.err.println("imageExportFile not found in UserSettings");
            imgFile = new File(System.getProperty("user.home") + File.separator + "correlation_graph.png");
        }
        Stage fileChooserStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(imgFolder);
        fileChooser.setInitialFileName(fileName);
        fileChooser.getExtensionFilters().add( new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        fullPathString.setValue((fileChooser.showSaveDialog(fileChooserStage)).getAbsolutePath());

        UserSettings.userSettings.put("imageExportFile", fullPathString.getValue());

    }

    /**
     * Detects the filetype of the selected output file based on the file extension and selects radio buttons accordingly
     * @param fileName String with the filename of the selected output file
     * @return
     */
    private String setFileType(String fileName) {
        String ext = FilenameUtils.getExtension(fileName);
        System.out.println("FT: " + ext);
        switch (ext) {
            case "png":
                imgRadioPng.setSelected(true);
                break;
            case "jpg":
                imgRadioJpg.setSelected(true);
                break;
            case "jpeg":
                imgRadioJpg.setSelected(true);
                break;
            case "gif":
                imgRadioGif.setSelected(true);
                break;
            default:
                System.out.println("No Correct File Ending");
                break;
        }
        return ext;
    }

    /**
     * Sets the file extension in the StringProperty containing the filepath
     * @param ext the extension which will be appended
     */
    private void setPathExtension(String ext) {
        fullPathString.setValue(FilenameUtils.removeExtension(fullPathString.getValue()) + ext);
    }


    /**
     * Controller method, calls other initialize method
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initializeElements();
    }

    /**
     * sets ViewPane from external call
     * @param vP
     */
    public void setViewPane(ViewPane vP) {
        this.vP = vP;
    }
}
