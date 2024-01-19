/*
 * Returns a promise to have suspended operations (sleep) for the
 * given duration once said promise is resolved.
 * @param duration the duration in milliseconds
 * @throws RangeError if the given duration is strictly negative
 */
export default function sleep (duration) {
	if (duration < 0) throw new RangeError("" + duration);
    return new Promise((resolve, reject) => setTimeout(resolve, duration));
}