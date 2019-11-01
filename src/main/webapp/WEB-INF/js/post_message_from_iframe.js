var height = 0;
function sendPostMessage() {
    if (height !== document.getElementById('bankidwrapper').offsetHeight) {
        height = document.getElementById('bankidwrapper').offsetHeight;
        window.parent.postMessage({
            frameHeight: height
        }, window.location.origin);
    }
}