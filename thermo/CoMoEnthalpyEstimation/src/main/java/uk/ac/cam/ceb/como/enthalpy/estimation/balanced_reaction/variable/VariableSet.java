/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.variable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.species.Species;

/**
 *
 * @author pb556
 * 
 */

public class VariableSet {
    
    private VariableFactory factory;
    
    private BiMap<Species, Variable> speciesToVariables = HashBiMap.create();
    
    public VariableSet(VariableFactory factory) {
    	
        this.factory = factory;        
    }
    
    public Variable getVariableOf(Species species) {
    	
        if (!speciesToVariables.containsKey(species) && species != null) {
        	
            speciesToVariables.put(species, factory.newVariable());
            
        }
        
        return speciesToVariables.get(species);
    }

    public Species findSpeciesByVariableName(String varName) {
    	
        for (Map.Entry<Species, Variable> entry : speciesToVariables.entrySet()) {
        	
            Species iSDSpecies = entry.getKey();
            Variable variable = entry.getValue();
            
            if (varName.equals(variable.name) || varName.equals(variable.absName)) {
            	
                return iSDSpecies;
            }
        }
        
        return null;
    }
    
    public Set<Variable> getSet() {
        HashSet<Variable> vars = new HashSet<Variable>();
        for (Species s : speciesToVariables.keySet()) {
            vars.add(speciesToVariables.get(s));
        }
        return vars;
    }
}