/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.wrapper.singlecore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.cmclinnovations.data.collections.ObjectPool;

import uk.ac.cam.ceb.como.combination.Combination;
import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.filter.SpeciesFilter;
import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.reaction.Reaction;
import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.reaction.ReactionList;
import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.solver.LPFormat;
import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.solver.LPSolver;
import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.solver.LpSolverException;
import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.solver.Solver;
import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.solver.SolverHelper;
import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.species.Species;
import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.variable.VariableFactory;

/**
 *
 * @author pb556
 */

public class PoolModificationCalculator extends ObjectPoolCalculator {

    protected HashMap<Species, ReactionList> result = null;
    protected PoolModificationCalculator calc = null;
    protected int numResults = 25;
    protected int maxDepth = 100;
    protected int maxSearches = Integer.MAX_VALUE;
    protected ReactionList reactionList = new ReactionList();
    protected int numSearches = 0;

    public PoolModificationCalculator(int numResults) {
        super();
        this.numResults = numResults;
    }

    public PoolModificationCalculator(int numResults, LPSolver solver, LPFormat format, ObjectPool<Species> pool) {
        super(solver, format);
        this.pool = pool;
        result = new HashMap<Species, ReactionList>();
        this.numResults = numResults;
    }

    public PoolModificationCalculator(int numResults, LPSolver solver, LPFormat format) {
    	
        super(solver, format);
        result = new HashMap<Species, ReactionList>();
        this.numResults = numResults;
        
    }

    public PoolModificationCalculator(int numResults, LPSolver solver, LPFormat format, SpeciesFilter filter) {
        super(solver, format, filter);
        result = new HashMap<Species, ReactionList>();
        this.numResults = numResults;
    }

    public PoolModificationCalculator(int numResults, LPSolver solver, LPFormat format, SpeciesFilter filter, ObjectPool<Species> pool) {
        super(solver, format, filter);
        this.pool = pool;
        result = new HashMap<Species, ReactionList>();
        this.numResults = numResults;
    }

    public int getMaximumNumberOfResults() {
        return numResults;
    }

    public int getMaximumSearchDepth() {
        return maxDepth;
    }

    public int getMaximumNumberOfSearchPools() {
        return maxSearches;
    }

    public void setProperties(int numResults, int depth, int searches) {
        setMaximumNumberOfResults(numResults);
        setMaximumNumberOfSearchPools(searches);
        setMaximumSearchDepth(depth);
    }

    public void setMaximumNumberOfResults(int numResults) {
        if (numResults > 0) {
            this.numResults = numResults;
        } else {
            // logger warning!
        }
    }

    public void setMaximumSearchDepth(int depth) {
        if (depth > 0) {
            this.maxDepth = depth;
        } else {
            // logger warning!
        }
    }

    public void setMaximumNumberOfSearchPools(int searches) {
        if (searches > 0) {
            this.maxSearches = searches;
        } else {
            // logger warning!
        }
    }

    //(2)
    @Override
    public void calculate(Collection<Species> targetSpecies) throws Exception {
    	
        HashMap<Species, ReactionList> sol = new HashMap<Species, ReactionList>();
        
        for (Species s : targetSpecies) {
        	
        	System.out.println("PoolModificationCalculator(calculate(Collection<Species> targetSpecies)) - speciesRef :  " + s.getRef());
        	
            reactionList = new ReactionList();
            
            numSearches = 0;
            //(3)
            calculate(0, s, pool);
            
            sol.put(s, reactionList);
            
            Collection<Reaction> reaction = reactionList.getCollection();
            
            //Print products and reactants
            for(Reaction r: reaction) {
            	
            	Map<Species, Double> reactants = r.getReactants();
            	
            	System.out.println("- - - - - - -  -PoolModificationCalculator(calculate (reactants) - - - - - - - - - - - - - ");
            	
            	for(Map.Entry<Species, Double> r1: reactants.entrySet()) {
            		
            		System.out.println("r1.getKey().getRef(): " + r1.getKey().getRef() + " r1.getValue(): " + r1.getValue());
            	}
            	
            	System.out.println("- - - - - - -  - /reactants - - - - - - - - - - - - -  ");
            	
               Map<Species,Double> products = r.getProducts();

           	
               System.out.println("- - - - - - -  -PoolModificationCalculator(calculate (products) - - - - - - - - - - - - - ");

           	
               for(Map.Entry<Species, Double> r2: products.entrySet()) {
            	   
            	   System.out.println("r2.getKey().getRef(): " + r2.getKey().getRef()+ "  r2.getValue(): " + r2.getValue());
               }
            	
               System.out.println("- - - - - - -  - /products - - - - - - - - - - - - -  ");
            	System.out.println();
            }
        }
        
        result = sol;
    }

    @Override
    public void calculate(Species targetSpecies) throws Exception {
        HashSet<Species> set = new HashSet<Species>();
        set.add(targetSpecies);
        
        System.out.println("(PoolModificationCalculator class) Calculate " + targetSpecies.getRef());
        //(1)
        calculate(set);
        
        result.put(targetSpecies, reactionList);
    }
    
    /**
     * 
     * @author NK510
     * @param s1 The HashMap that contains reactant species. 
     * @param s2 The HashMap that contains products species.
     * @return True if species from reactants and products reference (name) species are equal. Size od products and reactants is one.
     *  
     */
    public static boolean equals(Map<Species,Double> s1, Map<Species, Double> s2) {
    
    	
    	for(Map.Entry<Species, Double> r1: s1.entrySet()) {
    		
    		
    	for(Map.Entry<Species, Double> p1: s2.entrySet()) {
    	
    		if((!r1.getKey().getRef().equalsIgnoreCase(p1.getKey().getRef()))) {
    			
    			return false;
    		}
    	}
    	
    	}
    	
    	return true;
    }
    
