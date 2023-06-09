<!DOCTYPE html>
<html>

<head>
	<title>Power System Visualisation</title>
	<meta charset="utf-8">
	
	<!-- External JS and CSS -->
    <script src='https://api.mapbox.com/mapbox-gl-js/v2.3.0/mapbox-gl.js'></script>
	<script src='https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js'></script>
	<link href='https://api.tiles.mapbox.com/mapbox-gl-js/v2.3.0/mapbox-gl.css' rel='stylesheet' />

	<!--
	Remote CMCL JS and CSS are loaded from another file that is populated when building a Docker Image;
	this is because the imports differ for development and production environments, so a different
	version of 'head.html' can be generated depending on the target environment.

	If running locally (i.e. without Docker), copy 'head-dev.html' to 'head.html' temporarily.
	-->
	<?php include 'head.html'; ?>

	<!-- Local JS and CSS -->
	<script src='power-system.js'></script>
	<link href='style.css' rel='stylesheet' />
</head>

<body>
	<div id='map'></div>
	<div id="tiltShift"></div>
	<div id="controlsParent"></div>

	<script>
		var currentLayer = "power";

		// Add map controls
		document.getElementById("controlsParent").innerHTML = getControls();

		// Initial state of side-panel (from side-panel.js)
		initialiseSidePanel(document.getElementById("map"));
        resetSidePanel();

		// Show loading icon whilst stuff spins up
        var loadingHTML = `
            <br><br><br><br><br>
            <img width='150' src='loading.gif'></img>
            <br>
            <span style='color: grey; font-style: italic;'>Loading data, please wait...</span>
        `;
		appendSidePanelText(loadingHTML);

		// Initialise the MapBox map
		mapboxgl.accessToken = 'pk.eyJ1Ijoiam1hcnNkZW4iLCJhIjoiY2ttZzNqM3IxM2JyYzJ2bndzZnIxeG1lciJ9.uwb9ZnBO3vWssvcBsXcFeA';
		var map = new mapboxgl.Map(getDefaultMapOptions());

		// Initialise MapBox popup for mouse-overs
		var popup =  popup = new mapboxgl.Popup({
			closeButton: false,
			closeOnClick: false
		});

		// Change some layer colors depending on selected style
		var inner_text = '#3E3E3E';
		var outer_text = '#FFFFFF';
	
		function terrainCallback(terrainType) {
			if(terrainType === "light") {
                inner_text = '#3E3E3E';
		        outer_text = '#FFFFFF'; 
			} else {
                inner_text = '#FFFFFF';
		        outer_text = '#000000'; 
			}
			console.log("INFO: Updated label colors.");
		}		

		function cameraCallback() {
			popup.remove();
			resetSidePanel();
			updateLegend(currentLayer);
		}

		function layerCallback(layerID, enabled) {
			if(layerID == "power" && enabled) {
				map.setPaintProperty(
					"plant_locations",
					"circle-color",
					["get", "marker-color"]
				);

				currentLayer = "power";
				updateLegend("power");

			} else if(layerID == "indicator" && enabled) {
				map.setPaintProperty(
					"plant_locations",
					"circle-color",
					["get", "sdgcolor"]
				);

				currentLayer = "indicator";
				updateLegend("indicator");
			}
		}

		// Setup map with mapbox-controls.js
		setup(map, cameraCallback, terrainCallback, layerCallback);
	

		// Add tilt shift effect
		// Note: I've disabled this for now as the map is becoming unresponsive.
		// Once we've had time for some optimisation, we can enable it.
		//addTiltShiftSupport();

		// On initial load...
			map.on("load", function() {
			resetSidePanel();
			updateLegend(currentLayer);
		});

		map.on('style.load', function() {
			console.log("INFO: New style has been loaded.");
			refresh();
			
			// ============ PLANTS ===========	
			// Add the powerplant data
			map.addSource('powerplants', {
				type: 'geojson',
				data: 'UK_PowerPlants.geojson'
			});

			// Powerplant icons
			map.addLayer({
				'id': 'plant_locations',
				'type': 'circle',
				'source': 'powerplants',
				'layout': {
					'visibility': 'visible'
				},
				'paint': {
					'circle-radius': ["+", 3, ["*", 0.9, ["ln", ["to-number", ["get", "capacity"]]]]],
					'circle-color': (currentLayer == "power") ? ["get", "marker-color"] : ["get", "sdgcolor"],
					'circle-stroke-width': 1,
					'circle-stroke-color': inner_text,
				}
			});

			// Powerplant labels
			map.addLayer({
				id: 'plant_labels',
				type: 'symbol',
				source: 'powerplants',
				layout: {
					'visibility': 'visible',
					'text-field': ['concat',
						['get', 'name'],
						'\n Capacity: ',
						['get', 'capacity'],
						' MW \n Fuel: ',
						['get', 'fuel'],
						' \n | \n | \n | \n | ']
					,
					'text-font': ['DIN Offc Pro Medium', 'Arial Unicode MS Bold'],
					'text-size': 14,
					'text-offset': [0, -4],
				},
				"paint": {
					"text-color": inner_text,
					"text-halo-color": outer_text,
					"text-halo-width": 0.8,
					"text-opacity": ['interpolate', ['exponential', 2], ['zoom'], 8, 0, 10, 1]
				}
			});

			// On mouse enter
			map.on('mouseenter', 'plant_locations', function (e) {
				map.getCanvas().style.cursor = 'pointer';

				if(map.getZoom() < 7.5) {
					var coordinates = e.features[0].geometry.coordinates.slice();
					var name = e.features[0].properties["name"];
					name = name.replaceAll("_", " ");

					var fuel = e.features[0].properties["fuel"];

					var html = `
						<b> ` + name + `</b><br>
						Fuel: ` + fuel + `
						<br><p>Click for more details.</p>
					`;
					popup.setLngLat(coordinates).setHTML(html).addTo(map);
				}
			});

			// On mouse exit
			map.on('mouseleave', 'plant_locations', function (e) {
				map.getCanvas().style.cursor = '';
				popup.remove();
			});

			// On selection
			map.on('click', 'plant_locations', function (e) {
				var coordinates = e.features[0].geometry.coordinates.slice();
				var name = e.features[0].properties["name"];
				name = name.replaceAll("_", " ");

				var fuel = e.features[0].properties["fuel"];
				var capacity = e.features[0].properties["capacity"];
				var indicator = e.features[0].properties["sdg941"];

				selectPlant(name, fuel, capacity, indicator, coordinates);
				//updateLegend(currentLayer);

				map.flyTo({
					center: coordinates,
					curve: 1.9,
					speed: 1.6,
					pitch: 75,
					zoom: 14,
					bearing: Math.random() * 90
				});
			});

			// Ensure the plant locations is the top layer
			map.moveLayer('plant_locations');
		});

		// Register the power layer 
		registerLayer("Power Generation", ["power"], null, (currentLayer == "power"));
		registerLayer("SDG Indicator", ["indicator"], null, (currentLayer == "indicator"));
		// ============ PLANTS ===========	

		// Build the layer selection tree
		buildLayerTree("radio");
	</script>

</body>

</html>