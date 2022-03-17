from postprocagent.hypo_rxn.hypo_rxn import *

TIME_TEMPERATURE_ECO_SCORE_FACTOR = 0.002
AMBIENT_TEMPERATURE_DEGREECELSIUS = 25
ECO_SCORE_BASE_VALUE = 100

def calculate_yield(hypo_reactor: HypoReactor, hypo_end_stream: HypoEndStream) -> PerformanceIndicator:
    yield_limiting_species = retrieve_yield_limiting_species(hypo_reactor)
    target_product_species = retrieve_product_species(hypo_end_stream)

    yield_limiting_conc = utils.unit_conversion_return_value_dq(yield_limiting_species.run_conc, utils.UNIFIED_CONCENTRATION_UNIT)
    prod_run_conc = utils.unit_conversion_return_value_dq(target_product_species.run_conc, utils.UNIFIED_CONCENTRATION_UNIT)

    _yield = prod_run_conc / yield_limiting_conc

    # pi_yield = PerformanceIndicator(
    #     instance_iri=,
    #     clz=ONTORXN_YIELD,
    #     namespace_for_init=,
    #     objPropWithExp=[],
    #     hasValue=OM_Measure(
    #         instance_iri=,
    #         hasUnit=OM_ONE,
    #         hasNumericalValue=_yield
    #     ),
    #     positionalID= # TODO generated value by DoE Agent?
    # )

    return #pi_yield

def calculate_conversion(hypo_reactor: HypoReactor, hypo_end_stream: HypoEndStream) -> PerformanceIndicator:
    # NOTE here the conversion is calculated based on the yield limiting species
    yield_limiting_species = retrieve_yield_limiting_species(hypo_reactor)
    _species_in_end_stream = [species for species in hypo_end_stream.component if species.species_iri == yield_limiting_species.species_iri][0]

    yield_limiting_conc = utils.unit_conversion_return_value_dq(yield_limiting_species.run_conc, utils.UNIFIED_CONCENTRATION_UNIT)
    unreacted_conc = utils.unit_conversion_return_value_dq(_species_in_end_stream.run_conc, utils.UNIFIED_CONCENTRATION_UNIT)

    _conversion = 1 - unreacted_conc / yield_limiting_conc

    # pi_conversion = PerformanceIndicator(
    #     instance_iri=,
    #     clz=ONTORXN_CONVERSION,
    #     namespace_for_init=,
    #     objPropWithExp=,
    #     hasValue=OM_Measure(
    #         instance_iri=,
    #         hasUnit=OM_ONE,
    #         hasNumericalValue=_conversion
    #     ),
    #     positionalID= # TODO generated value by DoE Agent?
    # )

    return #pi_conversion

def calculate_space_time_yield(hypo_reactor: HypoReactor, hypo_end_stream: HypoEndStream) -> PerformanceIndicator:
    target_product_species = retrieve_product_species(hypo_end_stream)

    prod_run_mass = utils.unit_conversion_return_value_dq(target_product_species._run_mass, utils.UNIFIED_MASS_UNIT)
    residence_time = utils.unit_conversion_return_value_dq(hypo_reactor.residence_time, utils.UNIFIED_TIME_UNIT)
    reactor_volume = utils.unit_conversion_return_value_dq(hypo_reactor.reactor_volume, utils.UNIFIED_VOLUME_UNIT)

    _sty = prod_run_mass / residence_time / reactor_volume

    # pi_sty = PerformanceIndicator(
    #     instance_iri=,
    #     clz=ONTORXN_SPACETIMEYIELD,
    #     namespace_for_init=,
    #     objPropWithExp=,
    #     hasValue=OM_Measure(
    #         instance_iri=,
    #         hasUnit=utils.UNIFIED_SPACETIMEYIELD_UNIT,
    #         hasNumericalValue=_sty
    #     ),
    #     positionalID= # TODO generated value by DoE Agent?
    # )

    return #pi_sty

def calculate_eco_score(hypo_reactor: HypoReactor, hypo_end_stream: HypoEndStream) -> PerformanceIndicator:
    residence_time = utils.unit_conversion_return_value_dq(hypo_reactor.residence_time, utils.UNIFIED_TIME_UNIT)
    reactor_temperature = utils.unit_conversion_return_value_dq(hypo_reactor.reactor_temperature, OM_DEGREECELSIUS)
    time_temperature_eco_score = TIME_TEMPERATURE_ECO_SCORE_FACTOR * residence_time * (
        (reactor_temperature-AMBIENT_TEMPERATURE_DEGREECELSIUS)^2 / abs(reactor_temperature-AMBIENT_TEMPERATURE_DEGREECELSIUS))
    total_run_eco_score = retrieve_total_run_eco_score(hypo_reactor)
    _eco_score = ECO_SCORE_BASE_VALUE - time_temperature_eco_score - total_run_eco_score

    # pi_eco_score = PerformanceIndicator(
    #     instance_iri=,
    #     clz=ONTORXN_ECOSCORE,
    #     namespace_for_init=,
    #     objPropWithExp=,
    #     hasValue=OM_Measure(
    #         instance_iri=,
    #         hasUnit=utils.UNIFIED_ECOSCORE_UNIT,
    #         hasNumericalValue=_eco_score
    #     ),
    #     positionalID= # TODO generated value by DoE Agent?
    # )

    return #pi_eco_score

