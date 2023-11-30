import Controller from "./controller.js";


/**
 * Abstract menu controller type.
 * Copyright (c) 2019 Sascha Baumeister
 */
export default class MenuController extends Controller {

	/**
	 * Initializes a new menu controller instance.
	 * @throws {TypeError} if a semi-abstract type is instantiated
	 */
	constructor () {
		super();
		if (this.constructor === MenuController) throw new TypeError("abstract type");

		let active = false;
		Object.defineProperty(this, "active", {
			enumerable: true,
			get: function () { return active; },
			set: function (value) {
				if (active !== value) {
					active = value;
					this.dispatchEvent(new CustomEvent("switch", { detail: value }));
				}
			}
		});

		this.addEventListener("switch", event => event.detail ? this.display() : this.reset());
	}


	/**
	 * Returns a promise that resolves to an RTC peer connection's updated local description
	 * once the given connection's ICE candidates have been negotiated.
	 * @param {RTCPeerConnection} connection the RTC peer connection
	 * @param {Boolean} offering wether an offer or an answer shall be negotiated
	 * @return {Promise} the promise of an updated local description
	 */
	static negotiateLocalDescription (connection, offering) {
		return new Promise((resolve, reject) => {
			connection.onicecandidate = event => {
				if (!event.candidate) {
					delete connection.onicecandidate;
					resolve(connection.localDescription);
				}
			};

			const promise = offering ? connection.createOffer() : connection.createAnswer();
			promise.then(sessionDescription => connection.setLocalDescription(sessionDescription));
		});
	}
};


/**
 * Performs controller event listener registration during DOM load event handling.
 * The listeners handle menu item selection.
 */
window.addEventListener("load", event => {
	const anchors = document.querySelectorAll("header nav a");

	for (const anchor of anchors) {
		anchor.addEventListener("click", event => {
			const selectedItem = event.currentTarget.parentElement;
			for (const item of selectedItem.parentElement.children) item.classList.remove("selected");
			selectedItem.classList.add("selected");
		});
	}
});