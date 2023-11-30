import MenuController from "./menu-controller.js";
import Controller from "./controller.js";
import xhr from "./xhr.js";


/**
 * Welcome controller type.
 * Copyright (c) 2019 Sascha Baumeister
 */
export default class WelcomeController extends MenuController {

	/**
	 * Initializes a new controller instance.
	 */
	constructor () {
		super();
	}


	/**
	 * Displays the view associated with this controller.
	 */
	async display () {
		super.display();
		this.displayError();
		try {
			Controller.sessionOwner = null;

			const section = document.querySelector("#welcome-template").content.cloneNode(true).firstElementChild;
			section.querySelector("button").addEventListener("click", event => this.login());
			document.querySelector("main").append(section);
		} catch (error) {
			this.displayError(error);
		}
	}


	/**
	 * Performs a login check on the given user data, assigns the controller's
	 * user object if the login was successful, and initiates rendering of the
	 * preferences view.
	 */
	async login () {
		this.displayError();
		try {
			const inputs = document.querySelectorAll("section.welcome input");
			const email = inputs[0].value.trim();
			const password = inputs[1].value.trim();

			// Although fetch() supports sending credentials from a browser's hidden Basic-Auth credentials store, it lacks
			// support for storing them securely. This workaround uses a classic XMLHttpRequest invocation as a workaround.
			const person = await xhr("/services/people/0", "GET", {"Accept": "application/json"}, null, "json", email, password);
			if (person.blockingTimestamp > Date.now()) throw new Error("Account is blocked until" + new Date(person.blockingTimestamp).toLocaleString());

			Controller.sessionOwner = person;
			document.querySelector("header li:nth-of-type(2) > a").click();
		} catch (error) {
			this.displayError(error);
		}
	}
}


/**
 * Performs controller event listener registration during DOM load event handling.
 */
window.addEventListener("load", event => {
	const controller = new WelcomeController();

	const anchors = document.querySelectorAll("header li > a");
	anchors.forEach((anchor, index) => anchor.addEventListener("click", event => controller.active = (index === 0)));

	// auto-click welcome menu item
	anchors[0].click();
});