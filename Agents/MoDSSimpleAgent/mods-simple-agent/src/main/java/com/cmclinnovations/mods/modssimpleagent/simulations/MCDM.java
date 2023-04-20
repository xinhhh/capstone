package com.cmclinnovations.mods.modssimpleagent.simulations;

import java.io.IOException;
import java.util.List;

import com.cmclinnovations.mods.modssimpleagent.BackendInputFile;
import com.cmclinnovations.mods.modssimpleagent.FileGenerator.FileGenerationException;
import com.cmclinnovations.mods.modssimpleagent.MoDSBackend;
import com.cmclinnovations.mods.modssimpleagent.datamodels.InputMetaData;
import com.cmclinnovations.mods.modssimpleagent.datamodels.Request;
import com.cmclinnovations.mods.modssimpleagent.datamodels.Variable;

public class MCDM extends Simulation {

    public MCDM(Request request, BackendInputFile inputFile, MoDSBackend modsBackend, InputMetaData inputMetaData)
            throws IOException {
        super(request, inputFile, modsBackend, inputMetaData);
    }

    @Override
    public void run() throws IOException {
        populateInputFile();
        generateFiles();
    }

    @Override
    protected void populateAlgorithmNodes(List<Variable> variables) {
        populateMOOAlgorithmNode(Simulation.DEFAULT_MOO_ALGORITHM_NAME, getPrimaryAlgorithm().getName(),
                variables);
        super.populateAlgorithmNodes(variables);
    }

    @Override
    protected void generateFiles() throws FileGenerationException {
        generateMOOAlgDataFiles();
        generateInitialFileFromInputs();
        super.generateFiles();
    }

    @Override
    public Request getResponse() {
        return getMCDMResults();
    }
}