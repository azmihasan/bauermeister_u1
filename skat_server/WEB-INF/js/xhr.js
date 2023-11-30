const EMPTY_OBJECT = Object.freeze({});


/**
 * Sends an asynchronous HTTP request, and returns a promise that resolves into an HTTP
 * response body. The promise is rejected if there is a network I/O problem, or if the
 * response's status is out of the [200,299] success range; the resulting exception
 * carries the (raw) HTTP response headers in an additional "headers" property.
 * Copyright (c) 2016 Sascha Baumeister
 * @param resource {String} the service URI
 * @param method {String} the service method
 * @param headers {Object} the optional request headers map
 * @param body {Object} the optional request body
 * @param type {String} the optional response type, one of "text" (the default), "arraybuffer", "blob", "document", "json" 
 * @param alias {String} an optional user alias
 * @param password {String} an optional user password
 * @return {Promise} the promise of a resolved XMLHttpRequest
 * @throws {Error} if HTTP authentication fails
 */
 export default function xhr (resource, method, headers, body, type, alias, password) {
	if (!alias || !password || alias.length == 0 || password.length == 0) alias = password = null;

	let exchange = new XMLHttpRequest();
	exchange.responseType = type || "text";
	exchange.withCredentials = true;
	exchange.open(method, resource, true, alias, password);
	for (const key of Object.keys(headers || EMPTY_OBJECT))
		exchange.setRequestHeader(key, headers[key]);

	return new Promise((resolve, reject) => {
		exchange.addEventListener("load", event => {
			if (event.currentTarget.status >= 200 && event.currentTarget.status <= 299) {
				resolve(event.currentTarget.response);
			} else {
				let error = new Error("HTTP " + event.currentTarget.status + " " + event.currentTarget.statusText);
				error.headers = event.currentTarget.getAllResponseHeaders();
				reject(error);
			}
		});

		exchange.addEventListener("error", event => reject(new Error("HTTP exchange failed")));
		exchange.send(body || "");
	});
}