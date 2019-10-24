<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sb" uri="/struts-bootstrap-tags"%>

<!DOCTYPE html>
<html>
<!--  after pressing refresh button it clears content of page. -->
<head>

<meta charset="UTF-8">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<script src="${pageContext.request.contextPath}/js/Chart.min.js"></script>
<script src="${pageContext.request.contextPath}/js/utils.js"></script>
<script src="${pageContext.request.contextPath}/js/popper.min.js"></script>
<script src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/static/group/bootstrap.min.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/index.css">

<link rel="icon"
	href="${pageContext.request.contextPath}/css/static/group/favicon.ico" />

<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/static/group/ontokin.css">

<title>J-Park Simulator</title>

</head>

<body>
<div class="jumbotron text-center" id="topBanner">
    <a id="cares-banner" href="http://www.cares.cam.ac.uk">
        <img id="care-banner-img" src="images/cam_lang_negativ1%20NEW_0.png">
    </a>
		<h1 id="title">OntoKin</h1>
		<p id="description">
			A Knowledge Base built with the integration of semantic technologies and software agents for 
			enhancing the experience of chemists in querying chemical kinetic reaction 
			mechanisms. This User Interface (UI) demonstrates the query capability of OntoKin.
		</p>
</div>
	<div class="container">
		<div class="row">
             <div class="col-md-3 div-left">
             
                     <s:actionerror />
			<s:actionmessage />			
		
			<s:form action="upload" method="post"
				enctype="multipart/form-data" theme="bootstrap" label="Select CHEMKIN files to upload:">
				<hr class="line">
				<s:file name="myMechFile" label="Select a mechanism file to upload:"
					theme="bootstrap" />
				<s:file name="myThermoFile" label="Select a thermo chemistry file to upload:"
					theme="bootstrap" />
				<s:file name="mySurfaceFile" label="Select a surface chemistry file to upload:"
					theme="bootstrap" />
				<s:file name="myTransportFile" label="Select a transport file to upload:"
					theme="bootstrap" />								
				<s:textfield name="myMechanismName" type="text" label="Provide a name for the mechanism:" theme="bootstrap"/>
				<s:submit value="Upload" label="Select files" theme="bootstrap" />
			</s:form>
			
		<p></p>				
			
		<s:iterator value="column" var="c">
			<hr class="line">
			<legend>Mechanism upload report </legend>
			<table class="table table-bordered table-hover">
				<tr>
					<th>Item</th>
					<th>Result</th>
				</tr>
				<tr>
					<td ><s:property value="column1" /></td>
					<td><s:property value="myMechanismName" /></td>
				</tr>
				<tr>
					<td ><s:property value="column2" /></td>
					<td><s:property value="myMechFileFileName" /></td>
				</tr>
				<tr>
					<td ><s:property value="column3" /></td>
					<td><s:property value="myThermoFileFileName" /></td>
				</tr>
				<tr>
					<td ><s:property value="column4" /></td>
					<td><s:property value="mySurfaceFileFileName" /></td>
				</tr>
				<tr>
					<td ><s:property value="column5" /></td>
					<td><s:property value="myTransportFileFileName" /></td>
				</tr>
				<tr>
					<td ><s:property value="column6" /></td>
					<td><s:property value="myChemkinValidationReport" /><br></br>Please see the conversion log <a href="<s:property value="myChemkinValidationReportFile" />">here.</a><br></br>If the conversion fails, check line(s) starting with ERROR for potential reasons. Please contact Prof. Markus Kraft(mk306@cam.ac.uk) and Dr. Jethro Akroyd (jwja2@cam.ac.uk) for reporting any issue.</td>
				</tr>
					<tr>
					<td ><s:property value="column7" /></td>
					<td><s:property value="myOWLConsistencyReport" /></td>
				</tr>
			</table>
			
		</s:iterator>	
		</div>
		
			<div class="col-md-half">
			</div>
			<div class="col-md-eightpointfive div-right">
				<s:actionerror/>
				<legend style="padding-bottom: 10px;">Specify a Query:</legend>
				<hr class="line !important;">
				<span id ="queryType" style="">Select a type of query:</span>
				<span id ="errorType" style="display:none; color:red">No type selected</span>
				<div class="row">
					<div class="col-md-11">
						<s:select
							headerKey="-1" headerValue="Select query type"
							list="#{'mechAll':'Show All Mechanisms (no additional input is needed)', 'mechforS':'Show Mechanism(s) Containing Species', 'thermo':'Thermodynamic Data', 'compthermo':'Compare Thermodynamic Data', 'mechforR':'Show Mechanism(s) Containing Reaction', 'rateconstant':'Show Arrhenius Rate Constant Parameters (Matching Reaction Exactly)', 'comparerate':'Compare Arrhenius Rate Constant Parameters (Matching Reaction Exactly)', 'rateconstantAnyOrder':'Show Arrhenius Rate Constant Parameters (Matching Reactants and Products, may take several minutes)', 'comparerateAnyOrder':'Compare Arrhenius Rate Constant Parameters (Matching Reactants and Products, may take several minutes)'}" 
							name="querySelection" 
							value="thermo" theme="bootstrap" />
					</div>
					<div class="col-md-1">
						<span class="btn btn-sm btn-info btn-help" data-toggle="tooltip" data-placement="right" title="To view all the mechanisms, select 'Show All Mechanisms'. To search for a species, select either 'Show Mechanism(s) Containing Species' or 'Thermodynamic Data' or 'Compare Thermodynamic Data'. To search for a reaction, select either 'Show Arrhenius Rate Constant Parameters' or 'Compare Arrhenius Rate Constant Parameters'.">?</span>							
					</div>
				</div>
				<span id ="queryText" style="">Interactive text:</span>
				<span id ="errorQuery" style="display:none; color:red">No text provided</span>
				<span id ="errorQueryReaction" style="display:none; color:red">Provide a valid reaction</span>
				<div class="row">
					<div class="col-md-11">
						<s:textfield name="term" class="form-control"  placeholder="Specify the name of a species or reaction" theme="bootstrap"/>							
					</div>
					<div class="col-md-1">
						<span class="btn btn-sm btn-info btn-help" data-toggle="tooltip" data-placement="right" title="If you have selected 'Show All Mechanisms' in the menu above, click on the 'Search OntoKin' button. If you have selected a species related query, type in or paste a species (e.g. H2O). If you have selected a reaction related query, type in or past a reaction (e.g. O + HO2 [=] O2 + OH). Finally, click on the 'Search OntoKin' button.">?</span>
					</div>
				</div>

					<%-- <s:submit id="search_btn" value="OntoKin Search (to view the list of all mechanisms in the KB, go directly to the following drop-down menu)" theme="bootstrap"/> --%>
					<button id="execute" theme="bootstrap">Search OntoKin</button>
					<button id="refresh" theme="bootstrap">Clear</button>
					<span id="spinner" style="display: none"><i class="fa fa-spinner fa-spin" style="font-size:24px"></i></span>
					
					<p></p><span id ="noResult" style="display:none; color:red">No results found.</span>
				 	<p></p>
				 	
				 	<!-- Instruction -->
					<div class="row">
					 	<div class="col-md-12">
						 	<div id="accordion">
							  <div class="card">
							    <div class="card-header" id="headingOne">
							      <h5 class="mb-0">
							        <button class="btn btn-link" data-toggle="collapse" data-target="#collapseOne" aria-expanded="true" aria-controls="collapseOne">
									    How to search?
							        </button>
							      </h5>
							    </div>
							
							    <div id="collapseOne" class="collapse show" aria-labelledby="headingOne" data-parent="#accordion">
							      <div class="card-body">
										<h5 class="card-title" style="font-size: 18px;">How to search for a species?</h5>
									    <p class="card-text" style="font-size: 14px;">In the 'OntoKin Search' box above, provide a species (e.g. CH4) by typing in or pasting, select either 'Show Mechanism(s) Containing Species' or 'Thermodynamic Data' or 'Compare Thermodynamic Data' in the 'Select query type' drop-down menu and click on the 'Search OntoKin' button.</p>
									    <h5 class="card-title" style="font-size: 18px;">How to search for a reaction?</h5>
									    <p class="card-text" style="font-size: 14px;">In the 'OntoKin Search' box above, provide a reaction (e.g. OH [=] H2O + O) by typing in or pasting, select either 'Show Mechanism(s) Containing Reaction' or 'Show Arrhenius Rate Constant Parameters' or 'Compare Arrhenius Rate Constant Parameters' in the 'Select query type' drop-down menu and click on the 'Search OntoKin' button.</p>
									</div>
							    </div>
							  </div>
							</div>
						  </div>
						</div>
					
					<!-- Mechanism Table -->
				 	<div id="tableMechanism" class="Container Flipped">
					 	<div id="table" class="Content">
					 		<table class="table table-bordered table-hover">
					 			<thead>
					    			<tr class="row-header">
					    			</tr>
					   			</thead>
					   			<tbody id="table-query-results">
					   			</tbody>
					   		</table>
					   	</div>
				   	</div>
				  
				  <!-- Draw Charts -->
					<p></p>
					<div class="container chart-group">
					  <div id="resultBlock" class="row">
					   	<div id="chartCanvas" class="" style="display:none">
							<div id="unitsGasConstant" class="col-md-11" style="">
								<span id ="units" style="">Please select the unit system:</span>
								<s:select 
									headerKey="-1" headerValue="kcal / mol. K"
									list="#{'jmolk':'J / mol. K', 'ergmolk':'erg / mol. K', 'dimensionless':'Dimensionless'}" 
									name="unitsRSelection" 
									value="unitsR" theme="bootstrap" />
							</div>
							<canvas id="canvas" style="width:450px !important; height:188px"></canvas>
							<canvas id="canvasJMolK" style="width:450px !important; height:188px; display:none"></canvas>
							<canvas id="canvasErgMolK" style="width:450px !important; height:188px; display:none"></canvas>
							<canvas id="canvasNoDimension" style="width:450px !important; height:188px; display:none"></canvas>
							<p></p>
							<canvas id="canvasH" style="width:450px !important; height:188px"></canvas>
							<canvas id="canvasHJMolK" style="width:450px !important; height:188px; display:none"></canvas>
							<canvas id="canvasHErgMolK" style="width:450px !important; height:188px; display:none"></canvas>
							<canvas id="canvasHNoDimension" style="width:450px !important; height:188px; display:none"></canvas>
							<p></p>
							<canvas id="canvasS" style="width:450px !important; height:188px"></canvas>
							<canvas id="canvasSJMolK" style="width:450px !important; height:188px; display:none"></canvas>
							<canvas id="canvasSErgMolK" style="width:450px !important; height:188px; display:none"></canvas>
							<canvas id="canvasSNoDimension" style="width:450px !important; height:188px; display:none"></canvas>							
						</div>
					   	<div id="chartCanvasRateAE" class="" style="display:none">
							<canvas id="canvasRateAE" style="width:1200px !important; height:800px"></canvas>
							<p></p>
						</div>
					  </div>
					</div>
					</div>
		</div>
	</div>


