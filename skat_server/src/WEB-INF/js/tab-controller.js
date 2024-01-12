/**
 * Module variable containg shared controller properties.
 */
const SHARED_PROPERTIES = {};


/**
 * Semi-abstract tab controller class.
 * Copyright (c) 2023 Sascha Baumeister
 */
export default class TabController extends Object {
	#active;
	#tabControls;
	#messageElement;
	#centerArticle;
	#rootSection;


	/**
	 * Initializes a new instance, and throws an exception if
	 * there is an attempt to instanciate this class itself.
	 */
	constructor () {
		super();
		if (Object.getPrototypeOf(this).constructor === TabController) 
			throw new InternalError("this semi-abstract class cannot be instantiated!");

		this.#active = false;
		this.#tabControls = Array.from(document.querySelectorAll("header>nav.tab>button"));
		this.#messageElement = document.querySelector("footer>input.message");
		this.#centerArticle = document.querySelector("main>article.center");
		this.#rootSection = null;
	}


	/**
	 * Returns the activity.
	 * @return the activity state
	 */
	get active () {
		return this.#active;
	}


	/**
	 * Sets the activity.
	 * @param value the activity state
	 */
	set active (value) {
		if (typeof value !== "boolean") throw new TypeError();

		if (!this.#active && value)
			this.processActivated();
		else if (this.#active && !value)
			this.processDeactivated();

		this.#active = value;
	}


	/**
	 * Returns the shared controller properties.
	 * @return the shared controller properties
	 */
	get properties () {
		return SHARED_PROPERTIES;
	}


	/**
	 * Returns the tab controls.
	 * @return the tab control elements
	 */
	get tabControls () {
		return this.#tabControls;
	}


	/**
	 * Returns the message element.
	 * @return the message output element
	 */
	get messageElement () {
		return this.#messageElement;
	}


	/**
	 * Returns the center article.
	 * @return the center article element
	 */
	get centerArticle () {
		return this.#centerArticle;
	}


	/**
	 * Returns the root section.
	 * @return the (temporary) root section, or null for none
	 */
	get rootSection () {
		return this.#rootSection;
	}


	/**
	 * Sets the root section.
	 * @param value the (temporary) root section, or null for none
	 */
	set rootSection (value) {
		if (value && (!(value instanceof HTMLElement) || value.tagName !== "SECTION")) throw new TypeError("" + value.tagName);

		this.#rootSection = value;
	}


	/**
	 * Handles that activity has changed from false to true.
	 * This operation always throws an exception, and must be
	 * overridden to prevent this.
	 */
	processActivated () {
		throw new InternalError("this operation must be overridden!");
	}


	/**
	 * Handles that activity has changed from true to false.
	 * This operation resets the root section to null, and can
	 * optionally be overriden to additionally close ressources
	 * related to a tab.
	 */
	processDeactivated () {
		this.#rootSection = null;
	}


	/**
	 * Clears the nav buttons, the center article,
	 * and the message element.
	 */
	clear () {
		for (const button of this.tabControls)
			button.classList.remove("active");
		while (this.centerArticle.lastElementChild)
			this.centerArticle.lastElementChild.remove();
		this.messageElement.value = "";
	}
}