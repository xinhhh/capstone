var scenario;
var prefix = "http://localhost:8080";
iriofnetwork = 'http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork';
var branchInfo = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
+ "SELECT ?entity ?V_R ?V_X ?V_B ?V_RateA ?V_RateB ?V_RateC ?V_RatioCoeff ?V_Angle ?V_Angle_unit ?V_Status ?V_AngleMin ?V_AngleMin_unit ?V_AngleMax ?V_AngleMax_unit"

+ "WHERE {?entity  a  j1:UndergroundCable  ." 
+ "?entity   j2:isModeledBy ?model ."
+ "?model   j5:hasModelVariable ?res ." 

+ "?res  a  j3:R  ." 
+ "?res  j2:hasValue ?vres ."
+ "?vres   j2:numericalValue ?V_R ." // resistance
+ "?vres   j2:hasUnitOfMeasure ?V_R_unit ." // resistance

+ "?model   j5:hasModelVariable ?rea ." 
+ "?rea  a  j3:X  ." 
+ "?rea  j2:hasValue ?vrea ."
+ "?vrea   j2:numericalValue ?V_X ." // reactance

+ "?model   j5:hasModelVariable ?sus ." 
+ "?sus  a  j3:B  ." 
+ "?sus  j2:hasValue ?vsus ."
+ "?vsus   j2:numericalValue ?V_B ." // susceptance

+ "?model   j5:hasModelVariable ?ratea ." 
+ "?ratea  a  j3:RateA  ." 
+ "?ratea  j2:hasValue ?vratea ."
+ "?vratea   j2:numericalValue ?V_RateA ." // rateA

+ "?model   j5:hasModelVariable ?rateb ." 
+ "?rateb  a  j3:RateB  ." 
+ "?rateb  j2:hasValue ?vrateb ."
+ "?vrateb   j2:numericalValue ?V_RateB ." // rateB

+ "?model   j5:hasModelVariable ?ratec ." 
+ "?ratec  a  j3:RateC  ." 
+ "?ratec  j2:hasValue ?vratec ."
+ "?vratec   j2:numericalValue ?V_RateC ." // rateC

+ "?model   j5:hasModelVariable ?ratio ." 
+ "?ratio  a  j3:RatioCoefficient  ."
+ "?ratio  j2:hasValue ?vratio ." 
+ "?vratio   j2:numericalValue ?V_RatioCoeff ." // ratio

+ "?model   j5:hasModelVariable ?ang ." 
+ "?ang  a  j3:Angle  ." 
+ "?ang  j2:hasValue ?vang ."
+ "?vang   j2:numericalValue ?V_Angle ." // angle
+ "?vang   j2:hasUnitOfMeasure ?V_Angle_unit ." // angle

+ "?model   j5:hasModelVariable ?stat ." 
+ "?stat  a  j3:BranchStatus ." 
+ "?stat  j2:hasValue ?vstat ."
+ "?vstat   j2:numericalValue ?V_Status ." // status

+ "?model   j5:hasModelVariable ?angmin ." 
+ "?angmin  a  j3:AngleMin  ."
+ "?angmin  j2:hasValue ?vangmin ." 
+ "?vangmin   j2:numericalValue ?V_AngleMin ." // anglemin
+ "?vangmin   j2:hasUnitOfMeasure ?V_AngleMin_unit ." // anglemin

+ "?model   j5:hasModelVariable ?angmax ." 
+ "?angmax  a  j3:AngleMax  ."
+ "?angmax  j2:hasValue ?vangmax ." 
+ "?vangmax   j2:numericalValue ?V_AngleMax ." // anglemax
+ "?vangmax   j2:hasUnitOfMeasure ?V_AngleMax_unit ." // anglemin

