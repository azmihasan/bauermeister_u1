/**
 * BlobReader decorator type extending FileReader, which allows the read
 * operations to return content promises in order to facilitate async/await
 * based programming.
 * Copyright (c) 2019 Sascha Baumeister
 */
export default class BlobReader extends FileReader {

	/**
	 * Initializes a new instance.
	 */
	constructor () {
		super();
	}


	/**
	 * Returns true if the delegate's ready state is DONE after aborting, or
	 * false if it is EMPTY instead. Note that this operation actively aborts
	 * the delegate solely if the delegate's ready state is LOADING upon operation
	 * start, thereby avoiding any DOM_FILE_ABORT_ERR errors.
	 * @return {Boolean} whether the delegate is DONE or EMPTY after aborting
	 */
	abort () {
		if (this.delegate.readyState === 1) super.abort();
		return this.delegate.readyState === 2;
	}


	/**
	 * Returns a promise of array buffer content read from the given blob
	 * (or file), which can be evaluated using the await command. The latter
	 * throws an error if reading said blob fails.
	 * @param {Blob} blob the binary large object to be read
	 * @return {Promise} the promise of array buffer content read from the given
	 *		   blob, or null if the operation is aborted
	 */
	readAsArrayBuffer (blob) {
		return new Promise((resolve, reject) => {
			const eventListeners = {};
			eventListeners.load  = event => { for (const type in eventListeners) this.removeEventListener(type, eventListeners[type]); resolve(this.result); };
			eventListeners.abort = event => { for (const type in eventListeners) this.removeEventListener(type, eventListeners[type]); resolve(null); };
			eventListeners.error = event => { for (const type in eventListeners) this.removeEventListener(type, eventListeners[type]); reject(this.error); };

			for (const type in eventListeners) this.addEventListener(type, eventListeners[type]);
			super.readAsArrayBuffer(blob);
		});
	}


	/**
	 * Returns a promise of array buffer content read from the given blob
	 * (or file), which can be evaluated using the await command. The latter
	 * throws an error if reading said blob fails. Note that this operation
	 * is defined solely for backward compatibility.
	 * @param {Blob} blob the binary large object to be read
	 * @return {Promise} the promise of array buffer content read from the given
	 *		   blob, or null if the operation is aborted
	 */
	readAsBinaryString (blob) {
		return new Promise((resolve, reject) => {
			const eventListeners = {};
			eventListeners.load  = event => { for (const type in eventListeners) this.removeEventListener(type, eventListeners[type]); resolve(this.result); };
			eventListeners.abort = event => { for (const type in eventListeners) this.removeEventListener(type, eventListeners[type]); resolve(null); };
			eventListeners.error = event => { for (const type in eventListeners) this.removeEventListener(type, eventListeners[type]); reject(this.error); };

			for (const type of Objects.keys(eventListeners)) this.addEventListener(type, eventListeners[type]);
			super.readAsBinaryString(blob);
		});
	}


	/**
	 * Returns a promise of a Data URL read from the given blob (or file),
	 * which can be evaluated using the await command. The latter throws an
	 * error if reading said blob fails.
	 * @param {Blob} blob the binary large object to be read
	 * @return {Promise} the promise of a Data URL read from the given blob,
	 *		   or null if the operation is aborted
	 */ 
	readAsDataURL (blob) {
		return new Promise((resolve, reject) => {
			const eventListeners = {};
			eventListeners.load  = event => { for (const type in eventListeners) this.removeEventListener(type, eventListeners[type]); resolve(this.result); };
			eventListeners.abort = event => { for (const type in eventListeners) this.removeEventListener(type, eventListeners[type]); resolve(null); };
			eventListeners.error = event => { for (const type in eventListeners) this.removeEventListener(type, eventListeners[type]); reject(this.error); };

			for (const type of Objects.keys(eventListeners)) this.addEventListener(type, eventListeners[type]);
			super.readAsDataURL(blob);
		});
	}


	/**
	 * Returns a promise of text content read from the given blob (or file),
	 * which can be evaluated using the await command. The latter throws an
	 * error if reading said blob fails.
	 * @param {Blob} blob the binary large object to be read
	 * @param {String} encoding the (optional) encoding, or UTF-8 if not specified
	 * @return {Promise} the promise of text content read from the given blob,
	 *		   or null if the operation is aborted
	 */
	readAsText (blob, encoding) {
		return new Promise((resolve, reject) => {
			const eventListeners = {};
			eventListeners.load  = event => { for (const type in eventListeners) this.removeEventListener(type, eventListeners[type]); resolve(this.result); };
			eventListeners.abort = event => { for (const type in eventListeners) this.removeEventListener(type, eventListeners[type]); resolve(null); };
			eventListeners.error = event => { for (const type in eventListeners) this.removeEventListener(type, eventListeners[type]); reject(this.error); };

			for (const type of Objects.keys(eventListeners)) this.addEventListener(type, eventListeners[type]);
			super.readAsText(blob, encoding);
		});
	}
}