<!DOCTYPE html>

<html lan="eng">
	<head>
		<title>Land Use Visualisation</title>
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
		<script src='land-use.js'></script>
		<script src='land-use-dict.js'></script>
		<link href='style.css' rel='stylesheet' />
	</head>

	<body>
		<div id="map"></div>
		<div id="tiltShift"></div>
		<div id="controlsParent"></div>

		<!-- Custom content for country controls -->
		<div id="countyContainer">
			<p>County:</p>
			<input type="radio" id="cambs" name="county" onclick="onCountyChange(0)" checked>
			<label for="cambs">Cambridgeshire</label>
			<br>
			<input type="radio" id="norf" name="county" onclick="onCountyChange(1)">
			<label for="norf">Norfolk</label>
			<br>
			<input type="radio" id="suff" name="county" onclick="onCountyChange(2)">
			<label for="suff">Suffolk</label>
		</div>

		<script>
			// URIs of tilesets for each county
			var datasets = [
				"mapbox://cmclinnovations.2r3dmaiz",	// Cambridgeshire
				"mapbox://cmclinnovations.aauu9r35",	// Norfolk
				"mapbox://cmclinnovations.cbyd44fh"		// Suffolk
			];

			// Fired on county selection
			function onCountyChange(value) {
				currentCounty = datasets[value];
				map.getSource("crop-map-data").setUrl(currentCounty);
			}

			// Currently selected dataset
			var currentCounty = datasets[0];

			// Create set of enabled crop IRIs
			var enabledCrops = new Set();
			Object.entries(crops).forEach(([iri, name]) => {
				enabledCrops.add(iri);
			});

			// Add MapBox controls (from mapbox-controls.js)
			document.getElementById("controlsParent").innerHTML = getControls();

			// Insert county controls
			var controls = document.getElementById("controlContainer");
			controls.insertBefore(
				document.getElementById("countyContainer"),
				controls.childNodes[controls.childNodes.length - 2]
			);

			// Override default bird camera defined in mapbox-controls.js
			overrideDefaultBird({
				curve: 1.9,
				speed: 1.6,
				zoom: 7.75,
				pitch: 0.0,
				bearing: 0.0,
				center: [0.46723, 52.34311]
			});

			// Override default pitch camera defined in mapbox-controls.js
			overrideDefaultPitch({
				curve: 1.9,
				speed: 1.6,
				zoom: 8.5,
				pitch: 65,
				bearing: -30,
				center: [0.60090, 52.25456]
			});

			// Override default map options defined in mapbox-controls.js
			overrideDefaultMap({
				container: "map",
				style: "mapbox://styles/mapbox/light-v10?optimize=true",
				center: [0.46723, 52.34311],
				zoom: 7.75,
				pitch: 0.0,
				bearing: 0.0
			});

			// Initial state of side-panel (from side-panel.js)
			initialiseSidePanel(document.getElementById("map"));
			resetSidePanel();

 			// Initialise the MapBox map
			mapboxgl.accessToken = 'pk.eyJ1IjoiY21jbGlubm92YXRpb25zIiwiYSI6ImNrbGdqa3RoNDFnanIyem1nZXR3YzVhcmwifQ.hVk983r6YYlmFE8kSMbzhA';
			var map = new mapboxgl.Map(getDefaultMapOptions());
			
			// Dynamically build filter expression for map
			function updateFilters() {
				var arr = Array.from(enabledCrops);
				var filter = ['in', 'name'];

				for(var i = 0; i < arr.length; i += 1) {
					filter.push(arr[i]);
				}

				// Apply the filter to the map
				map.setFilter("crop-map-layer", filter);
			}

			// Initialise MapBox popup for mouse-overs
			var popup =  popup = new mapboxgl.Popup({
				closeButton: false,
				closeOnClick: false
			});

			// Fired when camera changes
			function cameraCallback() {
				popup.remove();
				resetSidePanel();
			}

			// Fired when layer selection changes
			function layerCallback(layerIRI, enabled) {
				if(enabled) {
					enabledCrops.add(layerIRI);
				} else {
					enabledCrops.delete(layerIRI);
				}
				console.log("-");
				updateFilters();
			}

			// Setup map with mapbox-controls.js
			setup(map, cameraCallback, null, layerCallback);
			setupLayerControl();

			// Add tilt shift effect
			// Note: I've disabled this for now as the map is becoming unresponsive.
			// Once we've had time for some optimisation, we can enable it.
			//addTiltShiftSupport();

			// On initial load...
			map.on("load", function() {
				resetSidePanel();
			});

			function reloadMap() {
				console.log("INFO: New style has been loaded.");
				refresh();

				map.addSource('crop-map-data', {
					type: 'vector',
					url: currentCounty,
				});

				var layers = map.getStyle().layers;
				var firstSymbolId;
				for (var i = 0; i < layers.length; i++) {
					if (layers[i].type === 'symbol') {
						firstSymbolId = layers[i].id;
						break;
					}
				}

				map.addLayer({
					"id": "crop-map-layer",
					"source": "crop-map-data",
					"source-layer": "data",
            		"type": "circle",
					"layout": {},
					"paint": {
						"circle-opacity": 0.7,
						"circle-radius": [
							"interpolate",
							["linear"],
							["zoom"],
							10,
							3,
							20,
							15
                		],
                		"circle-color": colors
           			},
					firstSymbolId
				});

				// On click handler within offtake layer
				map.on('click', 'crop-map-layer', function (e) {
					var coordinates = e.features[0].geometry.coordinates.slice();
					var iri = e.features[0].properties["name"];
					var crop = crops[iri];

					// Fire selection logic
					showCrop(crop, coordinates);

					map.flyTo({
						center: coordinates,
						curve: 1.9,
						speed: 1.6,
						zoom: 15
					});
				});

				// Show popup on mouse over
				map.on('mouseenter', 'crop-map-layer', function (e) {
					// Change the cursor style as a UI indicator.
					map.getCanvas().style.cursor = 'pointer';
									
					// Get the human readable name of the crop
					var coordinates = e.features[0].geometry.coordinates.slice();
					var name = e.features[0].properties.name;			
					var crop = crops[name];
					
					// Make the text look a little nicer
					var html = `<span style='text-transform:capitalize;'>` + crop + `</span>`;
					
					// Populate the popup and set its coordinates based on the feature found.
					popup.setLngLat(coordinates).setHTML(html).addTo(map);
					popup.remove();
				});
				
				// Clear the popup on mouse leave
				map.on('mouseleave', 'crop-map-layer', function () {
					map.getCanvas().style.cursor = '';
					popup.remove();
				});
			}

			// On style loaded...
			map.on('style.load', reloadMap);

			// Build the layer selection tree
			buildLayerTree("checkbox");
			$("ul.checktree").checktree();
		</script>
	</body>
</html>