+ "}";
var busInfo = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
    + "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
    + "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
    + "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
    + "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
    + "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
    + "SELECT ?entity ?V_Pd ?V_Pd_unit ?V_Pd_Gen ?V_Pd_Gen_unit ?V_Gd ?V_Gd_unit ?V_Gd_Gen ?V_Gd_Gen_unit " 
    + "?V_Gs ?V_Bs ?V_Vm ?V_Vm_unit ?V_Va ?V_Va_unit ?V_BaseKV ?V_BaseKV_unit ?V_VmMax ?V_VmMax_unit ?V_VmMin ?V_VmMin_unit ?V_x ?V_x_unit ?V_y ?V_y_unit "

    + "WHERE {?entity  a  j1:BusNode  ." 
    + "?entity   j2:isModeledBy ?model ."

    + "?model   j5:hasModelVariable ?Pd ." 
    + "?Pd  a  j3:PdBus  ." 
    + "?Pd  j2:hasValue ?vpd ."
    + "?vpd   j2:numericalValue ?V_Pd ." // pd
    + "?vpd   j2:hasUnitOfMeasure ?V_Pd_unit ." // pd unit

    + "?model   j5:hasModelVariable ?PdGen ." 
    + "?PdGen  a  j3:PdGen  ." 
    + "?PdGen  j2:hasValue ?vpdgen ."
    + "?vpdgen   j2:numericalValue ?V_Pd_Gen ." // pdgen
    + "?vpdgen   j2:hasUnitOfMeasure ?V_Pd_Gen_unit ." // pdgen
    
    + "?model   j5:hasModelVariable ?Gd ." 
    + "?Gd  a  j3:GdBus  ." 
    + "?Gd  j2:hasValue ?vgd ."
    + "?vgd   j2:numericalValue ?V_Gd ." // Gd
    + "?vgd   j2:hasUnitOfMeasure ?V_Gd_unit ." // Gd
    
    + "?model   j5:hasModelVariable ?Gd_Gen ." 
    + "?Gd_Gen  a  j3:GdGen  ." 
    + "?Gd_Gen  j2:hasValue ?vgdgen ."
    + "?vgdgen   j2:numericalValue ?V_Gd_Gen ." // Gdgen
    + "?vgdgen   j2:hasUnitOfMeasure ?V_Gd_Gen_unit ." // Gdgen


    + "?model   j5:hasModelVariable ?Gsvar ." 
    + "?Gsvar  a  j3:Gs  ." 
    + "?Gsvar  j2:hasValue ?vGsvar ."
    + "?vGsvar   j2:numericalValue ?V_Gs ." // Gs (has no unit)

    + "?model   j5:hasModelVariable ?Bsvar ." 
    + "?Bsvar  a  j3:Bs  ." 
    + "?Bsvar  j2:hasValue ?vBsvar ."
    + "?vBsvar   j2:numericalValue ?V_Bs ." // Bs (has no unit)

    + "?model   j5:hasModelVariable ?VM ." 
    + "?VM  a  j3:Vm  ." 
    + "?VM  j2:hasValue ?vVM ."
    + "?vVM   j2:numericalValue ?V_Vm ." // Vm
    + "?vVM   j2:hasUnitOfMeasure ?V_Vm_unit ." // Vm

    + "?model   j5:hasModelVariable ?VA ." 
    + "?VA  a  j3:Va  ." 
    + "?VA  j2:hasValue ?vVA ."
    + "?vVA   j2:numericalValue ?V_Va ." // Va
    + "?vVA   j2:hasUnitOfMeasure ?V_Va_unit ." // Va

    + "?model   j5:hasModelVariable ?BKV ." 
    + "?BKV  a  j3:baseKV  ." 
    + "?BKV  j2:hasValue ?vBKV ."
    + "?vBKV   j2:numericalValue ?V_BaseKV ." // Base KV
    + "?vBKV   j2:hasUnitOfMeasure ?V_BaseKV_unit ." // Base KV
    
    + "?model   j5:hasModelVariable ?vmaxvar ." 
    + "?vmaxvar  a  j3:VmMax  ."
    + "?vmaxvar  j2:hasValue ?vvmaxvar ." 
    + "?vvmaxvar   j2:numericalValue ?V_VmMax ." // Vmax
    + "?vvmaxvar   j2:hasUnitOfMeasure ?V_VmMax_unit ." // Vmax

    + "?model   j5:hasModelVariable ?vminvar ." 
    + "?vminvar  a  j3:VmMin  ."
    + "?vminvar  j2:hasValue ?vvminvar ." 
    + "?vvminvar   j2:numericalValue ?V_VmMin ." // Vmin
    + "?vvminvar   j2:hasUnitOfMeasure ?V_VmMin_unit ." // Vmin
    
    + "?coorsys  j7:hasProjectedCoordinate_y  ?y  ." 
    + "?y  j2:hasValue ?vy ." 
    + "?vy  j2:numericalValue ?V_y ."//longitude
    + "?vy  j2:hasUnitOfMeasure ?V_y_unit ."//longitude

    + "?coorsys  j7:hasProjectedCoordinate_x  ?x  ."
    + "?x  j2:hasValue ?vx ." 
    + "?vx  j2:numericalValue ?V_x ."//latitude
    + "?vx  j2:hasUnitOfMeasure ?V_x_unit ."//latitude
    

    + "}";
