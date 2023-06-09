/**
 * Handles the creation and population of the side panel to contain content
 * about the currently visualised locations/regions.
 */
class DigitalTwinSidePanel {

	// MapBox map
	_map;

	// Empty side panel template
	_sidePanelHTML = `
		<div class="tooltip" id="slideButtonContainer">
			<img id="slideButton" src="./img/arrow.png" width="16px" class="leftButton" height="16px" onclick="DT.sidePanelHandler.toggleExpansion()"/>
			<span class="tooltiptext">Expand/Collapse</span>
		</div>
		<div class="tooltip" id="expandButtonContainer">
			<img id="expandButton" src="./img/expand.png" width="16px" class="rightButton" height="16px" onclick="DT.sidePanelHandler.toggleMode()"/>
			<span class="tooltiptext">Maximise/Minimise</span>
		</div>
		<div id="sidePanelInner">
			<div id="titleContainer"></div>
			<div id="propsContainer"></div>
			<div id="contentContainer"></div>
			<div id="legendContainer"></div>
			<div id="footerContainer"></div>
		</div>
	`;

	// Store previous legendContainer visibility
	_previousLegendVisibility;

	//
	_defaultHTML;

	/**
	 * Initialise a new DigitalTwinSidePanel, appending the element
	 * into the document automatically.
	 */
	constructor(map) {
		this._map = map;
		this.#createSidePanel();
	}

	/**
	 * 
	 */
	#createSidePanel() {
		let sidePanel = document.getElementById("sidePanel");
		