    //(3)
    protected void calculate(int depth, Species targetSpecies, ObjectPool<Species> refPool) throws LpSolverException {
    	
        numSearches++;
        
        Solver enthalpySolver = new Solver(solver, format, new VariableFactory("v"));
        
        ObjectPool p = SolverHelper.clone(refPool);
        
        List<Species> species = (List<Species>) p.getValidatedObjects();
        
        Collections.shuffle(species);
        
        for (Species s : species) {
        
        enthalpySolver.addReferenceSpecies(s);
        	
        }

        try {
        	
            Reaction r = enthalpySolver.solve(targetSpecies);
           
            /**
             * 
             * @author NK510
             * Checks whether reactants are equal to products.
             * 
             */
            
            //here adds reactions to reaction list
            if (!reactionList.has(r)) {
            	
            	System.out.println("PoolModificationCalculator.equals(r.getProducts(), r.getReactants(): " + PoolModificationCalculator.equals(r.getProducts(), r.getReactants()));
            	
            	if(!PoolModificationCalculator.equals(r.getProducts(), r.getReactants())) {
            	
                reactionList.add(r);
                
            	}                
            }
            
           
           Collection<Reaction> reac = reactionList.getCollection();
            
           //Print products and reactants
           for(Reaction r12: reac) {
            	
            	Map<Species, Double> reactantss = r12.getReactants();
            	
            	System.out.println("- - - - - - -  -PoolModificationCalculator(calculate(int depth, Species targetSpecies, ObjectPool<Species> refPool) - - - - - - - - - - - - - ");
            	
            	for(Map.Entry<Species, Double> r1: reactantss.entrySet()) {
            		
            		System.out.println("r1.getKey().getRef(): " + r1.getKey().getRef() + " r1.getValue(): " + r1.getValue());
            	}
            	
            	System.out.println("- - - - - - -  - /reactants - - - - - - - - - - - - -  ");
            	
               Map<Species,Double> productss = r12.getProducts();

           	
               System.out.println("- - - - - - -  -PoolModificationCalculator(calculate (calculate(int depth, Species targetSpecies, ObjectPool<Species> refPool) - - - - - - - - - - - - - ");

           	
               for(Map.Entry<Species, Double> r2: productss.entrySet()) {
            	   
            	   System.out.println("r2.getKey().getRef(): " + r2.getKey().getRef()+ "  r2.getValue(): " + r2.getValue());
               }
            	
               System.out.println("- - - - - - -  - /products - - - - - - - - - - - - -  ");
            	System.out.println();
            }
            
            System.out.println("Passed");
            
            if (reactionList.size() >= numResults || depth >= maxDepth || numSearches > maxSearches) { 
            	
                return;
            }
            
            ArrayList list = new ArrayList<Species>();
            
            for (Species s : r.getProducts().keySet()) {
            	
            	System.out.println("PollModificationCalculator(calculate(int depth, Species targetSpecies, ObjectPool<Species> refPool) throws LpSolverException) - product: " + s.getRef());
            	
                if (!s.equals(targetSpecies, false)) {
                	
                    list.add(s);
                }
                
                //p.invalidate(s);
            }
            
            for (Species s : r.getReactants().keySet()) {
            	
            	System.out.println("PollModificationCalculator(calculate(int depth, Species targetSpecies, ObjectPool<Species> refPool) throws LpSolverException) - reactant: " + s.getRef());
            	
            	if (!s.equals(targetSpecies, false)) {
            		
            		
            		
                    list.add(s);
                    
                }
                
                //p.invalidate(s);
            }
            
            //List combs = any(list.size(), list);
            List combs = Combination.compute(list);
            
            for (int i = 0; i < combs.size(); i++) {
                List<Species> l = (List<Species>) combs.get(i);
                for (Species s : l) {
                    p.invalidate(s);
                }
                try {
                    calculate(depth++, targetSpecies, p);
                } catch (OutOfMemoryError oome) {
                    System.gc();
                    return;
                } catch (Exception e) {
                    for (int j = i + 1; j < combs.size(); j++) {
                        List<Species> lCheck = (List<Species>) combs.get(i);
                        boolean identified = true;
                        for (Species s : l) {
                            if (!lCheck.contains(s)) {
                                identified = false;
                                break;
                            }
                        }
                        if (identified) {
                            combs.remove(j);
                            j--;
                        }
                    }
                }
                
                for (Species s : l) {
                	
                    p.validate(s);
                    
                }
                
                if (reactionList.size() >= numResults) {
                    return;
                }
            }
        } catch (Exception e) {
        }
    }

    // returns multiple possible reactions for the same target species
    public List<List> any(final int n, final List list) {
        if (n < 0) {
            throw new RuntimeException("Choose Any less than zero : n = " + n);
        }
        if (n > list.size()) {
            throw new RuntimeException("Choose Any larger than the size of the list : n = " + n);
        }
        List<List> all = new ArrayList<List>();
        if (n == 0) {
            List sublist = new ArrayList();
            all.add(sublist);
            return all;
        } else if (n == 1) {
            for (Object l : list) {
                List sublist = new ArrayList();
                sublist.add(l);
                all.add(sublist);
            }
            return all;
        } else {
            for (int i = 0; i < list.size(); i++) {
                List rest = new ArrayList(list);
                Object left = list.get(i);
                List<List> rlist = any(n - 1, rest);
                for (List l : rlist) {
                    l.add(0, left);
                    all.add(l);
                }
            }
            return all;
        }
    }

    @Override
    public void calculate() throws Exception {
        calculate(pool.getInvalidatedObjects());
    }

    @Override
    public Object get() {
        return result;
    }
}