var genInfo = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
    + "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
    + "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
    + "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
    + "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
    + "PREFIX j9:<http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_performance.owl#> "
    + "PREFIX technical_system:<http://www.theworldavatar.com/ontology/ontocape/upper_level/technical_system.owl#> "
    + "SELECT ?entity ?V_BusNumber ?V_PGen ?V_PGen_unit ?V_QGen ?V_QGen_unit ?V_Qmax ?V_Qmax_unit ?V_Qmin ?V_Qmin_unit ?V_Vg ?V_mBase ?V_mBase_unit "
    + "?V_Pmax ?V_Pmax_unit ?V_Pmin ?V_Pmin_unit ?V_Pc1 ?V_Pc2 ?V_Qc1Min ?V_Qc1Max "
    + "?V_Qc2Min ?V_Qc2Max ?V_Ramp_agc ?V_Ramp_10 ?V_Ramp_30 ?V_Ramp_q ?V_APF "
    + "?V_StartupCost ?V_ShutdownCost ?V_genCostn ?V_genCostn1 ?V_genCostn2 ?V_genCostc0 "

    + "WHERE {?entity  a  j1:PowerGenerator  ."
    + "?entity   j2:isModeledBy ?model ."

    + "?model   j5:hasModelVariable ?num ." 
    + "?num  a  j3:BusNumber  ." 
    + "?num  j2:hasValue ?vnum ."
    + "?vnum   j2:numericalValue ?V_BusNumber ." // number

    + "?model   j5:hasModelVariable ?Pg ." 
    + "?Pg  a  j3:Pg  ." 
    + "?Pg  j2:hasValue ?vpg ."
    + "?vpg   j2:numericalValue ?V_PGen ." // pg
    + "?vpg   j2:hasUnitOfMeasure ?V_PGen_unit ." // pg

    + "?model   j5:hasModelVariable ?Qg ." 
    + "?Qg  a  j3:Qg  ." 
    + "?Qg  j2:hasValue ?vqg ."
    + "?vqg   j2:numericalValue ?VQ_Gen ." // qg
    + "?vqg   j2:hasUnitOfMeasure ?VQ_Gen_unit ." // qg

    + "?model   j5:hasModelVariable ?qmax ." 
    + "?qmax  a  j3:QMax  ." 
    + "?qmax  j2:hasValue ?vqmax ."
    + "?vqmax   j2:numericalValue ?V_Qmax ." // qmax
    + "?vqmax   j2:hasUnitOfMeasure ?V_Qmax_unit ." // qmax

    + "?model   j5:hasModelVariable ?qmin ." 
    + "?qmin  a  j3:QMin  ." 
    + "?qmin  j2:hasValue ?vqmin ."
    + "?vqmin   j2:numericalValue ?V_Qmin ." // qmin
    + "?vqmin   j2:hasUnitOfMeasure ?V_Qmin_unit ." // qmin

    + "?model   j5:hasModelVariable ?Vg ." 
    + "?Vg  a  j3:Vg  ." 
    + "?Vg  j2:hasValue ?vVg ."
    + "?vVg   j2:numericalValue ?V_Vg ." // vg

    + "?model   j5:hasModelVariable ?mbase ." 
    + "?mbase  a  j3:mBase  ." 
    + "?mbase  j2:hasValue ?vmbase ."
    + "?vmbase   j2:numericalValue ?V_mBase ." // mbase
    + "?vmbase   j2:hasUnitOfMeasure ?V_mBase_unit ." // mbase

    + "?model   j5:hasModelVariable ?pmax ." 
    + "?pmax  a  j3:PMax  ." 
    + "?pmax  j2:hasValue ?vpmax ."
    + "?vpmax   j2:numericalValue ?V_Pmax ." // pmax
    + "?vpmax   j2:hasUnitOfMeasure  ?V_Pmax_unit ." // pmax

    + "?model   j5:hasModelVariable ?pmin ." 
    + "?pmin  a  j3:PMin  ." 
    + "?pmin  j2:hasValue ?vpmin ."
    + "?vpmin   j2:numericalValue ?V_Pmin ." // pmin
    + "?vpmin   j2:hasUnitOfMeasure?V_Pmin_unit ." // pmin

    + "?model   j5:hasModelVariable ?pc1 ." 
    + "?pc1  a  j3:Pc1  ." 
    + "?pc1  j2:hasValue ?vpc1 ."
    + "?vpc1   j2:numericalValue ?V_Pc1 ." // pc1

    + "?model   j5:hasModelVariable ?pc2 ." 
    + "?pc2  a  j3:Pc2  ." 
    + "?pc2  j2:hasValue ?vpc2 ."
    + "?vpc2   j2:numericalValue ?V_Pc2 ." // pc2

    + "?model   j5:hasModelVariable ?qc1min ." 
    + "?qc1min  a  j3:QC1Min  ."
    + "?qc1min  j2:hasValue ?vqc1min ." 
    + "?vqc1min   j2:numericalValue ?V_Qc1Min ." // qc1min

    + "?model   j5:hasModelVariable ?Qc1max ." 
    + "?Qc1max  a  j3:QC1Max  ."
    + "?Qc1max  j2:hasValue ?vQc1max ." 
    + "?vQc1max   j2:numericalValue ?V_Qc1Max ." // qc1max

    + "?model   j5:hasModelVariable ?qc2min ." 
    + "?qc2min  a  j3:QC2Min  ."
    + "?qc2min  j2:hasValue ?vqc2min ."
    + "?vqc2min   j2:numericalValue ?V_Qc2Min ." // qc2min

    + "?model   j5:hasModelVariable ?Qc2max ."
    + "?Qc2max  a  j3:QC2Max  ."
    + "?Qc2max  j2:hasValue ?vQc2max ." 
    + "?vQc2max   j2:numericalValue ?V_Qc2Max ." // qc2max

    + "?model   j5:hasModelVariable ?rampagc ." 
    + "?rampagc  a  j3:Rampagc  ."
    + "?rampagc  j2:hasValue ?vrampagc ." 
    + "?vrampagc   j2:numericalValue ?V_Ramp_agc ." // rampagc

    + "?model   j5:hasModelVariable ?ramp10 ." 
    + "?ramp10  a  j3:Ramp10  ."
    + "?ramp10  j2:hasValue ?vramp10 ."
    + "?vramp10   j2:numericalValue ?V_Ramp_10 ." // ramp10

    + "?model   j5:hasModelVariable ?ramp30 ." 
    + "?ramp30  a  j3:Ramp30  ."
    + "?ramp30  j2:hasValue ?vramp30 ." 
    + "?vramp30   j2:numericalValue ?V_Ramp_30 ." // ramp30

    + "?model   j5:hasModelVariable ?rampq ." 
    + "?rampq  a  j3:Rampq  ." 
    + "?rampq  j2:hasValue ?vrampq ."
    + "?vrampq   j2:numericalValue ?V_Ramp_q ." // rampq

    + "?model   j5:hasModelVariable ?apf ."
    + "?apf  a  j3:APF  ." 
    + "?apf  j2:hasValue ?vapf ."
    + "?vapf   j2:numericalValue ?V_APF ." // apf
    
    + "?model   j5:hasModelVariable ?startup ." 
    + "?startup  a  j3:StartCost  ."
    + "?startup  j2:hasValue ?vstartup ." 
    + "?vstartup   j2:numericalValue ?V_StartupCost ." //startup cost

    + "?model   j5:hasModelVariable ?shutdown ." 
    + "?shutdown  a  j3:StopCost  ."
    + "?shutdown  j2:hasValue ?vshutdown ." 
    + "?vshutdown   j2:numericalValue ?V_ShutdownCost ."  //shutdown cost
    
    + "?model   j5:hasModelVariable ?gencostn ." 
    + "?gencostn  a  j3:genCostn  ."
    + "?gencostn  j2:hasValue ?vgencostn ." 
    + "?vgencostn   j2:numericalValue ?V_genCostn ." //genCostn

    + "?model   j5:hasModelVariable ?gencostn1 ." 
    + "?gencostn1  a  j3:genCostcn-1  ."
    + "?gencostn1  j2:hasValue ?vgencostn1 ." 
    + "?vgencostn1   j2:numericalValue ?V_genCostn1 ." //genCostn-1

    + "?model   j5:hasModelVariable ?gencostn2 ." 
    + "?gencostn2  a  j3:genCostcn-2  ."
    + "?gencostn2  j2:hasValue ?vgencostn2 ." 
    + "?vgencostn2   j2:numericalValue ?V_genCostn2 ."//genCostn-2

    + "?model   j5:hasModelVariable ?gencostc ." 
    + "?gencostc  a  j3:genCostc0  ."
    + "?gencostc  j2:hasValue ?vgencostc ." 
    + "?vgencostc   j2:numericalValue ?V_genCostc0 ." //genCostc0

    + "}";

