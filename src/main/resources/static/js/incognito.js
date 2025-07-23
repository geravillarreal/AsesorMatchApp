document.addEventListener('DOMContentLoaded', function() {
	if (!navigator.storage || !navigator.storage.estimate) {
		return;
	}
	navigator.storage.estimate().then(function(estimate) {
		var quota = estimate && estimate.quota ? estimate.quota : 0;
		var incognito = quota && quota < 120000000;
		if (incognito) {
			document.cookie = 'incognito=true; path=/';
		} else {
			document.cookie = 'incognito=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/';
		}
	});
});
