<!DOCTYPE html>
<html lan="eng">

<head>
    <title>Gas Grid Visualisation</title>
	<meta charset="utf-8">

	<!-- External JS and CSS -->
    <script src='https://api.mapbox.com/mapbox-gl-js/v2.3.0/mapbox-gl.js'></script>
	<script src='https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js'></script>
	<script src='https://d3js.org/d3.v6.min.js'></script>
	<link href='https://api.tiles.mapbox.com/mapbox-gl-js/v2.3.0/mapbox-gl.css' rel='stylesheet' />

	<!--
	Remote CMCL JS and CSS are loaded from another file that is populated when building a Docker Image;
	this is because the imports differ for development and production environments, so a different
	version of 'head.html' can be generated depending on the target environment.

	If running locally (i.e. without Docker), copy 'head-dev.html' to 'head.html' temporarily.
	-->
	<?php include 'head.html'; ?>

	<!-- Local JS and CSS -->
    <script src='gas-grid-vis.js'></script>
    <link href='style.css' rel='stylesheet' />
</head>

<body>
    <div id="map"></div>
    <div id="tiltShift"></div>
	<div id="controlsParent"></div>
	
    <script>
		// Add MapBox controls (from mapbox-controls.js)
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

		// Pre-load the flow data for all input terminals
		loadFlowData();

        // Initialise the MapBox map
        mapboxgl.accessToken = 'pk.eyJ1IjoiY21jbGlubm92YXRpb25zIiwiYSI6ImNrbGdqa3RoNDFnanIyem1nZXR3YzVhcmwifQ.hVk983r6YYlmFE8kSMbzhA';
        var map = new mapboxgl.Map(getDefaultMapOptions());
        
		// Initialise MapBox popup for mouse-overs
		var popup =  popup = new mapboxgl.Popup({
			closeButton: false,
			closeOnClick: false
		});

        // Change some layer colors depending on selected style
		var inner_text = '#3E3E3E';
		var outer_text = '#FFFFFF';
		var pipe_color = '#B1B1B1';

		function terrainCallback(terrainType) {
			if(terrainType === "light") {
				pipe_color = '#B1B1B1';
                inner_text = '#3E3E3E';
		        outer_text = '#FFFFFF'; 
			} else {
				pipe_color = '#F6F6F4';
                inner_text = '#FFFFFF';
		        outer_text = '#000000'; 
			}
			console.log("INFO: Updated label colors.");
		}	
		
		function cameraCallback() {
			popup.remove();
			resetSidePanel();
		}

		// Setup map with mapbox-controls.js
		setup(map, cameraCallback, terrainCallback, null);

		// Add tilt shift effect
		// Note: I've disabled this for now as the map is becoming unresponsive.
		// Once we've had time for some optimisation, we can enable it.
		//addTiltShiftSupport();

		// On initial load...
		map.on("load", function() {
			resetSidePanel();
		});
		
        // On style loaded...
        map.on('style.load', function () {
			console.log("INFO: New style has been loaded.");
			refresh();

			// ============ TERMINALS ===========	
            // Add the geoJSON data for terminals
            map.addSource('terminals-source', {
                'type': 'geojson',
                'data': '/geoJSON_assets/terminals.geojson'
            });

            // Layer style for terminals
            map.addLayer({
                'id': 'terminal-locations',
                'type': 'circle',
                'source': 'terminals-source',
				'layout': {
					'visibility': 'visible'
				},
                'paint': {
                    'circle-radius': 8,
                    'circle-color': '#108dcc',
                    "circle-stroke-width": 1,
                    "circle-stroke-color": '#FFFFFF',
                }
            });

            map.addLayer({
                id: 'terminal-labels',
                type: 'symbol',
                source: 'terminals-source',
				minzoom: 7.5,
                layout: {
                    'text-field': ['concat', ['get', 'name'], '\n |\n|\n|'],
                    'text-font': ['DIN Offc Pro Medium', 'Arial Unicode MS Bold'],
                    'text-size': 15,
                    'text-offset': [0, -2.5],
					'text-max-width': 25,
					'visibility': 'visible'
                },
                "paint": {
                    "text-color": inner_text,
                    "text-halo-color": outer_text,
                    "text-halo-width": 1,
                    "text-opacity": ['interpolate', ['exponential', 2], ['zoom'], 5, 0, 7, 1]
                }
            });

			// On click handler within offtake layer
            map.on('click', 'terminal-locations', function (e) {
                var coordinates = e.features[0].geometry.coordinates.slice();
                var name = e.features[0].properties.name;
                var type = "Terminal";

                map.flyTo({
                    center: coordinates,
                    curve: 1.9,
                    speed: 1.6,
                    pitch: 60,
                    zoom: 16
                });

                // Update the side panel
                showTerminal(name, type, coordinates);
            });

            // On mouse enter within terminals layer
			map.on('mouseenter', 'terminal-locations', function (e) {
            	map.getCanvas().style.cursor = 'pointer';

				if(map.getZoom() < 7.5) {
					var coordinates = e.features[0].geometry.coordinates.slice();
					var name = e.features[0].properties["name"];

					var html = `
						<span style='color: #108dcc;'> ` + name + `</span><br>Terminal
						<br><p>Click for more details.</p>`;
					popup.setLngLat(coordinates).setHTML(html).addTo(map);
				}
            });

            // On mouse leave within terminals layer
            map.on('mouseleave', 'terminal-locations', function () {
                map.getCanvas().style.cursor = '';
				popup.remove();
            });

			// Register the pipe layer with mapbox-controls.js
			registerLayer("Terminals", ["terminal-locations", "terminal-labels"], "UK Gas Grid", true);
			// ============ TERMINALS ===========	


			// ============ OFFTAKES ===========	
            // Add the geoJSON data for offtakes
            map.addSource('offtakes-source', {
                type: 'geojson',
                data: '/geoJSON_assets/offtakes.geojson'
            });

            // Add layer style for offtakes
            map.addLayer({
                'id': 'offtake-locations',
                'type': 'circle',
                'source': 'offtakes-source',
				'layout': {
					'visibility': 'visible'
				},
                'paint': {
                    'circle-radius': 6,
                    'circle-color': '#B42222',
                    "circle-stroke-width": 1,
                    "circle-stroke-color": '#FFFFFF',
                }
            });

			map.addLayer({
                id: 'offtake-labels',
                type: 'symbol',
                source: 'offtakes-source',
				minzoom: 8.5,
                layout: {
                    'text-field': ['concat', ['get', 'Offtake Point (License Name)'], '\n Offtake Type: ', ['get', 'Type of Offtake'], '\n | \n | \n |'],
                    'text-font': ['DIN Offc Pro Medium', 'Arial Unicode MS Bold'],
                    'text-size': 15,
                    'text-offset': [0, -3.5],
					'text-max-width': 25,
					'visibility': 'visible'
                },
                "paint": {
                    "text-color": inner_text,
                    "text-halo-color": outer_text,
                    "text-halo-width": 1,
                    "text-opacity": ['interpolate', ['exponential', 2], ['zoom'], 5, 0, 7, 1]
                }
            });

            // On click handler within offtake layer
            map.on('click', 'offtake-locations', function (e) {
                var coordinates = e.features[0].geometry.coordinates.slice();
                var name = e.features[0].properties["Offtake Point (License Name)"];
                var type = e.features[0].properties["Type of Offtake"];
                var LinepackZone = e.features[0].properties["Linepack Zone"];
                var NTSExitArea = e.features[0].properties["NTS Exit Area"];
                var NTSExitZone = e.features[0].properties["NTS Exit Zone"];
                var Pipeline = e.features[0].properties["Connected to Pipeline"];
                console.log(coordinates,LinepackZone,NTSExitArea,NTSExitZone,Pipeline)

                // Fire selection logic
                showOfftake(name, type, coordinates,LinepackZone,NTSExitArea,NTSExitZone,Pipeline);

                map.flyTo({
                    center: coordinates,
                    curve: 1.9,
                    speed: 1.6,
                    pitch: 75,
                    zoom: 15.5
                });
            });

            // On mouse enter within offtake layer
            map.on('mouseenter', 'offtake-locations', function (e) {
                map.getCanvas().style.cursor = 'pointer';

				if(map.getZoom() < 8.5) {
					var coordinates = e.features[0].geometry.coordinates.slice();
					var name = e.features[0].properties["Offtake Point (License Name)"];
					var type = e.features[0].properties["Type of Offtake"];

					var html = `
						<span style='color: #B42222;'> ` + name + `</span><br>` + type +
						`<br><p>Click for more details.</p>`;
					popup.setLngLat(coordinates).setHTML(html).addTo(map);
				} 
            });

            // On mouse leave within offtake layer
            map.on('mouseleave', 'offtake-locations', function () {
                map.getCanvas().style.cursor = '';
				popup.remove();
            });

			// Register the pipe layer with mapbox-controls.js
			registerLayer("Offtakes", ["offtake-locations", "offtake-labels"], "UK Gas Grid", true);
			// ============ OFFTAKES ===========	


            // ============ PIPES ===========	
            // Add the geoJSON data for NTS
			map.addSource('pipes-source', {
				type: 'geojson',
				data: '/geoJSON_assets/pipe_network.geojson'
			});

            // Add layer style for NTS
            map.addLayer({
                'id': 'pipes-layer',
                'type': 'line',
                'source': 'pipes-source',
                'layout': {
                    'line-join': 'round',
                    'line-cap': 'round',
					'visibility': 'visible'
                },
                'paint': {
                    'line-color': pipe_color,
                    'line-width': 4
                }
            });

			// Register the pipe layer with mapbox-controls.js
			registerLayer("Pipes", ["pipes-layer"], "UK Gas Grid", true);
			// ============ PIPES ===========	

			// Ensure the terminal and offtake markers are the top layers
			map.moveLayer('offtake-locations');
			map.moveLayer('terminal-locations');

			// Build the layer selection tree
			buildLayerTree("checkbox");
			$("ul.checktree").checktree();
        });
    </script>
</body>

</html>