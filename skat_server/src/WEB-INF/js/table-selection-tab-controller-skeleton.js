import TabController from "./tab-controller.js";


/**
 * Table selection tab controller type.
 */
export default class TableSelectionTabController extends TabController {

	/**
	 * Initializes a new instance.
	 */
	constructor () {
		super();
	}


	/**
	 * Handles that activity has changed from false to true.
	 */
	processActivated () {
		if (!this.properties.sessionOwner) {
			const authenticationButton = this.tabControls.find(button => button.classList.contains("authentication"));
			authenticationButton.click();
			return;
		}

		this.clear();
		this.tabControls.find(button => button.classList.contains("table-selection")).classList.add("active");

		const sectionTemplate = document.querySelector("template.table-selection");
		this.rootSection = sectionTemplate.content.cloneNode(true).firstElementChild;
		this.centerArticle.append(this.rootSection);

		this.displayTables();		
	}


	/**
	 * Queries and displays all available Skat tables and their
	 * seats using web service calls.
	 */
	async displayTables () {
		this.messageElement.value = ""
		try {
			// TODO
		} catch (error) {
			this.messageElement.value = "" + (error.message || error);
			console.log(error);
		}
	}


	/**
	 * Returns a promise that resolves after the given file has been stored
	 * as the given table's current avatar using webservice calls.
	 * @param {Object} table the Skat table
	 * @param {HTMLElement} avatarElement the Skat table's avatar image element
	 * @param {File} avatarFile the avatar file
	 * @return {Promise} a promise that resolves after the webservice call
	 */
	async submitTableAvatar (table, avatarElement, avatarFile) {
		if (this.properties.sessionOwner.group !== "ADMIN") return;

		this.messageElement.value = "";
		try {
			let response;

			response = await fetch("/services/documents", { method: "POST", headers: {"Content-Type": avatarFile.type}, body: avatarFile });
			if (!response.ok) throw new Error("HTTP " + response.status + " " + response.statusText);
			const avatarReference = parseInt(await response.text());
			if (this.properties.sessionOwner.avatar.identity === avatarReference) return;
			this.properties.sessionOwner.avatar.identity = avatarReference;
			
			response = await fetch("/services/tables", { method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify(this.properties.sessionOwner) });
			if (!response.ok) throw new Error("HTTP " + response.status + " " + response.statusText);
			this.properties.sessionOwner.version += 1;

			this.rootSection.querySelector("img.avatar").src = "/services/documents/" + avatarReference + "?cache-bust=" + Date.now();
			this.messageElement.value = "ok.";

			this.messageElement.value = "ok.";
		} catch (error) {
			this.messageElement.value = "" + (error.message || error);
			console.log(error);
		}
	}


	/**
	 * Lets the requester occupy the given table position
	 * using web-service calls.
	 * @param {Object} table the table
	 * @param {number} tablePosition the table position
	 */
	occupyTablePosition (table, tablePosition) {
		if (this.properties.sessionOwner.group === "ADMIN") return;

		this.messageElement.value = "";
		try {
			// TODO

			const playButton = this.tabControls.find(button => button.classList.contains("table-play"));
			playButton.click();
		} catch (error) {
			this.messageElement.value = "" + (error.message || error);
			console.log(error);
		}
	}
}


/**
 * Performs controller event listener registration during DOM load event handling.
 */
window.addEventListener("load", event => {
	const controller = new TableSelectionTabController();

	for (const button of document.querySelectorAll("header>nav.tab>button")) {
		const active = button.classList.contains("table-selection");
		button.addEventListener("click", event => controller.active = active);
	}
});