def calculate_enviromental_factor(hypo_reactor: HypoReactor, hypo_end_stream: HypoEndStream) -> PerformanceIndicator:
    target_product_species = retrieve_product_species(hypo_end_stream)
    all_reactant = [s for inlet in hypo_reactor.inlet_run_stream for s in inlet.solute if s._is_reactant]
    all_solvent = [inlet.solvent for inlet in hypo_reactor.inlet_run_stream]
    reactant_and_solvent = all_reactant + all_solvent

    total_reac_n_solvent_run_mass = sum([utils.unit_conversion_return_value_dq(s._run_mass, utils.UNIFIED_MASS_UNIT) for s in reactant_and_solvent])
    prod_run_mass = utils.unit_conversion_return_value_dq(target_product_species._run_mass, utils.UNIFIED_MASS_UNIT)

    _e_factor = prod_run_mass / (total_reac_n_solvent_run_mass - prod_run_mass)

    # pi_e_factor = PerformanceIndicator(
    #     instance_iri=,
    #     clz=ONTORXN_ENVIRONMENTALFACTOR,
    #     namespace_for_init=,
    #     objPropWithExp=,
    #     hasValue=OM_Measure(
    #         instance_iri=,
    #         hasUnit=utils.UNIFIED_ENVIRONMENTFACTOR_UNIT,
    #         hasNumericalValue=_e_factor
    #     ),
    #     positionalID= # TODO generated value by DoE Agent?
    # )

    return #pi_e_factor

def calculate_run_material_cost(hypo_reactor: HypoReactor, hypo_end_stream: HypoEndStream) -> PerformanceIndicator:
    all_reactant = [s for inlet in hypo_reactor.inlet_run_stream for s in inlet.solute if s._is_reactant]
    all_solvent = [inlet.solvent for inlet in hypo_reactor.inlet_run_stream]
    reactant_and_solvent = all_reactant + all_solvent
    # NOTE here the unit of the _run_volume and def_cost should already be standardised at creation of each HypoStreamSpecies instance
    # NOTE therefore the unit conversion is omitted
    _run_material_cost = sum([s._run_volume.hasNumericalValue * s.def_cost.hasNumericalValue for s in reactant_and_solvent])

    # pi_run_material_cost = PerformanceIndicator(
    #     instance_iri=,
    #     clz=ONTORXN_RUNMATERIALCOST,
    #     namespace_for_init=,
    #     objPropWithExp=,
    #     hasValue=OM_Measure(
    #         instance_iri=,
    #         hasUnit=utils.UNIFIED_RUN_MATERIAL_COST_UNIT,
    #         hasNumericalValue=_run_material_cost
    #     ),
    #     positionalID= # TODO generated value by DoE Agent?
    # )

    return #pi_run_material_cost

def retrieve_yield_limiting_species(hypo_reactor: HypoReactor) -> HypoStreamSpecies:
    all_inlet_stream = hypo_reactor.inlet_run_stream
    all_reactant_species = {r.species_iri:r for reac in all_inlet_stream for r in reac.solute if r._is_reactant}
    all_reactant_conc = {all_reactant_species[s].species_iri:all_reactant_species[s].run_conc for s in all_reactant_species}
    yield_limiting_conc = min([utils.unit_conversion_return_value_dq(all_reactant_conc[dq], utils.UNIFIED_CONCENTRATION_UNIT) for dq in all_reactant_conc])
    yield_limiting_species_lst = [species for species in all_reactant_conc if all_reactant_conc[species].hasNumericalValue == yield_limiting_conc]
    if len(yield_limiting_species_lst) > 1:
        raise NotImplementedError("Processing multiple yield limiting species in the reactor input streams is NOT yet supported, the HypoReactor: %s" % (
            str(hypo_reactor)))
    else:
        yield_limiting_species_iri = yield_limiting_species_lst[0]
    return all_reactant_species[yield_limiting_species_iri]

def retrieve_product_species(hypo_end_stream: HypoEndStream) -> HypoStreamSpecies:
    all_target_product = [comp for comp in hypo_end_stream.component if comp._is_target_product]

    if len(all_target_product) > 1:
        raise NotImplementedError("Multiple TargetProduct in the end stream is NOT yet supported: %s" % (str(all_target_product)))
    elif len(all_target_product) < 1:
        raise Exception("No TargetProduct identified in the end stream %s" % (str(hypo_end_stream)))
    else:
        target_product = all_target_product[0]
    return target_product

def retrieve_total_run_eco_score(hypo_reactor: HypoReactor):
    all_solute = [s for inlet in hypo_reactor.inlet_run_stream for s in inlet.solute]
    all_solvent = [inlet.solvent for inlet in hypo_reactor.inlet_run_stream]
    all_species = all_solute + all_solvent
    total_run_eco_score = sum([utils.unit_conversion_return_value_dq(s.def_eco_score, utils.UNIFIED_ECOSCORE_UNIT) for s in all_species])
    return total_run_eco_score