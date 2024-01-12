import TabController from "./tab-controller.js";
import xhr from "./xhr.js";


/**
 * Authentication tab controller type.
 * Copyright (c) 2023 Sascha Baumeister
 */
class AuthenticationTabController extends TabController {

	/**
	 * Initializes a new controller instance.
	 */
	constructor () {
		super();
	}


	/**
	 * Handles that activity has changed from false to true.
	 */
	processActivated () {
		this.clear();
		this.tabControls.find(button => button.classList.contains("authentication")).classList.add("active");

		try {
			this.properties.sessionOwner = null;

			const sectionTemplate = document.querySelector("template.authentication");
			this.rootSection = sectionTemplate.content.cloneNode(true).firstElementChild;
			this.centerArticle.append(this.rootSection);

			const loginButton = this.rootSection.querySelector("button.login");
			loginButton.addEventListener("click", event => this.authenticate());
		} catch (error) {
			this.messageElement.value = "" + (error.message || error);
			console.log(error);
		}
	}


	/**
	 * Authenticates the given user data, defines the controller's
	 * shared sessionOwner property if authentication was successful,
	 * and activates the preferences tab.
	 */
	async authenticate () {
		this.messageElement.value = "";
		try {
			const email = this.rootSection.querySelector("input.email").value.trim();
			const password = this.rootSection.querySelector("input.password").value.trim();

			// Although fetch() supports sending credentials from a browser's hidden Basic-Auth credentials store, it lacks
			// support for storing them securely. This workaround uses a classic XMLHttpRequest invocation as a workaround.
			this.properties.sessionOwner = await xhr("/services/people/0", "GET", {"Accept": "application/json"}, null, "json", email, password);

			const preferencesButton = this.tabControls.find(button => button.classList.contains("preferences-editor"));
			preferencesButton.click();
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
	const controller = new AuthenticationTabController();

	for (const button of document.querySelectorAll("header>nav.tab>button")) {
		const active = button.classList.contains("authentication");
		button.addEventListener("click", event => controller.active = active);
	}

	document.querySelector("header>nav.tab>button.authentication").click();
});