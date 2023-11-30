/**
 * Abstract controller type.
 * Copyright (c) 2019 Sascha Baumeister
 */
export default class Controller extends EventTarget {
	/**
	 * The user currently logged in.
	 */
	static sessionOwner = null;
 

	/**
	 * Initializes a new controller instance.
	 * @throws {TypeError} if a semi-abstract type is instantiated
	 */
	constructor () {
		super();
		if (this.constructor === Controller) throw new TypeError("abstract type");
	}


	/**
	 * Resets this controller.
	 */
 	reset () {}


	/**
	 * Displays the view associated with this controller.
	 */
 	display () {
		const main = document.querySelector("main");
		while (main.childElementCount > 0) main.lastElementChild.remove();
 	}


	/**
	 * Displays the given error in the footer, or resets it if none is given.
	 * @param error {Object} the optional error
	 */
	displayError (error) {
		const output = document.querySelector("body > footer output");
		if (error) {
			console.error(error);
			output.value = error instanceof Error ? error.message : error;
		} else {
			output.value = "";
		}
	}
};