var genInfo2 = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
    + "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
    + "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
    + "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
    + "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
    + "PREFIX j9:<http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_performance.owl#> "
    + "PREFIX technical_system:<http://www.theworldavatar.com/ontology/ontocape/upper_level/technical_system.owl#> "
    + "SELECT ?entity ?V_x ?V_x_unit ?V_y ?V_y_unit ?V_Actual_CO2_Emission ?V_Actual_CO2_Emission_unit "
    
    + "WHERE {?entity  a  j1:PowerGenerator  ."
    + "?entity   technical_system:realizes ?generation ."
    + "?generation j9:hasEmission ?emission ." 
    + "?emission a j9:Actual_CO2_Emission ."
    + "?emission   j2:hasValue ?valueemission ."
    + "?valueemission   j2:numericalValue ?V_Actual_CO2_Emission ." //
    + "?valueemission   j2:hasUnitOfMeasure ?V_Actual_CO2_Emission_unit ." //

    + "?coorsys  j7:hasProjectedCoordinate_y  ?y  ." 
    + "?y  j2:hasValue ?vy ." 
    + "?vy  j2:numericalValue ?V_y ."
    + "?vy  j2:hasUnitOfMeasure ?V_y_unit ."

    + "?coorsys  j7:hasProjectedCoordinate_x  ?x  ."
    + "?x  j2:hasValue ?vx ." 
    + "?vx  j2:numericalValue ?V_x ."//longitude
    + "?vx  j2:hasUnitOfMeasure ?V_x_unit ."//longitude
    

    + "}";

    (function PPMapAlt(){
		
    var ppMap = new PopupMap({useCluster:true});
    var anotherURL1 = 'https://sites.google.com/site/kmlfilescares/kmltest1/testfinalBASE.kml';
    var anotherURL2 = 'https://sites.google.com/site/kmlfilescares/kmltest1/testfinaltestPOWSYSNuclearStartSimulationAndProcessResultAgentCallForTestScenario.kml';
    // var anotherURL1 =  'http://theworldavatar.com/OntoEN/testfinalBASE.kml';
    // var anotherURL2 = 'http://theworldavatar.com/OntoEN/testfinaltestPOWSYSNuclearStartSimulationAndProcessResultAgentCallForTestScenario.kml';
    var val =  parseFloat($("#co2Value").text());
    setInterval(function(){
        distotalemission(arrSum);
    }, 5000);
    
    $(document).on('input', 'input', function () {//when user makes input
        console.log("input changed");
        cleanMsg();
        let el = $(this), value = el.val();
        if (value === "") { 
            return;
        }

        let attrid = el.attr("id");

        if (!validateInput(value)) {
            self.displayMsg(errMsgBox, "Wrong datatype.", "warning");
        }
    });


    //TODO: submit button that sends out simulation
    let runBtn = $("#run-btn");
    let resetBtn = $("#reset-btn");
    let selectedId = 0 ;
   
    // updatePredefined(selectedId)
    $("select#predefined-select").on('change', function () {
         selectedId = parseInt($("select#predefined-select option:checked").val());
         console.log(selectedId)


    })

    runBtn.click(function () {
        runKML(selectedId);
    })
    resetBtn.click(function(){
        keys = Object.keys(sessionStorage);
        i = keys.length;
        console.log("CHECK THIS OUT")
        while (i--){
            var retrievedObject = sessionStorage.getItem(keys[i]);
            console.log(" i: "+ i);
            updateOwlFile(keys[i], JSON.parse(retrievedObject), 'kml');
        } 
    })

    //TODO: register for changes if want blinking effect of modification
    function runKML(predefinedId){
        console.log('predefinedID = ', predefinedId)
        ppMap.clearAnimatedLines();
        ppMap.clearMarkers();
        if (predefinedId == '0') {
            
            kmlURL = anotherURL1;
            scenario = "base";
            // appPrefix = prefix1;
        }
        else if (predefinedId == '1') {
            kmlURL = anotherURL2;
            scenario = "testPOWSYSNuclearStartSimulationAndProcessResultAgentCallForTestScenario";
            // appPrefix = prefix2;
        }
            
        json = { "electricalnetwork":iriofnetwork ,"flag": scenario }
        console.log(arrSum);
        ppMap.drawLines(json );
        ppMap.drawMarkers( json);
        refreshLayer(json, kmlURL);
        kmlURL = null;
        
    }
    function refreshLayer(iriofnetwork, kmlURL){
        if (kmlLayer){
            kmlLayer.setMap(null);
         }
        drawGenerator(iriofnetwork, kmlURL);
        console.log('Check that it should have refreshed. ')
    }
    //TODO: validate this
    function validateInput() {
        return true;
    }
    /*Msg***/
    let errMsgPanel = $("");

    function msgTemplate (msg, type) {
        return "<p class='alert alert-" + type + "'>" + msg + "</p>";
    }
    function displayMsg(msg, type) {
        //TODO: swithc type
        cleanMsg();
        errMsgPanel.append(msgTemplate(msg, type));

    }
    function distotalemission(result){
        $("#co2Value").text(result);
    }
    //TODO: define err msg panel
    function cleanMsg() {
        errMsgPanel.html("");
    }
})();
var arrSum = 0.00;
var  kmlLayer;
function drawGenerator(data, anotherURL){
    var d = new Date();
    var n = d.getTime();
    var kmljson = {};
    var kmlurl = prefix + '/JPS_POWSYS/ENVisualization/createKMLFile'; 
    kmljson["electricalnetwork"] = data["electricalnetwork"];
    kmljson["n"] = String(n);
    kmljson["flag"] =  data["flag"];
    kmlurl += "?query=" + encodeURIComponent(JSON.stringify(kmljson));      


    var request = $.ajax({
        url: kmlurl,
        type: 'GET',
        data: kmljson,
        contentType: 'application/json; charset=utf-8',
        success: function(){  
        },
        error: function(ts) {
            alert(ts.responseText);
        }   
    });

    request.done( function(data) {
    console.log ("success create request");
    kmlLayer = new google.maps.KmlLayer({
        // url: 'http://www.theworldavatar.com/OntoEN/testfinal.kml',//In other cases, will eventually be read and overwritten here. NO PROBLEM!
        url: anotherURL+ "?r="+(new Date()).getTime(), //this is completely necessary for cache-busting. 
        suppressInfoWindows: false,
        map: map
    });


        kmlLayer.addListener('click', function(kmlEvent) {
            setKMLMenu(kmlEvent)
        });             
        
    });

    request.fail(function(jqXHR, textStatus) {
    });
}

