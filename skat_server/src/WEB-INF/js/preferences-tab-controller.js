import TabController from "./tab-controller.js";


/**
 * Preferences editor tab controller type.
 * Copyright (c) 2023 Sascha Baumeister
 */
export default class PreferencesTabController extends TabController {

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
		this.tabControls.find(button => button.classList.contains("preferences-editor")).classList.add("active");

		const sectionTemplate = document.querySelector("template.preferences-editor");
		this.rootSection = sectionTemplate.content.cloneNode(true).firstElementChild;
		this.centerArticle.append(this.rootSection);

		const addButton = this.rootSection.querySelector("button.add");
		addButton.addEventListener("click", event => this.addPhone(""));
		const submitButton = this.rootSection.querySelector("button.submit");
		submitButton.addEventListener("click", event => this.submitSessionOwner());
		const requesterAvatarElement = this.rootSection.querySelector("img.avatar");
		requesterAvatarElement.addEventListener("drop", event => this.submitSessionOwnerAvatar(event.dataTransfer.files[0]));

		this.displaySessionOwner();
	}


	/**
	 * Displays the session owner details.
	 */
	displaySessionOwner () {
		const requesterAvatarElement = this.rootSection.querySelector("img.avatar");
		requesterAvatarElement.src = "/services/documents/" + this.properties.sessionOwner.avatar.identity + "?cache-bust=" + Date.now();

		this.rootSection.querySelector("input.email").value = this.properties.sessionOwner.email;
		this.rootSection.querySelector("input.password").value = "";
		this.rootSection.querySelector("input.group").value = this.properties.sessionOwner.group;
		this.rootSection.querySelector("input.title").value = this.properties.sessionOwner.name.title || "";
		this.rootSection.querySelector("input.forename").value = this.properties.sessionOwner.name.given;
		this.rootSection.querySelector("input.surname").value = this.properties.sessionOwner.name.family;
		this.rootSection.querySelector("input.street").value = this.properties.sessionOwner.address.street;
		this.rootSection.querySelector("input.postcode").value = this.properties.sessionOwner.address.postcode;
		this.rootSection.querySelector("input.city").value = this.properties.sessionOwner.address.city;
		this.rootSection.querySelector("input.country").value = this.properties.sessionOwner.address.country;

		const phonesDivision = this.rootSection.querySelector("div.phones");
		while (phonesDivision.lastElementChild)
			phonesDivision.lastElementChild.remove();
		for (const phone of this.properties.sessionOwner.phones)
			this.addPhone(phone);
	}


	/**
	 * Adds the given phone number to the list of phone numbers.
	 */
	addPhone (phone) {
		const divisionElement = document.createElement("div");
		const inputElement = document.createElement("input");
		inputElement.classList.add("phone");
		inputElement.value = phone;
		divisionElement.append(inputElement);

		const phonesDivision = this.rootSection.querySelector("div.phones");
		phonesDivision.append(divisionElement);
	}


	/**
	 * Returns a promise that resolves after the session owner's current
	 * state has been stored using a webservice call.
	 * @return {Promise} a promise that resolves after the webservice call
	 */
	async submitSessionOwner () {
		this.messageElement.value = "";
		try {
			const password = this.rootSection.querySelector("input.password").value.trim();
			const clone = structuredClone(this.properties.sessionOwner);
			clone.email = this.rootSection.querySelector("input.email").value.trim();
			clone.group = this.rootSection.querySelector("input.group").value.trim();
			clone.name.title = this.rootSection.querySelector("input.title").value.trim() || null;
			clone.name.given = this.rootSection.querySelector("input.forename").value.trim();
			clone.name.family = this.rootSection.querySelector("input.surname").value.trim();
			clone.address.street = this.rootSection.querySelector("input.street").value.trim();
			clone.address.postcode = this.rootSection.querySelector("input.postcode").value.trim();
			clone.address.city = this.rootSection.querySelector("input.city").value.trim();
			clone.address.country = this.rootSection.querySelector("input.country").value.trim();

			clone.phones.length = 0;
			for (const phoneElement of this.rootSection.querySelectorAll("div.phones input.phone")) {
				const phone = phoneElement.value.trim();
				if (phone.length > 0) clone.phones.push(phone);
			}

			const headers = {"Content-Type": "application/json"};
			if (password) headers["X-Set-Password"] = password;
			const response = await fetch("/services/people", { method: "POST", headers: headers, body: JSON.stringify(clone) });
			if (!response.ok) throw new Error("HTTP " + response.status + " " + response.statusText);
			this.properties.sessionOwner = clone;
			this.properties.sessionOwner.version += 1;

			if (password) {
				const authenticationButton = this.tabControls.find(button => button.classList.contains("authentication"));
				authenticationButton.click();
			} else {
				this.displaySessionOwner();
				this.messageElement.value = "ok.";
			}
		} catch (error) {
			this.messageElement.value = "" + (error.message || error);
			console.log(error);
		}
	}


	/**
	 * Returns a promise that resolves after the given file has been stored
	 * as the session owner's current avatar using webservice calls.
	 * @param {File} avatarFile the avatar file
	 * @return {Promise} a promise that resolves after the webservice call
	 */
	async submitSessionOwnerAvatar (avatarFile) {
		this.messageElement.value = "";
		try {
			let response;

			response = await fetch("/services/documents", { method: "POST", headers: {"Content-Type": avatarFile.type}, body: avatarFile });
			if (!response.ok) throw new Error("HTTP " + response.status + " " + response.statusText);
			const avatarReference = parseInt(await response.text());
			if (this.properties.sessionOwner.avatar.identity === avatarReference) return;
			this.properties.sessionOwner.avatar.identity = avatarReference;

			response = await fetch("/services/people", { method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify(this.properties.sessionOwner) });
			if (!response.ok) throw new Error("HTTP " + response.status + " " + response.statusText);
			this.properties.sessionOwner.version += 1;

			this.rootSection.querySelector("img.avatar").src = "/services/documents/" + avatarReference + "?cache-bust=" + Date.now();
			this.messageElement.value = "ok.";
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
	const controller = new PreferencesTabController();

	for (const button of document.querySelectorAll("header>nav.tab>button")) {
		const active = button.classList.contains("preferences-editor");
		button.addEventListener("click", event => controller.active = active);
	}
});