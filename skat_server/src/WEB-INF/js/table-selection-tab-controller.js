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
			// fetch table
			const response = await fetch("/services/tables");
			if (!response.ok) {
				throw new Error("Failed to fetch Skat tables: " + response.status + " " + response.statusText);
			}

			// fetch player (necessary?)
			const responsePlayer = await fetch("/services/people");
			if (!responsePlayer.ok) {
				throw new Error("Failed to fetch Players: " + responsePlayer.status + " " + responsePlayer.statusText);
			}

			const players = await responsePlayer.json();
			console.log("players :", players)

			const tables = await response.json();
			console.log("tables :", tables)

			const tableSelectionRowTemplate = document.querySelector("template.table-selection-row");
			const tableBody = this.rootSection.querySelector("tbody");
        	tableBody.innerHTML = "";

			tables.forEach((table) => {
				console.log("table :", table)

				const tableSelectionRow = tableSelectionRowTemplate.content.cloneNode(true).firstElementChild;
				console.log("tableSelectionRow :", tableSelectionRow)

				// Table avatar
				const tableAvatarElement = tableSelectionRow.querySelector(".table");
				tableAvatarElement.src = `/services/documents/${table.avatar.identity}?cache-bust=${Date.now()}`;
				tableAvatarElement.addEventListener("drop", event => this.submitTableAvatar(table, tableAvatarElement, event.dataTransfer.files[0]));

				// init playerReferences
				table.playerReferences = [null, null, null]

				// Seats avatar
				const seatClasses = ["fore", "middle", "rear"];
				seatClasses.forEach((seatClass, i) => {
					const playerAvatarElement = tableSelectionRow.querySelector(`.${seatClass}`);
					playerAvatarElement.addEventListener("click", event => {
						this.messageElement.value = "occupyTablePosition is called";
						this.occupyTablePosition(table, i, seatClass);
					});
					console.log("playerAvatarElement :" + i, playerAvatarElement)

					// TODO:
					// if (player at position [i]) {
					// 	playerAvatarElement.src = `/services/documents/${the_players_at_position_i.avatar.identity}?cache-bust=${Date.now()}`;
					// 	playerAvatarElement.title = GET the_players_at_position_i name
					// }
				});
				tableBody.appendChild(tableSelectionRow);
			});
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
		console.log(this.properties.sessionOwner.group);
		this.messageElement.value = "";
		try {
			let response;

			response = await fetch("/services/documents", { method: "POST", headers: {"Content-Type": avatarFile.type}, body: avatarFile });
			if (!response.ok) throw new Error("HTTP " + response.status + " " + response.statusText);

			const avatarReference = parseInt(await response.text());
			console.log(table.avatar.identity);

			if (table.avatar.identity === avatarReference) return;
			table.avatar.identity = avatarReference;
			
			response = await fetch("/services/tables", { method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify(table) });
			if (!response.ok) throw new Error("HTTP " + response.status + " " + response.statusText);
			table.version += 1;

			this.rootSection.querySelector("img.table").src = "/services/documents/" + avatarReference + "?cache-bust=" + Date.now();

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
	occupyTablePosition (table, tablePosition, seatClass) {
		if (this.properties.sessionOwner.group === "ADMIN") return;

		this.messageElement.value = "";
		try {
			const player = this.properties.sessionOwner;
			console.log("player:", player);
			console.log("player identity:", player.avatar.identity);

			// Check if the player is already seated at a table
			if (table.playerReferences.includes(player.avatar.identity)) {
				this.messageElement.value = "Player is already seated at a table.";
				return;
			}

			// Check if the pos is already occupied
			if (table.playerReferences[tablePosition]) {
				this.messageElement.value = "Pos is already occupied.";
				return;
			}

			// Assign the player to the specified position
			table.playerReferences[tablePosition] = player.avatar.identity;

			// Update UI
			const playerAvatarElement = document.querySelector(`.${seatClass}`);
			playerAvatarElement.src = `/services/documents/${player.avatar.identity}?cache-bust=${Date.now()}`;
			playerAvatarElement.title = "OCCUPIED"; //`${player.name.family}`;

			console.log("table.playerReferences: ",table.playerReferences)
			console.log("rootSection: ", this.rootSection)

			//this.rootSection.querySelector("img" + `.${seatClass}`).src = "/services/documents/" + player.avatar.identity + "?cache-bust=" + Date.now();
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