function sendRequest(url,callback) {
    console.log(url)
$.ajax({
    url: "http://theworldavatar.com:82/getAttrList",
    method: "POST",
    data: JSON.stringify({uri: url}),
    contentType: "application/json; charset=utf-8",

success: function (attrPairs) {
            callback(attrPairs);
        }

});
}

function setKMLMenu(kmlEvent){
    var data = kmlEvent.featureData;
    var nameString = data.name.substr(1);
    var names = nameString.split('[');
    var buttonsList = '<p>Please select the Entity you would like to modify</p>';
    for(var index in names)
    {
        var name = names[index];

        buttonsList = buttonsList + '<div><label>' + name.split('#')[1] + '</label>' +
            '<button onclick="selectEBus(event)" style= "cursor: pointer;" id="' + name + '"> > </span></div>'
    }

    buttonsList = '<div id="buttonContainer">'+ buttonsList +'</div><hr/><div id="inputsContainer"></div>';
    // set the content of the popup window.
    kmlEvent.featureData.infoWindowHtml = '<div>' + buttonsList + '</div>';

}
function selectEBus(event) {
    selectedId =  event.srcElement.id;
    openWindow(selectedId);
}

function openWindow(selectedId){
    if (selectedId.includes("Bus")){
        openWindowLineAndBus(selectedId, busInfo);
    }else{
        openWindowGen(selectedId);
    }
    
}
function openWindowGen(id){
 //since geninfo too large for request header, I'll split it up
    selectedId =  id; //this needs to be saved on a local version, and not towards here. 
    var kmljson = {};
    kmljson["sparqlquery"] = genInfo;
    kmljson["scenarioresource"] = selectedId;
    var inputsHTML = '';
    var kmlurl = 'http://localhost:8080/jps/scenario/'+scenario+'/query?query=' + encodeURIComponent(JSON.stringify(kmljson));
    kmljson["sparqlquery"] = genInfo2;
    var kmlurl2 = 'http://localhost:8080/jps/scenario/'+scenario+'/query?query=' + encodeURIComponent(JSON.stringify(kmljson));
    $.when(
        $.ajax({
        url: kmlurl,
        type: 'GET',
        contentType: 'application/json; charset=utf-8',
        success: function(data){ 
            var obj0 = JSON.parse(data);
            obj1 = obj0['results']['bindings'][0];
        },
        error: function(ts) {
            alert(ts.responseText);
        }   
         }),
    
        $.ajax({
        url: kmlurl2,
        type: 'GET',
        contentType: 'application/json; charset=utf-8',
        success: function(data){   
            var obj0 = JSON.parse(data);
            obj2 = obj0['results']['bindings'][0];
        },
        error: function(ts) {
            alert(ts.responseText);
        }   
    })).then( function(){
        var obj0 = Object.assign(obj1, obj2)
        console.log(obj0)

        var result = Object.keys(obj0).map(function(key) {return [key, obj0[key]];});
        nameSet = [];
        console.log(selectedId);
        var owlName = selectedId.split('#')[1].split('.')[0];
        for(var item in result)
        {
            var pair = result[item];
            if (pair[0] == "entity"){}
            else if(!pair[1]['value'].includes('.owl')) //this is for values only. 
            {
                var inputLine = '<tr><td><label>' + pair[0]+"_" +owlName +'</label></td><td><input class="input_class" data-dataType="' + pair[1]['datatype'] + '" value="' + pair[1]['value'] + '" style="float: right;"></td></tr>';
                inputsHTML = inputsHTML + inputLine;
                nameSet.push(pair[0]);
            }else {
                //for units, just place below the box. 
                //remove the last 
                inputsHTML = inputsHTML.slice(0, -10)
                //add in the units 
                var inputLine = '</td><td><input class="input_class" data-dataType="' + pair[1]['datatype'] + '" value="' + pair[1]['value'].split('#')[1] + '" style="float: right;" disabled="disabled"> </td><td> </td></tr>';
                inputsHTML = inputsHTML + inputLine;
            }
        }

        console.log(inputsHTML);
        var div = document.getElementById('inputsContainer');
        div.innerHTML = '<table data-type="kml" data-url='+ selectedId +' id="inputsTable">' + inputsHTML + '</table><br/><button onclick="SubmitTable(this)">OPF</button><button onclick="SubmitTable(this)">PF</button>'+
        '<img id="myProgressBar" style="width:100px;height:100px;display:none" src="https://media.giphy.com/media/3oEjI6SIIHBdRxXI40/giphy.gif"/><br/>'


    })
}
function openWindowLineAndBus(id, type){ //gen has its own openWindow cos it's too large. 
    selectedId =  id; //this needs to be saved on a local version, and not towards here. 
    var kmljson = {};
    kmljson["sparqlquery"] = type;
    kmljson["scenarioresource"] = selectedId;
    // var url = 'http://www.theworldavatar.com/kb/sgp/jurongisland/jurongislandpowernetwork/' +selectedId.split('#')[1]; //will read from here. 
    var kmlurl = 'http://localhost:8080/jps/scenario/'+scenario+'/query?query=' + encodeURIComponent(JSON.stringify(kmljson));
    console.log(kmlurl);
    var inputsHTML = '';
    var request = $.ajax({
        url: kmlurl,
        type: 'GET',
        contentType: 'application/json; charset=utf-8',
        success: function(){  
        },
        error: function(ts) {
            alert(ts.responseText);
        }   
    });
    request.done( function(data) {
        var obj0 = JSON.parse(data);
        obj0 = obj0['results']['bindings'][0];
        console.log(obj0)


        var result = Object.keys(obj0).map(function(key) {return [key, obj0[key]];});
        nameSet = [];
        console.log(selectedId);
        var owlName = selectedId.split('#')[1].split('.')[0];
        for(var item in result)
        {
            var pair = result[item];
            if (pair[0] == "entity"){}
            else if(!pair[1]['value'].includes('.owl')) //this is for values only. 
            {
                var inputLine = '<tr><td><label>' + pair[0]+"_" +owlName +'</label></td><td><input class="input_class" data-dataType="' + pair[1]['datatype'] + '" value="' + pair[1]['value'] + '" style="float: right;"></td></tr>';
                inputsHTML = inputsHTML + inputLine;
                nameSet.push(pair[0]);
            }else {
                //for units, just place below the box. 
                //remove the last 
                inputsHTML = inputsHTML.slice(0, -10)
                //add in the units 
                var inputLine = '</td><td><input class="input_class" data-dataType="' + pair[1]['datatype'] + '" value="' + pair[1]['value'].split('#')[1] + '" style="float: right;" disabled="disabled"> </td><td> </td></tr>';
                inputsHTML = inputsHTML + inputLine;
            }
        }

        console.log(inputsHTML);
        var div = document.getElementById('inputsContainer');
        div.innerHTML = '<table data-type="kml" data-url='+ selectedId +' id="inputsTable">' + inputsHTML + '</table><br/><button onclick="SubmitTable(this)">OPF</button><button onclick="SubmitTable(this)">PF</button>'+
        '<img id="myProgressBar" style="width:100px;height:100px;display:none" src="https://media.giphy.com/media/3oEjI6SIIHBdRxXI40/giphy.gif"/><br/>'

    });

}
function SubmitTable(e) {

    opt = e.innerHTML;
    var table = document.getElementById('inputsTable');
    var rows = table.firstElementChild.childNodes;
    var url = table.getAttribute('data-url');
    var type = table.getAttribute('data-type');
    console.log('type',type);

    var JSONArray  = {};

    var proceed = true;
    console.log(rows.length, 'Rows length');
    for(var i = 0; i < rows.length; i++)
    {
        var row = rows[i];
        var name = row.getElementsByTagName('label')[0].innerText;
        var value = row.getElementsByTagName('input')[0].value;
        
        
        if(name.includes('EBus-001')){ // This is a slack bus, the magnitude is always 1 and the angle is always 0
            //console.log("label forbidden= "+label);
            if(name.includes('VoltageMagnitude')|| name.includes('Vm_EBus')) {
                if (value !== 1){
                    alert('The value of the voltage magnitude and Vm for a slack bus should always be 1 kV (in p.u format)')
                    proceed = false;
                }
            }
            
            if (name.includes('VoltageAngle')|| name.includes('Va_EBus')){
                if (value !== 0){
                    alert('The value of the voltage angle and Va for a slack bus should always be 0 degree')
                    proceed = false;
                }
            }
        }
        else{ // This is a load bus 
        //console.log("label forbidden= "+label);
            if(name.includes('VoltageMagnitude')|| name.includes('Vm_EBus')){
                if( value > 1.05 || value <= 0.95){
                    alert('The value of the voltage magnitude and Vm should be between 0.95 and 1.05 kV (in p.u format)')
                    proceed = false;
                }
            }           
        }
        
        
        
        
        

        
        var datatype = row.getElementsByTagName('input')[0].getAttribute('data-dataType');
        console.log('value',value,'name',name,'url',url);
        JSONArray[name] = {name: name,value:value, datatype: datatype }
    }



    if(proceed){
        var progress = document.getElementById('myProgressBar');
        progress.style.display = 'block';
        updateOwlFile(url,JSONArray,type);
    }


}
function updateOwlFile(filename,JSONArray,_type) {

    console.log('number',Object.keys(JSONArray).length);
    console.log('JSONArray',Object.keys(JSONArray));
    console.log('filename=',filename);
    console.log('type=',_type);

    var allItemsArray = [];
    var indexCounter = 0;
    var temp = [];
    for(var item in JSONArray){
            if(((indexCounter!== 0) && (indexCounter % 10 === 0)) || (indexCounter === parseInt(Object.keys(JSONArray).length - 1)) )
            {
                if((indexCounter === parseInt(Object.keys(JSONArray).length - 1)))
                {
                    //allItemsArray.push(temp);
                    //temp = [];
                    temp.push(item)
                    allItemsArray.push(temp);
                }
                else
                {
                    allItemsArray.push(temp);
                    temp = [];
                    temp.push(item)
                }


            }
            else
            {
                temp.push(item)
            }

            indexCounter++;
        }

    console.log(allItemsArray);


    var asyncLoop = function(o){
        var i=-1,
            length = o.length;
            console.log(length);
        var loop = function(){
            i++;
            console.log(i);
            if(i===length){
                console.log("CALLBACK called? ");
                o.callback(); 
                return;
            }
            o.functionToLoop(loop, i);
        };
        loop();//init
};


asyncLoop({
    length : Math.ceil(Object.keys(JSONArray).length / 10),
    functionToLoop : function(loop, i){


        var sampleUpdate = [];
        var uri = [];

        var Session = allItemsArray[i];
        console.log('Session',Session);
        for(var j = 0; j < Session.length; j++)
        {
            var item = Session[j];
            var obj = JSONArray[item];
            var targetIRI = obj.name;
            var dataType = obj.datatype;
            if(dataType === 'int')
            {
                dataType = 'integer'
            }
            console.log('dataType',dataType);
            var base = filename.split('#')[0] + '#';
            base = base.replace('/OntoEN','');
            base=base.replace('theworldavatar','jparksimulator'); //because in electrical it use jparksimulator instead of theworldavatar
            var value = obj.value;
            if(targetIRI)
            {

                if (targetIRI.includes("Costn1")){
                    targetIRI = targetIRI.replace("Costn1","Costn-1" );
                    
                    console.log(targetIRI);
                }else if (targetIRI.includes("Costn2")){
                    targetIRI = targetIRI.replace("Costn2","Costn-2" );
                    
                    console.log(typeof targetIRI);
                }
                var deleteUpdate = "DELETE WHERE {<" + base + targetIRI + "> <http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#numericalValue> " + "?o.}";
                var insertUpdate = "INSERT DATA {<" + base + targetIRI + "> <http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#numericalValue> " +value +".}";
                    
                    

                console.log('deleteUpdate',deleteUpdate);
                console.log('insertUpdate',insertUpdate);

                sampleUpdate.push(deleteUpdate);
                sampleUpdate.push(insertUpdate);
                
                uri.push(filename);
                uri.push(filename);
                
            }
        }
        console.log(scenario);
        console.log(sampleUpdate); 
        var myUrl = createUrlForSparqlUpdate(scenario,base.split('#')[0], sampleUpdate.join(';'));
        var request = $.ajax({
            url: myUrl,
            type: 'GET',
            contentType: 'application/json; charset=utf-8'
        });
        console.log(myUrl);
        request.done(function(data) {
            console.log('data received', data);
            loop();


        });

        request.fail(function(jqXHR, textStatus) {
            // your failure code here
        });


    },
    callback : function(){

        console.log('all done in callback');
        //var path = "C:@TOMCAT@webapps@ROOT@OntoEN@startSimulation.bat>" + filename.split('.com/')[1].split('.owl')[0] + '>' + opt;


        //var url = 'http://www.theworldavatar.com/Service_Node_BiodieselPlant3/startScript?path=' + encodeURIComponent(path);
        var url = 'http://localhost:8080/JPS_POWSYS/ENAgent/startsimulation'+opt;
        var agent = "testPOWSYSENSimulation"+opt + "CallAgent"
        // var url = 'http://www.theworldavatar.com/JPS_POWSYS/ENAgent/startsimulation'+opt;
        
        var request = $.ajax({
            url: url,
            type: 'GET',
            data: { "electricalnetwork":iriofnetwork , "jpscontext":{"scenariourl": agent}},
            contentType: 'application/json; charset=utf-8'
        });

        request.done(function(data) {
            
//             console.log('simulation finished');
            openWindow(filename);
//             sendRequest(url,function (response) {
            
//                 console.log('simulation finished url', url)
//                 response = sortByKey(response,'name');
//                 var inputsHTML = '';
                
//                 var nameSet = [];
                
//                 for(var item in response){
//                     var pair = response[item];

//                     if(pair['value'].includes('.owl'))
//                     {

//                     }
//                     else{
                    
//                     if(nameSet.includes(pair['name'])){
                        
//                     }
//                     else{
                    
                                                
//                     console.log(pair['name']);
//                     var inputLine = '<tr><td><label>' + pair['name'] +'</label></td><td><input data-dataType="' + pair['datatype'] + '" value="' + pair['value'] + '" style="float: right;"></td><td>' + pair['unit'] + '</td></tr>';
//                         inputsHTML = inputsHTML + inputLine;
//                         nameSet.push(pair['name'])                              
//                     }

//                     }
//                 }

//                 var _div;
//                 if(_type==='kml'){
//                     console.log('---kml');
//                     _div = document.getElementById('inputsContainer');
//                 }
//                 else{
//                     console.log('---line');
//                     _div = document.getElementById('something');
//                 }
                
//                 _div.innerHTML = '<table data-url='+ url +' id="inputsTable">' + inputsHTML + '</table><br/><button onclick="SubmitTable(this)">OPF</button><button onclick="SubmitTable(this)">PF</button>' +
// '<img id="myProgressBar" style="width:100px;height:100px;display:none" src="https://media.giphy.com/media/3oEjI6SIIHBdRxXI40/giphy.gif"/><br/>';
//                 //location.reload(); //temp
//             });



        });


    }
});

}
function sortByKey(array, key) {
return array.sort(function(a, b) {
    var x = a[key]; var y = b[key];
    return ((x < y) ? -1 : ((x > y) ? 1 : 0));
});
}

function constructLineMenu(id,callback){
    openWindowLineAndBus(id, branchInfo);

    
}
function createUrlForSparqlUpdate(scenarioname, iri, sparql) {

        var url2 = prefix + '/jps/scenario/' + scenarioname + '/update?query=';
        urljson = {"scenarioresource":iri,"sparqlupdate":sparql};
        url2 += encodeURIComponent(JSON.stringify(urljson)); 
        //url2 += JSON.stringify(urljson); 
        return url2;    
    }
function createUrlForSparqlQuery(scenarioname, iri, sparql) {

        var url2 = prefix + '/jps/scenario/' + scenarioname + '/query?query=';
        urljson = {"scenarioresource":iri,"sparqlupdate":sparql};
        url2 += encodeURIComponent(JSON.stringify(urljson)); 
        //url2 += JSON.stringify(urljson); 
        return url2;    
    }

function injectJpsContext(scenarioname, data) {

    var scenariourl = prefix + '/jps/scenario/' + scenarioname;

    data["jpscontext"] = {
        "scenariourl": scenariourl
    };
    
}