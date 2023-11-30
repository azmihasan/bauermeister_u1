"use strict";

const buttons = {
    copy: document.getElementById("copy"),
    accept: document.getElementById("accept"),
    send: document.getElementById("send"),
    close: document.getElementById("close"),
};
buttons.copy.disabled = true;
buttons.send.disabled = true;
buttons.close.disabled = true;
const texts = {
    offer: document.getElementById("offer"),
    answer: document.getElementById("answer"),
    message: document.getElementById("message"),
    logs: document.getElementById("logs"),
};
texts.answer.readOnly = true;
texts.logs.readOnly = true;

let ctx = null;
function newPC() {
    texts.logs.value = ``;
    const pc = new RTCPeerConnection({iceServers: [
        {url: "stun:stun.services.mozilla.com:3478"},
    ]});
    pc.addEventListener("datachannel", ev => {
        // receiver channel from remote
        console.log("datachannel", ev);
        ev.channel.addEventListener("message", ev => {
            const message = ev.data;
            texts.logs.value += `[offer] ${message}\n`;
        }, false);
        ev.channel.addEventListener("close", ev => {
            // NOTE: close event is not in RTCPeerConnection
            console.log("close");
            texts.logs.value += `[closed]\n`;
            buttons.copy.disabled = true;
            buttons.send.disabled = buttons.close.disabled = true;
            buttons.accept.disabled = false;
            texts.offer.value = texts.answer.value = "";
            ctx = null;
        }, false);
        buttons.send.disabled = buttons.close.disabled = false;
    }, false);
    // sender channel to remote
    const dc = pc.createDataChannel("answer");
    return {pc, dc};
}

// 3, 4. accept offer then make answer
buttons.copy.addEventListener("click", ev => {
    texts.answer.select();
    document.execCommand("copy");
}, false);
buttons.accept.addEventListener("click", ev => {
    ctx = newPC();
    const offer = new RTCSessionDescription({
        type: "offer", sdp: texts.offer.value,
    });
    ctx.pc.setRemoteDescription(offer).then(() => {
        console.log("remote");
        ctx.pc.addEventListener("icecandidate", ev => {
            console.log("icecandidate", ev);
            if (!ev.candidate) {
                // use SDP text with all candidate
                texts.answer.value = ctx.pc.localDescription.sdp;
                buttons.copy.disabled = false;
            }
        }, false);
        ctx.pc.createAnswer().then(answer => {
            console.log("answer");
            ctx.pc.setLocalDescription(answer);
        }, false);
    });
    buttons.accept.disabled = true;
}, false);

// 5. messaging
buttons.send.addEventListener("click", ev => {
    const message = texts.message.value;
    texts.logs.value += `[answer] ${message}\n`;
    ctx.dc.send(message);
}, false);

// 6. close connection
buttons.close.addEventListener("click", ev => {
    ctx.pc.close();
}, false);
