var videoPlayItem;
var videoRewindItem;

function playResultMovie() {
	videoPlayItem = setInterval(function() {
		var fwdBtnElem = document.getElementsByClassName('step-forward-btn')[0];
		if(fwdBtnElem != undefined) {
			if(!fwdBtnElem.classList.contains("v-disabled")) {
				fwdBtnElem.click();
			} else {
				pauseResultMovie();
			}
		}
	}, 100);
}

function pauseResultMovie() {
    clearInterval(videoPlayItem);
}

function rewindResultMovie() {
	videoRewindItem = setInterval(function() {
		var backBtnElem = document.getElementsByClassName('step-back-btn')[0];
		if(backBtnElem != undefined) {
			if(!backBtnElem.classList.contains("v-disabled")) {
				backBtnElem.click();
			} else {
				pauseRewindResultMovie();
			}
		}
	}, 100);
}

function pauseRewindResultMovie() {
    clearInterval(videoRewindItem);
}