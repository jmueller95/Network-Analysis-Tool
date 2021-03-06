package sampleParser;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;
import model.Sample;
import model.TaxonNode;
import model.TaxonTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <h1>The class implements the parser for BiomV2 files</h1>
 * <p>
 * The class is dependant on a TaxonTree to be available!
 * There are two different versions of the biom files. This ons only works for the version 2!
 * When done parsing the file a list of samples is returned.
 * </p>
 *
 * @see treeParser.TreeParser
 * @see Sample
 */
public class BiomV2Parser implements InputFile{
    private TaxonTree taxonTree;

    /**
     * Constructor for creating a new Parser.
     * @param taxonTree must always be provided to map taxonIDs to tree nodes
     */
    public BiomV2Parser(TaxonTree taxonTree) {
        this.taxonTree = taxonTree;
    }

    @Override
    public ArrayList<Sample> parse(String filepath) throws IOException{
        ArrayList<Sample> sampleList = new ArrayList<>();

        IHDF5SimpleReader reader = HDF5Factory.openForReading(filepath);

        // Get number of Samples in File
        String[] sampleIds = reader.readStringArray("/sample/ids");
        String[] observationIds = reader.readStringArray("/observation/ids");
        int[] indptr = reader.readIntArray("/sample/matrix/indptr");
        int[] indices = reader.readIntArray("/sample/matrix/indices");
        float[] data = reader.readFloatArray("/sample/matrix/data");

        // Loop over Samples
        for (int i = 0; i < sampleIds.length ; i++) {

            // Create new Sample
            Sample newSample = new Sample();
            newSample.setSampleId(sampleIds[i]);

            // Add counts to this sample
            for (int j = indptr[i]; j < indptr[i+1]; j++) {
                TaxonNode node = taxonTree.getNodeForID(Integer.parseInt(observationIds[indices[j]]));
                newSample.getTaxa2CountMap().put(node, Math.round(data[j]));
            }

            // Loop over Metadata-Entries
            for (String metaKey: reader.getGroupMembers("/sample/metadata")) {
                System.out.println(metaKey);
                String metaValue = reader.readStringArray("/sample/metadata/" + metaKey)[i];
                newSample.getMetaData().put(metaKey, metaValue);
            }

            sampleList.add(newSample);
        }

        return sampleList;
    }
}