<script type="text/javascript">

$( function() {
 
	 function splitString(string) {
		  	if (string.indexOf('[=]') > -1) {
		    	return string.split("[=]");
		    } else if (string.indexOf('=]') > -1) {
		    	return string.split("=]");
		    } else if (string.indexOf('<=>') > -1){
		    	return string.split("<=>");
		    } else if (string.indexOf('=>') > -1){
		    	return string.split("=>");
		    } else if (string.indexOf('=') > -1){
		    	return string.split("=");
		    }
		  	return string;
		  }
	 
	 function isReactionValid(string) {
		  	if (string.indexOf('[=]') > -1) {
		    	return 1;
		    } else if (string.indexOf('=]') > -1) {
		    	return 1;
		    } else if (string.indexOf('<=>') > -1){
		    	return 1;
		    } else if (string.indexOf('=>') > -1){
		    	return 1;
		    } else if (string.indexOf('=') > -1){
		    	return 1;
		    }
		  	// Return 0 if the given reaction is not valid
		  	return 0;
		  }
	 
	 function calculateR(unitsR) {
		  	let R = 1;
		 	if (unitsR.indexOf('-1') > -1) {
		 		R = 1.98720425864083 * Math.pow(10, -3);
		 	} else if (unitsR.indexOf('jmolk') > -1) {
		    	R = 8.31446261815324;
		    } else if (unitsR.indexOf('ergmolk') > -1){
		 		R = 8.314462618 * Math.pow(10, 7);
		    } else if (unitsR.indexOf('dimensionless') > -1){
				R = 1;
		    }
		 	return R;
		  }

	 function getTemperatures(minTemp, maxTemp) {
			let interval = (maxTemp - minTemp);
			let slices = 16;
			interval = interval / slices;
			interval = Math.ceil(interval / 100.0) * 100;
			var temperatures = [];
			console.log('In getTemperatures');
			var T;
			for(T = 300; T <= 6000; T = T + 300){
				console.log('T In getTemperatures');
				console.log(T);
				temperatures.push(T);
				console.log('temperatures getTemperatures');
				console.log(temperatures);
			}
			return temperatures;
		 }
	 
	 function get1000OverTemperatures(minTemp, maxTemp) {
			let interval = (maxTemp - minTemp);
			let slices = 16;
			interval = interval / slices;
			interval = Math.ceil(interval / 100.0) * 100;
			var temperatures = [];
			console.log('In getTemperatures');
			var T;
			for(T = 300; T <= 6000; T = T + 300){
				console.log('T In getTemperatures');
				console.log(T);
				temperatures.push(1000 / T);
				console.log('temperatures getTemperatures');
				console.log(temperatures);
			}
			return temperatures;
		 }
	 
	 function calculateMinTemp(minTemps) {
		 let minTemp = 0;
		 if(minTemps.length >= 1){
			 minTemp = minTemps[0];
		 }
		 for (temp of minTemps){
			if(minTemp > temp){
				minTemp = temp;
			}
		 }
		 return minTemp;
	 }
	 
	 function calculateMaxTemp(maxTemps) {
		 let maxTemp = 0;
		 if(maxTemps.length >= 1){
			 maxTemp = maxTemps[0];
		 }
		 for (temp of maxTemps){
			if(maxTemp < temp){
				maxTemp = temp;
			}
		 }
		 return maxTemp;
	 }

	 function calculateInterval(minTemp, maxTemp) {
		let interval = (maxTemp - minTemp);
		let slices = 16;
		interval = interval / slices;
		return Math.ceil(interval / 100.0) * 100;
	 }
	 
	 function calculateCp(unitsR, aLow, aHigh, minTemp, midTemp, maxTemp) {
		let R = calculateR(unitsR);
		let interval = calculateInterval(minTemp, maxTemp);
		console.log('interval:');
		console.log(interval);
		var T = 300;
		var CpAllTemps = [];
		if(aLow.length>=7 && aHigh.length>=7){
 			for(T = 300; T <= 6000; T += 300){
				console.log('T:');
				console.log(T);
 				if(T<midTemp){
					Cp = R * (parseFloat(aLow[0]) + parseFloat(aLow[1]) * T + parseFloat(aLow[2]) * Math.pow(T, 2)  + parseFloat(aLow[3]) * Math.pow(T, 3) + parseFloat(aLow[4]) * Math.pow(T, 4));
					console.log('CpLowInFuction');
					console.log(Cp);
				} else{
					Cp = R * (parseFloat(aHigh[0]) + parseFloat(aHigh[1]) * T + parseFloat(aHigh[2]) * Math.pow(T, 2)  + parseFloat(aHigh[3]) * Math.pow(T, 3) + parseFloat(aHigh[4]) * Math.pow(T, 4));
					console.log('CpHighInFuction');
					console.log(Cp);
				}
				CpAllTemps.push(Cp);
				console.log('CpAllTemps');
				console.log(CpAllTemps); 
			}
		}
		return CpAllTemps;
	 }
	 
	 function calculateH(unitsR, aLow, aHigh, minTemp, midTemp, maxTemp) {
			let R = calculateR(unitsR);
			let interval = calculateInterval(minTemp, maxTemp);
			console.log('interval:');
			console.log(interval);
			var T = 300;
			var HAllTemps = [];
			if(aLow.length>=7 && aHigh.length>=7){
	 			for(T = 300; T <= 6000; T += 300){
					console.log('T:');
					console.log(T);
	 				if(T<midTemp){
						H = R * (parseFloat(aLow[0]) * T + parseFloat(aLow[1]) * Math.pow(T, 2) / 2 + parseFloat(aLow[2]) * Math.pow(T, 3) / 3  + parseFloat(aLow[3]) * Math.pow(T, 4) / 4 + parseFloat(aLow[4]) * Math.pow(T, 5) / 5 + parseFloat(aLow[5]));
						console.log('HLowInFuction');
						console.log(H);
					} else{
						H = R * (parseFloat(aHigh[0]) * T + parseFloat(aHigh[1]) * Math.pow(T, 2) / 2 + parseFloat(aHigh[2]) * Math.pow(T, 3) / 3  + parseFloat(aHigh[3]) * Math.pow(T, 4) / 4 + parseFloat(aHigh[4]) * Math.pow(T, 5) / 5 + parseFloat(aHigh[5]));
						console.log('CpHighInFuction');
						console.log(H);
					}
					HAllTemps.push(H);
					console.log('HAllTemps');
					console.log(HAllTemps); 
				}
			}
			return HAllTemps;
		 }

	 function calculateS(unitsR, aLow, aHigh, minTemp, midTemp, maxTemp) {
			let R = calculateR(unitsR);
			let interval = calculateInterval(minTemp, maxTemp);
			console.log('interval:');
			console.log(interval);
			var T = 300;
			var SAllTemps = [];
			if(aLow.length>=7 && aHigh.length>=7){
	 			for(T = 300; T <= 6000; T += 300){
					console.log('T:');
					console.log(T);
	 				lnT = Math.log(T);
	 				console.log('lnT');
	 				console.log(lnT);
					if(T<midTemp){
						S = R * (parseFloat(aLow[0]) * lnT + parseFloat(aLow[1]) * T + parseFloat(aLow[2]) * Math.pow(T, 2) / 2  + parseFloat(aLow[3]) * Math.pow(T, 3) / 3 + parseFloat(aLow[4]) * Math.pow(T, 4) / 4 + parseFloat(aLow[6]));
						console.log('SLowInFuction');
						console.log(S);
					} else{
						S = R * (parseFloat(aHigh[0]) * lnT + parseFloat(aHigh[1]) * T + parseFloat(aHigh[2]) * Math.pow(T, 2) / 2  + parseFloat(aHigh[3]) * Math.pow(T, 3) / 3 + parseFloat(aHigh[4]) * Math.pow(T, 4) / 4 + parseFloat(aHigh[6]));
						console.log('SHighInFuction');
						console.log(S);
					}
					SAllTemps.push(S);
					console.log('SAllTemps');
					console.log(SAllTemps); 
				}
			}
			return SAllTemps;
		 }
	 
	 function calculateRateConstant(unitsR, A, b, E) {
			let R = calculateR(unitsR);
			var RateConstantAllTemps = [];
	 			for(T = 300; T <= 6000; T += 300){
					RC = A * Math.pow(1000 / T, b) * Math.exp(-E * T /(R * 1000));
					RateConstantAllTemps.push(RC);
					console.log('RateConstantAllTemps');
					console.log(RateConstantAllTemps); 
				}
			return RateConstantAllTemps;
		 }
	 
	 
	 function formatLabel(str){
		if(str.length>30){
			str = str.slice(0,30) + '...';
		}
		return str;
	}
	
	 $('#unitsRSelection').change(function(){ 
		    var value = $(this).val();
			if (value == 'jmolk') {
				$('#canvasJMolK').show();
				$('#canvas').hide();
				$('#canvasErgMolK').hide();
				$('#canvasNoDimension').hide();
			} else if (value == 'ergmolk') {
				$('#canvasJMolK').hide();
				$('#canvas').hide();
				$('#canvasErgMolK').show();
				$('#canvasNoDimension').hide();
			} else if (value == 'dimensionless') {
				$('#canvasJMolK').hide();
				$('#canvas').hide();
				$('#canvasErgMolK').hide();
				$('#canvasNoDimension').show();
			} else if (value == '-1') {
				$('#canvasJMolK').hide();
				$('#canvas').show();
				$('#canvasErgMolK').hide();
				$('#canvasNoDimension').hide();
			}
	 });
	
	 $("a#linkButton").on('click',function(){
	    window.open('www.google.com', '_blank');
	});
	
	$('[data-toggle="tooltip"]').tooltip({
	    trigger : 'hover'
	});  
	
	let getTableResultRowString = (index, resultObj) => {
		let tdNodes = '';
		
		//console.log(resultObj);
		for (let x in resultObj) {
			tdNodes += '<td>' + resultObj[x] + '</td>';
		}
		
		return '<tr class="row-query-results"> ' +
		    		'<td class="index">' + index + '</td>' +
	    		 
	    		tdNodes +
				'</tr>'
	};
	
	$('#refresh').click(function() {
		window.location.href = '/ontokin';
	});

	$("#execute").on("click", (event) => {
		
		// let chartCanvas =  $("#chartCanvas");
		let queryString;
		
		let search_term_name = $("#term").val(); //cl2
		let search_querySelection = $("#querySelection").val(); //thermo
		let search_unitsRSelection = $("#unitsRSelection").val(); //thermo
		console.log(search_unitsRSelection);
		$("#errorQuery").hide();
		$("#errorQueryReaction").hide();
		$("#errorType").hide();
		$("#noResult").hide();
		if (search_querySelection != 'mechAll' && (search_term_name.trim() ==  '' || search_querySelection ==  -1 || search_querySelection ==  undefined || search_querySelection ==  null)) {
			$("#resultBlock").hide();
			if(search_term_name ==  '') {
				$("#chartCanvas").hide();
				$("#chartCanvasRateAE").hide();
				$("#tableMechanism").hide();
				$("#errorQuery").show();
			}
			if(search_querySelection ==  -1 || search_querySelection ==  undefined || search_querySelection ==  null) {
				$("#chartCanvas").hide();
				$("#chartCanvasRateAE").hide();
				$("#tableMechanism").hide();
				$("#errorType").show();
			}			
			event.preventDefault(); 
		} else if ((search_querySelection == 'mechforR' || search_querySelection == 'rateconstant' || search_querySelection == 'comparerate'
				|| search_querySelection == 'rateconstantAnyOrder' || search_querySelection == 'comparerateAnyOrder') 
				&& isReactionValid(search_term_name) == 0){
			$("#errorQueryReaction").show();
			$("#resultBlock").hide();
			event.preventDefault(); 
		}
		else {
			search_term_name = search_term_name.toUpperCase();
			let result = splitString(search_term_name);			
			let reactantArray;
			let productArray;
			
			if (result.length == 2 ) {	
				reactantArray = result[0].split("+");
				productArray = result[1].split("+");
			}
			
			$("#errorQuery").hide();
			$("#errorQueryReaction").hide();
			$("#errorType").hide();
			$("#noResult").hide();
			$("#resultBlock").show();
			if (search_querySelection == 'mechAll') {			 
				 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'SELECT ?MechanismName ?MechanismIRI' + '\n' +
				'WHERE {' + '\n' +
				    '?MechanismIRI rdf:type ontokin:ReactionMechanism .' + '\n' +
				    '?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				'} ';
								
			} else if (search_querySelection == 'mechforS' && !(search_term_name.indexOf('=') > -1)) {			 
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'SELECT ?MechanismName ?MechanismIRI' + '\n' +
				'WHERE {' + '\n' +
					'?Species rdfs:label \"' + search_term_name + '\" . ?Species ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
				    '?MechanismIRI rdf:type ontokin:ReactionMechanism .' + '\n' +
				    '?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				'} ';
								
			} else if (search_querySelection == 'mechforR' && search_term_name.indexOf('=') > -1) {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'SELECT ?MechanismName ?MechanismIRI' + '\n' +
				'WHERE {' + '\n' +
					'?Reaction ontokin:hasEquation \"' + search_term_name + '\" . ?Reaction ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
				    '?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				'} ';
							
			} else if(search_querySelection == 'thermo') {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'SELECT ?MechanismName ?MechanismIRI ?SpeciesIRI ?ThermoModelIRI ?CoefficientValues ?MinTemp ?MaxTemp' + '\n' +
				'WHERE {' + '\n' +
					'?SpeciesIRI rdfs:label \"' + search_term_name + '\" . ?SpeciesIRI ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
					'?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
					'?MechanismIRI rdf:type ontokin:ReactionMechanism .' + '\n' +
				    '?SpeciesIRI ontokin:hasThermoModel ?ThermoModelIRI .' + '\n' +
				    '?ThermoModelIRI ontokin:hasCoefficientValues ?CoefficientValues .' + '\n' +
				    '?ThermoModelIRI ontokin:hasMinimumTemperature ?MinTemp .' + '\n' +
				    '?ThermoModelIRI ontokin:hasMaximumTemperature  ?MaxTemp .' + '\n' +
				'} ';
				
			} else if(search_querySelection == 'compthermo') {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'SELECT ?MechanismName ?MechanismIRI ?SpeciesIRI ?ThermoModelIRI ?CoefficientValues ?MinTemp ?MaxTemp ?Pressure' + '\n' +
				'WHERE {' + '\n' +
					'?SpeciesIRI rdfs:label \"' + search_term_name + '\" . ?SpeciesIRI ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
				    '?MechanismIRI rdf:type ontokin:ReactionMechanism .' + '\n' +
				    '?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				    '?SpeciesIRI ontokin:hasThermoModel ?ThermoModelIRI .' + '\n' +
				    '?ThermoModelIRI ontokin:hasCoefficientValues ?CoefficientValues .' + '\n' +
				    '?ThermoModelIRI ontokin:hasMinimumTemperature ?MinTemp .' + '\n' +
				    '?ThermoModelIRI ontokin:hasMaximumTemperature  ?MaxTemp .' + '\n' +
				    '?ThermoModelIRI ontokin:hasPressure  ?Pressure .' + '\n' +
	 				'}';
 				console.log("querystring:\n"+queryString);
 				//$('#unitsRSelection').prop('selectedIndex',0);
			} else if(search_querySelection == 'rateconstant') {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'SELECT ?MechanismName ?MechanismIRI ?ReactionIRI ?ActivationEnergy ?ActivationEnergyUnits ?PreExpFactor ?PreExpFactorUnits ?TempExponent ?TempExpUnits' + '\n' +
				'WHERE {' + '\n' +
					'?ReactionIRI ontokin:hasEquation \"' + search_term_name + '\" . ?ReactionIRI ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
			    	'?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				    '?ReactionIRI ontokin:hasArrheniusCoefficient ?ArrheniusRateCoefficients .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergy ?ActivationEnergy .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergyUnits ?ActivationEnergyUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactor ?PreExpFactor .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactorUnits ?PreExpFactorUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponent ?TempExponent .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponentUnits ?TempExpUnits .' + '\n' +
 				'}';

			} else if(search_querySelection == 'comparerate') {
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'SELECT ?MechanismName ?MechanismIRI ?ReactionIRI ?ActivationEnergy ?ActivationEnergyUnits ?PreExpFactor ?PreExpFactorUnits ?TempExponent ?TempExpUnits' + '\n' +
				'WHERE {' + '\n' +
					'?ReactionIRI ontokin:hasEquation \"' + search_term_name + '\" . ?ReactionIRI ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
			    	'?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				    '?ReactionIRI ontokin:hasArrheniusCoefficient ?ArrheniusRateCoefficients .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergy ?ActivationEnergy .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergyUnits ?ActivationEnergyUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactor ?PreExpFactor .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactorUnits ?PreExpFactorUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponent ?TempExponent .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponentUnits ?TempExpUnits .' + '\n' +
 				'}';
			} else if(search_querySelection == 'rateconstantAnyOrder' && reactantArray.length == 2 && productArray.length == 2) {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'PREFIX reaction_mechanism: <http://www.theworldavatar.com/ontology/ontocape/material/substance/reaction_mechanism.owl#>' + '\n' +
				'SELECT ?MechanismName ?MechanismIRI ?ReactionIRI ?ActivationEnergy ?ActivationEnergyUnits ?PreExpFactor ?PreExpFactorUnits ?TempExponent ?TempExpUnits' + '\n' +
				'WHERE {' + '\n' +
		           '?ReactionIRI reaction_mechanism:hasProduct ?Product1 .' + '\n' + 
		              	   '?Product1 owl:sameAs ?Species1 .' + '\n' +
		            	   '?Species1 rdfs:label \"' + productArray[0].trim() + '\" .' + '\n' +  
		                   '?ReactionIRI reaction_mechanism:hasProduct ?Product2 .' + '\n' +
		              	   '?Product2 owl:sameAs ?Species2 .' + '\n' +
		              	   '?Species2 rdfs:label \"' + productArray[1].trim() + '\" .' + '\n' + 
		                   '?ReactionIRI reaction_mechanism:hasReactant ?Reactant1 .' + '\n' +
		              	   '?Reactant1 owl:sameAs ?Species3 .' + '\n' + 
		            	   '?Species3 rdfs:label \"' + reactantArray[0].trim() + '\" .' + '\n' + 
		                   '?ReactionIRI reaction_mechanism:hasReactant ?Reactant2 .' + '\n' +
		              	   '?Reactant2 owl:sameAs ?Species4 .' + '\n' + 
		              	   '?Species4 rdfs:label \"' + reactantArray[1].trim() + '\" .' + '\n' +
					'?ReactionIRI ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .' + '\n' +
			    	'?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				    '?ReactionIRI ontokin:hasArrheniusCoefficient ?ArrheniusRateCoefficients .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergy ?ActivationEnergy .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergyUnits ?ActivationEnergyUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactor ?PreExpFactor .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactorUnits ?PreExpFactorUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponent ?TempExponent .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponentUnits ?TempExpUnits .' + '\n' +
 				'}';
 				
 				console.log("querystring:\n"+queryString);
 				console.log("\n reactant1:\n"+reactantArray[0].trim());
 				console.log("\n reactant2:\n"+reactantArray[1].trim());
 				console.log("\n product1:\n"+productArray[0].trim());
 				console.log("\n product2:\n"+productArray[1].trim());

			} else if(search_querySelection == 'rateconstantAnyOrder' && reactantArray.length == 1 && productArray.length == 2) {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'PREFIX reaction_mechanism: <http://www.theworldavatar.com/ontology/ontocape/material/substance/reaction_mechanism.owl#>'+ '\n' +
				'SELECT ?MechanismName ?MechanismIRI ?ReactionIRI ?ActivationEnergy ?ActivationEnergyUnits ?PreExpFactor ?PreExpFactorUnits ?TempExponent ?TempExpUnits' + '\n' +
				'WHERE {' + '\n' +
		                   '?ReactionIRI reaction_mechanism:hasProduct ?Product1 .'+ '\n' + 
		              	   '?Product1 owl:sameAs ?Species1 .'+ '\n' +
		            	   '?Species1 rdfs:label \"' + productArray[0].trim() + '\" .'+ '\n' +  
		                   '?ReactionIRI reaction_mechanism:hasProduct ?Product2 .'+ '\n' +
		              	   '?Product2 owl:sameAs ?Species2 .'+ '\n' +
		              	   '?Species2 rdfs:label \"' + productArray[1].trim() + '\" .'+ '\n' + 
		                   '?ReactionIRI reaction_mechanism:hasReactant ?Reactant1 .'
		              	   '?Reactant1 owl:sameAs ?Species3 .'+ '\n' + 
		            	   '?Species3 rdfs:label \"' + reactantArray[0].trim() + '\" .'+ '\n' + 
					'?ReactionIRI ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
			    	'?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				    '?ReactionIRI ontokin:hasArrheniusCoefficient ?ArrheniusRateCoefficients .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergy ?ActivationEnergy .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergyUnits ?ActivationEnergyUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactor ?PreExpFactor .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactorUnits ?PreExpFactorUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponent ?TempExponent .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponentUnits ?TempExpUnits .' + '\n' +
 				'}';

			} else if(search_querySelection == 'rateconstantAnyOrder' && reactantArray.length == 2 && productArray.length == 1) {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'PREFIX reaction_mechanism: <http://www.theworldavatar.com/ontology/ontocape/material/substance/reaction_mechanism.owl#>'+ '\n' +
				'SELECT ?MechanismName ?MechanismIRI ?ReactionIRI ?ActivationEnergy ?ActivationEnergyUnits ?PreExpFactor ?PreExpFactorUnits ?TempExponent ?TempExpUnits' + '\n' +
				'WHERE {' + '\n' +
		           		   '?ReactionIRI reaction_mechanism:hasProduct ?Product1 .'+ '\n' + 
		              	   '?Product1 owl:sameAs ?Species1 .'+ '\n' +
		            	   '?Species1 rdfs:label \"' + productArray[0].trim() + '\" .'+ '\n' +  
		                   '?ReactionIRI reaction_mechanism:hasReactant ?Reactant1 .'
		              	   '?Reactant1 owl:sameAs ?Species3 .'+ '\n' + 
		            	   '?Species3 rdfs:label \"' + reactantArray[0].trim() + '\" .'+ '\n' + 
		                   '?ReactionIRI reaction_mechanism:hasReactant ?Reactant2 .'+ '\n' +
		              	   '?Reactant2 owl:sameAs ?Species4 .'+ '\n' + 
		              	   '?Species4 rdfs:label \"' + reactantArray[1].trim() + '\" .'+ '\n' +
					'?ReactionIRI ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
			    	'?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				    '?ReactionIRI ontokin:hasArrheniusCoefficient ?ArrheniusRateCoefficients .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergy ?ActivationEnergy .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergyUnits ?ActivationEnergyUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactor ?PreExpFactor .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactorUnits ?PreExpFactorUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponent ?TempExponent .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponentUnits ?TempExpUnits .' + '\n' +
 				'}';

			} else if(search_querySelection == 'comparerateAnyOrder' && reactantArray.length == 2 && productArray.length == 2) {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'PREFIX reaction_mechanism: <http://www.theworldavatar.com/ontology/ontocape/material/substance/reaction_mechanism.owl#>' + '\n' +
				'SELECT ?MechanismName ?MechanismIRI ?ReactionIRI ?ActivationEnergy ?ActivationEnergyUnits ?PreExpFactor ?PreExpFactorUnits ?TempExponent ?TempExpUnits' + '\n' +
				'WHERE {' + '\n' +
		           '?ReactionIRI reaction_mechanism:hasProduct ?Product1 .' + '\n' + 
		              	   '?Product1 owl:sameAs ?Species1 .' + '\n' +
		            	   '?Species1 rdfs:label \"' + productArray[0].trim() + '\" .' + '\n' +  
		                   '?ReactionIRI reaction_mechanism:hasProduct ?Product2 .' + '\n' +
		              	   '?Product2 owl:sameAs ?Species2 .' + '\n' +
		              	   '?Species2 rdfs:label \"' + productArray[1].trim() + '\" .' + '\n' + 
		                   '?ReactionIRI reaction_mechanism:hasReactant ?Reactant1 .' + '\n' +
		              	   '?Reactant1 owl:sameAs ?Species3 .' + '\n' + 
		            	   '?Species3 rdfs:label \"' + reactantArray[0].trim() + '\" .' + '\n' + 
		                   '?ReactionIRI reaction_mechanism:hasReactant ?Reactant2 .' + '\n' +
		              	   '?Reactant2 owl:sameAs ?Species4 .' + '\n' + 
		              	   '?Species4 rdfs:label \"' + reactantArray[1].trim() + '\" .' + '\n' +
					'?ReactionIRI ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .' + '\n' +
			    	'?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				    '?ReactionIRI ontokin:hasArrheniusCoefficient ?ArrheniusRateCoefficients .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergy ?ActivationEnergy .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergyUnits ?ActivationEnergyUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactor ?PreExpFactor .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactorUnits ?PreExpFactorUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponent ?TempExponent .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponentUnits ?TempExpUnits .' + '\n' +
 				'}';
 				
 				console.log("querystring:\n"+queryString);
 				console.log("\n reactant1:\n"+reactantArray[0].trim());
 				console.log("\n reactant2:\n"+reactantArray[1].trim());
 				console.log("\n product1:\n"+productArray[0].trim());
 				console.log("\n product2:\n"+productArray[1].trim());

			} else if(search_querySelection == 'comparerateAnyOrder' && reactantArray.length == 1 && productArray.length == 2) {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'PREFIX reaction_mechanism: <http://www.theworldavatar.com/ontology/ontocape/material/substance/reaction_mechanism.owl#>'+ '\n' +
				'SELECT ?MechanismName ?MechanismIRI ?ReactionIRI ?ActivationEnergy ?ActivationEnergyUnits ?PreExpFactor ?PreExpFactorUnits ?TempExponent ?TempExpUnits' + '\n' +
				'WHERE {' + '\n' +
		                   '?ReactionIRI reaction_mechanism:hasProduct ?Product1 .'+ '\n' + 
		              	   '?Product1 owl:sameAs ?Species1 .'+ '\n' +
		            	   '?Species1 rdfs:label \"' + productArray[0].trim() + '\" .'+ '\n' +  
		                   '?ReactionIRI reaction_mechanism:hasProduct ?Product2 .'+ '\n' +
		              	   '?Product2 owl:sameAs ?Species2 .'+ '\n' +
		              	   '?Species2 rdfs:label \"' + productArray[1].trim() + '\" .'+ '\n' + 
		                   '?ReactionIRI reaction_mechanism:hasReactant ?Reactant1 .'
		              	   '?Reactant1 owl:sameAs ?Species3 .'+ '\n' + 
		            	   '?Species3 rdfs:label \"' + reactantArray[0].trim() + '\" .'+ '\n' + 
					'?ReactionIRI ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
			    	'?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				    '?ReactionIRI ontokin:hasArrheniusCoefficient ?ArrheniusRateCoefficients .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergy ?ActivationEnergy .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergyUnits ?ActivationEnergyUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactor ?PreExpFactor .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactorUnits ?PreExpFactorUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponent ?TempExponent .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponentUnits ?TempExpUnits .' + '\n' +
 				'}';

			} else if(search_querySelection == 'comparerateAnyOrder' && reactantArray.length == 2 && productArray.length == 1) {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'PREFIX reaction_mechanism: <http://www.theworldavatar.com/ontology/ontocape/material/substance/reaction_mechanism.owl#>'+ '\n' +
				'SELECT ?MechanismName ?MechanismIRI ?ReactionIRI ?ActivationEnergy ?ActivationEnergyUnits ?PreExpFactor ?PreExpFactorUnits ?TempExponent ?TempExpUnits' + '\n' +
				'WHERE {' + '\n' +
		           		   '?ReactionIRI reaction_mechanism:hasProduct ?Product1 .'+ '\n' + 
		              	   '?Product1 owl:sameAs ?Species1 .'+ '\n' +
		            	   '?Species1 rdfs:label \"' + productArray[0].trim() + '\" .'+ '\n' +  
		                   '?ReactionIRI reaction_mechanism:hasReactant ?Reactant1 .'
		              	   '?Reactant1 owl:sameAs ?Species3 .'+ '\n' + 
		            	   '?Species3 rdfs:label \"' + reactantArray[0].trim() + '\" .'+ '\n' + 
		                   '?ReactionIRI reaction_mechanism:hasReactant ?Reactant2 .'+ '\n' +
		              	   '?Reactant2 owl:sameAs ?Species4 .'+ '\n' + 
		              	   '?Species4 rdfs:label \"' + reactantArray[1].trim() + '\" .'+ '\n' +
					'?ReactionIRI ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
			    	'?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				    '?ReactionIRI ontokin:hasArrheniusCoefficient ?ArrheniusRateCoefficients .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergy ?ActivationEnergy .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergyUnits ?ActivationEnergyUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactor ?PreExpFactor .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactorUnits ?PreExpFactorUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponent ?TempExponent .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponentUnits ?TempExpUnits .' + '\n' +
 				'}';

			}
		
		let queryResultsTable = $("#table-query-results");
		$("#num-results").text("");
		$(".row-query-results").remove();
		
		$("#spinner").show();
		$.ajax({
			type: 'GET',
			//url: "http://www.theworldavatar.com/OntoKinGUI/OntoKinEndpointProxy",
			url: "http://localhost:8080/OntoKinGUI/OntoKinEndpointProxy",
			data: {queryString},
			success: data => {
				let trimmedResult = data.slice(1, data.length-2);
				let resultArray = trimmedResult.split('}');
				if (resultArray.length == 1) {
					$("#chartCanvas").hide();
					$("#chartCanvasRateAE").hide();
					$("#tableMechanism").hide();
					$("#noResult").show();
				} else {
					resultArray.pop();
					
					firstResult = resultArray[0] + '}';
					firstResult = firstResult.replace(/\n +/g, "");
					firstResultObj = JSON.parse(firstResult);
					$(".row-header").append(`<th class="row-query-results first-column">Index</th>`);
					for (let x in firstResultObj) {
						var theader = '<th class="row-query-results">' + x +'</th>';
						$(".row-header").append(theader);
					}								

					var chartLabel = [];
					var chartLabelsThermo = [];
					var chartLabelsRate = [];
					var co = [];
 					var co_1 = [];
					var co_2 = [];
					var co_3 = [];
					var co_4 = [];
					var co_5 = [];
					var co_6 = [];
					var co_7 = [];
					
					var minTemp = [];
					var maxTemp = [];
					var midTemp = [];
					var pressure = [];
					
					var minTemperature = 0;
					var maxTemperature = 0;
					var midTemperature = 0;
					var minTempForLabel = 0;
					var maxTempForLabel = 0;
					
					var maxTempInAllMech = -99999;
					var minTempInAllMech = 100000;
					
					var coLow = [];
					var coHigh = [];
					var CpAllMechs = [];
					var datasetsCp = [];
					var datasetsCpJMolK = [];
					var datasetsCpErgMolK = [];
					var datasetsCpNoDimension = [];
					var HAllMechs = [];
					var datasetsH = [];
					var datasetsHJMolK = [];
					var datasetsHErgMolK = [];
					var datasetsHNoDimension = [];
					var SAllMechs = [];
					var datasetsS = [];
					var datasetsSJMolK = [];
					var datasetsSErgMolK = [];
					var datasetsSNoDimension = [];
					
					var rateConstantAllMechs = [];
					
					var preExponentialFactor = 1;
					var activationEnergy = 1;
					var temperatureExponent = 0;
					var datasetsRCJMolK = [];
					
					var chartLabelMech = '';
					var chartLabelMechRateConstant = '';
					let count = 1;
					var countCoeffSequence = 0;

					var chartLabelRate = [];
					var ae = [];
					var ef = [];
					var te = [];
					
					for (let result of resultArray) {
						countCoeffSequence = countCoeffSequence + 1;
						jsonString = result + '}'
						jsonString = jsonString.replace(/\n +/g, "");
						resultObj = JSON.parse(jsonString);						
						$.each(resultObj, function(i, row) {
						  if(search_querySelection == 'compthermo'){
							if (i == 'CoefficientValues') {
					        	var coefficients = row.split(',');					        		
					        	$.each(coefficients, function(index, value) {
					        		 co[index] = value;
					        		 if(countCoeffSequence % 2 == 1){
					        			 coLow[index] = value; 
					        		 }
					        		 if(countCoeffSequence % 2 == 0){
					        			 coHigh[index] = value; 
					        		 }					        		 
					        	});	
					        	
					        	co_1.push(co[0]);
					        	co_2.push(co[1]); 
					        	co_3.push(co[2]); 
					        	co_4.push(co[3]); 
					        	co_5.push(co[4]); 
					        	co_6.push(co[5]); 
					        	co_7.push(co[6]); 

					        } else if (i == 'MinTemp') {					        	
				        		minTemp.push(row);
					        	if(countCoeffSequence % 2 == 1){
					        		minTemperature = row;
					        		if(row < minTempInAllMech){
					        			minTempInAllMech = row;
					        		}
					        	}
					        	if(countCoeffSequence == 1){
					        		minTempForLabel = row;
					        	}
					        	
					        } else if (i == 'MaxTemp') {
				        		maxTemp.push(row);
					        	if(countCoeffSequence % 2 == 0){
					        		maxTemperature = row;
					        		if(row > maxTempInAllMech){
					        			maxTempInAllMech = row;
					        		}
					        	}
					        	if(countCoeffSequence % 2 == 1){
					        		midTemperature = row;
					        	}
					        	if(countCoeffSequence == 2){
					        		maxTempForLabel = row;
					        	}
					        	
					        } else if (i == 'MechanismName') {
					        	if(countCoeffSequence % 2 == 0){
					        		chartLabelMech = row;
					        		console.log('chartLabelMech');
					        		console.log(chartLabelMech);
					        		chartLabel.push(formatLabel(row) + ' (HTR)');
					        	} else{
					        		chartLabel.push(formatLabel(row) + ' (LTR)');
					        	}
					        	
					        } else if (i == 'Pressure') {
					        	pressure.push(row);
					        }
						  } else if(search_querySelection == 'comparerate' || search_querySelection == 'comparerateAnyOrder'){
								if (i == 'ActivationEnergy') {					        	
						        	ae.push(row);
						        	activationEnergy = row;
						        } else if (i == 'PreExpFactor') {
						        	ef.push(row);
						        	preExponentialFactor = row;
						        } else if (i == 'TempExponent') {
						        	te.push(row);
						        	temperatureExponent = row;
						        } else if (i == 'MechanismName') {
						        		chartLabelRate.push(formatLabel(row));
						        		chartLabelMechRateConstant = row;
						        }					  
						  }
					      });
						
						if(countCoeffSequence % 2 == 0 && search_querySelection == 'compthermo'){
							console.log('countCoeffSequence');
							let color = '#'+(Math.random()*0xFFFFFF<<0).toString(16);
							console.log('color');
							console.log(color);
							var Cp = calculateCp('-1', coLow, coHigh, minTemperature, midTemperature, maxTemperature);
							console.log('Cp:' + Cp);
							datasetsCp.push({
								label: chartLabelMech,
								backgroundColor: color,
								borderColor: color,
								data: Cp,
								fill: false
							});
							console.log('datasetsCp:'+datasetsCp);
							var CpJMolK = calculateCp('jmolk', coLow, coHigh, minTemperature, midTemperature, maxTemperature);
							console.log('CpJMolK:' + CpJMolK);
							datasetsCpJMolK.push({
								label: chartLabelMech,
								backgroundColor: color,
								borderColor: color,
								data: CpJMolK,
								fill: false
							});
							console.log('datasetsCp:'+datasetsCp);
							var CpErgMolK = calculateCp('ergmolk', coLow, coHigh, minTemperature, midTemperature, maxTemperature);
							console.log('CpErgMolK:' + CpErgMolK);
							datasetsCpErgMolK.push({
								label: chartLabelMech,
								backgroundColor: color,
								borderColor: color,
								data: CpErgMolK,
								fill: false
							});
							console.log('datasetsCp:'+datasetsCp);
							var CpDimensionLess = calculateCp('dimensionless', coLow, coHigh, minTemperature, midTemperature, maxTemperature);
							console.log('CpDimensionLess:' + CpDimensionLess);
							datasetsCpNoDimension.push({
								label: chartLabelMech,
								backgroundColor: color,
								borderColor: color,
								data: CpDimensionLess,
								fill: false
							});
							console.log('datasetsCp:'+datasetsCp);
							console.log('countCoeffSequence');

							var H = calculateH('-1', coLow, coHigh, minTemperature, midTemperature, maxTemperature);
							console.log('H:' + H);
							datasetsH.push({
								label: chartLabelMech,
								backgroundColor: color,
								borderColor: color,
								data: H,
								fill: false
							});
							console.log('datasetsH:'+datasetsH);
							var HJMolK = calculateH('jmolk', coLow, coHigh, minTemperature, midTemperature, maxTemperature);
							console.log('HJMolK:' + HJMolK);
							datasetsHJMolK.push({
								label: chartLabelMech,
								backgroundColor: color,
								borderColor: color,
								data: HJMolK,
								fill: false
							});
							console.log('datasetsH:'+datasetsH);
							var HErgMolK = calculateH('ergmolk', coLow, coHigh, minTemperature, midTemperature, maxTemperature);
							console.log('HErgMolK:' + HErgMolK);
							datasetsHErgMolK.push({
								label: chartLabelMech,
								backgroundColor: color,
								borderColor: color,
								data: HErgMolK,
								fill: false
							});
							console.log('datasetsH:'+datasetsH);
							var HDimensionLess = calculateH('dimensionless', coLow, coHigh, minTemperature, midTemperature, maxTemperature);
							console.log('HDimensionLess:' + HDimensionLess);
							datasetsHNoDimension.push({
								label: chartLabelMech,
								backgroundColor: color,
								borderColor: color,
								data: HDimensionLess,
								fill: false
							});
							console.log('datasetsH:'+datasetsH);

							var S = calculateS('-1', coLow, coHigh, minTemperature, midTemperature, maxTemperature);
							console.log('S:' + S);
							datasetsS.push({
								label: chartLabelMech,
								backgroundColor: color,
								borderColor: color,
								data: S,
								fill: false
							});
							console.log('datasetsS:'+datasetsS);
							var SJMolK = calculateS('jmolk', coLow, coHigh, minTemperature, midTemperature, maxTemperature);
							console.log('SJMolK:' + SJMolK);
							datasetsSJMolK.push({
								label: chartLabelMech,
								backgroundColor: color,
								borderColor: color,
								data: SJMolK,
								fill: false
							});
							console.log('datasetsS:'+datasetsS);
							var SErgMolK = calculateS('ergmolk', coLow, coHigh, minTemperature, midTemperature, maxTemperature);
							console.log('SErgMolK:' + SErgMolK);
							datasetsSErgMolK.push({
								label: chartLabelMech,
								backgroundColor: color,
								borderColor: color,
								data: SErgMolK,
								fill: false
							});
							console.log('datasetsS:'+datasetsS);
							var SDimensionLess = calculateS('dimensionless', coLow, coHigh, minTemperature, midTemperature, maxTemperature);
							console.log('SDimensionLess:' + SDimensionLess);
							datasetsSNoDimension.push({
								label: chartLabelMech,
								backgroundColor: color,
								borderColor: color,
								data: SDimensionLess,
								fill: false
							});
							console.log('datasetsS:'+datasetsS);
						}
						
						if(search_querySelection == 'comparerate' || search_querySelection == 'comparerateAnyOrder'){
							let color = '#'+(Math.random()*0xFFFFFF<<0).toString(16);
							var RCJMolK = calculateRateConstant('jmolk', parseFloat(preExponentialFactor), parseFloat(temperatureExponent), parseFloat(activationEnergy));
							datasetsRCJMolK.push({
								label: chartLabelMechRateConstant,
								backgroundColor: color,
								borderColor: color,
								data: RCJMolK,
								fill: false,
							});
						}
						
 						if(countCoeffSequence >= resultArray.length && search_querySelection == 'compthermo'){
							console.log('minTempInAllMech');
							console.log(minTempInAllMech);
							console.log('maxTempInAllMech');
							console.log(maxTempInAllMech);
							chartLabelsThermo = getTemperatures(minTempInAllMech, maxTempInAllMech);
							console.log('chartLabelsThermo in conditional statement:');
							console.log(chartLabelsThermo);
							console.log('datasetsCp:');
							console.log(datasetsCp[0].label);
							console.log(datasetsCp[0].data);
						} 
						
 						if(countCoeffSequence >= resultArray.length && (search_querySelection == 'comparerate' || search_querySelection == 'comparerateAnyOrder')){
							chartLabelsRate = get1000OverTemperatures(300, 6000);
						} 
 						
						if (search_querySelection != 'compthermo' && search_querySelection != 'comparerate' && search_querySelection != 'comparerateAnyOrder') {
							queryResultsTable.append(getTableResultRowString(count++, resultObj));
							$("#chartCanvas").hide();
							$("#chartCanvasRateAE").hide();
							$("#tableMechanism").show();

						} else if(search_querySelection == 'compthermo' && countCoeffSequence >= resultArray.length){ // show chart
							$("#chartCanvas").show();
							$("#chartCanvasRateAE").hide();
							$("#tableMechanism").hide();
							$("canvas#canvas").remove();
							$("canvas#canvasJMolK").remove();
							$("canvas#canvasErgMolK").remove();
							$("canvas#canvasNoDimension").remove();
							$("canvas#canvasH").remove();
							$("canvas#canvasHJMolK").remove();
							$("canvas#canvasHErgMolK").remove();
							$("canvas#canvasHNoDimension").remove();							
							$("canvas#canvasS").remove();
							$("canvas#canvasSJMolK").remove();
							$("canvas#canvasSErgMolK").remove();
							$("canvas#canvasSNoDimension").remove();							
							$("div#chartCanvas").append('<canvas id="canvas" class="animated fadeIn" style="width:450px !important; height:188px; display:none"></canvas>');
							$("div#chartCanvas").append('<canvas id="canvasJMolK" class="animated fadeIn" style="width:450px !important; height:188px; display:none"></canvas>');
							$("div#chartCanvas").append('<canvas id="canvasErgMolK" class="animated fadeIn" style="width:450px !important; height:188px; display:none"></canvas>');
							$("div#chartCanvas").append('<canvas id="canvasNoDimension" class="animated fadeIn" style="width:450px !important; height:188px; display:none"></canvas>');
							$("div#chartCanvas").append('<canvas id="canvasH" class="animated fadeIn" style="width:450px !important; height:188px; display:none"></canvas>');
							$("div#chartCanvas").append('<canvas id="canvasHJMolK" class="animated fadeIn" style="width:450px !important; height:188px; display:none"></canvas>');
							$("div#chartCanvas").append('<canvas id="canvasHErgMolK" class="animated fadeIn" style="width:450px !important; height:188px; display:none"></canvas>');
							$("div#chartCanvas").append('<canvas id="canvasHNoDimension" class="animated fadeIn" style="width:450px !important; height:188px; display:none"></canvas>');
							$("div#chartCanvas").append('<canvas id="canvasS" class="animated fadeIn" style="width:450px !important; height:188px; display:none"></canvas>');
							$("div#chartCanvas").append('<canvas id="canvasSJMolK" class="animated fadeIn" style="width:450px !important; height:188px; display:none"></canvas>');
							$("div#chartCanvas").append('<canvas id="canvasSErgMolK" class="animated fadeIn" style="width:450px !important; height:188px; display:none"></canvas>');
							$("div#chartCanvas").append('<canvas id="canvasSNoDimension" class="animated fadeIn" style="width:450px !important; height:188px; display:none"></canvas>');
							if (search_unitsRSelection.indexOf('-1') > -1) {
						 		$("canvas#canvas").show();
						 		$("canvas#canvasH").show();
						 		$("canvas#canvasS").show();
						 	} else if (search_unitsRSelection.indexOf('jmolk') > -1) {
						 		$("canvas#canvasJMolK").show();
						 		$("canvas#canvasHJMolK").show();
						 		$("canvas#canvasSJMolK").show();
						    } else if (search_unitsRSelection.indexOf('ergmolk') > -1){
						    	$("canvas#canvasErgMolK").show();
						    	$("canvas#canvasHErgMolK").show();
						    	$("canvas#canvasSErgMolK").show();
						    } else if (search_unitsRSelection.indexOf('dimensionless') > -1){
						    	$("canvas#canvasNoDimension").show();
						    	$("canvas#canvasHNoDimension").show();
						    	$("canvas#canvasSNoDimension").show();
						    }
							var config = {
									type: 'line',
									data: {
										labels: chartLabelsThermo,
										datasets: datasetsCp,
									},
									options: {
										responsive: true,
										title: {
											display: true,
											text: 'Heat capacity at constant pressure evaluated as a function of temperature.'
										},
										tooltips: {
											mode: 'index',
											intersect: false,
										},
										hover: {
											mode: 'nearest',
											intersect: true
										},
										scales: {
											xAxes: [{
												display: true,
												scaleLabel: {
													display: true,
													labelString: 'Temperature (K)'
												}
											}],
											yAxes: [{
												display: true,
												scaleLabel: {
													display: true,
													labelString: 'Cp (kcal / K)'
												}
											}]
										}
									}
								};

								var ctx = document.getElementById('canvas').getContext('2d');
								var myChart = new Chart(ctx, config);

								var configJMolK = {
										type: 'line',
										data: {
											labels: chartLabelsThermo,
											datasets: datasetsCpJMolK,
										},
										options: {
											responsive: true,
											title: {
												display: true,
												text: 'Heat capacity at constant pressure evaluated as a function of temperature.'
											},
											tooltips: {
												mode: 'index',
												intersect: false,
											},
											hover: {
												mode: 'nearest',
												intersect: true
											},
											scales: {
												xAxes: [{
													display: true,
													scaleLabel: {
														display: true,
														labelString: 'Temperature (K)'
													}
												}],
												yAxes: [{
													display: true,
													scaleLabel: {
														display: true,
														labelString: 'Cp (J / K)'
													}
												}]
											}
										}
									};

									var ctxJMolK = document.getElementById('canvasJMolK').getContext('2d');
									var myChartJMolK = new Chart(ctxJMolK, configJMolK);
									
									var configErgMolK = {
											type: 'line',
											data: {
												labels: chartLabelsThermo,
												datasets: datasetsCpErgMolK,
											},
											options: {
												responsive: true,
												title: {
													display: true,
													text: 'Heat capacity at constant pressure evaluated as a function of temperature.'
												},
												tooltips: {
													mode: 'index',
													intersect: false,
												},
												hover: {
													mode: 'nearest',
													intersect: true
												},
												scales: {
													xAxes: [{
														display: true,
														scaleLabel: {
															display: true,
															labelString: 'Temperature (K)'
														}
													}],
													yAxes: [{
														type: 'logarithmic',														
														display: true,
														scaleLabel: {
															display: true,
															labelString: 'Cp (erg / K)'
														}
													}]
												}
											}
										};

										var ctxErgMolK = document.getElementById('canvasErgMolK').getContext('2d');
										var myChartErgMolK = new Chart(ctxErgMolK, configErgMolK);
										
										var configNoDimension = {
												type: 'line',
												data: {
													labels: chartLabelsThermo,
													datasets: datasetsCpNoDimension,
												},
												options: {
													responsive: true,
													title: {
														display: true,
														text: 'Heat capacity at constant pressure evaluated as a function of temperature.'
													},
													tooltips: {
														mode: 'index',
														intersect: false,
													},
													hover: {
														mode: 'nearest',
														intersect: true
													},
													scales: {
														xAxes: [{
															display: true,
															scaleLabel: {
																display: true,
																labelString: 'Temperature (K)'
															}
														}],
														yAxes: [{
															type: 'logarithmic',
															display: true,
															scaleLabel: {
																display: true,
																labelString: 'Cp ()'
															}
														}]
													}
												}
											};

											var ctxNoDimension = document.getElementById('canvasNoDimension').getContext('2d');
											var myChartNoDimension = new Chart(ctxNoDimension, configNoDimension);

											var configH = {
													type: 'line',
													data: {
														labels: chartLabelsThermo,
														datasets: datasetsH,
													},
													options: {
														responsive: true,
														title: {
															display: true,
															text: 'Enthalpy evaluated as a function of temperature.'
														},
														tooltips: {
															mode: 'index',
															intersect: false,
														},
														hover: {
															mode: 'nearest',
															intersect: true
														},
														scales: {
															xAxes: [{
																display: true,
																scaleLabel: {
																	display: true,
																	labelString: 'Temperature (K)'
																}
															}],
															yAxes: [{
																display: true,
																scaleLabel: {
																	display: true,
																	labelString: 'H (kcal)'
																}
															}]
														}
													}
												};

												var ctxH = document.getElementById('canvasH').getContext('2d');
												var myChartH = new Chart(ctxH, configH);

												var configHJMolK = {
														type: 'line',
														data: {
															labels: chartLabelsThermo,
															datasets: datasetsHJMolK,
														},
														options: {
															responsive: true,
															title: {
																display: true,
																text: 'Enthalpy evaluated as a function of temperature.'
															},
															tooltips: {
																mode: 'index',
																intersect: false,
															},
															hover: {
																mode: 'nearest',
																intersect: true
															},
															scales: {
																xAxes: [{
																	display: true,
																	scaleLabel: {
																		display: true,
																		labelString: 'Temperature (K)'
																	}
																}],
																yAxes: [{
																	display: true,
																	scaleLabel: {
																		display: true,
																		labelString: 'H (J)'
																	}
																}]
															}
														}
													};

													var ctxHJMolK = document.getElementById('canvasHJMolK').getContext('2d');
													var myChartHJMolK = new Chart(ctxHJMolK, configHJMolK);
													
													var configHErgMolK = {
															type: 'line',
															data: {
																labels: chartLabelsThermo,
																datasets: datasetsHErgMolK,
															},
															options: {
																responsive: true,
																title: {
																	display: true,
																	text: 'Enthalpy evaluated as a function of temperature.'
																},
																tooltips: {
																	mode: 'index',
																	intersect: false,
																},
																hover: {
																	mode: 'nearest',
																	intersect: true
																},
																scales: {
																	xAxes: [{
																		display: true,
																		scaleLabel: {
																			display: true,
																			labelString: 'Temperature (K)'
																		}
																	}],
																	yAxes: [{
																		type: 'logarithmic',														
																		display: true,
																		scaleLabel: {
																			display: true,
																			labelString: 'H (erg)'
																		}
																	}]
																}
															}
														};

														var ctxHErgMolK = document.getElementById('canvasHErgMolK').getContext('2d');
														var myChartHErgMolK = new Chart(ctxHErgMolK, configHErgMolK);
														
														var configHNoDimension = {
																type: 'line',
																data: {
																	labels: chartLabelsThermo,
																	datasets: datasetsHNoDimension,
																},
																options: {
																	responsive: true,
																	title: {
																		display: true,
																		text: 'Enthalpy evaluated as a function of temperature.'
																	},
																	tooltips: {
																		mode: 'index',
																		intersect: false,
																	},
																	hover: {
																		mode: 'nearest',
																		intersect: true
																	},
																	scales: {
																		xAxes: [{
																			display: true,
																			scaleLabel: {
																				display: true,
																				labelString: 'Temperature (K)'
																			}
																		}],
																		yAxes: [{
																			type: 'logarithmic',
																			display: true,
																			scaleLabel: {
																				display: true,
																				labelString: 'H'
																			}
																		}]
																	}
																}
															};

															var ctxHNoDimension = document.getElementById('canvasHNoDimension').getContext('2d');
															var myChartHNoDimension = new Chart(ctxHNoDimension, configHNoDimension);											
											

															var configS = {
																	type: 'line',
																	data: {
																		labels: chartLabelsThermo,
																		datasets: datasetsS,
																	},
																	options: {
																		responsive: true,
																		title: {
																			display: true,
																			text: 'Entropy evaluated as a function of temperature.'
																		},
																		tooltips: {
																			mode: 'index',
																			intersect: false,
																		},
																		hover: {
																			mode: 'nearest',
																			intersect: true
																		},
																		scales: {
																			xAxes: [{
																				display: true,
																				scaleLabel: {
																					display: true,
																					labelString: 'Temperature (K)'
																				}
																			}],
																			yAxes: [{
																				display: true,
																				scaleLabel: {
																					display: true,
																					labelString: 'S (kcal / K)'
																				}
																			}]
																		}
																	}
																};

																var ctxS = document.getElementById('canvasS').getContext('2d');
																var myChartS = new Chart(ctxS, configS);

																var configSJMolK = {
																		type: 'line',
																		data: {
																			labels: chartLabelsThermo,
																			datasets: datasetsSJMolK,
																		},
																		options: {
																			responsive: true,
																			title: {
																				display: true,
																				text: 'Entropy evaluated as a function of temperature.'
																			},
																			tooltips: {
																				mode: 'index',
																				intersect: false,
																			},
																			hover: {
																				mode: 'nearest',
																				intersect: true
																			},
																			scales: {
																				xAxes: [{
																					display: true,
																					scaleLabel: {
																						display: true,
																						labelString: 'Temperature (K)'
																					}
																				}],
																				yAxes: [{
																					display: true,
																					scaleLabel: {
																						display: true,
																						labelString: 'H (J / K)'
																					}
																				}]
																			}
																		}
																	};

																	var ctxSJMolK = document.getElementById('canvasSJMolK').getContext('2d');
																	var myChartSJMolK = new Chart(ctxSJMolK, configSJMolK);
																	
																	var configSErgMolK = {
																			type: 'line',
																			data: {
																				labels: chartLabelsThermo,
																				datasets: datasetsSErgMolK,
																			},
																			options: {
																				responsive: true,
																				title: {
																					display: true,
																					text: 'Entropy evaluated as a function of temperature.'
																				},
																				tooltips: {
																					mode: 'index',
																					intersect: false,
																				},
																				hover: {
																					mode: 'nearest',
																					intersect: true
																				},
																				scales: {
																					xAxes: [{
																						display: true,
																						scaleLabel: {
																							display: true,
																							labelString: 'Temperature (K)'
																						}
																					}],
																					yAxes: [{
																						type: 'logarithmic',														
																						display: true,
																						scaleLabel: {
																							display: true,
																							labelString: 'S (erg / K)'
																						}
																					}]
																				}
																			}
																		};

																		var ctxSErgMolK = document.getElementById('canvasSErgMolK').getContext('2d');
																		var myChartSErgMolK = new Chart(ctxSErgMolK, configSErgMolK);
																		
																		var configSNoDimension = {
																				type: 'line',
																				data: {
																					labels: chartLabelsThermo,
																					datasets: datasetsSNoDimension,
																				},
																				options: {
																					responsive: true,
																					title: {
																						display: true,
																						text: 'Entropy evaluated as a function of temperature.'
																					},
																					tooltips: {
																						mode: 'index',
																						intersect: false,
																					},
																					hover: {
																						mode: 'nearest',
																						intersect: true
																					},
																					scales: {
																						xAxes: [{
																							display: true,
																							scaleLabel: {
																								display: true,
																								labelString: 'Temperature (K)'
																							}
																						}],
																						yAxes: [{
																							type: 'logarithmic',
																							display: true,
																							scaleLabel: {
																								display: true,
																								labelString: 'S (-)'
																							}
																						}]
																					}
																				}
																			};

																			var ctxSNoDimension = document.getElementById('canvasSNoDimension').getContext('2d');
																			var myChartSNoDimension = new Chart(ctxSNoDimension, configSNoDimension);											
															
						} else if((search_querySelection == 'comparerate' || search_querySelection == 'comparerateAnyOrder')  && countCoeffSequence >= resultArray.length){ // show chart
							$("#chartCanvasRateAE").show();
							$("#chartCanvas").hide();
							$("#tableMechanism").hide();
							$("canvas#canvasRateAE").remove();
							$("div#chartCanvasRateAE").append('<canvas id="canvasRateAE" class="animated fadeIn" style="width:1200px !important; height:800px"></canvas>');
							var configRateConstant = {
									type: 'line',
									data: {
										labels: chartLabelsRate,
										datasets: datasetsRCJMolK
									},
									options: {
										responsive: true,
										title: {
											display: true,
											text: ['Rate constant evaluated as a function of temperature.']
										},
										tooltips: {
											mode: 'index',
											intersect: false,
										},
										hover: {
											mode: 'nearest',
											intersect: true
										},
										scales: {
											xAxes: [{
												display: true,
												scaleLabel: {
													display: true,
													labelString: '1000 K/T (-)'
												}
											}],
											yAxes: [{
												type: 'logarithmic',
												display: true,
												scaleLabel: {
													display: true,
													labelString: 'Rate Constant (m3/mol/s)'
												}
											}]
										}
									}
								};

								var ctxRateConstant = document.getElementById('canvasRateAE').getContext('2d');
								var myChartRateConstant = new Chart(ctxRateConstant, configRateConstant);
						}
						
					}
					$("#num-results").text(`${count-1} results found.`);
				}

				$("#spinner").hide();
			},
			error: (XMLHttpRequest, textStatus, errorThrown) => { 
				alert("INCORRECT SPARQL QUERY!")
	        }
		})
		}
	})
	
});


</script>

</body>
</html>