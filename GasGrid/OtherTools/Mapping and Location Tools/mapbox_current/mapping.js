
// TO MAKE THE MAP APPEAR YOU MUST
// ADD YOUR ACCESS TOKEN FROM
// https://account.mapbox.com

var light = false;
plot(light);

function l_switch(light) {
    if (light == true) {
        light = false;
        console.log(light)
        plot(light);
    }
    if (light == false) {
        light = true;
        console.log(light)
        plot(light);
    }
}

function plot(light) {
    if (light == true) {
        document.body.style.backgroundColor = "#CBD2D3";
        but1 = document.getElementById('reset');
        but1.style.color = '#6B6B6B';
        but2 = document.getElementById('but');
        but2.style.color = '#6B6B6B';
        mapboxgl.accessToken = 'pk.eyJ1IjoidG9tc2F2YWdlIiwiYSI6ImNram01MDFvczI5aHUyeG83N21obzBrcWsifQ.T_j8LAT55g3kuwCQFlNPuA';
        var map = new mapboxgl.Map({
            container: 'map',
            style: 'mapbox://styles/mapbox/light-v9', // stylesheet location
            center: [-3, 54.25], // starting position [lng, lat]
            zoom: 5, // starting zoom
            pitch: 0,
            bearing: 0,
        });
    }
    else {
        document.body.style.backgroundColor = "#091222";
        mapboxgl.accessToken = 'pk.eyJ1IjoidG9tc2F2YWdlIiwiYSI6ImNram01MDFvczI5aHUyeG83N21obzBrcWsifQ.T_j8LAT55g3kuwCQFlNPuA';
        var map = new mapboxgl.Map({
            container: 'map',
            style: 'mapbox://styles/mapbox/satellite-v9', // stylesheet location
            center: [-3, 54.25], // starting position [lng, lat]
            zoom: 5, // starting zoom
            minZoom: 5,
            pitch: 0,
            bearing: 0,
        });
    }



    // Loading 3D terrain
    map.on('load', function () {

        // map.addSource('mapbox-dem', {
        //     'type': 'raster-dem',
        //     'url': 'mapbox://mapbox.mapbox-terrain-dem-v1',
        //     'tileSize': 512,
        //     'maxzoom': 14
        // });

        // // add the DEM source as a terrain layer with exaggerated height
        terrain_exaggeration = 1.5;
        map.setTerrain({ 'source': 'mapbox-dem', 'exaggeration': terrain_exaggeration });

        document.getElementById('but').addEventListener('click', function () {
            light = false;
        });
        document.getElementById('reset').addEventListener('click', function () {
            map.flyTo({
                center: [-3, 54.25],
                curve: 2,
                speed: 1,
                pitch: 0,
                zoom: 5,
                bearing: 0
            });
        });

        if (light == true) {
            thumb_loc = 'https://tom-savage.co.uk/assets/images/pressure.png';
        }
        else {
            thumb_loc = 'https://tom-savage.co.uk/assets/images/pressure2.png';
        }
        map.loadImage(
            thumb_loc,
            function (error, image) {
                if (error) throw error;
                map.addImage('custom-marker', image);

                map.addSource('terminals', {
                    'type': 'geojson',
                    'data': 'https://tom-savage.co.uk/assets/geojson/terminals.geojson'
                });
                if (light == true) {
                    term_name_color = '#6B6B6B';
                    term_outline_color = '#F6F6F4';
                }
                else {
                    term_name_color = '#FFFFFF';
                    term_outline_color = '#000000';
                }
                // Add a symbol layer
                map.addLayer({
                    'id': 'terminals',
                    'type': 'symbol',
                    'source': 'terminals',
                    'layout': {

                        'icon-image': 'custom-marker',
                        // get the title name from the source's "name" property
                        'text-field': ['get', 'name'],
                        'text-font': [
                            'Open Sans Semibold',
                            'Arial Unicode MS Bold'
                        ],
                        'text-offset': [0, 1.25],
                        'text-anchor': 'top'
                    },
                    'paint': {
                        'text-color': term_name_color,
                        'text-halo-color': term_outline_color,
                        'text-halo-width': 2
                    }
                });
            }
        );





        var layers = map.getStyle().layers;

        var labelLayerId;
        for (var i = 0; i < layers.length; i++) {
            if (layers[i].type === 'symbol' && layers[i].layout['text-field']) {
                labelLayerId = layers[i].id;
                break;
            }
        }



        // add a sky layer that will show when the map is highly pitched
        map.addLayer({
            'id': 'sky',
            'type': 'sky',
            'paint': {
                'sky-type': 'atmosphere',
                'sky-atmosphere-sun': [0.0, 0.0],
                'sky-atmosphere-sun-intensity': 15
            }
        });


        map.addSource('National Transmission System', {
            type: 'geojson',
            data: 'https://tom-savage.co.uk/assets/geojson/pipe_network.geojson'
        });

        if (light == true) {
            pipe_color = '#6b6b6b';
        }
        else {
            pipe_color = '#FFFFFF'
        }

        map.addLayer({
            'id': 'National Transmission System',
            'type': 'line',
            'source': 'National Transmission System',
            'layout': {
                'line-join': 'round',
                'line-cap': 'round'
            },
            'paint': {
                'line-color': pipe_color,

                'line-width': 3
            }
        });


        map.on('click', 'terminals', function (e) {
            var coordinates = e.features[0].geometry.coordinates;
            map.flyTo({
                center: coordinates,
                curve: 1.9,
                speed: 0.8,
                pitch: 70,
                zoom: 16,
                bearing: Math.random() * 360
            });

            // new mapboxgl.Popup()
            //     .setLngLat(coordinates)
            //     .setHTML("INFORMATION FROM KNOWLEDGE GRAPH HERE")
            //     .addTo(map);
        });


        map.on('mouseenter', 'terminals', function () {
            map.getCanvas().style.cursor = 'pointer';
        });

        map.on('mouseleave', 'terminals', function () {
            map.getCanvas().style.cursor = '';
        });




        map.addSource('Pipe Buffer', {
            type: 'geojson',
            data: 'https://tom-savage.co.uk/assets/geojson/pipe_buffer.geojson'
        });

        map.addLayer({
            'id': 'Pipe Extrusion',
            'type': 'fill-extrusion',
            'source': 'Pipe Buffer',
            'paint': {
                // See the Mapbox Style Specification for details on data expressions.
                // https://docs.mapbox.com/mapbox-gl-js/style-spec/#expressions

                // Get the fill-extrusion-color from the source 'color' property.
                'fill-extrusion-color': pipe_color,

                // Get fill-extrusion-height from the source 'height' property.
                'fill-extrusion-height': 15,

                // Get fill-extrusion-base from the source 'base_height' property.
                'fill-extrusion-base': 0,

                // Make extrusions slightly opaque for see through indoor walls.
                'fill-extrusion-opacity': 1
            }
        });



        // // This section is concerned with plotting the geojson file
        // map.addSource('Contour', {
        //     type: 'geojson',
        //     data: 'https://tom-savage.co.uk/assets/geojson/contour.geojson'
        // });

        // map.addLayer({
        //     'id': 'Contour-Fill',
        //     'type': 'fill',
        //     'source': 'Contour',
        //     'layout': {},
        //     'paint': {
        //     'fill-color': {
        //         type: 'identity',
        //         property: 'fill',
        //         },
        //     'fill-opacity': {
        //         type: 'identity',
        //         property: 'fill-opacity'
        //         }
        //     }
        // });
        if (!map.getSource('composite')) {
            map.addSource('composite', {
                type: 'vector', url: 'mapbox://mapbox.mapbox-streets-v7'
            })
                ;
        }
        // Loading 3D buildings when zoomed in close enough 
        map.addLayer({
            'id': '3d-buildings',
            'source': 'composite',
            'source-layer': 'building',
            'filter': ['==', 'extrude', 'true'],
            'type': 'fill-extrusion',
            'minzoom': 15,
            'paint': {
                'fill-extrusion-color': '#aaa',

                // use an 'interpolate' expression to add a smooth transition effect to the
                // buildings as the user zooms in
                'fill-extrusion-height': [
                    'interpolate',
                    ['linear'],
                    ['zoom'],
                    15,
                    0,
                    15.05,
                    ['get', 'height']
                ],
                'fill-extrusion-base': [
                    'interpolate',
                    ['linear'],
                    ['zoom'],
                    16,
                    0,
                    15.05,
                    ['get', 'min_height']
                ],
                'fill-extrusion-opacity': 0.8
            }
        },
            labelLayerId
        );
    });
}