		if(sidePanel == null) {
			let newDiv = document.createElement("div")
			newDiv.id = "sidePanel";
			newDiv.classList.add("small");
			newDiv.classList.add("expanded");
			newDiv.innerHTML = this._sidePanelHTML;
			document.body.appendChild(newDiv);
		} 
	}

	/**
	 * Toggles the visibility of the title element.
	 * 
	 * @param {boolean} visible 
	 */
	toggleTitle(visible) {
		document.getElementById("titleContainer").style.display = (visible) ? "block" : "none";
	}

		/**
	 * Toggles the visibility of the properties element.
	 * 
	 * @param {boolean} visible 
	 */
	toggleProperties(visible) {
		document.getElementById("propsContainer").style.display = (visible) ? "block" : "none";
	}

	/**
	 * Toggles the visibility of the content element.
	 * 
	 * @param {boolean} visible 
	 */
	toggleContent(visible) {
		document.getElementById("contentContainer").style.display = (visible) ? "block" : "none";
	}

	/**
	 * Toggles the visibility of the legend element.
	 * 
	 * @param {boolean} visible 
	 */
	toggleLegend(visible) {
		document.getElementById("legendContainer").style.display = (visible) ? "block" : "none";

		if(visible) {
			var footerContent = document.getElementById("footerContent");
			footerContent.style.display = "block";
			var returnContainer = document.getElementById("returnContainer");
			returnContainer.style.display = "none";
		} else {
			var footerContent = document.getElementById("footerContent");
			footerContent.style.display = "none";
			var returnContainer = document.getElementById("returnContainer");
			returnContainer.style.display = "block";
		}
	}

	/**
	 * Toggles the visibility of the footer element.
	 * 
	 * @param {boolean} visible 
	 */
	toggleFooter(visible) {
		document.getElementById("footerContainer").style.display = (visible) ? "block" : "none";
	}

	/**
	 * Sets the title of the side panel.
	 * 
	 * @param {Stirng} title desired title HTML
	 */
	setTitle(title) {
		document.getElementById("sidePanel").style.visibility = "visible";
		document.getElementById("titleContainer").innerHTML = "<h3>" + title + "</h3>";
	}

	/**
	 * Set the properties to show in a table within the side panel.
	 * 
	 * @param {Map<String, String>} data dictionary of properties 
	 */
	setProperties(data) {
		document.getElementById("sidePanel").style.visibility = "visible";

		var tableHTML = `
			<table width="100%">
				<tr>
					<td colspan="3" style="border-bottom: 1px solid lightgrey; height: 25px;"><b>Location Properties</b></td>
				</tr>
				<tr>
					<td colspan="3" height="10px"></td>
				</tr>
		`;
		for (const [key, value] of Object.entries(data)) {
			tableHTML += `<tr>`;
			tableHTML += `<td width="5px"></td>`;
			tableHTML += `<td width="35%"><b>` + key + ":</b></td>";
			tableHTML += `<td width="calc(65% - 5px)">` + value + "</td>";
			tableHTML += "</tr>";
		}
		tableHTML += `
				<tr>
					<td colspan="3" height="10px"></td>
				</tr>
			</table>
		`;

		document.getElementById("propsContainer").innerHTML = tableHTML;
	}

	/**
	 * Sets the title of the side panel.
	 * 
	 * @param {Stirng} title desired title HTML
	 */
	 setTitle(title) {
		document.getElementById("sidePanel").style.visibility = "visible";
		document.getElementById("titleContainer").innerHTML = "<h2>" + title + "</h2>";
	}

	/** 
	 * Set the main content of the side panel.
	 * 
	 * @param {String} contentHTML HTML to add.
	 */
	setContent(contentHTML) {
		document.getElementById("sidePanel").style.visibility = "visible";
		document.getElementById("contentContainer").innerHTML = contentHTML;

		var sidePanel = document.getElementById("sidePanel");
		if(sidePanel.classList.contains("large")) {
			document.getElementById("controlsParent").style.visibility = "hidden";
		}
	}

	/**
	 * Override the legend content.
	 * 
	 * @param {String} legendHTML HTML to add. 
	 */
	setLegend(legendHTML) {
		document.getElementById("sidePanel").style.visibility = "visible";
		document.getElementById("legendContainer").innerHTML = legendHTML;
	}

	/**
	 * Builds a legend component using the getLegendContent of each
	 * loaded DigitalTwinModule.
	 */
	buildLegend() {
		var modules = DT.modules;

		var outerHTML = `
			<div id="legend" class="w3-sidebar w3-bar-block w3-light-grey w3-card">
				<div class="w3-bar-item legend-title"><b>Legends:</b></div>
		`;

		var innerHTML = ``;

		modules.forEach(module => {
			let moduleName = module.name;
			let sanitisedName = moduleName.toLowerCase().replace(" ", "_");

			outerHTML += `
				<button id="` + sanitisedName + `" class="w3-bar-item w3-button tablink" onclick="manager.openLegend(event, this.id)">` + moduleName + `</button>
			`;

			innerHTML += `
				<div id="` + sanitisedName + `" class="w3-container legend-right" style="display: none;">`
					+ module.getLegendContent() +
				`</div>
			`;
		});

		outerHTML += `</div>`;
		this.setLegend(outerHTML + innerHTML); 

		// Simulate a click on the first entry
		var firstEntry = document.getElementsByClassName("tablink w3-button")[0];
		firstEntry.click();
	}

	/**
	 * Set the footer content.
	 * 
	 * @param {String} footerHTML HTML to add. 
	 */
	setFooter(footerHTML) {
		var newHTML = `
			<div id="returnContainer">
				<a href="#" onclick="DT.sidePanelHandler.returnToDefault()">&lt; Return</a>
			</div>
			<div id="footerContent">
				` + footerHTML + `
			</div>
		`;
		document.getElementById("sidePanel").style.visibility = "visible";
		document.getElementById("footerContainer").innerHTML = newHTML;
	}

	storeDefault() {
		this._defaultHTML = document.getElementById("sidePanelInner").innerHTML;
	}

	returnToDefault() {
		document.getElementById("sidePanelInner").innerHTML = this._defaultHTML;
	}

	/**
	 * Changes the mode of the side panel.
	 */
	toggleMode() {
		var sidePanel = document.getElementById("sidePanel");
		var leftButton = document.getElementById("slideButton");
		var rightButton = document.getElementById("expandButton");
		var legend = document.getElementById("legendContainer");

		if(sidePanel.classList.contains("small")) {
			// Make large

			sidePanel.classList.replace("small", "large");
			document.getElementById("map").style.width = "100%";
			document.getElementById("controlsParent").style.visibility = "hidden";

			leftButton.style.visibility = "hidden";
			rightButton.src = "./img/collapse.png";

			// Hide the legend
			this._previousLegendVisibility = legend.style.visibility;
			legend.style.visibility = "hidden";
			
			// Stop keyboard events
			this._map["keyboard"].disable();
			this._map.resize();

		} else if(sidePanel.classList.contains("large")) {
			// Make small

			sidePanel.classList.replace("large", "small");
			document.getElementById("map").style.width = "calc(100% - 500px)";
			document.getElementById("controlsParent").style.visibility = "visible";

			leftButton.style.visibility = "visible";
			rightButton.src = "./img/expand.png";

			// Show the legend (if it was visible beforehand)
			legend.style.visibility = this._previousLegendVisibility;

			// Allow keyboard events
			this._map["keyboard"].enable();
			this._map.resize();
		}
	}

	/**
	 * Updates the visibility of the side panel.
	 */
	toggleExpansion() {
		var sidePanel = document.getElementById("sidePanel");
		var rightButton = document.getElementById("expandButton");

		if(sidePanel.classList.contains("small")) {

			if(sidePanel.classList.contains("collapsed")) {
				// Expand
				sidePanel.classList.replace("collapsed", "expanded")
				document.getElementById("map").style.width = "calc(100% - 500px)";

				document.getElementById("legendContainer").style.visibility = "visible";
				rightButton.style.visibility = "visible";
				
			} else if(sidePanel.classList.contains("expanded")) {
				// Collapse
				sidePanel.classList.replace("expanded", "collapsed")
				document.getElementById("map").style.width = "calc(100% - 28px)";

				document.getElementById("legendContainer").style.visibility = "hidden";
				rightButton.style.visibility = "hidden";
			}

		} 

		this._map.resize();
	}

	/**
	 * Opens the selected legend element
	 * 
	 * @param {MouseEvent} event mouse event
	 * @param {String} legendID id of selected legend
	 */
	openLegend(event, legendID) {
		var i, x, tablinks;

		x = document.getElementsByClassName("legend-right");
		for (i = 0; i < x.length; i++) {
			x[i].style.display = "none";
		}

		tablinks = document.getElementsByClassName("tablink");
		for (i = 0; i < x.length; i++) {
			tablinks[i].className = tablinks[i].className.replace(" w3-blue", ""); 
		}

		var legendContent = document.querySelector("div.legend-right[id='" + legendID + "']");
		if(legendContent != null) legendContent.style.display = "block";
		event.currentTarget.className += " w3-blue";
	}
}